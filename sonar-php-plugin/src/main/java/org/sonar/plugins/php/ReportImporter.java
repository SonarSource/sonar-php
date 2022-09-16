/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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

import java.nio.file.Path;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.PathUtils;
import org.sonar.api.utils.WildcardPattern;

public abstract class ReportImporter {

  private ExclusionPattern[] exclusionPatterns = {};

  public abstract void execute(SensorContext context);

  protected void prepareExclusions(SensorContext context) {
    exclusionPatterns = ExclusionPattern.create(context.config().getStringArray("sonar.exclusion"));
  }

  protected boolean isExcluded(String filePath) {
    if (exclusionPatterns.length == 0) {
      return false;
    }

    Path path = Path.of(filePath);
    for (ExclusionPattern exclusionPattern : exclusionPatterns) {
      if (exclusionPattern.match(path)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Inspired by org.sonar.api.batch.fs.internal.PathPattern
   */
  private static class ExclusionPattern {

    final WildcardPattern pattern;

    private ExclusionPattern(String pattern) {
      this.pattern = WildcardPattern.create(pattern);
    }

    static ExclusionPattern[] create(String[] s) {
      ExclusionPattern[] result = new ExclusionPattern[s.length];
      for (int i = 0; i < s.length; i++) {
        result[i] = new ExclusionPattern(s[i]);
      }
      return result;
    }

    boolean match(Path relativePath) {
      String path = PathUtils.sanitize(relativePath.toString());
      return path != null && pattern.match(path);
    }
  }
}
