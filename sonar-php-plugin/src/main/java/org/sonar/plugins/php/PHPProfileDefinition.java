/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.php.checks.CheckList;
import org.sonar.plugins.php.api.Php;
import org.sonarsource.analyzer.commons.ProfileDefinitionReader;

/**
 * Sonar way profile.
 * <p>
 * We currently also define two other profiles, see {@link DrupalProfileDefinition} and {@link PSR2ProfileDefinition}.
 *
 */
public final class PHPProfileDefinition extends ProfileDefinition {

  public static final String SONAR_WAY_PROFILE = "Sonar way";
  public static final String SONAR_WAY_PATH = "org/sonar/l10n/php/rules/php/Sonar_way_profile.json";

  private final RuleFinder ruleFinder;

  public PHPProfileDefinition(RuleFinder ruleFinder) {
    this.ruleFinder = ruleFinder;
  }

  @Override
  public RulesProfile createProfile(ValidationMessages validation) {
    RulesProfile profile = RulesProfile.create(SONAR_WAY_PROFILE, Php.KEY);

    activateCommonRules(profile);

    ProfileDefinitionReader definitionReader = new ProfileDefinitionReader(ruleFinder);
    definitionReader.activateRules(profile, CheckList.REPOSITORY_KEY, SONAR_WAY_PATH);

    return profile;
  }

  private void activateCommonRules(RulesProfile profile) {
    Rule duplicatedBlocksRule = ruleFinder.findByKey("common-" + Php.KEY, "DuplicatedBlocks");

    // in SonarLint duplicatedBlocksRule == null
    if (duplicatedBlocksRule != null) {
      profile.activateRule(duplicatedBlocksRule, null);
    }
  }
}
