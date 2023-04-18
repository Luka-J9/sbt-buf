package com.github.lukaj9.sbt.platform

sealed trait SupportedOs {
  def name: String
}

case object Windows extends SupportedOs {
    override val name = "Windows"
}
case object OSX extends SupportedOs {
    override val name = "Darwin"
}
case object Linux extends SupportedOs {
    override val name = "Linux"
}

case object UnknownOs extends SupportedOs {
    override val name = "unknown"
}

object SupportedOs {
    def extract() = {
       val name = System.getProperty("os.name").toLowerCase()

       if(name.startsWith("windows")){
        Windows
       } else if(name.startsWith("osx") || name.startsWith("macos")) {
        OSX
       } else if(name.startsWith("linux")) {
        Linux
       } else {
        UnknownOs
       }
    }
}

sealed trait Arch {
    def name: String
}

case object Intel extends Arch {
    override val name: String = "x86_64"
}
case object Arm extends Arch {
    override val name: String = "aarch64"
}

object Arch {
    def extract(): Arch = 
        System.getProperty("os.arch").toLowerCase.filter(_.isLetterOrDigit) match {
            case "amd64" | "x64" | "x8664" | "x86" => Intel
            case "aarch64" | "arm64" => Arm
        }
}

case class SystemInformation(os: SupportedOs, arch: Arch) {
    def toPluginName = {
        val base = s"buf-${os.name}-${arch.name}"
        os match {
            case Windows => s"buf-${os.name}-${arch.name}.exe"
            case _ => s"buf-${os.name}-${arch.name}"
        }
    }
}

object SystemInformation {
    def extract(): SystemInformation = 
        SystemInformation(SupportedOs.extract(), Arch.extract())
}