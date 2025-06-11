package id.resdev.easy_file_saver

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import android.net.Uri
import android.util.Log
import android.media.MediaScannerConnection


object MediaStoreSaver {

    fun saveToExternal(context: Context, fileName: String, mimeType: String, bytes: ByteArray): String {
        val resolver = context.contentResolver

        val appName = context.applicationInfo.loadLabel(context.packageManager).toString()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Android/media/${context.packageName}/$appName/")
            }

            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
                ?: throw Exception("Failed to insert to MediaStore to External")

            resolver.openOutputStream(uri)?.use { output ->
                output.write(bytes)
            } ?: throw Exception("Failed to open output stream for External")

            return uri.toString()
        } else {
            // For Android 10 and below
            val legacyDir = File(Environment.getExternalStorageDirectory(), appName)
            if (!legacyDir.exists()) legacyDir.mkdirs()

            val file = File(legacyDir, fileName)

            file.outputStream().use { output ->
                output.write(bytes)
            }

            var mediaStoreUri: Uri? = null
            val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA)
            val selection = "${MediaStore.MediaColumns.DATA} = ?"
            val selectionArgs = arrayOf(file.absolutePath)

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    // File already exists in MediaStore, get its URI and update it
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    val id = cursor.getLong(idColumn)
                    mediaStoreUri = Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), id.toString())

                    val updateValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.SIZE, bytes.size.toLong())
                        put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                    }
                    resolver.update(mediaStoreUri!!, updateValues, null, null)
                }
            }

            if (mediaStoreUri == null) {
                // File does not exist in MediaStore, insert a new entry
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.TITLE, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                    put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                    put(MediaStore.MediaColumns.SIZE, bytes.size.toLong())
                    put(MediaStore.MediaColumns.DATA, file.absolutePath)
                }
                mediaStoreUri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
            }

            mediaStoreUri ?: throw Exception("Failed to insert/update to MediaStore External (Android 10 and below)")

            // Media scanner updates the file.
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(mimeType), null)

            return mediaStoreUri.toString()
        }
    }

    fun saveToDownloads(context: Context, fileName: String, mimeType: String, bytes: ByteArray): String {
        val resolver = context.contentResolver
        val appName = context.applicationInfo.loadLabel(context.packageManager).toString()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val contentUri: Uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$appName")
            }

            val uri = resolver.insert(contentUri, values)
                ?: throw Exception("Failed to insert to MediaStore Downloads (Android 11+)")

            resolver.openOutputStream(uri)?.use { output ->
                output.write(bytes)
            } ?: throw Exception("Failed to open output stream for Downloads (Android 11+)")

            return uri.toString()
        } else {
            // For Android 10 and below
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appSpecificDir = File(downloadsDir, appName)

            if (!appSpecificDir.exists()) {
                appSpecificDir.mkdirs()
            }

            val file = File(appSpecificDir, fileName)

            file.outputStream().use { output ->
                output.write(bytes)
            }

            var mediaStoreUri: Uri? = null
            val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA)
            val selection = "${MediaStore.MediaColumns.DATA} = ?"
            val selectionArgs = arrayOf(file.absolutePath)

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    // File already exists in MediaStore, get its URI and update it
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    val id = cursor.getLong(idColumn)
                    mediaStoreUri = Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), id.toString())

                    val updateValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.SIZE, bytes.size.toLong())
                        put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                    }
                    resolver.update(mediaStoreUri!!, updateValues, null, null)
                }
            }

            if (mediaStoreUri == null) {
                // File does not exist in MediaStore, insert a new entry
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.TITLE, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                    put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                    put(MediaStore.MediaColumns.SIZE, bytes.size.toLong())
                    put(MediaStore.MediaColumns.DATA, file.absolutePath)
                }
                mediaStoreUri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
            }

            mediaStoreUri ?: throw Exception("Failed to insert/update to MediaStore Downloads (Android 10 and below)")

            // Media scanner updates the file.
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(mimeType), null)

            return mediaStoreUri.toString()
        }
    }
    
}
