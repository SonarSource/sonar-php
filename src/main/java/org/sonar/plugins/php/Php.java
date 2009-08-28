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

import org.sonar.api.resources.AbstractLanguage;
import org.apache.commons.lang.StringUtils;

public class Php extends AbstractLanguage {

  public static final Php INSTANCE = new Php();

  public static final String KEY = "php";

  public static final String[] SUFFIXES = {"php", "php3", "php4", "php5", "phtml"};

  public Php() {
    super(KEY, "PHP");
  }

  public String[] getFileSuffixes() {
    return SUFFIXES;
  }




  /*public File newFile(String directory, String fileName) {
    return (File) new File(directory, fileName).setLanguage(this);

  }

  public static Resource newFileFromAbsolutePath(String path, List<String> sourceDirs) {
    if (path == null) {
      return null;
    }
    String unixPath = path.trim().replace("\\", SEPARATOR);
    for (String rootAbsolutePath : sourceDirs) {
      String unixRoot = rootAbsolutePath.replace("\\", SEPARATOR);
      if (!unixRoot.endsWith(SEPARATOR)) {
        unixRoot += SEPARATOR;
      }
      if (unixPath.contains(unixRoot)) {
        String relativePath = StringUtils.substringAfter(unixPath, unixRoot); // "foo/bar/Myfile.sql" or "Myfile.sql"
        relativePath = StringUtils.removeStart(relativePath, SEPARATOR);

        String dirName = null;
        String fileName = relativePath;

        if (relativePath.indexOf(SEPARATOR) >= 0) {
          dirName = StringUtils.substringBeforeLast(relativePath, SEPARATOR);
          fileName = StringUtils.substringAfterLast(relativePath, SEPARATOR);
        }
        return new File(dirName, fileName);
      }
    }
    return null;
  }
           */


  protected static boolean containsValidSuffixes(String path) {
    String pathLowerCase = StringUtils.lowerCase(path);
    for (String suffix : SUFFIXES) {
      if (pathLowerCase.endsWith("." + StringUtils.lowerCase(suffix))) {
        return true;
      }
    }
    return false;
  }


}
