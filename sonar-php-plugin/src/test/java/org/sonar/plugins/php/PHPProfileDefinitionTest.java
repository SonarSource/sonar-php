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

import org.junit.Test;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.api.utils.Version;
import org.sonar.php.checks.CheckList;
import org.sonar.plugins.php.api.Php;

import static org.assertj.core.api.Assertions.assertThat;

public class PHPProfileDefinitionTest {

  @Test
  public void should_create_sonar_way_profile() {
    ValidationMessages validation = ValidationMessages.create();

    SonarRuntime sonarRuntime = SonarRuntimeImpl.forSonarQube(Version.create(7, 3), SonarQubeSide.SERVER);
    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
    PHPProfileDefinition definition = new PHPProfileDefinition(sonarRuntime);
    definition.define(context);
    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("php", "Sonar way");
    assertThat(profile.language()).isEqualTo(Php.KEY);
    assertThat(profile.name()).isEqualTo(PHPProfileDefinition.SONAR_WAY_PROFILE);
    assertThat(profile.rules().size()).isGreaterThan(50);
    assertThat(profile.rules().size()).isLessThan(CheckList.getAllChecks().size());
    assertThat(profile.rules()).extracting("ruleKey").contains("DuplicatedBlocks");
    assertThat(validation.hasErrors()).isFalse();
  }

}
