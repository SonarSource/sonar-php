/*
 * Copyright (C) 2009-2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.samples.php;


import com.google.common.collect.ImmutableList;
import org.sonar.plugins.php.api.visitors.PHPCustomRulesDefinition;

public class CustomPHPRulesDefinition extends PHPCustomRulesDefinition {

  @Override
  public String repositoryName() {
    return "Custom Repository";
  }

  @Override
  public String repositoryKey() {
    return "php-custom-rules";
  }

  @Override
  public ImmutableList<Class> checkClasses() {
      return ImmutableList.<Class>of(
      CustomPHPVisitorCheck.class,
      CustomPHPSubscriptionCheck.class);
  }
}
