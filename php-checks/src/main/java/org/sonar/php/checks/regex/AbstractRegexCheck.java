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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.php.regex.RegexCheck;
import org.sonar.php.regex.RegexCheckContext;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;

import static org.sonar.php.regex.PhpRegexSource.BRACKET_DELIMITERS;

public abstract class AbstractRegexCheck extends FunctionUsageCheck implements RegexCheck {

  private static final Pattern DELIMITER_PATTERN = Pattern.compile("^[^\\w\\r\\n\\t\\f\\v ]");

  protected static final Set<String> REGEX_FUNCTIONS = SetUtils.immutableSetOf(
    "preg_replace", "preg_match", "preg_filter", "preg_replace_callback", "preg_split"
  );

  private RegexCheckContext regexContext;

  // We want to report only one issue per element for one rule.
  private final Set<RegexSyntaxElement> reportedRegexTrees = new HashSet<>();

  @Override
  protected Set<String> lookedUpFunctionNames() {
    return REGEX_FUNCTIONS;
  }

  @Override
  public List<PhpIssue> analyze(CheckContext context) {
    this.regexContext = (RegexCheckContext) context;
    reportedRegexTrees.clear();
    return super.analyze(context);
  }

  @Override
  // TODO: parse regex flags from pattern and dedicated arguments
  protected void checkFunctionCall(FunctionCallTree tree) {
    CheckUtils.argumentValue(tree, "pattern", 0)
      .flatMap(AbstractRegexCheck::getLiteral)
      .filter(AbstractRegexCheck::isSingleLinePattern)
      .map(pattern -> regexForLiteral(new FlagSet(), pattern))
      .ifPresent(result -> checkRegex(result, tree));
  }

  protected static boolean isSingleLinePattern(LiteralTree literalTree) {
    SyntaxToken token = literalTree.token();
    return token.line() == token.endLine();
  }

  protected static Optional<LiteralTree> getLiteral(ExpressionTree expr) {
    if (expr.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      return patternWithDelimiter((LiteralTree) expr);
    } else if (expr.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      return CheckUtils.uniqueAssignedValue((VariableIdentifierTree) expr).flatMap(AbstractRegexCheck::getLiteral);
    }
    return Optional.empty();
  }

  protected static Optional<LiteralTree> patternWithDelimiter(LiteralTree tree) {
    String pattern = CheckUtils.trimQuotes(tree);
    if (pattern.length() >= 2) {
      Matcher m = DELIMITER_PATTERN.matcher(pattern);
      if (m.find() && containsEndDelimiter(pattern.substring(1), m.group().charAt(0))) {
        return Optional.of(tree);
      }
    }
    return Optional.empty();
  }

  protected static boolean containsEndDelimiter(String croppedPattern, Character startDelimiter) {
    return croppedPattern.indexOf(BRACKET_DELIMITERS.getOrDefault(startDelimiter, startDelimiter)) >= 0;
  }

  protected final RegexParseResult regexForLiteral(FlagSet flags, LiteralTree literals) {
    return regexContext.regexForLiteral(flags, literals);
  }

  public abstract void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall);

  public final void newIssue(RegexSyntaxElement regexTree, String message) {
    newIssue(regexTree, message, Collections.emptyList());
  }

  public final void newIssue(RegexSyntaxElement regexTree, String message, List<RegexIssueLocation> secondaries) {
    if (reportedRegexTrees.add(regexTree)) {
      PreciseIssue issue = regexContext.newIssue(this, regexTree, message);
      secondaries.forEach(issue::secondary);
    }
  }
}
