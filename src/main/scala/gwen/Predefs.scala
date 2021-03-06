/*
 * Copyright 2014-2017 Branko Juric, Brady Wood
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gwen

import java.io.FileWriter
import java.io.File
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.StringWriter
import java.io.PrintWriter
import java.nio.file.{Files, Paths}

import scala.util.matching.Regex
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.Duration
import java.text.DecimalFormat

import gwen.dsl.Position
import org.apache.commons.text.StringEscapeUtils

import scala.io.Source

/**
  * Predefined implicits.
  * 
  * @author Branko Juric
  */
object Predefs extends LazyLogging {
  
  /** Kestrel function for tapping in side effects. */
  implicit class Kestrel[A](val value: A) extends AnyVal { 
    def tap[B](f: A => B): A = { f(value); value }
  }
  
  /** Implicit File IO functions. */
  implicit class FileIO[F <: File](val file: F) extends AnyVal {
    
    def writeText(text: String): File = 
      file tap { f =>
        if (f.getParentFile != null && !f.getParentFile.exists()) {
          f.getParentFile.mkdirs()
        }
        new FileWriter(f) tap { fw =>
          try {
            fw.write(text)
          } finally {
            fw.close()
          }
        }
      }
    
    def writeBinary(bis: BufferedInputStream): File = 
      file tap { f =>
        if (f.getParentFile != null && !f.getParentFile.exists()) {
          f.getParentFile.mkdirs()
        }
        new BufferedOutputStream(new FileOutputStream(f)) tap { bos =>
          try {
            var c = 0
            while ({c = bis.read(); c != -1}) {
              bos.write(c)
            }
          } finally {
            try {
              bis.close()
            } finally {
              bos.close()
            }
          }
        }
      }
    
    def extension: String = file.getName.lastIndexOf(".") match {
      case -1 => ""
      case idx => file.getName.substring(idx + 1)
    }
    
    def writeFile(source: File) {
      file.writeBinary(new BufferedInputStream(new FileInputStream(source)))
    }

    def readBytes: Array[Byte] = Files.readAllBytes(Paths.get(file.getAbsolutePath))
    
    def deleteDir() {
      val files = file.listFiles() 
      if (files != null) { 
        files foreach { _.deleteFile() }
      }
      file.delete()
    }
    
    def deleteFile() {
      if (file.isDirectory) {
        file.deleteDir()
      } else {
        file.delete()
      }
    }
    
    def toDir(targetDir: File, targetSubDir: Option[String]): File =
      new File(toPath(targetDir, targetSubDir))
    
    def toPath(targetDir: File, targetSubDir: Option[String]): String =
      targetDir.getPath + File.separator + FileIO.encodeDir(file.getParent) + targetSubDir.map(File.separator + _).getOrElse("")
    
    def toFile(targetDir: File, targetSubDir: Option[String]): File =
      new File(toDir(targetDir, targetSubDir), file.getName)

    def mimeType: String = file.extension match {
      case "png" => "image/png"
      case "json" => "application/json"
      case _ => "text/plain"
    }
    
  }
  
  object FileIO {
    def encodeDir(dirpath: String): String = 
      if (dirpath != null) dirpath.replaceAll("""[/\:\\]""", "-") else ""
    def isDirectory(location: File): Boolean = location != null && location.isDirectory
    def hasParentDirectory(location: File): Boolean = location != null && isDirectory(location.getParentFile)
    def isFeatureFile(file: File): Boolean = hasFileExtension("feature", file)
    def isMetaFile(file: File): Boolean = hasFileExtension("meta", file)
    def isCsvFile(file: File): Boolean = hasFileExtension("csv", file)
    def hasFileExtension(extension: String, file: File): Boolean = file.isFile && file.getName.endsWith(s".$extension")
    def recursiveScan(dir: File, extension: String): List[File] = {
      val files = dir.listFiles
      val metas = files.filter(file => hasFileExtension(extension, file)).toList
      files.filter(isDirectory).toList match {
        case Nil => metas
        case dirs => metas ::: dirs.flatMap(dir => recursiveScan(dir, extension))
      }
    }
  }
  
