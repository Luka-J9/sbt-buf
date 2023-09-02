package com.github.lukaj9.sbt.actions

import munit.FunSuite

import com.github.lukaj9.sbt.platform.DetectedSystem
import sbt.internal.util.ConsoleLogger
import java.nio.file.Paths
import com.github.lukaj9.sbt.platform._
import java.io.File
import java.io.File
import java.nio.file.Path

class BufActionsSpec extends FunSuite {

  private implicit val logger = ConsoleLogger()

  private val downloadLocation =
    Paths.get(getClass().getResource("/").toURI()).resolve("test-directory")

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
  test("should fail when buf.yaml and buf.workspace file not present") {
    val bufActions = BufActions(baseDir, fileHash, false)
    val exitCode = bufActions.executeBufCommand(Seq("generate"))
    assertEquals(exitCode, 1)
  }

  test("should fail if both a buf.yaml and a buf.workspace file are present") {}

  test("should fail if the buf.yaml detected is not valid") {}

  test("should fail if the buf.workspace detected is not valid") {}

  test("should do nothing if a hashfile exists and the hashes match") {}

  test("should not fail if a hashfile does not exist") {}

  test("should fail to generate if a buf.gen.yaml file does not exist") {}

  test("should generate the necessary scala code in the correct location") {}

  test("should generate the correct hashfile") {}

  test(
    "should exclude the directories in the buf files are configured to ignore"
  ) {}

  test(
    "should fail if the workspace dir points to a directory without a buf file"
  ) {}

  test(
    "should fail if the workspace dir points to a directory with an invalid buf file"
  ) {}

  test("should generate a whole workspace") {}

}
