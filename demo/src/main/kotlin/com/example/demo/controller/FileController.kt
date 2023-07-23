package com.example.demo.controller

import com.example.demo.payload.UploadFileResponse
import com.example.demo.service.FileStorageService

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.io.IOException
import java.nio.file.Path
import java.util.*



@RestController
class FileController(private val fileStorageService: FileStorageService) {
    @PostMapping("/uploadFile")
    fun uploadFile(@RequestParam("file") file: MultipartFile): UploadFileResponse {
        val fileName = fileStorageService.storeFile(file)
        val path: Path = fileStorageService.fileStorageLocation
        println(path.toString())
        val fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString()
        return UploadFileResponse(fileName, fileDownloadUri, file.contentType!!, file.size)
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    fun downloadFile(@PathVariable fileName: String, request: HttpServletRequest): ResponseEntity<Resource> {
        // Load file as Resource
        val resource: Resource = fileStorageService.loadFileAsResource(fileName)

        // Try to determine file's content type
        val contentType = try {
            request.servletContext.getMimeType(resource.file.absolutePath)
        } catch (ex: IOException) {
            logger.info("Could not determine file type.")
            null
        }

        // Fallback to the default content type if type could not be determined
        val finalContentType = contentType ?: "application/octet-stream"

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(finalContentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${resource.filename}\"")
                .body(resource)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FileController::class.java)
    }
}