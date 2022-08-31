package com.jevzo.limitcloud.library.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipUtils {

    companion object {
        fun zipFiles(startDirectory: File, finalDestination: File): File {
            val files = FileUtils.getAllFiles(startDirectory)

            val fileOutputStream = FileOutputStream(finalDestination)
            val zipOutputStream = ZipOutputStream(fileOutputStream)

            for (file in files) {
                if (file.isDirectory) continue

                val fileInputStream = FileInputStream(file)
                val zipPath = file.canonicalPath.substring(startDirectory.canonicalPath.length + 1)
                val zipEntry = ZipEntry(zipPath)

                zipOutputStream.putNextEntry(zipEntry)

                val buffer = ByteArray(1024)
                var byteLength: Int

                while ((fileInputStream.read(buffer).also { byteLength = it }) >= 0) {
                    zipOutputStream.write(buffer, 0, byteLength)
                }

                zipOutputStream.closeEntry()
                fileInputStream.close()
            }

            zipOutputStream.close()
            fileOutputStream.close()
            return finalDestination
        }

        fun unzipFiles(zipFile: File, finalDestination: File): File {
            val buffer = ByteArray(1024)

            if (finalDestination.exists()) finalDestination.mkdirs()

            val zipInputStream = ZipInputStream(FileInputStream(zipFile))
            var zipEntry = zipInputStream.nextEntry

            while (zipEntry != null) {
                val fileName = zipEntry.name
                val file = File(finalDestination, fileName)

                val parentDirectories = File(file.parent)
                parentDirectories.mkdirs()

                val fileOutputStream = FileOutputStream(file)
                var byteLength: Int

                while ((zipInputStream.read(buffer).also { byteLength = it }) >= 0) {
                    fileOutputStream.write(buffer, 0, byteLength)
                }

                fileOutputStream.close()
                zipEntry = zipInputStream.nextEntry
            }

            zipInputStream.closeEntry()
            zipInputStream.close()

            return finalDestination
        }
    }
}