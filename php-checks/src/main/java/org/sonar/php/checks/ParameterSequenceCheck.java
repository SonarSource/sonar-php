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

import java.util.*;
import java.util.stream.Collectors;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.*;
import org.sonar.plugins.php.api.tree.expression.*;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import javax.annotation.CheckForNull;

@Rule(key = "S2234")
public class ParameterSequenceCheck extends PHPVisitorCheck {

  public static final String MESSAGE = "Parameters to \"%s\" have the same names but not the same order as the method arguments.";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (!tree.arguments().isEmpty()) {
      Symbol symbol = getDeclarationSymbol(tree);
      if (symbol != null && isVerifiableSymbol(symbol)) {
        checkParameterSequence(tree, (NameIdentifierTree) symbol.declaration());
      }
    }

    super.visitFunctionCall(tree);
  }

  @CheckForNull
  private Symbol getDeclarationSymbol(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();

    if (callee.is(Tree.Kind.NAMESPACE_NAME)) {
      return context().symbolTable().getSymbol(((NamespaceNameTree) callee).name());
    } else if(isVerifiableObjectMemberAccess(callee)) {
      return context().symbolTable().getSymbol(((MemberAccessTree) callee).member());
    } else if(isVerifiableClassMemberAccess(callee)) {
      return context().symbolTable().getSymbol(((MemberAccessTree) callee).member());
    }

    return null;
  }

  private boolean isVerifiableObjectMemberAccess(ExpressionTree tree) {
    return tree.is(Tree.Kind.OBJECT_MEMBER_ACCESS)
      && ((MemberAccessTree) tree).member().is(Tree.Kind.NAME_IDENTIFIER)
      && ((MemberAccessTree) tree).object().is(Tree.Kind.VARIABLE_IDENTIFIER)
      && ((VariableIdentifierTree) ((MemberAccessTree) tree).object()).text().equals("$this");
  }

  private boolean isVerifiableClassMemberAccess(ExpressionTree tree) {
    return tree.is(Tree.Kind.CLASS_MEMBER_ACCESS)
      && ((MemberAccessTree) tree).member().is(Tree.Kind.NAME_IDENTIFIER)
      && ((MemberAccessTree) tree).object().is(Tree.Kind.NAMESPACE_NAME)
      && ((NamespaceNameTree) ((MemberAccessTree) tree).object()).fullName().equals("self");
  }

  private boolean isVerifiableSymbol(Symbol symbol) {
    return symbol.is(Symbol.Kind.FUNCTION)
      && symbol.declaration() != null
      && symbol.declaration().is(Tree.Kind.NAME_IDENTIFIER);
  }

  private void checkParameterSequence(FunctionCallTree call, NameIdentifierTree identifier) {
    List<String> parameters = ((FunctionTree) identifier.getParent()).parameters().parameters().stream()
      .map(e -> e.variableIdentifier().text())
      .collect(Collectors.toList());

    List<String> arguments = call.arguments().stream()
      .filter(e -> e.is(Tree.Kind.VARIABLE_IDENTIFIER))
      .map(e -> ((VariableIdentifierTree) e).text())
      .collect(Collectors.toList());

    if (arguments.size() == parameters.size() && !arguments.equals(parameters) && new HashSet<>(parameters).equals(new HashSet<>(arguments))) {
      context().newIssue(this, call, String.format(MESSAGE, identifier.text()));
    }
  }
}
