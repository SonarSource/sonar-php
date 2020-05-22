/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.php.checks;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import javax.annotation.CheckForNull;

@Rule(key = "S2234")
public class ParameterSequenceCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Parameters to \"%s\" have the same names but not the same order as the method arguments.";
  private static final String SECONDARY_MESSAGE = "Implementation of the parameters sequence.";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    // only check method and function calls expect constructors with more than 1 argument
    if (!tree.getParent().is(Kind.NEW_EXPRESSION) && tree.arguments().size() > 1) {
      Symbol symbol = getDeclarationSymbol(tree);
      if (symbol != null && symbol.declaration() != null) {
        checkParameterSequence(tree, (NameIdentifierTree) symbol.declaration());
      }
    }

    super.visitFunctionCall(tree);
  }

  @CheckForNull
  private Symbol getDeclarationSymbol(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();

    if (callee.is(Kind.NAMESPACE_NAME)) {
      return context().symbolTable().getSymbol(((NamespaceNameTree) callee).name());
    } else if(isVerifiableObjectMemberAccess(callee)) {
      return context().symbolTable().getSymbol(((MemberAccessTree) callee).member());
    } else if(isVerifiableClassMemberAccess(callee)) {
      return context().symbolTable().getSymbol(((MemberAccessTree) callee).member());
    }

    return null;
  }

  private static boolean isVerifiableObjectMemberAccess(ExpressionTree tree) {
    if (tree.is(Kind.OBJECT_MEMBER_ACCESS) && ((MemberAccessTree) tree).member().is(Kind.NAME_IDENTIFIER)) {
      Tree object = ((MemberAccessTree) tree).object();

      return object.is(Kind.VARIABLE_IDENTIFIER) && ((VariableIdentifierTree) object).text().equals("$this");
    }

    return false;
  }

  private static boolean isVerifiableClassMemberAccess(ExpressionTree tree) {
    if (tree.is(Kind.CLASS_MEMBER_ACCESS) && ((MemberAccessTree) tree).member().is(Kind.NAME_IDENTIFIER)) {
      Tree object = ((MemberAccessTree) tree).object();

      return ((object.is(Kind.NAMESPACE_NAME) && ((NamespaceNameTree) object).fullName().equals("self"))
        || (object.is(Kind.NAME_IDENTIFIER) && ((NameIdentifierTree) object).text().equals("static")));
    }
    return false;
  }

  private void checkParameterSequence(FunctionCallTree call, NameIdentifierTree identifier) {
    List<String> parameters = ((FunctionTree) identifier.getParent()).parameters().parameters().stream()
      .map(e -> e.variableIdentifier().text())
      .collect(Collectors.toList());

    List<String> arguments = call.arguments().stream()
      .filter(e -> e.is(Kind.VARIABLE_IDENTIFIER))
      .map(e -> ((VariableIdentifierTree) e).text())
      .collect(Collectors.toList());

    if (arguments.size() == parameters.size() && !arguments.equals(parameters) && new HashSet<>(parameters).equals(new HashSet<>(arguments))) {
      context().newIssue(this, call, String.format(MESSAGE, identifier.text())).secondary(identifier, SECONDARY_MESSAGE);
    }
  }
}
