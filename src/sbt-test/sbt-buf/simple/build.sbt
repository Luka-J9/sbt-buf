version := "0.1"
scalaVersion := "2.12.1"

enablePlugins(BufPlugin)

import BufPlugin.autoImport._
import com.lukaj9.sbt.platform._
import java.net.URI

lazy val v = ("com.thesamet.scalapb" % "protoc-gen-scalapb-validate" % "0.3.4")

lazy val validatePlugin = "https://repo1.maven.org/maven2/com/thesamet/scalapb/protoc-gen-scalapb-validate/0.3.4/protoc-gen-scalapb-validate-0.3.4"

protocPlugins := Seq(
    ProtocPlugin(
        "protoc-gen-scalapb-validate",
        detectedSystem.value.os match {
            case Windows => new URI(s"$validatePlugin-windows.bat")
            case Linux | Osx => new URI(s"$validatePlugin-unix.sh")
            case e => throw new IllegalArgumentException(s"$e is not a supported os for this plugin")
        }
    )
)
