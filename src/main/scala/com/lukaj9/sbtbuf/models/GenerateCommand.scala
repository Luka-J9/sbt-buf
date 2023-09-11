package com.lukaj9.sbtbuf.models

import java.nio.file.Path

final case class GenerateCommand(
    template: Either[String, Path] = Right(Path.of("buf.gen.yaml")),
    output: Option[RelativePath] = None,
    path: List[RelativePath] = List.empty,
    location: Option[String] = None,
    setIncludeWellKnownTypes: Boolean = false,
    setIncludeImports: Boolean = false,
    excludePaths: List[RelativePath],
    setDisableSymlinks: Boolean,
    input: Option[String],
)


