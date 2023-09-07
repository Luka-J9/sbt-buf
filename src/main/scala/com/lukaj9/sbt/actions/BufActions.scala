package com.lukaj9.sbt.actions

import sbt.util.Logger
import com.lukaj9.sbt.platform.DetectedSystem
import java.nio.file.Path
import java.nio.file.Files
import java.nio.charset.StandardCharsets
import com.lukaj9.sbt.platform.Windows
import java.io.File
import com.lukaj9.sbt.models.Buf
import com.lukaj9.sbt.models.BufWorkspace

import sys.process.Process
import com.lukaj9.sbt.platform.FileHasher
import scala.util._
import scala.sys.process.ProcessLogger

trait BufActions {
  def executeBufCommand(args: Seq[String], directory: Option[File]): Int

  def generate(workspaceDir: Path): Int

  def generateDependencies(
      workspaceDir: Path
  ): Int

}

object BufActions {

  private val bufFileName = "buf.yaml"
  private val bufWorkspaceFileName = "buf.work.yaml"
  private val bufGenFileName = "buf.gen.yaml"
  private val hashFile = "sbt-buf-hashing.txt"

  def apply(
      baseDir: BaseDirectory,
      fileHasher: FileHasher,
      containsPlugins: Boolean
  )(implicit logger: Logger): BufActions =
    new BufActions {


      private val processLogger = new ProcessLogger {

        override def out(s: => String): Unit = logger.info(s)

        override def err(s: => String): Unit = logger.err(s)

        override def buffer[T](f: => T): T = f

      }

      override def executeBufCommand(
          args: Seq[String],
          directory: Option[File]
      ): Int = {
        if (directory.forall(_.isDirectory())) {
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

          val processBuilder = Process(execute, directory, env.toSeq: _*)
          processBuilder.run(processLogger).exitValue()
        } else {
          logger.err(s"Submitted directory ${directory} is not a directory}")
          1
        }
      }

      override def generateDependencies(workspaceDir: Path): Int = {
        val bufPath = workspaceDir.resolve(bufFileName)
        val isBuf = Files.exists(bufPath)
        if(isBuf) {
          val buf = Buf.fromFile(bufPath.toFile()).get
          buf.deps.map(
            dep =>
              executeBufCommand(
                args = 
                  List(
                    "generate",
                    "--include-imports",
                    "--template",
                    s"${workspaceDir.toAbsolutePath().toString()}/buf.gen.yaml",
                    dep.name
                  ),
                  Some(workspaceDir.toAbsolutePath().toFile())
              )
          ).max
        } else {
          logger.err("Cannot generate dependencies for a workspace file")
          1
        }
      }

      override def generate(workspaceDir: Path): Int = {
        val bufPath = workspaceDir.resolve(bufFileName)
        val isBuf = Files.exists(bufPath)
        val bufWorkspacePath = workspaceDir.resolve(bufWorkspaceFileName)
        val isWorkspace = Files.exists(bufWorkspacePath)

        if (isBuf && isWorkspace) {
          throw new RuntimeException(
            "We are seeing both a buf file and a workspace file in the same directory, cannot proceed"
          )
        }

        if (!isBuf && !isWorkspace) {
          throw new RuntimeException(
            "We are seeing neither a buf file nor a workspace file in the same directory, cannot proceed"
          )
        }

        val result: Either[Buf, BufWorkspace] = if (isBuf) {
          Left(Buf.fromFile(bufPath.toFile()).get) // We just die here
        } else {
          Right(BufWorkspace.fromFile(bufWorkspacePath.toFile()).get)
        }

        generateInternal(workspaceDir, result)
      }

      private def generateInternal(
          workspaceDir: Path,
          workspace: Either[Buf, BufWorkspace]
      ): Int = {
        val path = baseDir.value.resolve(hashFile)
        lazy val existing =
          new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
        lazy val hash = generateHash(workspaceDir, workspace)

        if (
          Files.exists(path) && Files.isRegularFile(path) && existing == hash
        ) {
          logger.info(
            "Proto files and generation hashes match, skipping generation"
          )
          0
        } else {
          logger.info(
            s"Generating at directory ${workspaceDir.toAbsolutePath.toString()}"
          )
          val exitCode = executeBufCommand(
            List(
              "generate",
              "--template",
              s"${workspaceDir.toAbsolutePath().toString()}/buf.gen.yaml"
            ),
            Some(workspaceDir.toAbsolutePath().toFile())
          )
          if (exitCode == 0) {
            val _ = Files.write(path, hash.getBytes())
          }
          exitCode
        }
      }

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
              workspaceDir.normalize().toAbsolutePath().toString(),
              excludeDirs = excludeDir
            )
            logger.debug(
              s"Search Directory: ${workspaceDir.toAbsolutePath().toString()}"
            )
            logger.debug("Proto Files Detected")
            protofiles.map(_.getPath()).foreach(logger.debug(_))
            val bufGenHash = Try(
              buf.originalFile.toPath.getParent.resolve(bufGenFileName).toFile()
            ).map(fileHasher.calculate(_)).getOrElse("")
            s"${fileHasher.calculate(buf.originalFile)}\n${bufGenHash}\n${fileHasher.hashSet(protofiles)}"
          case Right(bufWorkspace) =>
            val alldir = bufWorkspace.directories
              .map(rp => workspaceDir.resolve(rp.path))
              .map { wp =>
                val file = new File(wp.toFile(), "buf.yaml")

                if (file.exists) {

                  val buf =
                    Buf.fromFile(new File(wp.toFile(), "buf.yaml")) match {
                      case Success(x) => x
                      case Failure(ex) =>
                        logger.error(
                          s"The Buf file found in path ${wp.toUri().toString()} is not valid, bailing out"
                        )
                        throw ex
                    }

                  generateHash(
                    workspaceDir = wp,
                    workspace = Left(buf)
                  )
                } else {
                  fileHasher.hashSet(
                    fileHasher.findProtoFiles(
                      wp.normalize().toAbsolutePath().toString(),
                      Nil
                    )
                  )
                }
              }
              .mkString("\n")

            s"${bufWorkspace.hashCode.toString}\n${alldir}"

        }
      }
    }

}
