package io.qpointz.mill.security.auth.filestore

import org.springframework.boot.autoconfigure.SpringBootApplication

/** Isolated entry point for [FileStoreAuthIntegrationTest] (avoids scanning test JPA wiring). */
@SpringBootApplication
class FileStoreAuthTestApplication
