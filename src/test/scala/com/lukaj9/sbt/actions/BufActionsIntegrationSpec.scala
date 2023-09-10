package com.lukaj9.sbt.actions

import scala.util.Try
import io.circe.DecodingFailure
import scala.util.Failure
import scala.util.Success
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitResult
import java.io.IOException
import java.nio.file.Files

import com.lukaj9.sbt.models._
import java.nio.charset.StandardCharsets
import munit.FunSuite
import com.lukaj9.sbt.platform.FileHasher
import java.io.File
import sbt.internal.util.ConsoleLogger
import java.nio.file.Paths
import com.lukaj9.sbt.platform.DetectedSystem
import java.nio.file.Path

class BufActionsIntegrationSpec extends FunSuite {
  private implicit val logger = ConsoleLogger()
  val resourcePath = Paths.get(getClass().getResource("/").toURI())

  private val detectedSystem = DetectedSystem.detect

  val sampleDir = resourcePath.resolve("samples")

  def staticHasher = new FileHasher {

    override def calculate(file: File): String = file.getName()

    override def hashSet(files: Seq[File]): String =
      files.map(_.getName()).mkString(",")

    override def findProtoFiles(
        directory: String,
        excludeDirs: List[Path]
    ): Seq[File] = FileHasher.apply().findProtoFiles(directory, excludeDirs)

  }

  val bufAction = BufActions(BaseDirectory(sampleDir), staticHasher, false)

  def bootrapAndCleanup(directory: BaseDirectory)(
      fn: Try[Int] => Unit
  ) = {
    try {
      fn(Try(bufAction.generate(directory.value)))
    } finally {
      val targetDir = directory.value.resolve("target")
      if (targetDir.toFile().exists()) {
        deleteNio(targetDir)
      }
      if (sampleDir.resolve("sbt-buf-hashing.txt").toFile().exists()) {
        val _ = sampleDir.resolve("sbt-buf-hashing.txt").toFile().delete()
      }
    }
  }

  override def beforeAll(): Unit = {
    DownloadActions(BaseDirectory(sampleDir), detectedSystem)
      .downloadBuf(None, None)
  }

  override def afterAll(): Unit = {
    if (sampleDir.resolve("buf").toFile().exists()) {
      val _ = sampleDir.resolve("buf").toFile().delete()
    }
  }

  test("should fail when buf.yaml and buf.workspace file not present") {
    bootrapAndCleanup(
      BaseDirectory(sampleDir.resolve("bad_cases/no_buf_files"))
    ) { result =>
      val _ = interceptMessage[RuntimeException](
        "We are seeing neither a buf file nor a workspace file in the same directory, cannot proceed"
      )(result.get)
    }
  }

  test("should fail if both a buf.yaml and a buf.workspace file are present") {
    bootrapAndCleanup(
      BaseDirectory(sampleDir.resolve("bad_cases/too_many_bufs"))
    ) { result =>
      val _ = interceptMessage[RuntimeException](
        "We are seeing both a buf file and a workspace file in the same directory, cannot proceed"
      )(result.get)
    }
  }

  test("should fail if the buf.yaml detected is not valid") {
    bootrapAndCleanup(
      BaseDirectory(sampleDir.resolve("bad_cases/bad_buf_yaml"))
    ) { result =>
      val _ = intercept[DecodingFailure](result.get)
    }
  }

  test("should fail if the buf.workspace detected is not valid") {
    bootrapAndCleanup(
      BaseDirectory(sampleDir.resolve("bad_cases/bad_buf_workspace"))
    ) { result =>
      val _ = intercept[DecodingFailure](result.get)
    }
  }

  test("should do nothing if a hashfile exists and the hashes match") {
    val expectedHash =
      """|buf.yaml
         |buf.gen.yaml
         |petstore.proto""".stripMargin.getBytes(StandardCharsets.UTF_8)

    val dir = sampleDir.resolve("good_cases/with_hashfile")

    Files.write(sampleDir.resolve("sbt-buf-hashing.txt"), expectedHash)

    bootrapAndCleanup(
      directory = BaseDirectory(sampleDir.resolve("good_cases/with_hashfile"))
    ) { result =>
      assert(!dir.resolve("target").toFile().exists())
      assertEquals(result, Success(0))
    }
  }

