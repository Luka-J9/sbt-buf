package com.github.lukaj9.sbt

import munit.FunSuite
import com.github.lukaj9.sbt.platform.GithubRetreaver

class BufTest extends FunSuite{


  test("latest version") {
    assertEquals(GithubRetreaver.latestVersion(), "v1.17.0")
  }
}
