import sbt.AutoPlugin
import sbt._
import Keys._
import com.github.lukaj9.sbt.platform.GithubRetreaver
import sys.process._
import java.io.File
import java.nio.file.Files
import complete.DefaultParsers._
import _root_.com.github.lukaj9.sbt.platform.Windows
import java.nio.file.Paths
import com.github.lukaj9.sbt.platform.Linux
import com.github.lukaj9.sbt.platform.UnknownOs
import protocbridge.SystemDetector
import com.github.lukaj9.sbt.platform.DetectedSystem

object BufPlugin extends AutoPlugin {

  object autoImport {

    case class ProtocPlugin(fn: DetectedSystem => Option[String], fileNameOverride: Option[String] = None)
    
    lazy val bufVersion = settingKey[Option[String]](
        s"Version of buf to install, if not set defaults to latest"
    )

    lazy val protocPlugins = settingKey[Seq[ProtocPlugin]](
        "If the buf files define a local plugin these are the paths to download the artifacts"
    )

    lazy val bufDownload = taskKey[Unit]("Download Buf")

    lazy val bufDownloadGen = taskKey[Unit]("Download Plugins")

    lazy val bufClear = taskKey[Unit]("Clears all sbt-buf files from target cache")

    lazy val buf = inputKey[Unit]("passes through buf commands")
  }

  import autoImport._

  lazy val downloadBufSetting =
    Def.task {
      lazy implicit val logger = streams.value.log
      val targetDir = (Compile / target).value

      val managedDir = targetDir/ "sbt-buf"
      
      if(!Files.exists(managedDir.toPath()) ) {
        Files.createDirectories(managedDir.toPath())
      }

      val outputFile = managedDir/ "buf"

      val detectedSystem = DetectedSystem.detect

      if(!outputFile.exists()){
        val buf = GithubRetreaver.downloadBuf(detectedSystem, bufVersion.value, outputFile.toPath())(percent => logger.info(s"Downloading Buf Progress: $percent%"))
        makeExecutable(buf)
      }
    }

  lazy val downloadProtocPlugins = 
    Def.task {
      lazy implicit val logger = streams.value.log
      val targetDir = (Compile /target).value
      val managedDir = targetDir / "sbt-buf" / "protoc"

      val detectedSystem = DetectedSystem.detect
      if(!Files.exists(managedDir.toPath())) {
        Files.createDirectories(managedDir.toPath())
      }

      protocPlugins.value.flatMap{
        plugin => (plugin.fn(detectedSystem).map((_, plugin.fileNameOverride)))
      }.foreach{
        case (str, fileNameOpt) => 

          val uri = new URI(str)
          val path = uri.getPath();
          val fileName = fileNameOpt.getOrElse(path.substring(path.lastIndexOf('/') + 1));
          val outputFile = managedDir / fileName
          if(!outputFile.exists()) {
            val plugin = GithubRetreaver.downloadPlugin(uri.toURL(), outputFile.toPath())(percent => logger.info(s"Downloading Plugin `${fileName}`: $percent%"))
            makeExecutable(plugin)
          }
      }

    }

  lazy val runBuf = 
    Def.inputTask {
      lazy implicit val logger = streams.value.log
      val targetDir = (Compile / target).value
      val bufEx = targetDir/ "sbt-buf" / "buf"
      val userInput: Seq[String] = Def.spaceDelimited("<arg>").parsed


      val env = if(protocPlugins.value.nonEmpty) {
        val detectedSystem = DetectedSystem.detect
        val envVar = detectedSystem.os match {
          case Windows => "Path"
          case _ => "PATH"
        }
        sys.env + (envVar -> (sys.env.getOrElse(envVar, "") + File.pathSeparator + (targetDir / "sbt-buf" / "protoc").getAbsolutePath))
      } else {
        sys.env
      }

      val execute = (List(bufEx.absolutePath) ++ userInput)


      val processBuilder = Process(execute, None, env.toSeq: _*)
      processBuilder.run()
    }

  override def projectSettings =
    Seq(
      bufVersion := None,
      bufDownload := downloadBufSetting.value,
      bufDownloadGen := downloadProtocPlugins.value,
      buf := (runBuf.dependsOn(bufDownloadGen.dependsOn(downloadBufSetting))).evaluated,
    )

  private def makeExecutable(file: File): Unit = {
    file.setExecutable(true)
  }
}
