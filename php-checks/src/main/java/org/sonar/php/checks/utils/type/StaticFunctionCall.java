/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.checks.utils.type;

import org.sonar.plugins.php.api.symbols.QualifiedName;

import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

public class StaticFunctionCall {
  private QualifiedName callee;
  private String functionName;

  private StaticFunctionCall(QualifiedName callee, String functionName) {
    this.callee = callee;
    this.functionName = functionName;
  }

  public boolean matches(QualifiedName className, String memberName) {
    return functionName.equalsIgnoreCase(memberName) && callee.equals(className);
  }

  /**
   * Utility method to conveniently create StaticFunctionCall objects with PHP notation.
   * Ex: staticFunctionCall("Foo\Bar\FooBar::staticMethod")
   */
  public static StaticFunctionCall staticFunctionCall(String qualifiedFunctionCall) {
    int i = qualifiedFunctionCall.indexOf("::");
    if (i < 0) {
      throw new IllegalStateException("StaticFunctionCall notation must contain the \"::\" separator");
    }
    QualifiedName name = qualifiedName(qualifiedFunctionCall.substring(0, i));
    return new StaticFunctionCall(name, qualifiedFunctionCall.substring(i + 2));
  }

}
