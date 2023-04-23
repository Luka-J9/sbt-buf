package com.github.lukaj9.sbt.platform

sealed trait Os

case object Aix extends Os
case object Hpux extends Os
case object Linux extends Os
case object Osx extends Os
case object Windows extends Os
case object Freebsd extends Os
case object Openbsd extends Os
case object Netbsd extends Os
case object UnknownOs extends Os


sealed trait Arch
case object X86_64 extends Arch
case object X86_32 extends Arch
case object Aarch64 extends Arch
case object Ppc64le extends Arch
case object Ppc_64 extends Arch
case object S390x extends Arch
case object Arm extends Arch
case object UnknownArch extends Arch
