package com.github.lukaj9.sbt.models

import munit.FunSuite

class BufSpec extends FunSuite {

  test("should decode a buf yaml correctly (all inclusive)") {}

  test("should decode a buf yaml correctly (no deps)") {}

  test("should decode a buf yaml correctly (no build)") {}

  test("should decode a relative path correctly if it is a valid path") {}

  test("should fail to decode a relative path if it's not a relative path") {}

  test("should fail to decode a relative path is jumping down a directory") {}

  test("should fail to decode a relative path if it's not a valid path") {}

  test("should decode a dependency if it contains three parts") {}

  test("should fail to decode if there are more than three parts") {}

  test("should fail to decode if there are less than three parts") {}

}
