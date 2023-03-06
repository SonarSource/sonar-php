/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.plugins.php.reports;

import javax.annotation.Nullable;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.JSONParser;

public abstract class JsonReportReader {

  public static class Issue {
    @Nullable
    public String filePath;
    @Nullable
    public String message;
    @Nullable
    public String ruleId;
    @Nullable
    public Integer startLine;
    @Nullable
    public Integer startColumn;
    @Nullable
    public Integer endLine;
    @Nullable
    public Integer endColumn;
    @Nullable
    public String type;
    @Nullable
    public String severity;
  }

  protected final JSONParser jsonParser = new JSONParser();

  protected static Integer toInteger(Object value) {
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    return null;
  }
}
