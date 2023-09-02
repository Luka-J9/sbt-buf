package com.github.lukaj9.sbt.models

import munit.FunSuite
import java.io.File
import scala.util.Success
import scala.util.Failure
import io.circe.yaml.parser
import scala.util.Try
import io.circe.Json
import io.circe.JsonObject
import munit.ScalaCheckSuite
import org.scalacheck.Prop._
import com.github.lukaj9.sbt.platform.DetectedSystem
import com.github.lukaj9.sbt.platform.Windows

class BufSpec extends FunSuite with ScalaCheckSuite {

  test("should decode a buf yaml correctly (all inclusive)") {
    val yaml =
      """
    |version: v1
    |deps:
    |  - buf.build/scalapb/scalapb-validate:v0.3.4
    |build:
    |  excludes:
    |    - this/is/a/path 
    |""".stripMargin

    val file = new File("") // Dummy file
    implicit val decoder = Buf.decoder(file)

    parser.parse(yaml).flatMap(_.as[Buf]).toTry match {
      case Success(buf) =>
        assertEquals(
          buf,
          Buf(
            version = Version("v1"),
            deps = List(
              Dependency(
                root = Root("buf.build"),
                org = Organization("scalapb"),
                project = Project("scalapb-validate:v0.3.4")
              )
            ),
            build = Some(
              Build(
                excludes = List(Excludes("this/is/a/path"))
              )
            ),
            originalFile = file
          )
        )
      case Failure(e) => fail("Failed to decode", e)
    }

  }

  test("should decode a buf yaml correctly (no deps)") {
    val yaml =
      """
    |version: v1
    |build:
    |  excludes:
    |    - this/is/a/path 
    |""".stripMargin

    val file = new File("") // Dummy file
    implicit val decoder = Buf.decoder(file)

    parser.parse(yaml).flatMap(_.as[Buf]).toTry match {
      case Success(buf) =>
        assertEquals(
          buf,
          Buf(
            version = Version("v1"),
            deps = List.empty,
            build = Some(
              Build(
                excludes = List(Excludes("this/is/a/path"))
              )
            ),
            originalFile = file
          )
        )
      case Failure(e) => fail("Failed to decode", e)
    }

  }

  test("should decode a buf yaml correctly (no build)") {
    val yaml = """
    |version: v1
    |deps:
    |  - buf.build/scalapb/scalapb-validate:v0.3.4
    |""".stripMargin

    val file = new File("") // Dummy file
    implicit val decoder = Buf.decoder(file)

    parser.parse(yaml).flatMap(_.as[Buf]).toTry match {
      case Success(buf) =>
        assertEquals(
          buf,
          Buf(
            version = Version("v1"),
            deps = List(
              Dependency(
                root = Root("buf.build"),
                org = Organization("scalapb"),
                project = Project("scalapb-validate:v0.3.4")
              )
            ),
            build = None,
            originalFile = file
          )
        )
      case Failure(e) => fail("Failed to decode", e)
    }

  }

  test("should parse from file correctly") {
    val exampleFile =
      new File(getClass().getResource("/samples/buf.yaml").toURI())
    Buf.fromFile(exampleFile) match {
      case Failure(exception) =>
        fail("This should have successfully parse", exception)
      case Success(value) =>
        assertEquals(
          value,
          Buf(
            version = Version("v1"),
            deps = List(
              Dependency(
                Root(
                  "buf.build"
                ),
                Organization(
                  "scalapb"
                ),
                Project(
                  "scalapb-validate:v0.3.4"
                )
              ),
              Dependency(
                Root(
                  "buf.build"
                ),
                Organization(
                  "envoyproxy"
                ),
                Project(
                  "protoc-gen-validate:728c81676f9e54d3571603b90b34c0e6419770c6"
                )
              ),
              Dependency(
                Root(
                  "buf.build"
                ),
                Organization(
                  "scalapb"
                ),
                Project(
                  "scalapb:v0.11.11"
                )
              )
            ),
            build = None,
            exampleFile
          )
        )

    }
  }

