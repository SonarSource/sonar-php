/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.php.regex.PhpRegexCheck;
import org.sonar.php.regex.PhpRegexUtils;
import org.sonar.php.regex.RegexCheckContext;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;
import org.sonarsource.analyzer.commons.regex.RegexIssueLocation;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;

import static org.sonar.php.regex.PhpRegexUtils.BRACKET_DELIMITERS;

public abstract class AbstractRegexCheck extends FunctionUsageCheck implements PhpRegexCheck {

  public static final int PCRE_CASELESS = Pattern.CASE_INSENSITIVE;
  public static final int PCRE_MULTILINE = Pattern.MULTILINE;
  public static final int PCRE_DOTALL = Pattern.DOTALL;
  public static final int PCRE_EXTENDED = Pattern.COMMENTS;
  public static final int PCRE_UTF8 = Pattern.UNICODE_CHARACTER_CLASS;

  protected static final Pattern DELIMITER_PATTERN = Pattern.compile("^[^a-zA-Z\\d\\r\\n\\t\\f\\v]");
  protected static final Set<String> REGEX_FUNCTIONS = SetUtils.immutableSetOf(
    "preg_replace", "preg_match", "preg_filter", "preg_replace_callback", "preg_split", "preg_match_all"
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
  protected void checkFunctionCall(FunctionCallTree tree) {
    CheckUtils.argumentValue(tree, "pattern", 0)
      .flatMap(AbstractRegexCheck::getLiteral)
      .filter(this::hasValidDelimiters)
      .map(pattern -> regexForLiteral(getFlagSet(pattern), pattern))
      .ifPresent(result -> checkRegex(result, tree));
  }

  // Visible for testing
  static FlagSet getFlagSet(LiteralTree literalTree) {
    String pattern = trimPattern(literalTree);
    Character endDelimiter = PhpRegexUtils.getEndDelimiter(pattern);
    String patternModifiers = pattern.substring(pattern.lastIndexOf(endDelimiter) + 1);
    FlagSet flags = new FlagSet();
    for (char modifier: patternModifiers.toCharArray()) {
      Optional.ofNullable(parseModifier(modifier)).ifPresent(flags::add);
    }
    return flags;
  }

  // Visible for testing
  static Optional<LiteralTree> getLiteral(ExpressionTree expr) {
    if (expr.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      return Optional.of((LiteralTree) expr);
    } else if (expr.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      return CheckUtils.uniqueAssignedValue((VariableIdentifierTree) expr).flatMap(AbstractRegexCheck::getLiteral);
    }
    return Optional.empty();
  }

  protected boolean hasValidDelimiters(LiteralTree tree) {
    String pattern = trimPattern(tree);
    if (pattern.length() >= 2) {
      Matcher m = DELIMITER_PATTERN.matcher(pattern);
      return m.find() && containsEndDelimiter(pattern.substring(1), m.group().charAt(0));
    }
    return false;
  }

  protected static String trimPattern(LiteralTree tree) {
    return CheckUtils.trimQuotes(tree).trim();
  }

  protected static boolean containsEndDelimiter(String croppedPattern, Character startDelimiter) {
    return croppedPattern.indexOf(BRACKET_DELIMITERS.getOrDefault(startDelimiter, startDelimiter)) >= 0;
  }

  protected final RegexParseResult regexForLiteral(FlagSet flags, LiteralTree literals) {
    return regexContext.regexForLiteral(flags, literals);
  }

  protected abstract void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall);

  protected void newIssue(RegexSyntaxElement regexTree, String message, @Nullable Integer cost, List<RegexIssueLocation> secondaries) {
    if (reportedRegexTrees.add(regexTree)) {
      PreciseIssue issue = regexContext.newIssue(this, regexTree, message);
      secondaries.stream().map(PhpRegexCheck.PhpRegexIssueLocation::new).forEach(issue::secondary);
      if (cost != null) {
        issue.cost(cost);
      }
    }
  }

  protected final void newIssue(Tree tree, String message, @Nullable Integer cost, List<RegexIssueLocation> secondaries) {
    PreciseIssue issue = newIssue(tree, message);
    secondaries.stream().map(PhpRegexCheck.PhpRegexIssueLocation::new).forEach(issue::secondary);
    if (cost != null) {
      issue.cost(cost);
    }
  }

  @CheckForNull
  private static Integer parseModifier(char ch) {
    switch (ch) {
      case 'i':
        return PCRE_CASELESS;
      case 'm':
        return PCRE_MULTILINE;
      case 's':
        return PCRE_DOTALL;
      case 'u':
        return PCRE_UTF8;
      case 'x':
        return PCRE_EXTENDED;
      default:
        return null;
    }
  }
}
