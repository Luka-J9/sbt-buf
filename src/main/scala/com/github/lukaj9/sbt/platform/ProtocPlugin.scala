package com.github.lukaj9.sbt.platform

final case class ProtocPlugin(fn: DetectedSystem => Option[java.net.URI], fileNameOverride: Option[String] = None)
