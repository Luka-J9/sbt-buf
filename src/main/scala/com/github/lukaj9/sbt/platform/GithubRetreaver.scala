package com.github.lukaj9.sbt
package platform

import java.net.URL
import scala.io.Source
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.io.File
import sbt.internal.util.ManagedLogger

object GithubRetreaver {
    private lazy val username = "bufbuild"
    private lazy val repo = "buf"
    private lazy val baseUrl = s"https://api.github.com/repos/$username/$repo/releases"
    private lazy val downloadUrl = s"https://github.com/$username/$repo/releases/download"

    def latestVersion(): String = {
        val apiUrl = s"$baseUrl/latest"
        val url = new URL(apiUrl)

        // Open a connection to the API endpoint and read the response
        val inputStream = url.openConnection().getInputStream()
        val response = Source.fromInputStream(inputStream).mkString

        // Parse the response to get the version number of the latest release
        response.split("\"tag_name\":")(1).split(",")(0).replaceAll("\"", "")
    }


    def downloadVersion(version: String, system: DetectedSystem) = {
        system.toBufPlugin match {
            case None => throw new IllegalStateException(s"Detected system was ${system.os} ${system.arch}, which is not supported by Buf at this time - if you think this is wrong open a PR")
            case Some(value) => new URL(s"$downloadUrl/$version/buf-$value")
        }
        
    }

    def downloadBuf(system: DetectedSystem, version: Option[String], to: java.nio.file.Path)(report: Int => Unit)(implicit logger: ManagedLogger) = {
        val selectedVersion = version.getOrElse(latestVersion())

        val file = to.toFile()
        val downloadUrl = downloadVersion(selectedVersion, system)
        logger.info(s"Downloading Buf Version `$selectedVersion`")
        downloadFile(downloadUrl, file)(report)
    }

    def downloadPlugin(pluginURL: URL, to: java.nio.file.Path)(report: Int => Unit) = {
        downloadFile(pluginURL, to.toFile())(report)
        to.toFile()
    }

    private def downloadFile(url: URL, to: File)(report: Int => Unit) = {
        val connection = url.openConnection()
        val contentLength = connection.getContentLength()
        lazy val out = new FileOutputStream(to)
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
                    report(percentage)
                }
            }
            to
        } finally {
            out.close()
            in.close()
        }
    }
}
