/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.php.core;

import org.sonar.plugins.php.api.Php;
import org.sonar.squidbridge.commonrules.api.CommonRulesEngine;
import org.sonar.squidbridge.commonrules.api.CommonRulesRepository;

public class PhpCommonRulesEngine extends CommonRulesEngine {

  public PhpCommonRulesEngine() {
    super(Php.KEY);
  }

  @Override
  protected void doEnableRules(CommonRulesRepository repository) {
    repository
      .enableInsufficientLineCoverageRule(null)
      .enableInsufficientBranchCoverageRule(null)
      .enableInsufficientCommentDensityRule(null)
      .enableDuplicatedBlocksRule()
      .enableFailedUnitTestsRule()
      .enableSkippedUnitTestsRule();
  }

}
