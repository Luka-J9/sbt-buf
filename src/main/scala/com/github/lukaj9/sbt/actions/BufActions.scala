package com.github.lukaj9.sbt.actions

import sbt.util.Logger
import com.github.lukaj9.sbt.platform.DetectedSystem
import java.nio.file.Path
import java.nio.file.Files
import com.github.lukaj9.sbt.platform.Windows
import java.io.File

import sys.process.Process

trait BufActions {
  def executeBufCommand(args: Seq[String]): Int
}

object BufActions {

  def apply(baseDir: BaseDirectory, containsPlugins: Boolean)(implicit logger: Logger): BufActions =
    new BufActions {
      override def executeBufCommand(
          args: Seq[String]
      ): Int = {

        val env = 
          if(containsPlugins) {
            val detectedSystem = DetectedSystem.detect
            val envVar = detectedSystem.os match {
              case Windows => "Path"
              case _       => "PATH"
            }
            sys.env + (envVar -> (sys.env.getOrElse(
              envVar,
              ""
            ) + File.pathSeparator + baseDir.toProtocPluginsPath.toAbsolutePath()))
        } else {
            sys.env
        }

        val execute = (List(baseDir.toBufPath.toAbsolutePath().toString()) ++ args)

        val processBuilder = Process(execute, None, env.toSeq: _*)
        processBuilder.run().exitValue()
      }
    }

}
