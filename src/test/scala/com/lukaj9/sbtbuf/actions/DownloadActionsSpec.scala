package com.lukaj9.sbtbuf.actions

import munit.FunSuite
import com.lukaj9.sbtbuf.platform.DetectedSystem
import sbt.internal.util.ConsoleLogger
import java.nio.file.Paths
import com.lukaj9.sbtbuf.platform._
import java.net.URI
import java.nio.file.Files
import java.security.MessageDigest

class DownloadActionsSpec extends FunSuite {

  private implicit val logger = ConsoleLogger()

  private val downloadLocation =
    Paths.get(getClass().getResource("/").toURI()).resolve("test-directory")

  private val detectedSystem = DetectedSystem.detect

  private val baseDir = BaseDirectory(downloadLocation)

  private val downloadActions = DownloadActions(baseDir, detectedSystem)

  override def beforeAll(): Unit = {
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

  test(
    "DownloadActions downloadBuf downloads the override version if provided (Windows X86)"
  ) {

    assume(
      DetectedSystem.detect.os == Windows && detectedSystem.arch == X86_64,
      "This test is only for Windows"
    )
    val bufPath = baseDir.toBufPath
    downloadActions.downloadBuf(Some("v1.15.1"), None)
    assert(bufPath.toFile.exists)
    // Assert that the sha256sum of the downloaded file matches the expected value
    assertEquals(
      sha256sum(
        bufPath.toFile
      ),
      "481a6f48a1c17b31c9a81c60bc8bd753cee7670a51047703cdc609af60c5f933"
    )
    bufPath.toFile().delete()
  }

  test(
    "DownloadActions downloadBuf downloads the override version if provided (Linux X86)"
  ) {
    assume(
      DetectedSystem.detect.os == Linux && detectedSystem.arch == X86_64,
      "This test is only for Linux"
    )
    val bufPath = baseDir.toBufPath
    downloadActions.downloadBuf(Some("v1.15.1"), None)
    assert(bufPath.toFile.exists)
    assertEquals(
      sha256sum(
        bufPath.toFile
      ),
      "91b7005e3b11ac2e7925fbf36b5e97af256db397c7754aa491c479b6da61b5f9"
    )

    bufPath.toFile().delete()
  }

  test(
    "DownloadActions downloadBuf downloads the override version if provided (OSX X86)"
  ) {
    assume(
      DetectedSystem.detect.os == Osx && detectedSystem.arch == X86_64,
      "This test is only for Linux"
    )
    val bufPath = baseDir.toBufPath
    downloadActions.downloadBuf(Some("v1.15.1"), None)
    assert(bufPath.toFile.exists)

    assertEquals(
      sha256sum(
        bufPath.toFile
      ),
      "a1a71ccd7c2ebd4cb3ebe401c593f63f8970e4f18057bd68227db6cd427a2f44"
    )
    bufPath.toFile().delete()
  }

  test(
    "DownloadActions downloadProtocPlugins should download protoc plugins to the correct path"
  ) {
    val protocPluginsPath = baseDir.toProtocPluginsPath
    downloadActions.downloadProtocPlugins(
      Seq(
        ProtocPlugin(
          "validate",
          new URI(
            "https://repo1.maven.org/maven2/com/thesamet/scalapb/protoc-gen-scalapb-validate/0.3.4/protoc-gen-scalapb-validate-0.3.4-windows.bat"
          )
        )
      )
    )
    val expectedPath = protocPluginsPath.resolve("validate")
    assert(expectedPath.toFile.exists)
    assertEquals(
      sha256sum(
        expectedPath.toFile()
      ),
      "62bde39678dcc4ae6e4143cd444e7a34cf59b22abb4548b3ce43df73970d444d"
    )
    expectedPath.toFile.delete()
  }

  test(
    "DownloadActions downloadProtocPlugins should download buf from override uri"
  ) {
    val bufPath = baseDir.toBufPath
    val overrideUri =
      getClass().getResource("/override/buf").toURI()

    Paths.get(overrideUri).toFile().setExecutable(true)

    val shouldNoBeUsed = Some("v1.15.1")
    val over = Some(overrideUri)
    downloadActions.downloadBuf(shouldNoBeUsed, over)
    assertEquals(
      sha256sum(bufPath.toFile()),
      sha256sum(Paths.get(overrideUri).toFile())
    )

    bufPath.toFile().delete()

  }

  private def sha256sum(file: java.io.File): String = {
    val bytes = Files.readAllBytes(file.toPath())
    val checksum = MessageDigest.getInstance("SHA-256").digest(bytes)
    checksum.map("%02x".format(_)).mkString
  }

}
