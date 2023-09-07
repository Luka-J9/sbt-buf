package com.lukaj9.sbt.models

import java.nio.file.Path
import scala.util._
import io.circe._
import java.io.File
import io.circe.yaml.parser
import java.nio.file.Files
import java.nio.file.Paths

final case class Buf(
    version: Version,
    deps: List[Dependency],
    build: Option[Build],
    originalFile: File
)

object Buf {

  def fromFile(originalFile: File): Try[Buf] = {
    implicit val d: Decoder[Buf] = decoder(originalFile)
    parser
      .parse(new String(Files.readAllBytes(originalFile.toPath())))
      .toTry
      .flatMap(_.as[Buf].toTry)
  }

  implicit def decoder(originalFile: File): Decoder[Buf] = new Decoder[Buf] {

    override def apply(c: HCursor): Decoder.Result[Buf] = {
      for {
        version <- c.get[Version]("version")
        deps <- c.getOrElse[List[Dependency]]("deps")(List.empty)
        build <- c.getOrElse[Option[Build]]("build")(None)
      } yield (
        Buf(version, deps, build, originalFile)
      )
    }

  }
}

final case class Version(value: String) extends AnyVal
object Version {
  implicit val decoder: Decoder[Version] = new Decoder[Version] {
    override def apply(c: HCursor): Decoder.Result[Version] = {
      c.as[String].map(Version(_))
    }
  }
}

case class RelativePath private (path: Path) extends AnyVal
object RelativePath {

  implicit val decoder: Decoder[RelativePath] = new Decoder[RelativePath] {

    override def apply(c: HCursor): Decoder.Result[RelativePath] = {
      Decoder.decodeString.emapTry(
        RelativePath(_)
      )(c)
    }

  }

  def apply(pathString: String): Try[RelativePath] = {
    Try(Paths.get(pathString)).flatMap { path =>
      if (path.isAbsolute() || path.startsWith(".."))
        Failure(
          new IllegalArgumentException(
            s"Path `${path.toString}` must be relative"
          )
        )
      else Success(new RelativePath(path))
    }

  }
}

case class Build(excludes: List[Excludes])

object Build {
  implicit val decoder: Decoder[Build] = new Decoder[Build] {
    override def apply(c: HCursor): Decoder.Result[Build] = {
      c.downField("excludes").as[Option[List[Excludes]]].map {
        case None        => Build(List.empty)
        case Some(value) => Build(value)
      }
    }

  }
}
case class Excludes(value: String) extends AnyVal

object Excludes {
  implicit val decoder: Decoder[Excludes] = new Decoder[Excludes] {
    override def apply(c: HCursor): Decoder.Result[Excludes] = {
      c.as[String].map(Excludes(_))
    }
  }
}

case class Name(value: String) extends AnyVal
case class Dependency(root: Root, org: Organization, project: Project) {
  def name = s"${root.value}/${org.value}/${project.value}"
}
object Dependency {
  implicit val decoder: Decoder[Dependency] = new Decoder[Dependency] {
    override def apply(c: HCursor): Decoder.Result[Dependency] = {
      Decoder.decodeString.emapTry(
        _.split("/") match {
          case Array(root, org, project) =>
            Success(Dependency(Root(root), Organization(org), Project(project)))
          case Array(_, _) =>
            Failure(
              new IllegalArgumentException(
                "Splitting by / results in fewer than 3 parts"
              )
            )
          case _ =>
            Failure(
              new IllegalArgumentException(
                "Splitting by / results in more than 3 parts"
              )
            )
        }
      )(c)
    }
  }
}
case class Root(value: String) extends AnyVal

object Root {
  implicit val decoder: Decoder[Root] = new Decoder[Root] {
    override def apply(c: HCursor): Decoder.Result[Root] = {
      c.as[String].map(Root(_))
    }
  }
}
case class Organization(value: String) extends AnyVal

object Organization {
  implicit val decoder: Decoder[Organization] = new Decoder[Organization] {
    override def apply(c: HCursor): Decoder.Result[Organization] = {
      c.as[String].map(Organization(_))
    }
  }
}
case class Project(value: String) extends AnyVal

object Project {
  implicit val decoder: Decoder[Project] = new Decoder[Project] {
    override def apply(c: HCursor): Decoder.Result[Project] = {
      c.as[String].map(Project(_))
    }
  }
}