  /** Exception functions. */
  implicit class Exceptions[T <: Throwable](val error: T) extends AnyVal {
    
    def writeStackTrace(): String = {
      val sw = new StringWriter()
      val pw = new PrintWriter(sw)
      error.printStackTrace(pw)
      pw.flush()
      pw.close()
      sw.toString
    }
    
  }
  
  /**
    * Implicit regex string interpolator.  This makes it easy to match 
    * incoming steps against regular expressions and capture their parameters.
    */
  implicit class RegexContext(val sc: StringContext) extends AnyVal {
    def r: Regex = {
      logger.debug(s"Matched: ${sc.parts.mkString}") 
      new Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*) 
    }
  }
  
  object Formatting {
    
    /**
      * Formats durations for presentation purposes.
      */
    object DurationFormatter {
      
      import scala.concurrent.duration._
  
      private val Formatters = List(
        HOURS -> ("h", new DecimalFormat("00")),
        MINUTES -> ("m", new DecimalFormat("00")),
        SECONDS -> ("s", new DecimalFormat("00")),
        MILLISECONDS -> ("ms", new DecimalFormat("000"))
      )

      /**
        * Formats a given duration to ##h ##m ##s ###ms format.
        * 
        * @param duration the duration to format
        */
      def format(duration: Duration): String = {
        val nanos = duration.toNanos
        val msecs = (nanos / 1000000) + (if ((nanos % 1000000) < 500000) 0 else 1)
        if (msecs > 0) {
          var duration = Duration(msecs, MILLISECONDS)
          Formatters.foldLeft("") { (acc: String, f: (TimeUnit, (String, DecimalFormat))) =>
            val (unit, (unitName, formatter)) = f
            val unitValue = duration.toUnit(unit).toLong
            if (acc.length() == 0 && unitValue == 0) "" 
            else {
              duration = duration - Duration(unitValue, unit)
              s"$acc ${formatter.format(unitValue)}$unitName"
            }
          }.trim.replaceFirst("^0+(?!$)", "")
        } else "~0ms"
      }
  
    }

    def padWithZeroes(num: Int): String = "%04d".format(num)
    def formatDuration(duration: Duration): String = DurationFormatter.format(duration)
    def escapeHtml(text: String): String = StringEscapeUtils.escapeHtml4(text)
    def escapeXml(text: String): String = StringEscapeUtils.escapeXml10(text)
    def escapeJson(text: String): String = StringEscapeUtils.escapeJson(text)
    def rightPad(str: String, size: Int): String = if (str.length < size) rightPad(str + " ", size) else str
    def padTailLines(str: String, padding: String) = str.replaceAll("""\r?\n""", s"""\n$padding""")

    def resolveParams(source: String, params: List[(String, String)]): String = {
      params match {
        case Nil => source
        case head :: tail =>
          val (name, value) = head
          resolveParams(source.replaceAll(s"<$name>", value), tail)
      }
    }
    def formatTableRow(table: List[(Int, List[String])], rowIndex: Int): String = {
      val maxWidths = (table map { case (_, rows) => rows.map(_.length) }).transpose.map(_.max)
      s"| ${(table(rowIndex)._2.zipWithIndex map { case (data, dataIndex) => s"${rightPad(data, maxWidths(dataIndex))}" }).mkString(" | ") } |"
    }
    def formatDocString(docString: (Int, String, Option[String]), includeType: Boolean = false) = docString match {
      case (_, content, contentType) =>
        s"""|${"\"\"\""}${if(includeType) contentType.getOrElse("") else ""}
            |$content
            |${"\"\"\""}""".stripMargin
    }
  }
  
  object DurationOps {
    import scala.concurrent.duration._
    def sum(durations: Seq[Duration]): Duration =
      if (durations.isEmpty) Duration.Zero
      else if (durations.size == 1) durations.head
      else durations.reduce(_+_)
  }

  object StringOps {
    def lastPositionIn(source: String): Position = {
      Source.fromString(s"$source ").getLines().toList match {
        case Nil =>
          Position(1, 1)
        case lines =>
          val lastLength = lines.last.length - 1
          Position(if (lines.size > 0) lines.size else 1, if (lastLength > 0) lastLength else 1)
      }
    }
  }
  
}