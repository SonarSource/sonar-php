/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.api.utils.Version;
import org.sonar.php.checks.CheckList;

import static org.assertj.core.api.Assertions.assertThat;

public class PHPRulesDefinitionTest {

  @Test
  public void test() {
    PHPRulesDefinition rulesDefinition = new PHPRulesDefinition(Version.create(5, 6));
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    RulesDefinition.Repository repository = context.repository("php");

    assertThat(repository.name()).isEqualTo("SonarAnalyzer");
    assertThat(repository.language()).isEqualTo("php");
    assertThat(repository.rules()).hasSize(CheckList.getAllChecks().size());

    List<Rule> activated = repository.rules().stream().filter(Rule::activatedByDefault).collect(Collectors.toList());
    assertThat(activated).isEmpty();
  }

  @Test
  public void testActivationSonarLint() {
    PHPRulesDefinition rulesDefinition = new PHPRulesDefinition(Version.create(6, 0));
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    RulesDefinition.Repository repository = context.repository("php");

    List<Rule> activated = repository.rules().stream().filter(Rule::activatedByDefault).collect(Collectors.toList());
    assertThat(activated).isNotEmpty();
    assertThat(activated.size()).isLessThan(CheckList.getAllChecks().size());
  }

}
