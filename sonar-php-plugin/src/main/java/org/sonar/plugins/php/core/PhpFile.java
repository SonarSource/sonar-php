/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.core;

import static org.sonar.plugins.php.core.Php.PHP;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.resources.DefaultProjectFileSystem;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.WildcardPattern;

/**
 * This class defines a PhpFile. Its "fromIoFile" infer the package name and class name depending on the complete IO File name.
 */
public class PhpFile extends Resource<PhpPackage> {

  private static final Logger LOG = LoggerFactory.getLogger(PhpFile.class);

  /** The Constant SEPARATOR. */
  private static final String SEPARATOR = "/";

  private static Map<Project, PhpFile> instances = new HashMap<Project, PhpFile>();

  private boolean unitTest;
  private String filename;
  private String packageKey;
  private String longName;
  private PhpPackage parent = null;

  protected Project project;

  public PhpFile(Project project) {
    this.project = project;
  }

  /**
   * The default Constructor.
   * 
   * @param key
   *          String representing the resource key.
   * @throws IllegalArgumentException
   *           If the given key is null or empty.
   */
  public PhpFile(String key) {
    this(key, false);
  }

  public static PhpFile getInstance(Project project) {
    PhpFile instance = instances.get(project);
    if (instance == null) {
      instance = new PhpFile(project);
      instances.put(project, instance);
    }
    return instance;
  }

  /**
   * From absolute path.
   * 
   * @param path
   *          the path
   * @param sourceDirs
   *          the source dirs
   * @param unitTest
   *          the unit test
   * @return the php file
   */
  public PhpFile fromAbsolutePath(String path, List<File> sourceDirs, boolean unitTest) {
    return path == null ? null : fromIOFile(new File(path), sourceDirs, unitTest);
  }

  /**
   * From absolute path.
   * 
   * @param path
   *          the path
   * @param project
   *          the current project
   * @return the php file
   */
  public PhpFile fromAbsolutePath(String path, Project project) {
    PhpFile phpFile = null;
    if (path != null) {
      File file = new File(path);
      ProjectFileSystem fileSystem = project.getFileSystem();
      List<File> sourceFiles = fileSystem.getSourceFiles(PHP);
      if (sourceFiles.contains(file)) {
        phpFile = fromIOFile(file, fileSystem.getSourceDirs(), false);
        if (phpFile == null) {
          phpFile = fromIOFile(file, fileSystem.getTestDirs(), true);
        }
      }
    }
    return phpFile;
  }

  /**
   * Returns a PhpFile if the given file is a php file and can be found in the given directories. This instance will be initialized with
   * inferred attribute values
   * 
   * @param file
   *          the file to load
   * @param isUnitTest
   *          if <code>true</code> the given resource will be marked as a unit test, otherwise it will be marked as a class
   * @param dirs
   *          the dirs
   * @return the php file
   */
  public PhpFile fromIOFile(File file, List<File> dirs, boolean isUnitTest) {
    // If the file has a valid suffix
    if (file == null || !Php.hasValidSuffixes(file.getName())) {
      return null;
    }

    String relativePath = DefaultProjectFileSystem.getRelativePath(file, dirs);
    // and can be found in the given directories
    if (relativePath != null) {
      String packageName = null;
      String className = relativePath;

      if (relativePath.indexOf('/') >= 0) {
        packageName = StringUtils.substringBeforeLast(relativePath, "/");
        packageName = StringUtils.replace(packageName, "/", ".");
        className = StringUtils.substringAfterLast(relativePath, "/");
      }
      String extension = "." + StringUtils.substringAfterLast(className, ".");
      className = StringUtils.substringBeforeLast(className, ".");
      return new PhpFile(packageName, className, extension, isUnitTest);
    }
    return null;
  }

