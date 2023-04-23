package com.github.lukaj9.sbt
package platform

import java.net.URL
import scala.io.Source
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.io.File
import sbt.internal.util.ManagedLogger
import java.net.URI
import java.nio.file.{Files, Paths}
import java.io.InputStream
import sbt.util.Logger

object BufResourceFetchers {
    lazy val username = "bufbuild"
    lazy val repo = "buf"
    lazy val baseUrl = s"https://api.github.com/repos/$username/$repo/releases"
    lazy val downloadUrl = s"https://github.com/$username/$repo/releases/download"

    def latestVersion(): String = {
        val apiUrl = s"$baseUrl/latest"
        val url = new URL(apiUrl)
        val inputStream = url.openConnection().getInputStream()
        extractVersion(inputStream)
    }

    protected[platform] def extractVersion(inputStream: InputStream) = {
        try{
            val response = Source.fromInputStream(inputStream).mkString
            response.split("\"tag_name\":")(1).split(",")(0).replaceAll("\"", "")
        } finally {
            inputStream.close()
        }
    }


    def downloadVersion(version: String, system: DetectedSystem): URI = {
        system.toBufPlugin match {
            case None => throw new IllegalArgumentException(s"Detected system was ${system.os} ${system.arch}, which is not supported by Buf at this time - Please Report and issue if you think this is wrong")
            case Some(value) => new URL(s"$downloadUrl/$version/buf-$value").toURI()
        }
        
    }

    def downloadBuf(system: DetectedSystem, version: Option[String], to: java.nio.file.Path)(implicit logger: Logger): File = {
        val selectedVersion = version.getOrElse(latestVersion())
        val downloadUrl = downloadVersion(selectedVersion, system)
        logger.info(s"Downloading Buf Version `$selectedVersion`")
        downloadFile(downloadUrl, to)
    }

    def downloadPlugin(pluginURL: URI, to: java.nio.file.Path)(implicit logger: Logger): File = {
        downloadFile(pluginURL, to)
        to.toFile()
    }

    def downloadFile(uri: URI, to: java.nio.file.Path)(implicit logger: Logger): File = {

        val file = to.toFile
        uri.getScheme match {
            case "file" => Files.copy(Paths.get(uri), to).toFile()
            case _ =>
                val connection = new URL(uri.toString()).openConnection()
                val contentLength = connection.getContentLength()
                lazy val out = new FileOutputStream(file)
                lazy val in = connection.getInputStream
                try {
                    val buffer = Array.ofDim[Byte](1024)
                    var length = 0
                    var downloaded = 0
                    var percentage = 0

                    while({length = in.read(buffer); length} != -1) {
                        out.write(buffer, 0, length)
                        downloaded += length
                        val newPercentage = downloaded/contentLength*100
                        if(percentage != newPercentage) {
                            
                            percentage = newPercentage
                            logger.info(s"Downloading `${uri.toString()}` - ${percentage}%")
                        }
                    }
                    file
                } finally {
                    out.close()
                    in.close()
                }
                }

    }
}
