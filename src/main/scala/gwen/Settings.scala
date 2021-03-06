/*
 * Copyright 2015-2018 Branko Juric, Brady Wood
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

import scala.collection.JavaConverters._
import java.io.File
import java.util.Properties
import java.io.FileReader

import gwen.errors._

import scala.collection.mutable

/**
  * Provides access to system properties loaded from properties files.
  * If a gwen.properties file exists in the user's home directory, then
  * its properties are loaded first. Once a property is loaded it is never
  * replaced. Therefore it is important to load properties in the right 
  * order. Existing system properties are not overriden by values in 
  * properties files (and therefore have precedence).
  *
  * @author Branko Juric
  */
object Settings {

  // thread local settings
  private final val localSettings = new ThreadLocal[Properties]() {
    override protected def initialValue: Properties = new Properties()
  }

  private final val InlineProperty = """.*\$\{(.+?)\}.*""".r
  
  loadAll(UserOverrides.UserProperties.toList)
  
  /**
    * Loads all properties from the given files.
    * 
    * @param propsFiles the properties files to load
    */
  def loadAll(propsFiles: List[File]): Unit = {
    val props = propsFiles.foldLeft(new Properties()) { 
      (props, file) => 
        props.load(new FileReader(file))
        props.entrySet().asScala foreach { entry =>
          val key = entry.getKey.asInstanceOf[String]
          if (key == null || key.trim.isEmpty) invalidPropertyError(entry.toString, file)
        }
        props
    }
    props.entrySet().asScala.foreach { entry =>
      val key = entry.getKey.asInstanceOf[String]
      if (!names.contains(key)) {
        try {
          val value = resolve(props.getProperty(key), props)
          Settings.set(key, value)
        } catch {
          case e: Throwable => propertyLoadError(key, e)
        }
      }
    }
  }
  
  /**
   * Resolves a given property by performing any property substitutions.
   * 
   * @param value the value to resolve
   * @param props the properties already read (candidates for substitution)
   * @throws MissingPropertyException if a property cannot be substituted 
   *         because it is missing from the given props
   */
  private[gwen] def resolve(value: String, props: Properties): String = value match {
    case InlineProperty(key) =>
      val inline = if (props.containsKey(key)) {
        props.getProperty(key)
      } else {
        getOpt(key).getOrElse(missingPropertyError(key))
      }
      val resolved = inline match {
        case InlineProperty(_) => resolve(inline, props)
        case _ => inline
      }
      resolve(value.replaceAll("""\$\{""" + key + """\}""", resolved), props)
    case _ => value
  }
  
  /**
    * Gets an optional property (returns None if not found)
    * 
    * @param name the name of the property to get
    */
  def getOpt(name: String): Option[String] = Option(localSettings.get.getProperty(name)).orElse(sys.props.get(name))
  
  /**
    * Gets a mandatory property (throws exception if not found)
    * 
    * @param name the name of the property to get
    * @throws gwen.errors.MissingPropertyException if no such property is set
    */
  def get(name: String): String = getOpt(name).getOrElse(missingPropertyError(name))

  /**
    * Finds all properties that match the given predicate.
    *
    * @param predicate name => Boolean
    * @return map of properties that match the predicate
    */
  def findAll(predicate: String => Boolean): Map[String, String] = entries.filter {
    case (name, _) => predicate(name)
  }

  /**
   * Provides access to multiple settings. This method merges a comma separated list of name-value pairs
   * set in the given multiName property with all name-value properties that start with singleName.
   * See: https://github.com/SeleniumHQ/selenium/wiki/DesiredCapabilities
   */
  def findAllMulti(multiName: String, singleName: String): Map[String, String] = {
    val assignments: Seq[String] = Settings.getOpt(multiName).map(_.split(",")).map(_.toList).getOrElse(Nil)
    val nvps: Seq[(String, String)] = assignments.map(_.split('=')).map { nvp =>
      if (nvp.length == 2) (nvp(0), nvp(1))
      else if (nvp.length == 1) (nvp(0), "")
      else propertyLoadError(nvp(0), "name-value pair expected")
    }
    (nvps ++ Settings.findAll(_.startsWith(s"$singleName.")).map { case (n, v) =>
      (n.substring(singleName.length + 1), v)
    }).toMap
  }

  /**
   * Provides access to multiple settings. This method merges a comma separated list of name-value pairs
   * set in the given multiName property with all name-value properties that start with singleName.
   * See: https://github.com/SeleniumHQ/selenium/wiki/DesiredCapabilities
   */
  def findAllMulti(name: String): List[String] = {
    val values = Settings.getOpt(name).map(_.split(",").toList.map(_.trim)).getOrElse(Nil)
    values ++ Settings.findAll(_.startsWith(s"$name.")).map { case (_, v) => v }
  }

  /** Gets all settings names (keys). */
  def names: mutable.Set[String] = localSettings.get.keySet.asScala.map(_.toString) ++ sys.props.keySet

  /** Gets number of names (keys). */
  def size: Int = names.size

  /** Gets all settings entries (name-value pairs). */
  def entries: Map[String, String] =
    localSettings.get.entrySet().asScala.map(entry => (entry.getKey.toString, entry.getValue.toString)).toMap ++
      sys.props.toMap.filter{case (k, _) => !localSettings.get.containsKey(k)}

  /**
    * Checks to see if a setting exists.
    * @param name the name of the setting to check
    * @return true if the setting exists, false otherwise
    */
  def contains(name: String): Boolean = getOpt(name).nonEmpty

  /**
    * Adds a system property (overrides any existing value)
    *
    * @param name the name of the property to add
    * @param value the value to bind to the property
    */
  def set(name: String, value: String): Unit = {
    sys.props += ((name, value))
  }

  /**
    * Clears local and global settings of the given name.
    *
    * @param name the name of the setting to clear
    */
  def clear(name: String): Unit = {
    localSettings.get.remove(name)
    sys.props -= name
  }
   /**
    * Adds a system property.
    * 
    * @param name the name of the property to add
    * @param value the value to bind to the property
    */
  private[gwen] def add(name: String, value: String, overrideIfExists: Boolean): Unit = {
    if (overrideIfExists || !contains(name)) {
      set(name, value)
    }
  }

  /**
    * Adds a thread local Gwen setting (overrides any existing value)
    *
    * @param name the name of the setting to set
    * @param value the value to bind to the setting
    */
  def setLocal(name: String, value: String): Unit = {
    if (!name.startsWith("gwen.")) unsupportedLocalSetting(name)
    localSettings.get.setProperty(name, value)
  }

  /**
    * Clears a local Gwen settings entry.
    * @param name the setting entry to remove
    * @return the removed entry value
    */
  def clearLocal(name: String): Unit = {
    if (!name.startsWith("gwen.")) unsupportedLocalSetting(name)
    localSettings.get.remove(name)
  }
  
}