  /**
   * The Constructor.
   * 
   * @param key
   *          the key
   * @param unitTest
   *          the unit test
   */
  public PhpFile(String key, boolean unitTest) {
    LOG.debug("key=[" + key + "], unitTest=[" + unitTest + "]");
    if (key == null) {
      throw new IllegalArgumentException("Php filename can not be null");
    }
    if (key.indexOf('$') > 0) {
      throw new IllegalArgumentException("Php inner classes are not supported yet : " + key);
    }
    this.unitTest = unitTest;
    String extension = FilenameUtils.getExtension(StringUtils.trim(key));
    if (extension != null) {
      extension = "." + extension;
    }
    String realKey = FilenameUtils.removeExtension(StringUtils.trim(key)).replaceAll(SEPARATOR, ".");
    if (realKey.contains(".")) {
      this.filename = StringUtils.substringAfterLast(realKey, ".");
      this.packageKey = StringUtils.substringBeforeLast(realKey, ".");
      this.longName = realKey;
    } else {
      this.filename = realKey;
      this.longName = realKey;
      this.packageKey = PhpPackage.DEFAULT_PACKAGE_NAME;
      realKey = new StringBuilder().append(realKey).toString();
    }
    setKey(realKey + extension);
  }

  /**
   * Calls the default constructor supposing the class isn't a Unit Test.
   * 
   * @param packageName
   *          the package name
   * @param fileName
   *          the class name
   */
  public PhpFile(String packageName, String className) {
    this(packageName, className, "", false);
  }

  /**
   * The default constructor. aPackageName
   * 
   * @param fileName
   *          String representing the class name
   * @param isUnitTest
   *          String representing the unit test
   * @param aPackageName
   *          the a package name
   */
  public PhpFile(String packageKey, String className, String extension, boolean isUnitTest) {
    LOG.debug("aPackageName=[" + packageKey + "], fileName=[" + className + "], unitTest=[" + isUnitTest + "]");
    if (className == null) {
      throw new IllegalArgumentException("Php filename can not be null");
    }
    if (className.indexOf('$') > 0) {
      throw new IllegalArgumentException("Php inner classes are not supported yet : " + className);
    }
    this.filename = StringUtils.trim(className);
    String key;
    if (StringUtils.isBlank(packageKey)) {
      this.packageKey = PhpPackage.DEFAULT_PACKAGE_NAME;
      this.longName = this.filename;
      key = new StringBuilder().append(this.filename).append(extension).toString();
    } else {
      this.packageKey = packageKey.trim();
      this.longName = new StringBuilder().append(this.packageKey).append(".").append(this.filename).toString();
      key = new StringBuilder().append(this.packageKey).append(".").append(this.filename).append(extension).toString();
    }
    setKey(key);
    this.unitTest = isUnitTest;
  }

  /**
   * @return SCOPE_ENTITY
   */
  @Override
  public String getScope() {
    return Resource.SCOPE_ENTITY;
  }

  /**
   * @return QUALIFIER_UNIT_TEST_CLASS or QUALIFIER_CLASS depending whether it is a unit test class
   */
  @Override
  public String getQualifier() {
    return unitTest ? Resource.QUALIFIER_UNIT_TEST_CLASS : Resource.QUALIFIER_CLASS;
  }

  /**
   * @return null
   */
  @Override
  public String getDescription() {
    return null;
  }

  /**
   * @return Java
   */
  @Override
  public Language getLanguage() {
    return PHP;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return filename;
  }

  /**
   * Returns a concatenation of the package name and the class name.
   * 
   * @returnString representing the complete class name.
   * @see org.sonar.api.resources.Resource#getLongName()
   */
  @Override
  public String getLongName() {
    return longName;
  }

  /**
   * Gets the parent.
   * 
   * @return the parent
   * @see org.sonar.api.resources.Resource#getParent()
   */
  @Override
  public PhpPackage getParent() {
    if (parent == null) {
      parent = new PhpPackage(packageKey);
    }
    return parent;
  }

  /**
   * Match file pattern.
   * 
   * @param antPattern
   *          the ant pattern
   * @return true, if match file pattern
   * @see org.sonar.api.resources.Resource#matchFilePattern(java.lang.String)
   */
  @Override
  public boolean matchFilePattern(String antPattern) {
    String patternWithoutFileSuffix = StringUtils.substringBeforeLast(antPattern, ".");
    WildcardPattern matcher = WildcardPattern.create(patternWithoutFileSuffix, ".");
    return matcher.match(getKey());
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    builder.append("filename", filename);
    builder.append("longName", longName);
    builder.append("packageKey", packageKey);
    builder.append("parent", parent);
    builder.append("unitTest", unitTest);
    return builder.toString();
  }

}
