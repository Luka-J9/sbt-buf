package com.github.lukaj9.sbt.platform

import scala.util.Properties

import munit.FunSuite
class DetectedSystemSpec extends FunSuite {
  test("detect should return correct system Linux") {
    assume(Properties.isLinux, "This test is only for Linux")
    val system = DetectedSystem.detect
    assert(system.os == Linux)
  }

  test("detect should return correct system Windows") {
    assume(Properties.isWin, "This test is only for Windows")
    val system = DetectedSystem.detect
    assert(system.os == Windows)
  }

  test("detect should return correct system Mac") {
    assume(Properties.isMac, "This test is only for Mac")
    val system = DetectedSystem.detect
    assert(system.os == Osx)
  }
}
