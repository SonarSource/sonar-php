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
