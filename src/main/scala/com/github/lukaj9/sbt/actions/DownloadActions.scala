package com.github.lukaj9.sbt.actions

import java.nio.file.Path
import com.github.lukaj9.sbt.platform.DetectedSystem
import sbt.util.Logger
import java.nio.file.Files
import java.net.URI
import com.github.lukaj9.sbt.platform.BufResourceFetchers
import com.github.lukaj9.sbt.platform.ProtocPlugin

trait DownloadActions {

  def downloadBuf(
        bufVersion: Option[String],
        overrideLocation: Option[URI],
  ): Unit

  def downloadProtocPlugins(protocPlugins: Seq[ProtocPlugin]): Unit 

}

object DownloadActions {
  def apply(baseDir: BaseDirectory, system: DetectedSystem)(implicit logger: Logger): DownloadActions = new DownloadActions {
    override def downloadBuf(
        bufVersion: Option[String],
        overrideLocation: Option[URI],
    ): Unit = {
      if (!Files.exists(baseDir.value)) {
        Files.createDirectories(baseDir.value)
      }

      val outputPath = baseDir.toBufPath

      val detectedSystem = DetectedSystem.detect

      if (!outputPath.toFile.exists()) {
        val buf = overrideLocation match {
          case None =>
            BufResourceFetchers.downloadBuf(
              detectedSystem,
              bufVersion,
              outputPath
            )
          case Some(value) =>
            logger.info(
              s"Buf download overriden - pulling from ${value.toString}"
            )
            BufResourceFetchers.downloadFile(value, outputPath)
        }
        buf.setExecutable(true)
      }
    }

    override def downloadProtocPlugins(protocPlugins: Seq[ProtocPlugin]): Unit = {
        val protocManaged = baseDir.toProtocPluginsPath
        if(!Files.exists(protocManaged)) {
            Files.createDirectories(protocManaged)
        }

        protocPlugins.foreach{
            case ProtocPlugin(name, uri) => 
            val path = uri.getPath();
            val outputFile = protocManaged.resolve(name).toFile
            if(!outputFile.exists()) {
                val plugin = BufResourceFetchers.downloadPlugin(uri, outputFile.toPath())
                plugin.setExecutable(true)
            }
        }
    }
  }
}
