package com.github.lukaj9.sbt.platform

import munit.FunSuite
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class BufResourceFetchersSpec extends FunSuite{
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