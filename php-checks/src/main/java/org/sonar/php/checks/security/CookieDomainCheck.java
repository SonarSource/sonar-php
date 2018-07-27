/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.php.ini.BasePhpIniIssue;
import org.sonar.php.ini.PhpIniCheck;
import org.sonar.php.ini.PhpIniIssue;
import org.sonar.php.ini.tree.PhpIniFile;
import org.sonar.php.tree.visitors.AssignmentExpressionVisitor;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;

import static org.sonar.php.checks.utils.CheckUtils.getFunctionName;

@Rule(key = "S3331")
public class CookieDomainCheck extends FunctionUsageCheck implements PhpIniCheck {

  private static final String MESSAGE = "Specify at least a second-level cookie domain.";

  // The key is the function name, the value is the index of the 'domain' parameter
  private static final Map<String, Integer> FUNCTION_AND_PARAM_INDEX = ImmutableMap.of(
    "setcookie", 4,
    "session_set_cookie_params", 2);

  private AssignmentExpressionVisitor assignmentExpressionVisitor;

  @Override
  protected ImmutableSet<String> functionNames() {
    return ImmutableSet.copyOf(FUNCTION_AND_PARAM_INDEX.keySet());
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    assignmentExpressionVisitor = new AssignmentExpressionVisitor(context().symbolTable());
    tree.accept(assignmentExpressionVisitor);
    super.visitCompilationUnit(tree);
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    int domainIndex = FUNCTION_AND_PARAM_INDEX.get(getFunctionName(tree));
    List<ExpressionTree> args = tree.arguments();
    if (args.size() <= domainIndex) {
      return;
    }
    ExpressionTree argumentVariable = args.get(domainIndex);
    ExpressionTree domainValue = getAssignedValue(argumentVariable);
    if (domainValue.is(Tree.Kind.REGULAR_STRING_LITERAL, Tree.Kind.NULL_LITERAL) && isFirstLevelDomain(((LiteralTree) domainValue).value())) {
      if (argumentVariable == domainValue) {
        context().newIssue(this, domainValue, MESSAGE);
      } else {
        context().newIssue(this, domainValue, MESSAGE).secondary(argumentVariable, MESSAGE);
      }
    }
  }

  @Override
  public List<PhpIniIssue> analyze(PhpIniFile phpIniFile) {
    return phpIniFile.directivesForName("session.cookie_domain").stream()
        .filter(d -> isFirstLevelDomain(d.value().text()))
        .map(d -> BasePhpIniIssue.newIssue(MESSAGE).line(d.name().line()))
        .collect(Collectors.toList());
  }

  private ExpressionTree getAssignedValue(ExpressionTree value) {
    Symbol valueSymbol = context().symbolTable().getSymbol(value);
    return assignmentExpressionVisitor
      .getUniqueAssignedValue(valueSymbol)
      .orElse(value);
  }

  private static boolean isFirstLevelDomain(String domain) {
    return Arrays.stream(CheckUtils.trimQuotes(domain).split("\\."))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .count() < 2;
  }

}
