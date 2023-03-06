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
package org.sonar.plugins.php.warning;

import org.sonar.api.notifications.AnalysisWarnings;

public class DefaultAnalysisWarningsWrapper implements AnalysisWarningsWrapper {

  private final AnalysisWarnings analysisWarnings;

  public DefaultAnalysisWarningsWrapper(AnalysisWarnings analysisWarnings) {
    this.analysisWarnings = analysisWarnings;
  }

  /**
   * Noop instance which can be used as placeholder when {@link AnalysisWarnings} is not supported
   */
  public static final AnalysisWarningsWrapper NOOP_ANALYSIS_WARNINGS = new DefaultAnalysisWarningsWrapper(null) {
    @Override
    public void addWarning(String text) {
      // no operation
    }
  };

  public void addWarning(String text) {
    this.analysisWarnings.addUnique(text);
  }
}
