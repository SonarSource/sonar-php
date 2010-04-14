/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 MyCompany
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.core.resources;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.resources.DefaultProjectFileSystem;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.plugins.php.core.Php;

/**
 * This class defines a PhpFile. Its "fromIoFile" infer the package name and class name depending on the complete IO File name.
 */
public class PhpFile extends Resource<PhpPackage> {

	private static final Logger LOG = LoggerFactory.getLogger(PhpFile.class);

	/** The Constant SEPARATOR. */
	private static final String SEPARATOR = "/";
	
	private boolean unitTest;
	private String filename;
	private String packageKey;
	private String longName;
	private PhpPackage parent = null;

	/**
	 * From absolute path.
	 * 
	 * @param path the path
	 * @param sourceDirs the source dirs
	 * @param unitTest the unit test
	 * @return the php file
	 */
	public static PhpFile fromAbsolutePath(String path, List<File> sourceDirs, boolean unitTest) {
		return path == null ? null : fromIOFile(new File(path), sourceDirs, unitTest);
	}

	/**
	 * From absolute path.
	 * 
	 * @param path the path
	 * @param project the current project
	 * @return the php file
	 */
	public static PhpFile fromAbsolutePath(String path, Project project) {
		PhpFile phpFile = path == null ? null : fromIOFile(new File(path), project.getFileSystem().getSourceDirs(), false);
		if (phpFile == null) {
			phpFile = path == null ? null : fromIOFile(new File(path), project.getFileSystem().getTestDirs(), true);
		}
		return phpFile;
	}

	/**
	 * Returns a PhpFile if the given file is a php file and can be found in the given directories. This instance will be initialized
	 * with inferred attribute values
	 * 
	 * @param file the file to load
	 * @param unitTest if <code>true</code> the given resource will be marked as a unit test, otherwise it will be marked has a class
	 * @param dirs the dirs
	 * @return the php file
	 */
	public static PhpFile fromIOFile(File file, List<File> dirs, boolean unitTest) {
		// If the file has a valid suffix
		if (file == null || !Php.hasValidSuffixes(file.getName())) {
			return null;
		}
		String relativePath = DefaultProjectFileSystem.getRelativePath(file, dirs);
		// and can be found in the given directories
		if (relativePath != null) {
			String pacname = null;
			String classname = relativePath;

			if (relativePath.indexOf('/') >= 0) {
				pacname = StringUtils.substringBeforeLast(relativePath, "/");
				pacname = StringUtils.replace(pacname, "/", ".");
				classname = StringUtils.substringAfterLast(relativePath, "/");
			}
			classname = StringUtils.substringBeforeLast(classname, ".");
			return new PhpFile(pacname, classname, unitTest);
		}
		return null;
	}

	/**
	 * The default Constructor.
	 * 
	 * @param key String representing the resource key.
	 * @throws IllegalArgumentException If the given key is null or empty.
	 */
	public PhpFile(String key) {
		this(key, false);
	}

	/**
	 * The Constructor.
	 * 
	 * @param key the key
	 * @param unitTest the unit test
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
		setKey(realKey);
	}

	/**
	 * Calls the default constructor supposing the class isn't a Unit Test.
	 * 
	 * @param packageName the package name
	 * @param className the class name
	 */
	public PhpFile(String packageName, String className) {
		this(packageName, className, false);
	}

	/**
	 * The default constructor. aPackageName
	 * 
	 * @param className String representing the class name
	 * @param unitTest String representing the unit test
	 * @param aPackageName the a package name
	 */
	public PhpFile(String packageKey, String className, boolean unitTest) {
		LOG.debug("aPackageName=[" + packageKey + "], className=[" + className + "], unitTest=[" + unitTest + "]");
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
	      key = new StringBuilder().append(this.filename).toString();
	    } else {
	      this.packageKey = packageKey.trim();
	      key = new StringBuilder().append(this.packageKey).append(".").append(this.filename).toString();
	      this.longName = key;
	    }
	    setKey(key);
	    this.unitTest = unitTest;
	}

	  /**
	   * @return SCOPE_ENTITY
	   */
	  public String getScope() {
	    return Resource.SCOPE_ENTITY;
	  }

	  /**
	   * @return QUALIFIER_UNIT_TEST_CLASS or QUALIFIER_CLASS depending whether it is a unit test class
	   */
	  public String getQualifier() {
	    return unitTest ? Resource.QUALIFIER_UNIT_TEST_CLASS : Resource.QUALIFIER_CLASS;
	  }

	  /**
	   * @return null
	   */
	  public String getDescription() {
	    return null;
	  }

	  /**
	   * @return Java
	   */
	  public Language getLanguage() {
	    return Php.INSTANCE;
	  }

	  /**
	   * {@inheritDoc}
	   */
	  public String getName() {
	    return filename;
	  }
	  
	  /**
	 * Returns a concatenation of the package name and the class name.
	 * 
	 * @returnString representing the complete class name.
	 * @see org.sonar.api.resources.Resource#getLongName()
	 */
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
	 * @param antPattern the ant pattern
	 * @return true, if match file pattern
	 * @see org.sonar.api.resources.Resource#matchFilePattern(java.lang.String)
	 */
	public boolean matchFilePattern(String antPattern) {
		String patternWithoutFileSuffix = StringUtils.substringBeforeLast(antPattern, ".");
		WildcardPattern matcher = WildcardPattern.create(patternWithoutFileSuffix, ".");
		return matcher.match(getKey());
	}

}
