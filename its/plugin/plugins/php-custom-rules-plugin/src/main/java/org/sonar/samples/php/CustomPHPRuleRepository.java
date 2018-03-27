/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2018 SonarSource SA
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
package org.sonar.samples.php;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;
import org.sonar.plugins.php.api.visitors.PHPCustomRuleRepository;

public class CustomPHPRuleRepository implements RulesDefinition, PHPCustomRuleRepository {

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(repositoryKey(), "php").setName("My custom repo");
    new RulesDefinitionAnnotationLoader().load(repository, checkClasses().toArray(new Class[] {}));
    Map<String, String> remediationCosts = new HashMap<>();
    remediationCosts.put("visitor", "5min");
    remediationCosts.put("subscription", "10min");
    repository.rules().forEach(rule -> rule.setDebtRemediationFunction(
      rule.debtRemediationFunctions().constantPerIssue(remediationCosts.get(rule.key()))));
    repository.done();
  }

  @Override
  public String repositoryKey() {
    return "php-custom-rules";
  }

  @Override
  public List<Class> checkClasses() {
    return Arrays.asList(CustomPHPVisitorCheck.class, CustomPHPSubscriptionCheck.class);
  }
}