  test("should decode a relative path correctly if it is a valid path") {
    assume(
      DetectedSystem.detect.os != Windows
    )
    parser.parse("a/b/c/d").flatMap(_.as[RelativePath]).toTry match {
      case Failure(e) => fail("Failed to decode", e)
      case success: Success[RelativePath] =>
        assertEquals[Try[RelativePath], Try[RelativePath]](
          success,
          RelativePath("a/b/c/d")
        )

    }
  }

  test("should fail to decode a relative path if it's not a relative path") {
    parser.parse("/a/b/c/d").flatMap(_.as[RelativePath]).toTry match {
      case Failure(e) =>
        assert(
          e.getMessage()
            .startsWith(
              "java.lang.IllegalArgumentException: Path `/a/b/c/d` must be relative"
            )
        )
      case Success(_) =>
        fail("This path should have not decoded")
    }
  }

  test("should fail to decode a relative path is jumping down a directory") {
    parser.parse("../a/b/c/d").flatMap(_.as[RelativePath]).toTry match {
      case Failure(e) =>
        assert(
          e.getMessage()
            .startsWith(
              "java.lang.IllegalArgumentException: Path `../a/b/c/d` must be relative"
            )
        )
      case Success(_) =>
        fail("This path should have not decoded")
    }
  }

  test("should fail to decode a relative path if it's not a valid path") {
    Json.fromString("a/b/v" + '\u0000').as[RelativePath].toTry match {
      case Failure(e) =>
        assert(
          e.getMessage()
            .startsWith(
              "java.nio.file.InvalidPathException: Nul character not allowed: a/b/v"
            )
        )
      case Success(_) =>
        fail("This path should have not decoded")
    }
  }

  test("should decode a dependency if it contains three parts") {
    Json.fromString("part1/part2/part3").as[Dependency].toTry match {
      case Failure(e) => fail("Failed to decode", e)
      case Success(result) =>
        assertEquals(
          result,
          Dependency(
            root = Root("part1"),
            org = Organization("part2"),
            project = Project("part3")
          )
        )
    }
  }

  test("should fail to decode if there are more than three parts") {
    Json.fromString("part1/part2/part3/part4").as[Dependency].toTry match {
      case Failure(e) =>
        assert(
          e.getMessage()
            .startsWith(
              "java.lang.IllegalArgumentException: Splitting by / results in more than 3 parts"
            )
        )
      case Success(_) => fail("This path should have not decoded")
    }
  }

  test("should fail to decode if there are less than three parts") {
    Json.fromString("part1/part2").as[Dependency].toTry match {
      case Failure(e) =>
        assert(
          e.getMessage()
            .startsWith(
              "java.lang.IllegalArgumentException: Splitting by / results in fewer than 3 parts"
            )
        )
      case Success(_) => fail("This path should have not decoded")
    }
  }

  test("Build should parse if nothing is present") {
    parser
      .parse(
        "excludes:"
      )
      .flatMap(_.as[Build]) match {
      case Left(value) => fail("This should have parsed", value)
      case Right(value) =>
        assertEquals(value, Build(List.empty))
    }
  }

  test("Build should parse if even the key is not present") {
    Json.fromJsonObject(JsonObject.empty).as[Build] match {
      case Left(value) =>
        throw value
      case Right(value) =>
        assertEquals(value, Build(List.empty))
    }
  }

  property("There should be no limits to Root") {
    forAll { (str: String) =>
      Json.fromString(str).as[Root].toTry match {
        case Failure(e) => fail("Failed to decode", e)
        case Success(result) =>
          assertEquals(
            result,
            Root(str)
          )
      }
    }
  }

  property("There should be no limits to Org") {
    forAll { (str: String) =>
      Json.fromString(str).as[Organization].toTry match {
        case Failure(e) => fail("Failed to decode", e)
        case Success(result) =>
          assertEquals(
            result,
            Organization(str)
          )
      }
    }
  }

  property("There should be no limits to Org") {
    forAll { (str: String) =>
      Json.fromString(str).as[Project].toTry match {
        case Failure(e) => fail("Failed to decode", e)
        case Success(result) =>
          assertEquals(
            result,
            Project(str)
          )
      }
    }
  }

}
