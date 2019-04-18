/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.php;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFileFilter;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.php.api.Php;

public class PhpExclusionsFileFilter implements InputFileFilter {

  private final String[] excludedPatterns;

  private static final Logger LOG = Loggers.get(PhpExclusionsFileFilter.class);

  public PhpExclusionsFileFilter(Configuration configuration) {
    excludedPatterns = configuration.getStringArray(PhpPlugin.PHP_EXCLUSIONS_KEY);
  }

  @Override
  public boolean accept(InputFile inputFile) {
    if (!Php.KEY.equals(inputFile.language())) {
      return true;
    }

    String relativePath = inputFile.uri().toString();
    if (WildcardPattern.match(WildcardPattern.create(excludedPatterns), relativePath)) {
      LOG.debug("File [" + inputFile.uri() + "] is excluded by '" + PhpPlugin.PHP_EXCLUSIONS_KEY + "' property and will not be analyzed");
      return false;
    }

    return true;
  }
}
