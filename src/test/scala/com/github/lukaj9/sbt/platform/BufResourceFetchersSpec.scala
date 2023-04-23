package com.github.lukaj9.sbt.platform

import munit.FunSuite
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import sbt.internal.util.ConsoleLogger
import java.net.URI


class BufResourceFetchersSpec extends FunSuite{


  implicit val logger = ConsoleLogger()
  
  test("latest version") {
    assert(BufResourceFetchers.latestVersion().matches("""^v\d+\.\d+\.\d+$"""))
  }

  test("InputStream for extracting version should be closed") {
    val inputStream = new TestInputStream(""""tag_name": "v1.17.0",""")
    val _ = BufResourceFetchers.extractVersion(inputStream)
    intercept[IOException](inputStream.close())
  }
  
  test("downloadVersion should return correct URI") {
    val version = "v1.17.0"
    val system = DetectedSystem(Linux, X86_64)
    val expected = new java.net.URL("https://github.com/bufbuild/buf/releases/download/v1.17.0/buf-Linux-x86_64").toURI()

    assertEquals(BufResourceFetchers.downloadVersion(version, system), expected)
  }

  test("downloadVersion should throw exception for unsupported system") {
    val version = "v1.17.0"
    val system = DetectedSystem(Hpux, X86_64)

    intercept[IllegalArgumentException](BufResourceFetchers.downloadVersion(version, system))
  }

  test("download Buf should create file in the test resource directory") {
    val system = DetectedSystem(Linux, X86_64)
    val version = "v1.17.0"
    val to = Paths.get(getClass().getResource("/").toURI()).resolve("buf")
    val file = BufResourceFetchers.downloadBuf(system, Some(version), to)
    assert(file.exists())
    assertEquals(file.getName(), "buf") 
    file.delete()
  }

  test("download Buf from File URI and place it in the test resource directory") {
    val system = DetectedSystem(Linux, X86_64)
    val version = "v1.17.0"
    val to = Paths.get(getClass().getResource("/").toURI())
    val testFile= to.resolve("buf-test")
    val file = BufResourceFetchers.downloadBuf(system, Some(version), testFile)

    val fileUri = file.toURI()

    val file2 = BufResourceFetchers.downloadFile(fileUri, to.resolve("buf-test2"))
    assert(file2.exists())
    assertEquals(file2.getName(), "buf-test2")
    file.delete()
    file2.delete()
  }

  test("download Plugin from File URI and place it in the test resource directory") {

    val fileUri = new URI("https://repo1.maven.org/maven2/com/thesamet/scalapb/protoc-gen-scalapb-validate/0.3.4/protoc-gen-scalapb-validate-0.3.4-unix.sh")
    val to = Paths.get(getClass().getResource("/").toURI())

    val download = BufResourceFetchers.downloadPlugin(fileUri, to.resolve("plugin"))
    assert(download.exists())
    assertEquals(download.getName(), "plugin")
    download.delete()
  }
}


class TestInputStream(data: String) extends InputStream {
  private val bytes = data.getBytes("UTF-8")
  private var pos = 0
  private var closed = false

  override def read(): Int = {
    if (closed) {
      throw new IOException("Stream is closed")
    }
    if (pos >= bytes.length) {
      return -1
    }
    val byteRead = bytes(pos)
    pos += 1
    byteRead.toInt
  }

  override def close(): Unit = {
    if (closed) {
      throw new IOException("Stream is already closed")
    }
    closed = true
  }
}