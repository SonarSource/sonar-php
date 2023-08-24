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
package org.sonar.plugins.php.reports.psalm;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.rule.RulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class PsalmRuleDefinitionTest {

  @Test
  void psalmExternalRepository() {
    RulesDefinition.Context context = new RulesDefinition.Context();
    PsalmRulesDefinition rulesDefinition = new PsalmRulesDefinition();
    rulesDefinition.define(context);

    assertThat(context.repositories()).hasSize(1);
    RulesDefinition.Repository repository = context.repository("external_psalm");
    assertThat(repository).isNotNull();
    assertThat(repository.name()).isEqualTo("Psalm");
    assertThat(repository.language()).isEqualTo("php");
    assertThat(repository.isExternal()).isTrue();
    assertThat(repository.rules()).hasSize(266);

    RulesDefinition.Rule rule = repository.rule("AbstractMethodCall");
    assertThat(rule).isNotNull();
    assertThat(rule.name()).isEqualTo("AbstractMethodCall");
    assertThat(rule.tags()).isEmpty();
  }

}
