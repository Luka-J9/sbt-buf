package com.github.lukaj9.sbt.actions

import munit.FunSuite
import com.github.lukaj9.sbt.platform.DetectedSystem
import sbt.internal.util.ConsoleLogger
import java.nio.file.Paths
import com.github.lukaj9.sbt.platform._
import java.net.URI

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
      directory.delete()
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
      "9175bbcae32e45a5e7cda0340d835f918070c8fd03b0e0c3e065c98779195308"
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
      "a4b18f4e44fd918847e310b93ad94ea66913f2040956f856520b92f731e52d7f"
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
      "63d7cdf641a2c21705c065d6c9407225ef846a03be45cdb668ee5eb43990538b"
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
    println(expectedPath.toFile.getAbsolutePath())
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
    "DownloadACtions downloadProtocPlugins should download buf from override uri"
  ) {
    val bufPath = baseDir.toBufPath
    val overrideUri =
      getClass().getResource("/override/buf-Linux-x86_64").toURI()
    downloadActions.downloadBuf(Some("1.15.1"), Some(overrideUri))
    assertEquals(
      sha256sum(bufPath.toFile()),
      sha256sum(Paths.get(overrideUri).toFile())
    )

    bufPath.toFile().delete()

  }

  private def sha256sum(file: java.io.File): String = {
    import java.security.MessageDigest
    val digest = MessageDigest.getInstance("SHA-256")
    val is = new java.io.FileInputStream(file)
    val buffer = new Array[Byte](8192)
    var read = 0
    while ({ read = is.read(buffer); read != -1 }) {
      digest.update(buffer, 0, read)
    }
    is.close()
    digest.digest().map("%02x".format(_)).mkString
  }

}
