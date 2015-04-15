/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
package org.sonar.plugins.php;

import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;
import org.sonar.php.checks.CheckList;
import org.sonar.plugins.php.api.Php;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class PHPRulesDefinition implements RulesDefinition {

  private static final String RULES_DESCRIPTIONS_DIRECTORY = "/org/sonar/l10n/php/rules/php/";
  private static final String PARAM_RESOURCE_BUNDLE_BASE_NAME = "org.sonar.l10n.php";
  private static final String SQALE_RESOURCE_BUNDLE_BASE_NAME = "org.sonar.l10n.php-model";

  private static final String REPOSITORY_NAME = "SonarQube";

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(CheckList.REPOSITORY_KEY, Php.KEY).setName(REPOSITORY_NAME);
    List<Class> checks = CheckList.getChecks();
    (new RulesDefinitionAnnotationLoader()).load(repository, checks.toArray(new Class[checks.size()]));

    setDescriptionAndParamTitle(repository);

    repository.done();
  }

  private void setDescriptionAndParamTitle(NewRepository repository) {
    ResourceBundle paramResourceBundle = ResourceBundle.getBundle(PARAM_RESOURCE_BUNDLE_BASE_NAME, Locale.ENGLISH);
    ResourceBundle sqaleResourceBundle = ResourceBundle.getBundle(SQALE_RESOURCE_BUNDLE_BASE_NAME, Locale.ENGLISH);

    for (NewRule newRule : repository.rules()) {

      // FIXME: this should be replace with tag when rule API will be available
      newRule.setDebtSubCharacteristic(sqaleResourceBundle.getString(newRule.key() + ".characteristic"));
      String time = sqaleResourceBundle.getString(newRule.key() + ".time");
      DebtRemediationFunction remediationFunction = newRule.debtRemediationFunctions().constantPerIssue(time);
      // FIXME: Use SQALE annotations in rule classes
      if ("S1996".equals(newRule.key())) {
        remediationFunction = newRule.debtRemediationFunctions().linear(time);
      }
      newRule.setDebtRemediationFunction(remediationFunction);

      newRule.setHtmlDescription(getClass().getResource(RULES_DESCRIPTIONS_DIRECTORY + newRule.key() + ".html"));

      for (NewParam newParam : newRule.params()) {
        newParam.setDescription(paramResourceBundle.getString("rule.php." + newRule.key() + ".param." + newParam.key()));
      }
    }
  }


}
