package com.github.lukaj9.sbt.platform

import java.security.MessageDigest
import java.io.FileInputStream
import java.io.File
import java.nio.file.Path

trait FileHasher {

  def calculate(file: File): String

  def hashSet(files: Seq[File]): String

  def findProtoFiles(
      directory: String,
      excludeDirs: List[Path] = Nil
  ): Seq[File]
}

object FileHasher {

  def apply() = new FileHasher {
    override def calculate(file: File): String = {
      val fis = new FileInputStream(file)
      try {
        val digest = MessageDigest.getInstance("SHA-256")
        val byteBuffer = new Array[Byte](1024)
        var bytes = fis.read(byteBuffer)
        while (bytes != -1) {
          digest.update(byteBuffer, 0, bytes)
          bytes = fis.read(byteBuffer)
        }
        digest.digest().map("%02x".format(_)).mkString
      } catch {
        case e: Throwable => throw e
      } finally {
        fis.close()
      }

    }

    override def hashSet(files: Seq[File]): String = {
      val digest = MessageDigest.getInstance("SHA-256")
      files.foreach { file =>
        val fileHash = calculate(file)
        digest.update(fileHash.getBytes("UTF-8"))

      }
      digest.digest().map("%02x".format(_)).mkString
    }

    override def findProtoFiles(
        directory: String,
        excludeDirs: List[Path] = Nil
    ): Seq[File] = {
      val dir = new File(directory)
      if (dir.exists() && dir.isDirectory) {
        val allFiles = listFilesRecursively(dir, excludeDirs)
        val protoFiles = allFiles.filter(file =>
          file.isFile && file.getName.endsWith(".proto")
        )
        protoFiles.sortBy(_.getAbsolutePath())
      } else {
        Seq.empty[File]
      }
    }

    private def listFilesRecursively(
        dir: File,
        excludeDirs: List[Path]
    ): Seq[File] = {
      val these = dir.listFiles()
      val includedFiles = these.filter(_.isFile)

      val subDirs = these.filter(_.isDirectory)
      val filteredSubDirs = subDirs.filterNot(dir =>
        excludeDirs.exists(excludePath =>
          dir.toPath.normalize.toAbsolutePath.startsWith(excludePath.normalize().toAbsolutePath())  
        )
      )
      includedFiles ++ filteredSubDirs.flatMap(
        listFilesRecursively(_, excludeDirs)
      )
    }
  }

}
