package com.github.lukaj9.sbt.actions

import munit.FunSuite
import com.github.lukaj9.sbt.platform.DetectedSystem
import sbt.internal.util.ConsoleLogger
import java.nio.file.Paths
import com.github.lukaj9.sbt.platform._
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
    super.beforeAll()
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

  test("DownloadActions downloadBuf should download buf to the correct path") {
    val bufPath = baseDir.toBufPath
    downloadActions.downloadBuf(None, None)
    assert(bufPath.toFile.exists)
  }

  test(
    "DownloadActions downloadBuf downloads the override version if provided (Windows X86)"
  ) {

    assume(
      DetectedSystem.detect.os == Windows && detectedSystem.arch == X86_64,
      "This test is only for Windows"
    )
    val bufPath = baseDir.toBufPath
    downloadActions.downloadBuf(Some("1.15.1"), None)
    assert(bufPath.toFile.exists)
    // Assert that the sha256sum of the downloaded file matches the expected value
    assertEquals(
      sha256sum(
        bufPath.toFile
      ),
      "b94a5fa0b0f7b35090c6064125fcd43e95ac8f1da914bbafb8ea311451d2babe"
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
    downloadActions.downloadBuf(Some("1.15.1"), None)
    assert(bufPath.toFile.exists)
    assertEquals(
      sha256sum(
        bufPath.toFile
      ),
      "cc317bbc69af962ebad09ee415dc61f6792784bffbd0a1d6d80246ae7a7e93fc"
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
    downloadActions.downloadBuf(Some("1.15.1"), None)
    assert(bufPath.toFile.exists)

    assertEquals(
      sha256sum(
        bufPath.toFile
      ),
      "fcf9cbcc16385cfe4fec442fd4a6c414e525311f34939126c313e19f9d8a4aa6"
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

    val shouldNoBeUsed = Some("1.15.1")
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
