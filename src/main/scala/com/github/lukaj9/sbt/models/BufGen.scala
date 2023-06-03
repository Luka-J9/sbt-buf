package com.github.lukaj9.sbt.models

import java.nio.file.Path

final case class BufGen(version: Version, plugins: String)

final case class Plugin(plugin: String, opt: List[String], out: Path, path: Option[Path], revision: Option[String], strategy: Option[Strategy], protocPath: Option[Path])

sealed trait Strategy
case object Direcory extends Strategy
case object All extends Strategy

final case class Managed(
    enabled: Boolean = true,
    ccEnableArenas: Boolean = false,
    javaMultipleFiles: Boolean = false,
    javaPackagePrefix: String = "com",
    javaStringCheckUtf8: Boolean = false,
    optimizeFor: String = "CODE_SIZE",
    goPackagePrefix: Option[GoPackagePrefix],
    `override`: List[Map[String, String]]
)

case class GoPackagePrefix(
    default: String,
    except: List[String],
    `override`: Map[String, String],
)