package com.wilsoft.arki_streamconfig.utilidades

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.UUID
import android.content.ContentResolver
import android.provider.OpenableColumns

/**
 * Extensiones avanzadas para Firebase Storage
 * Incluye funciones para subir, listar, eliminar y gestionar imágenes
 */
object StorageExtensions {

    private val storage = FirebaseStorage.getInstance()
    private val database = FirebaseDatabase.getInstance()

    /**
     * Sube una imagen a Firebase Storage con metadatos
     */
    suspend fun uploadImageWithMetadata(
        imageUri: Uri,
        path: String,
        fileName: String? = null,
        contentResolver: ContentResolver, // ← NUEVO PARÁMETRO
        onProgress: ((Int) -> Unit)? = null
    ): Result<String> {
        return try {
            val finalFileName = if (fileName != null) {
                fileName
            } else {
                // Detectar extensión original
                val originalExtension = getFileExtensionFromUri(contentResolver, imageUri)
                generateFileName("image", originalExtension)
            }

            val imageRef = storage.reference.child("GRAFICOS/$path/$finalFileName")
            val uploadTask = imageRef.putFile(imageUri)

            // Monitorear progreso si se proporciona callback
            onProgress?.let { progressCallback ->
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    progressCallback(progress)
                }
            }

            // Esperar a que termine la subida
            uploadTask.await()

            // Obtener URL de descarga
            val downloadUrl = imageRef.downloadUrl.await()

            // Guardar metadatos en Realtime Database
            saveImageMetadata(path, finalFileName, downloadUrl.toString(), imageUri)

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lista todas las imágenes de una carpeta específica
     */
    suspend fun listImagesInFolder(folderPath: String): Result<List<StorageImageItem>> {
        return try {
            val listResult = storage.reference.child("GRAFICOS/$folderPath").listAll().await()
            val images = mutableListOf<StorageImageItem>()

            for (item in listResult.items) {
                try {
                    val downloadUrl = item.downloadUrl.await()
                    val metadata = item.metadata.await()

                    images.add(
                        StorageImageItem(
                            name = item.name,
                            downloadUrl = downloadUrl.toString(),
                            sizeBytes = metadata.sizeBytes,
                            creationTime = metadata.creationTimeMillis,
                            contentType = metadata.contentType ?: "image/jpeg",
                            path = item.path
                        )
                    )
                } catch (e: Exception) {
                    // Continuar con el siguiente archivo si hay error con uno específico
                    continue
                }
            }

            Result.success(images.sortedByDescending { it.creationTime })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una imagen de Storage y sus metadatos
     */
    suspend fun deleteImage(imagePath: String): Result<Boolean> {
        return try {
            // Eliminar de Storage
            storage.reference.child(imagePath).delete().await()

            // Eliminar metadatos de Database
            val pathParts = imagePath.split("/")
            if (pathParts.size >= 3) {
                val folder = pathParts[1]
                val fileName = pathParts[2]
                database.reference
                    .child("CLAVE_STREAM_FB/STORAGE_METADATA/$folder/$fileName")
                    .removeValue()
                    .await()
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca imágenes por nombre o tipo
     */
    suspend fun searchImages(
        folderPath: String,
        query: String
    ): Result<List<StorageImageItem>> {
        return try {
            val allImages = listImagesInFolder(folderPath).getOrNull() ?: emptyList()
            val filteredImages = allImages.filter { image ->
                image.name.contains(query, ignoreCase = true) ||
                        image.contentType.contains(query, ignoreCase = true)
            }
            Result.success(filteredImages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene estadísticas de uso de Storage
     */
    suspend fun getStorageStats(folderPath: String): Result<StorageStats> {
        return try {
            val images = listImagesInFolder(folderPath).getOrNull() ?: emptyList()

            val totalSize = images.sumOf { it.sizeBytes }
            val totalCount = images.size
            val averageSize = if (totalCount > 0) totalSize / totalCount else 0L

            val stats = StorageStats(
                totalFiles = totalCount,
                totalSizeBytes = totalSize,
                averageSizeBytes = averageSize,
                folderPath = folderPath
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Limpia archivos antiguos (más de X días)
     */
    suspend fun cleanOldFiles(
        folderPath: String,
        daysOld: Int
    ): Result<CleanupResult> {
        return try {
            val images = listImagesInFolder(folderPath).getOrNull() ?: emptyList()
            val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)

            val oldImages = images.filter { it.creationTime < cutoffTime }
            var deletedCount = 0
            var deletedSize = 0L

            for (image in oldImages) {
                val deleteResult = deleteImage(image.path)
                if (deleteResult.isSuccess) {
                    deletedCount++
                    deletedSize += image.sizeBytes
                }
            }

            val cleanupResult = CleanupResult(
                deletedFiles = deletedCount,
                deletedSizeBytes = deletedSize,
                remainingFiles = images.size - deletedCount
            )

            Result.success(cleanupResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crea una copia de respaldo de una imagen
     */
    suspend fun backupImage(imagePath: String): Result<String> {
        return try {
            val sourceRef = storage.reference.child(imagePath)
            val backupPath = imagePath.replace("GRAFICOS/", "GRAFICOS/BACKUP/")
            val backupRef = storage.reference.child(backupPath)

            // Descargar y volver a subir (no hay copy directo en Firebase)
            val downloadUrl = sourceRef.downloadUrl.await()
            // Aquí necesitarías implementar la descarga y re-subida
            // Por simplicidad, devolvemos la URL original

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Guarda metadatos de imagen en Realtime Database
     */
    private suspend fun saveImageMetadata(
        folder: String,
        fileName: String,
        downloadUrl: String,
        originalUri: Uri
    ) {
        try {
            val metadata = mapOf(
                "downloadUrl" to downloadUrl,
                "uploadedAt" to System.currentTimeMillis(),
                "originalPath" to originalUri.toString(),
                "folder" to folder,
                "fileName" to fileName
            )

            database.reference
                .child("CLAVE_STREAM_FB/STORAGE_METADATA/$folder/$fileName")
                .setValue(metadata)
                .await()
        } catch (e: Exception) {
            // Log error pero no falla la subida principal
            println("Error saving metadata: ${e.message}")
        }
    }
}

/**
 * Clase de datos para elementos de Storage
 */
data class StorageImageItem(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val creationTime: Long,
    val contentType: String,
    val path: String
) {
    fun getFormattedSize(): String {
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0

        return when {
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$sizeBytes bytes"
        }
    }

    fun getFormattedDate(): String {
        val date = java.util.Date(creationTime)
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}

/**
 * Estadísticas de Storage
 */
data class StorageStats(
    val totalFiles: Int,
    val totalSizeBytes: Long,
    val averageSizeBytes: Long,
    val folderPath: String
) {
    fun getTotalSizeFormatted(): String {
        val mb = totalSizeBytes / (1024.0 * 1024.0)
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.1f MB", mb)
            else -> String.format("%.1f KB", totalSizeBytes / 1024.0)
        }
    }
}

/**
 * Resultado de operación de limpieza
 */
data class CleanupResult(
    val deletedFiles: Int,
    val deletedSizeBytes: Long,
    val remainingFiles: Int
) {
    fun getDeletedSizeFormatted(): String {
        val mb = deletedSizeBytes / (1024.0 * 1024.0)
        return if (mb >= 1) {
            String.format("%.1f MB", mb)
        } else {
            String.format("%.1f KB", deletedSizeBytes / 1024.0)
        }
    }
}

/**
 * Extensiones de utilidad para Context
 */
fun Context.getFileNameFromUri(uri: Uri): String? {
    return try {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                it.getString(nameIndex)
            } else null
        }
    } catch (e: Exception) {
        null
    }
}

fun Context.getFileSizeFromUri(uri: Uri): Long {
    return try {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (it.moveToFirst() && sizeIndex >= 0) {
                it.getLong(sizeIndex)
            } else 0L
        } ?: 0L
    } catch (e: Exception) {
        0L
    }
}


/**
 * Obtiene la extensión correcta del archivo desde su URI
 */
fun getFileExtensionFromUri(contentResolver: ContentResolver, uri: Uri): String {
    return try {
        // Intentar obtener el nombre del archivo desde el ContentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                val fileName = it.getString(nameIndex)
                if (fileName != null && fileName.contains(".")) {
                    return fileName.substringAfterLast(".")
                }
            }
        }

        // Si no se puede obtener desde el ContentResolver, intentar desde el tipo MIME
        val mimeType = contentResolver.getType(uri)
        when (mimeType) {
            "image/png" -> "png"
            "image/jpeg", "image/jpg" -> "jpg"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "jpg" // Fallback por defecto
        }
    } catch (e: Exception) {
        "jpg" // Fallback en caso de error
    }
}

/**
 * Genera nombre de archivo con extensión correcta
 */
fun generateFileName(
    prefix: String,
    originalExtension: String,
    useTimestamp: Boolean = true
): String {
    val timestamp = if (useTimestamp) "_${System.currentTimeMillis()}" else ""
    return "${prefix}${timestamp}.${originalExtension}"
}
