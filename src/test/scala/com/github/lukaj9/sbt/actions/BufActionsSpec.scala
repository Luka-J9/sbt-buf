package com.github.lukaj9.sbt.actions

import munit.FunSuite

import com.github.lukaj9.sbt.platform.DetectedSystem
import sbt.internal.util.ConsoleLogger
import java.nio.file.Paths
import com.github.lukaj9.sbt.platform._
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import scala.util.Try
import io.circe.DecodingFailure
import scala.util.Failure
import scala.util.Success

import com.github.lukaj9.sbt.models._

class BufActionsSpec extends FunSuite {

  private implicit val logger = ConsoleLogger()
  val resourcePath = Paths.get(getClass().getResource("/").toURI())

  private val downloadLocation = resourcePath.resolve("test-directory")

  private val detectedSystem = DetectedSystem.detect

  private val baseDir = BaseDirectory(downloadLocation)

  private val downloadActions = DownloadActions(baseDir, detectedSystem)

  override def beforeAll(): Unit = {
    super.beforeAll()
    downloadActions.downloadBuf(None, None)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    val directory = downloadLocation.toFile()
    if (directory.exists() && directory.isDirectory) {
      val files = directory.listFiles()
      if (files != null) {
        for (file <- files) {
          file.delete()
        }
      }
      val _ = directory.delete()
    }
  }

  private val fileHash = new FileHasher {

    override def calculate(file: File): String = "stable"

    override def hashSet(files: Seq[File]): String = "stable-files"

    override def findProtoFiles(
        directory: String,
        excludeDirs: List[Path]
    ): Seq[File] = Seq.empty

  }

  test("BufActions executeBufCommand should execute buf command") {
    val bufActions = BufActions(baseDir, fileHash, false)
    val exitCode = bufActions.executeBufCommand(Seq("--help"))
    assertEquals(exitCode, 0)
  }

  test("BufActions executeBufCommand should execute buf command with plugins") {
    val bufActions = BufActions(baseDir, fileHash, true)
    val exitCode = bufActions.executeBufCommand(Seq("--help"))
    assertEquals(exitCode, 0)
  }

  // New Tests

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

  test("should fail when buf.yaml and buf.workspace file not present") {
    bootrapAndCleanup(
      BaseDirectory(sampleDir.resolve("bad_cases/no_buf_files")),
      staticHasher
    ) { result =>
      val _ = interceptMessage[RuntimeException](
        "We are seeing neither a buf file nor a workspace file in the same directory, cannot proceed"
      )(result.get)
    }
  }

  test("should fail if both a buf.yaml and a buf.workspace file are present") {
    bootrapAndCleanup(
      BaseDirectory(sampleDir.resolve("bad_cases/too_many_bufs")),
      staticHasher
    ) { result =>
      val _ = interceptMessage[RuntimeException](
        "We are seeing both a buf file and a workspace file in the same directory, cannot proceed"
      )(result.get)
    }
  }

  test("should fail if the buf.yaml detected is not valid") {
    bootrapAndCleanup(
      BaseDirectory(sampleDir.resolve("bad_cases/bad_buf_yaml")),
      staticHasher
    ) { result =>
      val _ = intercept[DecodingFailure](result.get)
    }
  }

  test("should fail if the buf.workspace detected is not valid") {
    bootrapAndCleanup(
      BaseDirectory(sampleDir.resolve("bad_cases/bad_buf_workspace")),
      staticHasher
    ) { result =>
      val _ = intercept[DecodingFailure](result.get)
    }
  }

  test("should do nothing if a hashfile exists and the hashes match") {}

  test("should fail to generate if a buf.gen.yaml file does not exist") {
    bootrapAndCleanup(
      BaseDirectory(sampleDir.resolve("bad_cases/no_buf_gen_file")),
      staticHasher
    ) { result =>
      result match {
        case Failure(exception) =>
          fail("Failed when it should not have", exception)
        case Success(value) => assertEquals(value, 1)
      }
    }
  }

  test("should generate the necessary scala code in the correct location") {}

  test("should generate the correct hashfile") {}

  test(
    "should exclude the directories in the buf files are configured to ignore"
  ) {}

  test(
    "should produce nothing but still work since buf works"
  ) {
    val testDir = sampleDir.resolve("bad_cases/bad_pointer")
    bootrapAndCleanup(BaseDirectory(testDir), staticHasher) { result =>
      assert(!testDir.resolve("target").toFile().exists())
      assertEquals(
        new String(Files.readAllBytes(testDir.resolve("sbt-buf-hashing.txt"))),
        List(BufWorkspace(Version("v1"), List(RelativePath.apply(pathString = "petapis").get)).hashCode(), "petstore.proto").mkString("\n")
      )
      assertEquals(
        result,
        Success(0)
      )
    }
  }

  test(
    "should fail if the workspace dir points to a directory with an invalid buf file"
  ) {}

  test("should generate a whole workspace") {}

  def bootrapAndCleanup(directory: BaseDirectory, fileHasher: FileHasher)(
      fn: Try[Int] => Unit
  ) = {
    val bufActions = BufActions(directory, fileHasher, false)
    DownloadActions(directory, detectedSystem).downloadBuf(None, None)

    fn(Try(bufActions.generate(directory.value)))
    val targetDir = directory.value.resolve("target")
    if (targetDir.toFile().exists()) {
      val _ = targetDir.toFile().delete()
    }
    if (directory.value.resolve("buf").toFile().exists()) {
      val _ = directory.value.resolve("buf").toFile().delete()
    }
    if (directory.value.resolve("sbt-buf-hashing.txt").toFile().exists()) {
      val _ = directory.value.resolve("sbt-buf-hashing.txt").toFile().delete()
    }
  }

}
