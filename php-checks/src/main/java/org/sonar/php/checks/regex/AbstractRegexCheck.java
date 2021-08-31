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
package org.sonar.php.checks.regex;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.php.regex.RegexCheck;
import org.sonar.php.regex.RegexCheckContext;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;

public abstract class AbstractRegexCheck extends FunctionUsageCheck implements RegexCheck {

  // TODO: Should be extended by all other regex methods we want to handle
  protected static final Set<String> REGEX_FUNCTIONS = new HashSet<>(Collections.singletonList(
    "preg_replace"
  ));

  private RegexCheckContext regexContext;

  // We want to report only one issue per element for one rule.
  private final HashSet<RegexSyntaxElement> reportedRegexTrees = new HashSet<>();

  @Override
  protected Set<String> expectedFunctions() {
    return REGEX_FUNCTIONS;
  }

  @Override
  public List<PhpIssue> analyze(CheckContext context) {
    this.regexContext = (RegexCheckContext) context;
    reportedRegexTrees.clear();
    return super.analyze(context);
  }

  @Override
  protected void checkFunctionCall(FunctionCallTree tree) {
    CheckUtils.argumentValue(tree, "pattern", 0)
      .flatMap(AbstractRegexCheck::getLiteral)
      .map(pattern -> regexForLiteral(getFlagSet(tree), pattern))
      .ifPresent(result -> checkRegex(result, tree));
  }

  protected static Optional<LiteralTree> getLiteral(ExpressionTree expr) {
    if (expr.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      return Optional.of((LiteralTree) expr);
    } else if (expr.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      return CheckUtils.uniqueAssignedValue((VariableIdentifierTree) expr).flatMap(AbstractRegexCheck::getLiteral);
    }
    return Optional.empty();
  }

  protected final RegexParseResult regexForLiteral(FlagSet flags, LiteralTree literals) {
    return regexContext.regexForLiteral(flags, literals);
  }

  /**
   * TODO: parse regex flags from pattern and dedicated arguments
   */
  protected static FlagSet getFlagSet(FunctionCallTree tree) {
    return new FlagSet();
  }

  public abstract void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall);
}
