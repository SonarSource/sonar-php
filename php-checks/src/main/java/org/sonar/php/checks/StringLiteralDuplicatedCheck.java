/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

import static org.sonar.php.checks.utils.RegexUtils.firstOf;
import static org.sonar.php.checks.utils.RegexUtils.oneOrMore;
import static org.sonar.php.checks.utils.RegexUtils.optional;

@Rule(key = "S1192")
public class StringLiteralDuplicatedCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Define a constant instead of duplicating this literal \"%s\" %s times.";
  private static final String SECONDARY_MESSAGE = "Duplication.";

  private static final String ONLY_ALPHANUMERIC_UNDERSCORES_HYPHENS_AND_PERIODS = "^[a-zA-Z_][.\\-\\w]+$";
  private static final String HTML_SIMPLE_TAG = "^</?[a-zA-Z][a-zA-Z0-9\\-_:.]*>$";

  private final Map<String, LiteralTree> firstOccurrenceTrees = new HashMap<>();
  private final Map<String, List<LiteralTree>> sameLiteralOccurrences = new HashMap<>();

  public static final int THRESHOLD_DEFAULT = 3;
  public static final int MINIMAL_LITERAL_LENGTH_DEFAULT = 5;

  // Single elements
  private static final String IDENTIFIER = "[a-zA-Z][a-zA-Z0-9\\-_:.]*+";
  private static final String DOUBLE_QUOTED_STRING = "\"(?:\\\\.|[^\"])*+\"";
  private static final String SINGLE_QUOTED_STRING = "'(?:\\\\.|[^'])*+'";
  private static final String NO_QUOTED_STRING = "[a-zA-Z0-9\\\\-_:./]++";
  private static final String TAG_ATTRIBUTE = IDENTIFIER + "\\s*+(?:=\\s*+(?:" + DOUBLE_QUOTED_STRING + "|" + SINGLE_QUOTED_STRING + "|" + NO_QUOTED_STRING + "))?+";
  private static final String OPT_TEXT_OUTSIDE_OF_TAGS = "[^<>]*+";

  // Partial elements matching
  private static final String DOUBLE_QUOTED_STRING_PARTIAL_START = "\"(?:\\\\.|[^\"])*+";
  private static final String SINGLE_QUOTED_STRING_PARTIAL_START = "'(?:\\\\.|[^'])*+";
  private static final String NO_QUOTED_STRING_PARTIAL_START = "[a-zA-Z0-9\\\\-_:./]++";
  private static final String TAG_ATTRIBUTE_PARTIAL_START = "\\s*+(?:=\\s*+(?:" + DOUBLE_QUOTED_STRING_PARTIAL_START + "|" + SINGLE_QUOTED_STRING_PARTIAL_START + "|" + NO_QUOTED_STRING_PARTIAL_START + ")?+)?+";

  // Complex regexes
  private static final String COMPLEX_HTML_TAG = "</?\\s*+" + IDENTIFIER + "\\s*+(?:" + TAG_ATTRIBUTE + "\\s*+)*+\\s*+/?+>";
  private static final String TAG_END = "[\"']?+\\s*+(?:" + TAG_ATTRIBUTE + "\\s*+)*+/?+>";
  private static final String TAG_START = "\\s*+</?+\\s*+(?:" + IDENTIFIER + "\\s*+(?:" + TAG_ATTRIBUTE + "\\s*+)*+(?:" + TAG_ATTRIBUTE_PARTIAL_START + ")?+)?+";
  private static final String HTML_CONTENT = optional(TAG_END) + oneOrMore(OPT_TEXT_OUTSIDE_OF_TAGS, COMPLEX_HTML_TAG) + OPT_TEXT_OUTSIDE_OF_TAGS + optional(TAG_START);

  private static final String FULL_ALLOW_REGEX = firstOf(HTML_CONTENT, TAG_START, TAG_END, TAG_END + TAG_START, ONLY_ALPHANUMERIC_UNDERSCORES_HYPHENS_AND_PERIODS);
  private static final Pattern ALLOWED_DUPLICATED_LITERALS = Pattern.compile(FULL_ALLOW_REGEX);

  @RuleProperty(
    key = "threshold",
    defaultValue = "" + THRESHOLD_DEFAULT)
  int threshold = THRESHOLD_DEFAULT;

  @RuleProperty(
    key = "minimal_literal_length",
    defaultValue = "" + MINIMAL_LITERAL_LENGTH_DEFAULT)
  int minimalLiteralLength = MINIMAL_LITERAL_LENGTH_DEFAULT;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    firstOccurrenceTrees.clear();
    sameLiteralOccurrences.clear();

    super.visitCompilationUnit(tree);

    finish();
  }

  private void finish() {
    for (Map.Entry<String, List<LiteralTree>> literalOccurrences : sameLiteralOccurrences.entrySet()) {
      List<LiteralTree> occurrences = literalOccurrences.getValue();

      if (occurrences.size() >= threshold) {
        String literal = literalOccurrences.getKey();
        String message = String.format(MESSAGE, literal, occurrences.size());
        LiteralTree firstOccurrenceTree = firstOccurrenceTrees.get(literal);
        PreciseIssue issue = context().newIssue(this, firstOccurrenceTree, message).cost(occurrences.size());
        occurrences.stream()
          .filter(o -> !o.equals(firstOccurrenceTree))
          .forEach(occurrence -> issue.secondary(occurrence, SECONDARY_MESSAGE));
      }
    }
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Kind.REGULAR_STRING_LITERAL)) {
      String literal = tree.value();
      String value = StringUtils.substring(literal, 1, literal.length() - 1);

      if (value.length() >= minimalLiteralLength && !ALLOWED_DUPLICATED_LITERALS.matcher(value).find()) {

        if (!sameLiteralOccurrences.containsKey(value)) {
          List<LiteralTree> occurrences = new ArrayList<>();
          occurrences.add(tree);
          sameLiteralOccurrences.put(value, occurrences);
          firstOccurrenceTrees.put(value, tree);

        } else {
          sameLiteralOccurrences.get(value).add(tree);
        }
      }
    }
  }

}
