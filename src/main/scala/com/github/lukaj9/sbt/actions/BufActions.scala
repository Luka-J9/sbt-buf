package com.github.lukaj9.sbt.actions

import sbt.util.Logger
import com.github.lukaj9.sbt.platform.DetectedSystem
import java.nio.file.Path
import java.nio.file.Files
import java.nio.charset.StandardCharsets
import com.github.lukaj9.sbt.platform.Windows
import java.io.File
import com.github.lukaj9.sbt.models.Buf
import com.github.lukaj9.sbt.models.BufWorkspace

import sys.process.Process
import com.github.lukaj9.sbt.platform.FileHasher

trait BufActions {
  def executeBufCommand(args: Seq[String]): Int

  def generate(workspaceDir: Path, workspace: Either[Buf, BufWorkspace]): Unit

}

object BufActions {

  def apply(
      baseDir: BaseDirectory,
      fileHasher: FileHasher,
      containsPlugins: Boolean
  )(implicit logger: Logger): BufActions =
    new BufActions {
      override def executeBufCommand(
          args: Seq[String]
      ): Int = {

        val env =
          if (containsPlugins) {
            val detectedSystem = DetectedSystem.detect
            val envVar = detectedSystem.os match {
              case Windows => "Path"
              case _       => "PATH"
            }
            sys.env + (envVar -> (sys.env.getOrElse(
              envVar,
              ""
            ) + File.pathSeparator + baseDir.toProtocPluginsPath
              .toAbsolutePath()))
          } else {
            sys.env
          }

        val execute =
          (List(baseDir.toBufPath.toAbsolutePath().toString()) ++ args)

        val processBuilder = Process(execute, None, env.toSeq: _*)
        processBuilder.run().exitValue()
      }

      override def generate(
          workspaceDir: Path,
          workspace: Either[Buf, BufWorkspace]
      ): Unit = {
        logger.info("Gen Start")
        val path = baseDir.value.resolve("buf-gen-hash.txt")
        logger.info(path.toAbsolutePath().toString())
        lazy val existing =
          new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
        lazy val hash = generateHash(workspaceDir, workspace)

        if (
          Files.exists(path) && Files.isRegularFile(path) && existing == hash
        ) {
          logger.info("We skipping dis shiz")
          Unit
        } else {
          logger.info("Generating....")
          executeBufCommand(
            List(
              "generate",
              "--template",
              "buf.gen.yaml",
              workspaceDir.toAbsolutePath().toString()
            )
          )
          Files.write(path, hash.getBytes())
        }
      }

      // TODO: Add gen files to hashing
      private def generateHash(
          workspaceDir: Path,
          workspace: Either[Buf, BufWorkspace]
      ): String = {
        workspace match {
          case Left(buf) =>
            val excludeDir = buf.build
              .map(_.excludes.map { exclude =>
                workspaceDir.resolve(exclude.value)
              })
              .getOrElse(List.empty)

            val protofiles: Seq[File] = fileHasher.findProtoFiles(
              workspaceDir.normalize().toAbsolutePath().toString()
            )
            logger.info("Files")
            logger.info(workspaceDir.toAbsolutePath().toString())
            logger.info(protofiles.map(_.getPath()).mkString)
            s"${buf.hashCode().toString()}\n${fileHasher.hashSet(protofiles)}"
          case Right(bufWorkspace) =>
            val alldir = bufWorkspace.directories
              .map(rp => workspaceDir.resolve(rp.path))
              .map { wp =>
                val bytes =
                  Files.readAllBytes(new File(wp.toFile(), "buf.yaml").toPath())

                generateHash(
                  workspaceDir = wp,
                  workspace = Left(Buf.fromBytes(bytes))
                )
              }
              .mkString("\n")

            s"${bufWorkspace.hashCode.toString}\n${alldir}"

        }
      }
    }

}
