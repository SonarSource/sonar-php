/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
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
package org.sonar.plugins.php.api;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.AbstractLanguage;

import java.util.List;

/**
 * This class defines the PHP language.
 */
@Properties({
  @Property(
    key = PhpConstants.FILE_SUFFIXES_KEY,
    defaultValue = PhpConstants.FILE_SUFFIXES_DEFVALUE,
    name = "File suffixes",
    description = "Comma-separated list of suffixes for files to analyze. To not filter, leave the list empty.",
    project = true,
    global = true)
})
public final class Php extends AbstractLanguage {

  private Settings settings;

  /**
   * Construct the PHP language.
   */
  public Php(Settings settings) {
    super(PhpConstants.LANGUAGE_KEY, PhpConstants.LANGUAGE_NAME);
    this.settings = settings;
  }

  /**
   * Only for testing purposes.
   */
  public Php() {
    this(new Settings());
  }

  /**
   * {@inheritDoc}
   */
  public String[] getFileSuffixes() {
    String[] suffixes = filterEmptyStrings(settings.getStringArray(PhpConstants.FILE_SUFFIXES_KEY));
    if (suffixes.length == 0) {
      suffixes = StringUtils.split(PhpConstants.FILE_SUFFIXES_DEFVALUE, ",");
    }
    return suffixes;
  }

  private String[] filterEmptyStrings(String[] stringArray) {
    List<String> nonEmptyStrings = Lists.newArrayList();
    for (String string : stringArray) {
      if (StringUtils.isNotBlank(string.trim())) {
        nonEmptyStrings.add(string.trim());
      }
    }
    return nonEmptyStrings.toArray(new String[nonEmptyStrings.size()]);
  }

  /**
   * Allows to know if the given file name has a valid suffix.
   * 
   * @param fileName
   *          String representing the file name
   * @return boolean <code>true</code> if the file name's suffix is known, <code>false</code> any other way
   */
  public boolean hasValidSuffixes(String fileName) {
    String pathLowerCase = StringUtils.lowerCase(fileName);
    for (String suffix : getFileSuffixes()) {
      if (pathLowerCase.endsWith("." + StringUtils.lowerCase(suffix))) {
        return true;
      }
    }
    return false;
  }

}
