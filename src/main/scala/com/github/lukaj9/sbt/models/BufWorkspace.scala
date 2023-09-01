package com.github.lukaj9.sbt.models

import java.nio.file.Path
import scala.util._
import javax.xml.stream.events.Comment
import io.circe._
import java.io.File
import io.circe.yaml.parser
import java.nio.file.Files

final case class BufWorkspace(version: Version, directories: List[RelativePath])

object BufWorkspace {

    implicit val decoder: Decoder[BufWorkspace] = new Decoder[BufWorkspace] {
        override def apply(c: HCursor): Decoder.Result[BufWorkspace] = 
            for {
                version <- c.get[Version]("version")
                directories <- c.get[List[RelativePath]]("directories")
            } yield(BufWorkspace(version, directories))
    }

    def fromFile(file:File): Try[BufWorkspace] = {
        parser.parse(new String(Files.readAllBytes(file.toPath()))).toTry.flatMap(_.as[BufWorkspace].toTry)
    }
}
