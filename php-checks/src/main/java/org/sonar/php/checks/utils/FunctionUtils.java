/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
package org.sonar.php.checks.utils;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;

import java.util.List;

public class FunctionUtils {

  private static final Kind[] DECLARATION_KINDS_ARRAY = {
    Kind.METHOD_DECLARATION,
    Kind.FUNCTION_DECLARATION,
    Kind.FUNCTION_EXPRESSION};

  public static final List<Kind> DECLARATION_KINDS = ImmutableList.copyOf(DECLARATION_KINDS_ARRAY);

  private FunctionUtils() {
  }

  /**
   * Returns function or method's name, or "expression" if the given node is a function expression.
   *
   * @param functionDec FUNCTION_DECLARATION, METHOD_DECLARATION or FUNCTION_EXPRESSION
   * @return name of function or "expression" if function expression
   */
  public static String getFunctionName(FunctionTree functionDec) {
    if (functionDec.is(Kind.FUNCTION_DECLARATION)) {
      return "\"" + ((FunctionDeclarationTree) functionDec).name().text() + "\"";
    } else if (functionDec.is(Kind.METHOD_DECLARATION)) {
      return "\"" + ((MethodDeclarationTree) functionDec).name().text() + "\"";
    }
    return "expression";
  }

  /**
   * Return whether the method is overriding a parent method or not.
   *
   * @param declaration METHOD_DECLARATION
   * @return true if method has tag "@inheritdoc" in it's doc comment.
   */
  public static boolean isOverriding(MethodDeclarationTree declaration) {
    for (SyntaxTrivia comment : ((PHPTree) declaration).getFirstToken().trivias()) {
      if (StringUtils.containsIgnoreCase(comment.text(), "@inheritdoc")) {
        return true;
      }
    }
    return false;
  }

  public static boolean isFunctionDeclaration(Tree tree) {
    return tree.is(DECLARATION_KINDS_ARRAY);
  }

}
