import sbt.AutoPlugin
import sbt._
import Keys._
import com.lukaj9.sbtbuf.platform.DetectedSystem
import com.lukaj9.sbtbuf.actions.BufActions
import com.lukaj9.sbtbuf.actions.DownloadActions
import com.lukaj9.sbtbuf.actions.BaseDirectory
import com.lukaj9.sbtbuf.platform.ProtocPlugin
import com.lukaj9.sbtbuf.platform.FileHasher
import scala.annotation.nowarn

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

    lazy val bufDownloadGen =
      taskKey[Unit]("Download Protoc Plugins Used for Gen")

    lazy val bufClear =
      taskKey[Unit]("Clears all sbt-buf files from target cache")

    lazy val bufGenerate =
      inputKey[Unit]("Generates code according to the buf.gen.yaml file")

    lazy val generateBufWithImports =
      inputKey[Unit](
        "Generates all dependencies in the buf.yaml file, as defined in the buf.gen.yaml file, if you have transitive dependencies that collide in definition you will have to explicitly define them"
      )
    lazy val generateBufDependencies =
      inputKey[Unit](
        "Generates all dependencies in the buf.yaml file, as defined in the buf.gen.yaml file, if you have transitive dependencies that collide in definition you will have to explicitly define them"
      )

    lazy val buf = inputKey[Unit](
      "Run Buf! This process passes commands through to the downloaded CLI tool"
    )

    lazy val detectedSystem = settingKey[DetectedSystem](
      "Detected system for which Buf and associated plugins will run on"
    )

    lazy val bufPath = settingKey[java.nio.file.Path](
      "The base path containing the buf or buf workspace file"
    )

  }

  import autoImport._

  private lazy val baseDir = Def.setting[BaseDirectory] {
    BaseDirectory(((Compile / target).value / "sbt-buf").toPath())
  }

  private lazy val bufActions = Def.task[BufActions] {
    implicit val logger = streams.value.log
    BufActions(
      baseDir.value,
      FileHasher(),
      protocPlugins.value.nonEmpty
    )
  }

  @nowarn(
    "cat=unused"
  ) // This is used in the downloadBufSetting task as well as the downloadProtocPlugins task
  private lazy val downloadActions = Def.task[DownloadActions] {
    implicit val logger = streams.value.log
    DownloadActions(
      baseDir = baseDir.value,
      detectedSystem = detectedSystem.value
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

  lazy val generateBufDependenciesTask = Def.inputTask {
    bufActions.value.generateDependencies(bufPath.value, false)
  }

  lazy val generateBufWithImportsTask = Def.inputTask {
    bufActions.value.generateDependencies(bufPath.value, true)
  }

  lazy val runBuf =
    Def.inputTask {
      val userInput: Seq[String] = Def.spaceDelimited("<arg>").parsed
      bufActions.value.executeBufCommand(userInput, None)
    }

  lazy val bufGen =
    Def.inputTask {
      bufActions.value.generate(bufPath.value)
    }

  @nowarn() // For some reason `buf` thinks it's discarding a non unit value
  override def projectSettings =
    Seq(
      bufVersion := None,
      bufOverride := None,
      bufDownload := downloadBufSetting.value,
      bufDownloadGen := downloadProtocPlugins.value,
      buf := runBuf
        .dependsOn(bufDownloadGen)
        .dependsOn(downloadBufSetting)
        .evaluated,
      detectedSystem := DetectedSystem.detect,
      bufPath := baseDirectory.value.toPath(),
      bufGenerate := bufGen
        .dependsOn(bufDownloadGen)
        .dependsOn(downloadBufSetting)
        .evaluated,
      protocPlugins := Seq.empty,
      generateBufDependencies :=
        generateBufDependenciesTask
          .dependsOn(bufDownload)
          .dependsOn(downloadBufSetting)
          .evaluated,
      generateBufWithImports :=
        generateBufWithImportsTask
          .dependsOn(bufDownload)
          .dependsOn(downloadBufSetting)
          .evaluated
    )
}
