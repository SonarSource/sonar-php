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
package org.sonar.plugins.php;

import com.sonar.plugins.security.api.PhpRules;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.php.checks.CheckList;
import org.sonar.plugins.php.api.Php;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.php.PHPProfileDefinition.REPOSITORY_KEYS_METHOD_NAME;
import static org.sonar.plugins.php.PHPProfileDefinition.RULES_KEYS_METHOD_NAME;
import static org.sonar.plugins.php.PHPProfileDefinition.SECURITY_RULES_CLASS;
import static org.sonar.plugins.php.PHPProfileDefinition.getSecurityRuleKeys;

class PHPProfileDefinitionTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @Test
  void shouldCreateSonarWayProfile() {
    ValidationMessages validation = ValidationMessages.create();

    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
    PHPProfileDefinition definition = new PHPProfileDefinition();
    definition.define(context);
    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("php", "Sonar way");
    assertThat(profile.language()).isEqualTo(Php.KEY);
    assertThat(profile.name()).isEqualTo(PHPProfileDefinition.SONAR_WAY_PROFILE);
    assertThat(profile.rules().size()).isGreaterThan(50);
    assertThat(profile.rules().size()).isLessThan(CheckList.getAllChecks().size());
    assertThat(validation.hasErrors()).isFalse();
  }

  @Test
  void shouldContainsSecurityRulesIfPresent() {
    // no security rule available
    PhpRules.getRuleKeys().clear();
    assertThat(getSecurityRuleKeys(SECURITY_RULES_CLASS, RULES_KEYS_METHOD_NAME, REPOSITORY_KEYS_METHOD_NAME)).isEmpty();

    // one security rule available
    PhpRules.getRuleKeys().add("S3649");
    assertThat(getSecurityRuleKeys(SECURITY_RULES_CLASS, RULES_KEYS_METHOD_NAME, REPOSITORY_KEYS_METHOD_NAME))
      .containsOnly(RuleKey.of("phpsecurity", "S3649"));
  }

  @Test
  void shouldLogExceptionOnInvalidClassName() {
    getSecurityRuleKeys("invalidClassName", RULES_KEYS_METHOD_NAME, REPOSITORY_KEYS_METHOD_NAME);

    assertThat(logTester.logs()).hasSize(1);
    assertThat(logTester.logs().get(0)).contains("com.sonar.plugins.security.api.PhpRules is not found");
  }

  @Test
  void shouldLogExceptionOnInvalidMethodName() {
    getSecurityRuleKeys(SECURITY_RULES_CLASS, "invalidMethodName", REPOSITORY_KEYS_METHOD_NAME);

    assertThat(logTester.logs()).hasSize(1);
    assertThat(logTester.logs().get(0)).contains("Method not found on com.sonar.plugins.security.api.PhpRules");
  }

}
