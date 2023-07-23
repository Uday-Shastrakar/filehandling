package com.example.demo.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "file")
class FileStorageProperties {
    var uploadDir: String? = null
}
