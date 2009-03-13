/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
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

package org.sonar.plugins.php;

import org.apache.commons.lang.StringUtils;
import org.sonar.commons.resources.Resource;
import org.sonar.plugins.api.AbstractLanguage;

import java.util.List;

public class Php extends AbstractLanguage {

  public static final String KEY = "php";
  public static final Php INSTANCE = new Php();
  public static final String[] SUFFIXES = {".php", ".php3", ".php4", ".php5", ".phtml"};

  public static final String DEFAULT_DIRECTORY_NAME = "/";

  public Php() {
    super(KEY, "PHP");
  }

  public Resource getParent(Resource resource) {
    if (resource.isFile()) {
      if (resource.getKey().indexOf("/") >= 0) {
        return newDirectory(StringUtils.substringBeforeLast(resource.getKey(), "/"));
      }
      return newDirectory(null);
    }
    return null;
  }

  public boolean matchExclusionPattern(Resource resource, String wildcardPattern) {
    return false;
  }

  public static Resource newDirectory(String key) {
    String resourceKey = StringUtils.trim(key);
    if (!StringUtils.isBlank(key)) {
      return Resource.newDirectory(resourceKey, Resource.QUALIFIER_DIRECTORY, KEY);
    } else {
      return null;
    }
  }

  public static Resource newFile(String key) {
    String resourceKey = StringUtils.trim(key);
    String name = resourceKey;
    if (name.contains("/")) {
      name = StringUtils.substringAfterLast(name, "/");
    }
    Resource resource = Resource.newFile(resourceKey, Resource.QUALIFIER_FILE, KEY);
    resource.setName(name);
    return resource;
  }

  public static Resource newFile(String dirKey, String fileKey) {
    if (StringUtils.isBlank(dirKey)) {
      return newFile(fileKey);
    }
    return newFile(dirKey + "/" + fileKey);
  }

  public static Resource newFileFromAbsolutePath(String path, List<String> sourceDirs) {
    if (path == null || !(containsValidSuffixes(path))) {
      return null;
    }
    String unixPath = path.trim().replace('\\', '/');
    for (String rootAbsolutePath : sourceDirs) {
      String unixRoot = rootAbsolutePath.replace('\\', '/');
      if (!unixRoot.endsWith("/")) {
        unixRoot += "/";
      }
      if (unixPath.contains(unixRoot)) {
        String relativePath = StringUtils.substringAfter(unixPath, unixRoot); // "foo/bar/Myfile.php" or "Myfile.php"
        relativePath = StringUtils.removeStart(relativePath, "/");

        String dirName = null;
        String fileName = relativePath;

        if (relativePath.indexOf("/") >= 0) {
          dirName = StringUtils.substringBeforeLast(relativePath, "/");
          fileName = StringUtils.substringAfterLast(relativePath, "/");
        }
        return newFile(dirName, fileName);
      }
    }
    return null;
  }

  protected static boolean containsValidSuffixes(String path) {
    String pathLowerCase = StringUtils.lowerCase(path);
    for (String suffix : SUFFIXES) {
      if (pathLowerCase.endsWith(suffix)) {
        return true;
      }
    }
    return false;
  }
}
