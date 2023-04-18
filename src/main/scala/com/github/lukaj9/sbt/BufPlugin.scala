import sbt.AutoPlugin
import sbt._
import Keys._
import com.github.lukaj9.sbt.platform.GithubRetreaver
import com.github.lukaj9.sbt.platform.SystemInformation
import sys.process._
import java.io.File
import java.nio.file.Files
import complete.DefaultParsers._
import _root_.com.github.lukaj9.sbt.platform.Windows
import java.nio.file.Paths

object BufPlugin extends AutoPlugin {

  object autoImport {

    case class ProtocPlugin(fn: SystemInformation => Option[String], fileName: Option[String])
    
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

  private lazy val systemInformation = SystemInformation.extract()

  lazy val downloadBufSetting =
    Def.task {
      val targetDir = (Compile / target).value

      val managedDir = targetDir/ "sbt-buf"

      if(!Files.exists(managedDir.toPath()) ) {
        Files.createDirectories(managedDir.toPath())
      }

      val outputFile = managedDir/ "buf"

      if(!outputFile.exists()){
        val buf = GithubRetreaver.downloadBuf(systemInformation, bufVersion.value, outputFile.toPath())(x => println(s"Downloading Buf Progress: $x%"))
        makeExecutable(buf)
        // appendToPath(buf)
      }
    }

  lazy val downloadProtocPlugins = 
    Def.task {
      val targetDir = (Compile /target).value
      val managedDir = targetDir / "sbt-buf" / "protoc"

      if(!Files.exists(managedDir.toPath())) {
        Files.createDirectories(managedDir.toPath())
      }

      protocPlugins.value.flatMap{
        x => (x.fn(systemInformation).map((_, x.fileName)))
      }.foreach{
        case (str, fileNameOpt) => 

          val uri = new URI(str)
          val path = uri.getPath();
          val fileName = fileNameOpt.getOrElse(path.substring(path.lastIndexOf('/') + 1));
          val outputFile = managedDir / fileName
          if(!outputFile.exists()) {
            val plugin = GithubRetreaver.downloadPlugin(uri.toURL(), outputFile.toPath())(x => println(s"Downloading Plugin `${fileName}`: $x%"))
            makeExecutable(plugin)
          }
      }

    }

  lazy val runBuf = 
    Def.inputTask {
      val targetDir = (Compile / target).value
      val bufEx = targetDir/ "sbt-buf" / "buf"
      val userInput: Seq[String] = Def.spaceDelimited("<arg>").parsed


      val env = if(protocPlugins.value.nonEmpty) {
        sys.env + ("PATH" -> (sys.env.getOrElse("PATH", "") + File.pathSeparator + (targetDir / "sbt-buf" / "protoc").getAbsolutePath))
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
