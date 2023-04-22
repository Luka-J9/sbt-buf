version := "0.1"
scalaVersion := "2.12.1"

enablePlugins(BufPlugin)

import BufPlugin.autoImport._



lazy val v = ("com.thesamet.scalapb" % "protoc-gen-scalapb-validate" % "0.3.4")
protocPlugins := Seq(
    ProtocPlugin(plugin => 
        Some(
            "https://repo1.maven.org/maven2/com/thesamet/scalapb/protoc-gen-scalapb-validate/0.3.4/protoc-gen-scalapb-validate-0.3.4-unix.sh"
        ),
        Some("protoc-gen-scalapb-validate")
    )
)
