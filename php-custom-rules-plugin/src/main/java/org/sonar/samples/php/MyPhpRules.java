/*
 * SonarQube PHP Plugin
 * Copyright (C) 2014-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.samples.php;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;
import org.sonar.plugins.php.api.visitors.PHPCustomRuleRepository;
import org.sonar.samples.php.checks.ForbiddenFunctionUseCheck;
import org.sonar.samples.php.checks.OtherForbiddenFunctionUseCheck;

/**
 * Extension point to define a PHP rule repository.
 */
public class MyPhpRules implements RulesDefinition, PHPCustomRuleRepository {

  /**
   * Provide the repository key
   */
  @Override
  public String repositoryKey() {
    return "custom";
  }

  /**
   * Provide the list of checks class that implements rules
   * to be part of the rule repository
   */
  @Override
  public List<Class<?>> checkClasses() {
    return List.of(ForbiddenFunctionUseCheck.class, OtherForbiddenFunctionUseCheck.class);
  }

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(repositoryKey(), "php")
      .setName("MyCompany Custom Repository");

    // Load rule meta data from annotations
    RulesDefinitionAnnotationLoader annotationLoader = new RulesDefinitionAnnotationLoader();
    checkClasses().forEach(ruleClass -> annotationLoader.load(repository, ruleClass));

    // Optionally override html description from annotation with content from html files
    repository.rules().forEach(rule -> rule.setHtmlDescription(loadResource("/org/sonar/l10n/php/rules/custom/" + rule.key() + ".html")));

    // Optionally define remediation costs
    Map<String, String> remediationCosts = new HashMap<>();
    remediationCosts.put(ForbiddenFunctionUseCheck.KEY, "5min");
    remediationCosts.put(OtherForbiddenFunctionUseCheck.KEY, "10min");
    repository.rules().forEach(rule -> rule.setDebtRemediationFunction(
      rule.debtRemediationFunctions().constantPerIssue(remediationCosts.get(rule.key()))));

    repository.done();
  }

  private String loadResource(String path) {
    URL resource = getClass().getResource(path);
    if (resource == null) {
      throw new IllegalStateException("Resource not found: " + path);
    }
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    try (InputStream in = resource.openStream()) {
      byte[] buffer = new byte[1024];
      for (int len = in.read(buffer); len != -1; len = in.read(buffer)) {
        result.write(buffer, 0, len);
      }
      return new String(result.toByteArray(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read resource: " + path, e);
    }
  }
}