  test("should fail to generate if a buf.gen.yaml file does not exist") {
    bootrapAndCleanup(
      BaseDirectory(sampleDir.resolve("bad_cases/no_buf_gen_file"))
    ) { result =>
      result match {
        case Failure(exception) =>
          fail("Failed when it should not have", exception)
        case Success(value) => assertEquals(value, 1)
      }
    }
  }

  test("should generate the correct hashfile") {
    bootrapAndCleanup(
      BaseDirectory(sampleDir.resolve("good_cases/happy_path"))
    ) { result =>
      result match {
        case Failure(exception) =>
          fail("Failed when it should not have", exception)
        case Success(value) => assertEquals(value, 0)
      }

      val expectedHash =
        """|buf.yaml
           |buf.gen.yaml
           |petstore.proto""".stripMargin

      assert(
        sampleDir.resolve("good_cases/happy_path/target").toFile().exists()
      )

      assertEquals(
        sampleDir
          .resolve(
            "good_cases/happy_path/target/com/example/petstore/generated/petstore"
          )
          .toFile()
          .listFiles()
          .map(_.getName())
          .toList
          .sorted,
        List(
          "ListUsersRequest.scala",
          "Pet.scala",
          "PetByIdRequest.scala",
          "PetByIdResponse.scala",
          "PetstoreProto.scala",
          "PetStoreServiceGrpc.scala",
          "StoreUsersResponse.scala",
          "User.scala",
          "UserByNameRequest.scala",
          "UserByNameResponse.scala"
        ).sorted
      )

      assertEquals(
        new String(
          Files.readAllBytes(
            sampleDir.resolve("sbt-buf-hashing.txt")
          )
        ),
        expectedHash
      )
    }
  }

  test(
    "should exclude the directories in the buf files are configured to ignore"
  ) {
    val testDir = sampleDir.resolve("good_cases/exclude_dir")
    bootrapAndCleanup(BaseDirectory(testDir)) { result =>
      assertEquals(
        result,
        Success(0)
      )
      assert(testDir.resolve("target").toFile().exists())
      assert(
        !testDir.resolve("target/petstore/ignored/v1/ignored").toFile().exists()
      )
      assertEquals(
        new String(Files.readAllBytes(sampleDir.resolve("sbt-buf-hashing.txt"))),
        List("buf.yaml", "buf.gen.yaml", "petstore.proto").mkString("\n")
      )

    }
  }

  test(
    "should generate the correct hashfile and code when a workspace file is present"
  ) {
    val testDir = sampleDir.resolve("good_cases/workspace")
    bootrapAndCleanup(BaseDirectory(testDir)) { result =>
      assert(testDir.resolve("target").toFile().exists())
      assertEquals(
        new String(Files.readAllBytes(sampleDir.resolve("sbt-buf-hashing.txt"))),
        List(
          BufWorkspace(
            Version("v1"),
            List(RelativePath.apply(pathString = "petapis").get)
          ).hashCode(),
          "petstore.proto"
        ).mkString("\n")
      )
      assertEquals(
        result,
        Success(0)
      )
    }
  }

  test("BufActions executeBufCommand should execute buf command") {
    val exitCode = bufAction.executeBufCommand(Seq("--version"), None)
    assertEquals(exitCode, 0)
  }

  test("BufActions executeBufCommand should execute buf command with plugins") {
    val exitCode = bufAction.executeBufCommand(Seq("--version"), None)
    assertEquals(exitCode, 0)
  }


  // Shamlessly copied from https://www.baeldung.com/scala/delete-directories-recursively
  private def deleteNio(root: Path): Unit = {
    val _ = Files.walkFileTree(
      root,
      new SimpleFileVisitor[Path] {
        override def visitFile(
            file: Path,
            attrs: BasicFileAttributes
        ): FileVisitResult = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }
        override def postVisitDirectory(
            dir: Path,
            exc: IOException
        ): FileVisitResult = {
          Files.delete(dir)
          FileVisitResult.CONTINUE
        }
      }
    )
  }
}
