/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.php.reports.psalm;

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarRuntime;
import org.sonar.plugins.php.reports.ExternalRulesDefinition;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

import static org.sonar.plugins.php.reports.psalm.PsalmSensor.PSALM_REPORT_KEY;
import static org.sonar.plugins.php.reports.psalm.PsalmSensor.PSALM_REPORT_NAME;

public class PsalmRulesDefinition extends ExternalRulesDefinition {
  private static final Logger LOG = LoggerFactory.getLogger(PsalmRulesDefinition.class);

  public PsalmRulesDefinition(@Nullable SonarRuntime sonarRuntime) {
    super(sonarRuntime, PSALM_REPORT_KEY, PSALM_REPORT_NAME);
  }

  static ExternalRuleLoader ruleLoader() {
    if (ruleLoader == null) {
      LOG.debug("Psalm importing not initialized at startup, initializing it now.");
      ruleLoader = initializeRuleLoader(null, PSALM_REPORT_KEY, PSALM_REPORT_NAME);
    }
    return ruleLoader;
  }
}
