package com.lukaj9.sbt.models

import munit.FunSuite
import io.circe.yaml.parser
import scala.util.Success
import scala.util.Failure
import java.io.File

class BufWorkspaceSpec extends FunSuite {

  test("should decode a buf workspace (all inclusive)") {
    parser
      .parse("""
    |version: v1
    |directories:
    |  - proto
    |  - api
    """.stripMargin)
      .flatMap(_.as[BufWorkspace])
      .toTry match {
      case Success(bufWorkspace) =>
        assertEquals(
          bufWorkspace,
          BufWorkspace(
            version = Version("v1"),
            directories = List(
              RelativePath(pathString = "proto").get,
              RelativePath(pathString = "api").get
            )
          )
        )
      case Failure(e) => fail("Failed to decode", e)
    }
  }

  test("should fail to decode if we are missing a version") {
    parser
      .parse("""
    |directories:
    |  - proto
    |  - api
    """.stripMargin)
      .flatMap(_.as[BufWorkspace])
      .toTry match {
      case Success(_) => fail("Should not have decoded")
      case Failure(e) =>
        assertEquals(
          e.getMessage,
          "Missing required field: DownField(version)"
        )
    }
  }

  test("should fail to decode if we are missing directories") {
    parser
      .parse("""
    |version: v1
    """.stripMargin)
      .flatMap(_.as[BufWorkspace])
      .toTry match {
      case Success(_) => fail("Should not have decoded")
      case Failure(e) =>
        assertEquals(
          e.getMessage,
          "Missing required field: DownField(directories)"
        )
    }

  }

  test("should decode from file") {
    val file =
      new File(getClass().getResource("/samples/decoder/buf.work.yaml").toURI())
    BufWorkspace.fromFile(file) match {
      case Success(bufWorkspace) =>
        assertEquals(
          bufWorkspace,
          BufWorkspace(
            version = Version("v1"),
            directories = List(
              RelativePath(pathString = "paymentapis").get,
              RelativePath(pathString = "petapis").get
            )
          )
        )
      case Failure(e) => fail("Failed to decode", e)
    }
  }
}
