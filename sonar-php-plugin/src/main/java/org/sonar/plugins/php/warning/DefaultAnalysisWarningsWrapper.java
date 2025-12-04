/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
