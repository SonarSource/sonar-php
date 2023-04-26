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
package org.sonar.php.filters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class SuppressWarningFilter extends PHPVisitorCheck implements PHPIssueFilter {

  private final SuppressedWarnings suppressedWarnings = new SuppressedWarnings();

  private static final String ARGUMENT_FORMAT = "\"[a-zA-Z0-9:]++\"";
  private static final String ARGUMENTS_FORMAT = ARGUMENT_FORMAT + "(?:\\s*+,\\s*+"+ARGUMENT_FORMAT+")*+";
  private static final Pattern SUPPRESS_WARNING_COMMENT_PATTERN = Pattern.compile("@SuppressWarnings\\s*+\\(\\s*+(?<arguments>"+ARGUMENTS_FORMAT+")\\s*+\\)");

  public void reset() {
    suppressedWarnings.clear();
  }

  @Override
  public boolean accept(String fileUri, String ruleName, int line) {
    return !suppressedWarnings.hasSuppressedWarnings(fileUri, line, ruleName);
  }

  @Override
  public void visitAttribute(AttributeTree tree) {
    List<String> rulesSuppressed = extractedSuppressedWarningsFromArgument(tree.arguments());
    suppressedWarnings.addSuppressedWarning(getFileUri(), retrieveAttributeLine(tree), rulesSuppressed);
    super.visitAttribute(tree);
  }

  public List<String> extractedSuppressedWarningsFromArgument(Collection<CallArgumentTree> arguments) {
    return arguments.stream()
      .map(CallArgumentTree::value)
      // consider only string literal
      .filter(expr -> expr.is(Tree.Kind.REGULAR_STRING_LITERAL))
      .map(LiteralTree.class::cast)
      .map(literal -> stripDoubleQuotes(literal.value()))
      .collect(Collectors.toList());
  }

  public int retrieveAttributeLine(AttributeTree tree) {
    // TODO: SONARPHP-1368 replace this logic "ignore next line issue" by more advanced "the scope of parent group attribute"
    AttributeGroupTree parent = (AttributeGroupTree) tree.getParent();
    return parent.endToken().endLine()+1;
  }

  @Override
  public void visitToken(SyntaxToken token) {
    for (SyntaxTrivia trivia : token.trivias()) {
      String comment = getContents(trivia.text());
      processSuppressedWarningsInComment(token, comment);
    }
    super.visitToken(token);
  }

  private void processSuppressedWarningsInComment(SyntaxToken token, String comment) {
    Matcher matcher = SUPPRESS_WARNING_COMMENT_PATTERN.matcher(comment);
    while (matcher.find()) {
      String arguments = matcher.group("arguments");
      Arrays.stream(arguments.split(","))
        .map(str -> stripDoubleQuotes(str.trim()))
        .forEach(ruleName -> {
          for (int line = token.line(); line <= token.endLine(); line++) {
            suppressedWarnings.addSuppressedWarning(getFileUri(), line, ruleName);
          }
        });
    }
  }

  private String getFileUri() {
    return this.context().getPhpFile().uri().toString();
  }

  private static String getContents(String comment) {
    if (comment.startsWith("//")) {
      return comment.substring(2);
    } else if (comment.startsWith("#")) {
      return comment.substring(1);
    } else {
      return comment.substring(2, comment.length() - 2);
    }
  }

  private static String stripDoubleQuotes(String str) {
    if (str.startsWith("\"") && str.endsWith("\"")) {
      return str.substring(1, str.length()-1);
    }
    return str;
  }

  static class SuppressedWarnings {
    private final Map<String, Map<Integer, Set<String>>> suppressedRulesPerLinePerFile = new HashMap<>();

    public void addSuppressedWarning(String fileUri, int line, String ruleName) {
      addSuppressedWarning(fileUri, line, List.of(ruleName));
    }

    public void addSuppressedWarning(String fileUri, int line, Collection<String> ruleName) {
      suppressedRulesPerLinePerFile
        .computeIfAbsent(fileUri, key -> new HashMap<>())
        .computeIfAbsent(line, key -> new HashSet<>())
        .addAll(ruleName);
    }

    public boolean hasSuppressedWarnings(String fileUri, int line, String ruleName) {
      return suppressedRulesPerLinePerFile
        .getOrDefault(fileUri, Collections.emptyMap())
        .getOrDefault(line, Collections.emptySet())
        .contains(ruleName);
    }

    public void clear() {
      suppressedRulesPerLinePerFile.clear();
    }
  }
}
