package com.github.lukaj9.sbt.actions

import munit.FunSuite

import com.github.lukaj9.sbt.platform.DetectedSystem
import sbt.internal.util.ConsoleLogger
import java.nio.file.Paths
import com.github.lukaj9.sbt.platform._
import java.net.URI


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
      directory.delete()
    }
  }

  test("BufActions executeBufCommand should execute buf command") {
    val bufPath = baseDir.toBufPath
    val bufActions = BufActions(baseDir, false)
    val exitCode = bufActions.executeBufCommand(Seq("--help"))
    assertEquals(exitCode, 0)
  }

    test("BufActions executeBufCommand should execute buf command with plugins") {
        val bufPath = baseDir.toBufPath
        val bufActions = BufActions(baseDir, true)
        val exitCode = bufActions.executeBufCommand(Seq("--help"))
        assertEquals(exitCode, 0)
    }
  
}
