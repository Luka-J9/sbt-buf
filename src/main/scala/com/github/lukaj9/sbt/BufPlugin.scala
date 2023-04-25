import sbt.AutoPlugin
import sbt._
import Keys._
import com.github.lukaj9.sbt.platform.BufResourceFetchers
import sys.process._
import java.io.File
import java.nio.file.Files
import complete.DefaultParsers._
import com.github.lukaj9.sbt.platform.Windows
import java.nio.file.Paths
import com.github.lukaj9.sbt.platform.Linux
import com.github.lukaj9.sbt.platform.UnknownOs
import protocbridge.SystemDetector
import com.github.lukaj9.sbt.platform.DetectedSystem
import com.github.lukaj9.sbt.actions.BufActions
import com.github.lukaj9.sbt.actions.DownloadActions
import com.github.lukaj9.sbt.actions.BaseDirectory
import com.github.lukaj9.sbt.platform.ProtocPlugin


object BufPlugin extends AutoPlugin {

  object autoImport {
    
    lazy val bufVersion = settingKey[Option[String]](
        s"Version of buf to install, if not set defaults to latest"
    )

    lazy val bufOverride = settingKey[Option[java.net.URI]](
      "Override where buf gets downloaded from"
    )

    lazy val protocPlugins = settingKey[Seq[ProtocPlugin]](
        "If the buf files define a local plugin these are the paths to download the artifacts"
    )

    lazy val bufDownload = taskKey[Unit]("Download Buf Cli Tool")

    lazy val bufDownloadGen = taskKey[Unit]("Download Protoc Plugins Used for Gen")

    lazy val bufClear = taskKey[Unit]("Clears all sbt-buf files from target cache")

    lazy val buf = inputKey[Unit]("Run Buf! This process passes commands through to the downloaded CLI tool")

    lazy val detectedSystem = settingKey[DetectedSystem]("Detected system for which Buf and associated plugins will run on")

    
  }

  import autoImport._

  private lazy val baseDir = Def.setting[BaseDirectory] {
    BaseDirectory(((Compile / target).value / "sbt-buf").toPath())
  }

  private lazy val bufActions = Def.task[BufActions]{
    implicit val logger = streams.value.log
    BufActions(
      baseDir.value,
      protocPlugins.value.nonEmpty
    )
  }

  private lazy val downloadActions = Def.task[DownloadActions]{
    implicit val logger = streams.value.log
    DownloadActions(
      baseDir.value,
      system = detectedSystem.value
    )
  }

  lazy val downloadBufSetting =
    Def.task {
      downloadActions.value.downloadBuf(bufVersion.value, bufOverride.value)
    }

  lazy val downloadProtocPlugins = 
    Def.task {
      downloadActions.value.downloadProtocPlugins(protocPlugins.value)
    }

  lazy val runBuf = 
    Def.inputTask {
      val userInput: Seq[String] = Def.spaceDelimited("<arg>").parsed
      bufActions.value.executeBufCommand(userInput)
    }

  override def projectSettings =
    Seq(
      bufVersion := None,
      bufOverride := None,
      bufDownload := downloadBufSetting.value,
      bufDownloadGen := downloadProtocPlugins.value,
      buf := runBuf.dependsOn(bufDownloadGen).dependsOn(downloadBufSetting).evaluated,
      detectedSystem := DetectedSystem.detect,
    )
}
