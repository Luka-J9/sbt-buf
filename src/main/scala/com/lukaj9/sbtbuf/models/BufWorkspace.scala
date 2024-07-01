package com.lukaj9.sbtbuf.models

import scala.util._
import io.circe._
import java.io.File
import io.circe.yaml.parser
import java.nio.file.Files

final case class BufWorkspace(
    version: Version,
    directories: List[RelativePath],
    originalFile: File
)

object BufWorkspace {

  implicit def decoder(originalFile: File): Decoder[BufWorkspace] =
    new Decoder[BufWorkspace] {
      override def apply(c: HCursor): Decoder.Result[BufWorkspace] =
        for {
          version <- c.get[Version]("version")
          directories <- c.get[List[RelativePath]]("directories")
        } yield (BufWorkspace(version, directories, originalFile))
    }

  def fromFile(file: File): Try[BufWorkspace] = {
    implicit val decoder = BufWorkspace.decoder(file)
    parser
      .parse(new String(Files.readAllBytes(file.toPath())))
      .toTry
      .flatMap(_.as[BufWorkspace].toTry)
  }
}
