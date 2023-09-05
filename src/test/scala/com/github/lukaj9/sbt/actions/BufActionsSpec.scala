package com.github.lukaj9.sbt.actions

import munit.FunSuite

import com.github.lukaj9.sbt.platform.DetectedSystem
import sbt.internal.util.ConsoleLogger
import java.nio.file.Paths
import com.github.lukaj9.sbt.platform._
import java.io.File
import java.nio.file.Path

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
    val exitCode = bufActions.executeBufCommand(Seq("--version"), None)
    assertEquals(exitCode, 0)
  }

  test("BufActions executeBufCommand should execute buf command with plugins") {
    val bufActions = BufActions(baseDir, fileHash, true)
    val exitCode = bufActions.executeBufCommand(Seq("--version"), None)
    assertEquals(exitCode, 0)
  }

}
