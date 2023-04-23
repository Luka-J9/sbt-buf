package com.github.lukaj9.sbt

package object actions {
  final case class BaseDirectory(value: java.nio.file.Path) extends AnyVal {
    def toBufPath: java.nio.file.Path = value.resolve("buf")
    def toProtocPluginsPath: java.nio.file.Path = value.resolve("protoc-plugins")
  }
}
