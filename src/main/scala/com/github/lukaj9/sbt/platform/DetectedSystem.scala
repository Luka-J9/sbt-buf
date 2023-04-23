package com.github.lukaj9.sbt.platform

import com.github.lukaj9.sbt.platform._

case class DetectedSystem(os: Os, arch: Arch) {
    def toBufPlugin: Option[String] = {
        (os, arch) match {
            case (Osx, X86_64)      => Some("Darwin-x86_64")
            case (Osx, Aarch64)     => Some("Darwin-arm64")
            case (Linux, Aarch64)   => Some("Linux-aarch64")
            case (Linux, X86_64)    => Some("Linux-x86_64")
            case (Windows, Aarch64) => Some("Windows-arm.exe")
            case (Windows, X86_64)  => Some("Windows-x86_64.exe")
            case _                  => None
        }
    }
}

object DetectedSystem {
    def detect = {
        val osName = sys.props.getOrElse("os.name", "")
        val osArch = sys.props.getOrElse("os.arch", "")

        DetectedSystem(SystemDetector.normalizedOs(osName), SystemDetector.normalizedArch(osArch))
    }
}