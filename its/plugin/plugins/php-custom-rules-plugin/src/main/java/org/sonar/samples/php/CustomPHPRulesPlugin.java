/*
 * Copyright (C) 2009-2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.samples.php;

import com.google.common.collect.ImmutableList;
import org.sonar.api.SonarPlugin;

import java.util.List;

public class CustomPHPRulesPlugin extends SonarPlugin {

  @Override
  public List getExtensions() {
    return ImmutableList.of(
      CustomPHPRulesDefinition.class
    );
  }

}
