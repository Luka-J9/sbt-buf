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
import scala.util._

trait BufActions {
  def executeBufCommand(args: Seq[String]): Int

  def generate(workspaceDir: Path): Unit

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

      override def generate(workspaceDir: Path): Unit = {
        val bufPath = workspaceDir.resolve(bufFileName)
        val isBuf = Files.exists(bufPath)
        val bufWorkspacePath = workspaceDir.resolve(bufWorkspaceFileName)
        val isWorkspace = Files.exists(bufWorkspacePath)

        if(isBuf && isWorkspace) {
          throw new RuntimeException("We are seeing both a buf file and a workspace file in the same directory, cannot proceed")
        }

        val result: Either[Buf, BufWorkspace] = if(isBuf){
          Left(Buf.fromFile(bufPath.toFile()).get)// We just die here
        } else {
          Right(BufWorkspace.fromFile(bufWorkspacePath.toFile()).get)
        }

        generateInternal(workspaceDir, result)
      }

       def generateInternal(
          workspaceDir: Path,
          workspace: Either[Buf, BufWorkspace]
      ): Unit = {
        val path = baseDir.value.resolve(hashFile)
        lazy val existing =
          new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
        lazy val hash = generateHash(workspaceDir, workspace)

        if (
          Files.exists(path) && Files.isRegularFile(path) && existing == hash
        ) {
          logger.info("Proto files and generation hashes match, skipping generation")
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
            logger.debug(s"Search Directory: ${workspaceDir.toAbsolutePath().toString()}")
            logger.debug("Proto Files Detected")
            protofiles.map(_.getPath()).foreach(logger.debug(_))
            val bufGenHash = Try(buf.originalFile.toPath.getParent.resolve(bufGenFileName).toFile()).map(fileHasher.calculate(_)).getOrElse("")
            s"${fileHasher.calculate(buf.originalFile)}\n${bufGenHash}\n${fileHasher.hashSet(protofiles)}"
          case Right(bufWorkspace) =>
            val alldir = bufWorkspace.directories
              .map(rp => workspaceDir.resolve(rp.path))
              .map { wp =>

                val buf = Buf.fromFile(new File(wp.toFile(), "buf.yaml")) match {
                  case Success(x) => x
                  case Failure(ex) =>
                    logger.error(s"The Buf file found in path ${wp.toUri().toString()} is not valid, bailing out")
                    throw ex
                }

                generateHash(
                  workspaceDir = wp,
                  workspace = Left(buf)
                )
              }
              .mkString("\n")

            s"${bufWorkspace.hashCode.toString}\n${alldir}"

        }
      }
    }

}
