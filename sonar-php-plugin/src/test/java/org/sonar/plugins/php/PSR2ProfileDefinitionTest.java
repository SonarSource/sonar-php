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

import org.junit.Test;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.BuiltInActiveRule;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.BuiltInQualityProfile;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.Context;
import org.sonar.plugins.php.api.Php;

import static org.assertj.core.api.Assertions.assertThat;

public class PSR2ProfileDefinitionTest {

  @Test
  public void profile_creation() {
    PSR2ProfileDefinition definition = new PSR2ProfileDefinition();
    Context context = new Context();
    definition.define(context);
    BuiltInQualityProfile profile = context.profile("php", "PSR-2");

    assertThat(profile.language()).isEqualTo(Php.KEY);
    assertThat(profile.name()).isEqualTo("PSR-2");
    assertThat(profile.rules()).hasSize(20);

    BuiltInActiveRule lineLengthRule = profile.rule(RuleKey.of("php", "S103"));
    assertThat(lineLengthRule.overriddenParams()).isEmpty();
    assertThat(lineLengthRule.overriddenSeverity()).isNull();

    BuiltInActiveRule ruleS1788 = profile.rule(RuleKey.of("php", "S1788"));
    assertThat(ruleS1788.overriddenParams()).isEmpty();
    assertThat(ruleS1788.overriddenSeverity()).isEqualTo("CRITICAL");

    BuiltInActiveRule ruleS101 = profile.rule(RuleKey.of("php", "S101"));
    assertThat(ruleS101.overriddenParam("format").overriddenValue()).isEqualTo("^[A-Z][a-zA-Z]*$");
  }

}
