name := """sbt-buf"""
organization := "com.lukaj9"
scalaVersion := "2.12.17"

sbtPlugin := true

libraryDependencies ++= Seq(
  "io.circe" %% "circe-yaml" % "0.14.2",
  "org.scalameta" %% "munit" % "0.7.29" % Test,
  "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test
)

inThisBuild(
  List(
    organization := "com.lukaj9",
    homepage := Some(url("https://github.com/luka-j9/sbt-buf")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "Luka-J9",
        "Luka Jurukovski",
        "@luka-j9",
        url("https://lukaj9.com")
      )
    )
  )
)

sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

console / initialCommands := """import com.lukaj9.sbt._"""

enablePlugins(ScriptedPlugin)
// set up 'scripted; sbt plugin for testing sbt plugins
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(
    RefPredicate.StartsWith(Ref.Tag("v")),
    RefPredicate.Equals(Ref.Branch("main"))
  )

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

ThisBuild / versionScheme := Some("early-semver")

ThisBuild / githubWorkflowOSes := Seq(
  "ubuntu-latest",
  "macos-latest",
  "windows-latest"
)

Test / parallelExecution := false
