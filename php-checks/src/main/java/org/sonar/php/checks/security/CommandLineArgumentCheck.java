/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.checks.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.php.tree.symbols.SymbolImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.statement.GlobalStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static java.util.Collections.emptyList;
import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

@Rule(key = "S4823")
public class CommandLineArgumentCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure that command line arguments are used safely here.";

  private static final Set<QualifiedName> SUSPICIOUS_CLASS_INSTANTIATIONS = SetUtils.immutableSetOf(
    qualifiedName("Zend\\Console\\Getopt"),
    qualifiedName("GetOpt\\Option"));

  private static final Set<String> SUSPICIOUS_ARRAY_ACCESSES = SetUtils.immutableSetOf("$GLOBALS", "$_SERVER");
  private static final Set<String> SUSPICIOUS_GLOBAL_IDENTIFIERS = SetUtils.immutableSetOf("$argv", "$HTTP_SERVER_VARS");

  private Map<Scope, List<String>> variableSetAsGlobalInScopes = new HashMap<>();

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    variableSetAsGlobalInScopes.clear();
    super.visitCompilationUnit(tree);
    variableSetAsGlobalInScopes.clear();
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();
    if (isGlobalGetOptMethod(callee) || isSuspiciousClassInstantiation(callee)) {
      context().newIssue(this, tree, MESSAGE);
    }

    super.visitFunctionCall(tree);
  }

  private static boolean isGlobalGetOptMethod(ExpressionTree callee) {
    return callee.is(Tree.Kind.NAMESPACE_NAME) && "getopt".equalsIgnoreCase(((NamespaceNameTree) callee).qualifiedName());
  }

  private boolean isSuspiciousClassInstantiation(ExpressionTree callee) {
    if (callee.is(Tree.Kind.NAMESPACE_NAME)) {
      QualifiedName className = getFullyQualifiedName((NamespaceNameTree) callee);
      return SUSPICIOUS_CLASS_INSTANTIATIONS.stream().anyMatch(className::equals);
    }
    return false;
  }

  @Override
  public void visitArrayAccess(ArrayAccessTree tree) {
    ExpressionTree offset = tree.offset();
    if (tree.object().is(Tree.Kind.VARIABLE_IDENTIFIER) && offset != null && offset.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      String variable = ((VariableIdentifierTree) tree.object()).text();
      String indexValue = trimQuotes((LiteralTree) offset);
      if ("argv".equals(indexValue) && SUSPICIOUS_ARRAY_ACCESSES.contains(variable)) {
        context().newIssue(this, tree, MESSAGE);
      }
    }

    super.visitArrayAccess(tree);
  }

  @Override
  public void visitGlobalStatement(GlobalStatementTree tree) {
    for (VariableTree variableTree : tree.variables()) {
      Symbol symbol = context().symbolTable().getSymbol(variableTree);
      if (variableTree.is(Tree.Kind.VARIABLE_IDENTIFIER) && symbol != null) {
        Scope scope = ((SymbolImpl) symbol).scope();
        String variable = ((VariableIdentifierTree) variableTree).text();
        variableSetAsGlobalInScopes
          .computeIfAbsent(scope, key -> new ArrayList<>())
          .add(variable);
      }
    }

    super.visitGlobalStatement(tree);
  }

  @Override
  public void visitVariableIdentifier(VariableIdentifierTree tree) {
    if (SUSPICIOUS_GLOBAL_IDENTIFIERS.contains(tree.text()) && isGlobalVariable(tree)) {
      context().newIssue(this, tree, MESSAGE);
    }

    super.visitVariableIdentifier(tree);
  }

  private boolean isGlobalVariable(VariableIdentifierTree tree) {
    SymbolImpl symbol = (SymbolImpl) context().symbolTable().getSymbol(tree);
    return symbol == null || isGlobalScope(symbol.scope(), tree.text());
  }

  private boolean isGlobalScope(Scope scope, String variable) {
    return scope.isGlobal() || variableSetAsGlobalInScopes.getOrDefault(scope, emptyList()).contains(variable);
  }

}
