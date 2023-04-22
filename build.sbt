
name := """sbt-buf"""
organization := "com.github.lukaj9"
version := "0.1-SNAPSHOT"

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "protoc-bridge" % "0.9.6",
  "org.scalameta" %% "munit" % "0.7.29" % Test
)

inThisBuild(List(
  organization := "com.github.lukaj9",
  homepage := Some(url("https://github.com/luka-j9/sbt-buf")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "Luka-J9",
      "Luka Jurukovski",
      "foo@bar.org",
      url("https://scala-lang.org")
    )
  )
))

initialCommands in console := """import com.github.lukaj9.sbt._"""

enablePlugins(ScriptedPlugin)
// set up 'scripted; sbt plugin for testing sbt plugins
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))

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

