package com.github.lukaj9.sbt.platform

import java.{util => ju}

object SystemDetector {
  private val UNKNOWN = "unknown"
  private val X86_64_RE = "^(x8664|amd64|ia32e|em64t|x64)$".r
  private val X86_32_RE = "^(x8632|x86|i[3-6]86|ia32|x32)$".r

  def normalizedOs(s: String): Os =
    normalize(s) match {
      case p if p.startsWith("aix")                           => Aix
      case p if p.startsWith("hpux")                          => Hpux
      case p if p.startsWith("linux")                         => Linux
      case p if p.startsWith("osx") || p.startsWith("macosx") => Osx
      case p if p.startsWith("windows")                       => Windows
      case p if p.startsWith("freebsd")                       => Freebsd
      case p if p.startsWith("openbsd")                       => Openbsd
      case p if p.startsWith("netbsd")                        => Netbsd
      case _                                                  => UnknownOs
    }

  def normalizedArch(s: String): Arch =
    normalize(s) match {
      case X86_64_RE(_)         => X86_64
      case X86_32_RE(_)         => X86_32
      case "aarch64" | "arm"    => Aarch64
      case "ppc64le"            => Ppc64le
      case "ppc64"              => Ppc_64
      case "s390x"              => S390x
      case _                    => UnknownArch
    }

  def detectedClassifier(): String = {
    val osName = sys.props.getOrElse("os.name", "")
    val osArch = sys.props.getOrElse("os.arch", "")
    normalizedOs(osName) + "-" + normalizedArch(osArch)
  }

  def normalize(s: String) =
    s.toLowerCase(ju.Locale.US).replaceAll("[^a-z0-9]+", "")
}