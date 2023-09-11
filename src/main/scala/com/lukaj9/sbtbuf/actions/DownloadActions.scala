package com.lukaj9.sbtbuf.actions

import com.lukaj9.sbtbuf.platform.DetectedSystem
import sbt.util.Logger
import java.nio.file.Files
import java.net.URI
import com.lukaj9.sbtbuf.platform.BufResourceFetchers
import com.lukaj9.sbtbuf.platform.ProtocPlugin

trait DownloadActions {

  def downloadBuf(
      bufVersion: Option[String],
      overrideLocation: Option[URI]
  ): Unit

  def downloadProtocPlugins(protocPlugins: Seq[ProtocPlugin]): Unit

}

object DownloadActions {
  def apply(baseDir: BaseDirectory, detectedSystem: DetectedSystem)(implicit
      logger: Logger
  ): DownloadActions = new DownloadActions {
    override def downloadBuf(
        bufVersion: Option[String],
        overrideLocation: Option[URI]
    ): Unit = {
      if (!Files.exists(baseDir.value)) {
        Files.createDirectories(baseDir.value)
      }

      val outputPath = baseDir.toBufPath

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

        val isSet = buf.setExecutable(true)

        if (!isSet) {
          throw new IllegalStateException(
            "Failed to set Buf executable, you may not have enough permissions to do this"
          )
        }
      }
    }

    override def downloadProtocPlugins(
        protocPlugins: Seq[ProtocPlugin]
    ): Unit = {
      val protocManaged = baseDir.toProtocPluginsPath
      if (!Files.exists(protocManaged)) {
        Files.createDirectories(protocManaged)
      }

      protocPlugins.foreach { case ProtocPlugin(name, uri) =>
        val outputFile = protocManaged.resolve(name).toFile
        if (!outputFile.exists()) {
          val plugin =
            BufResourceFetchers.downloadPlugin(uri, outputFile.toPath())
          plugin.setExecutable(true)
        }
      }
    }
  }
}
