/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

import static org.sonar.php.checks.utils.RegexUtils.firstOf;
import static org.sonar.php.checks.utils.RegexUtils.oneOrMore;
import static org.sonar.php.checks.utils.RegexUtils.optional;
import static org.sonar.php.checks.utils.RegexUtils.zeroOrMore;

@Rule(key = "S1192")
public class StringLiteralDuplicatedCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Define a constant instead of duplicating this literal \"%s\" %s times.";
  private static final String SECONDARY_MESSAGE = "Duplication.";

  private static final String ONLY_ALPHANUMERIC_UNDERSCORES_HYPHENS_AND_PERIODS = "^[a-zA-Z_][.:,|\\-\\w]+$";

  // Single elements
  private static final String IDENTIFIER = "[a-zA-Z][a-zA-Z0-9\\-_:.]*+";
  private static final String OPT_SPACING = "\\s*+";
  private static final String OPT_TEXT_OUTSIDE_OF_TAGS = "[^<>]*+";
  private static final String DOUBLE_QUOTED_STRING = "\"(?:\\\\.|[^\"])*+\"";
  private static final String SINGLE_QUOTED_STRING = "'(?:\\\\.|[^'])*+'";
  private static final String NO_QUOTED_STRING = "[a-zA-Z0-9\\-_:./]++";

  // Partial elements matching
  private static final String DOUBLE_QUOTED_STRING_PARTIAL_START = "\"(?:\\\\.|[^\"])*+";
  private static final String SINGLE_QUOTED_STRING_PARTIAL_START = "'(?:\\\\.|[^'])*+";
  private static final String NO_QUOTED_STRING_PARTIAL_START = "[a-zA-Z0-9\\-_:./]++";
  private static final String TAG_ATTRIBUTE_PARTIAL_START = OPT_SPACING
    + optional("=", OPT_SPACING, optional(firstOf(DOUBLE_QUOTED_STRING_PARTIAL_START, SINGLE_QUOTED_STRING_PARTIAL_START, NO_QUOTED_STRING_PARTIAL_START)));

  // Complex regexes
  private static final String TAG_ATTRIBUTE = IDENTIFIER + OPT_SPACING + optional("=", OPT_SPACING, firstOf(DOUBLE_QUOTED_STRING, SINGLE_QUOTED_STRING, NO_QUOTED_STRING));
  private static final String HTML_TAG_FULL = "</?" + OPT_SPACING + IDENTIFIER + OPT_SPACING + zeroOrMore(TAG_ATTRIBUTE, OPT_SPACING) + "/?+>";
  private static final String HTML_TAG_PARTIAL_START = OPT_SPACING + "</?+" + OPT_SPACING
    + optional(IDENTIFIER, OPT_SPACING, zeroOrMore(TAG_ATTRIBUTE, OPT_SPACING), optional(TAG_ATTRIBUTE_PARTIAL_START));
  private static final String HTML_TAG_PARTIAL_END = "[\"']?+" + OPT_SPACING + zeroOrMore(TAG_ATTRIBUTE, OPT_SPACING) + "/?+>";
  private static final String HTML_CONTENT = optional(HTML_TAG_PARTIAL_END) + oneOrMore(OPT_TEXT_OUTSIDE_OF_TAGS, HTML_TAG_FULL) + OPT_TEXT_OUTSIDE_OF_TAGS
    + optional(HTML_TAG_PARTIAL_START);

  private static final String FULL_ALLOWED_LITERALS_REGEX = firstOf(
    HTML_CONTENT,
    OPT_TEXT_OUTSIDE_OF_TAGS + HTML_TAG_PARTIAL_START,
    HTML_TAG_PARTIAL_END + OPT_TEXT_OUTSIDE_OF_TAGS,
    HTML_TAG_PARTIAL_END + OPT_TEXT_OUTSIDE_OF_TAGS + HTML_TAG_PARTIAL_START,
    ONLY_ALPHANUMERIC_UNDERSCORES_HYPHENS_AND_PERIODS);
  private static final Pattern ALLOWED_DUPLICATED_LITERALS = Pattern.compile(FULL_ALLOWED_LITERALS_REGEX);

  private static final Set<String> TRANSLATION_FUNCTIONS = Set.of(
    "__", "_e", "t", "esc_html__", "esc_html_e", "esc_attr__", "esc_attr_e", "_n", "_x", "_nx", "_ex");

  public static final int THRESHOLD_DEFAULT = 3;
  public static final int MINIMAL_LITERAL_LENGTH_DEFAULT = 5;

  private final Map<String, LiteralTree> firstOccurrenceTrees = new HashMap<>();
  private final Map<String, List<LiteralTree>> sameLiteralOccurrences = new HashMap<>();
  private boolean isPhpUnitTestCase = false;

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
    // Skip importmap.php as it is generated by frameworks and can legitimately contain duplicated literals.
    if ("importmap.php".equals(context().getPhpFile().filename())) {
      return;
    }
    firstOccurrenceTrees.clear();
    sameLiteralOccurrences.clear();
    isPhpUnitTestCase = false;
    super.visitCompilationUnit(tree);
    finish();
  }

  private void finish() {
    for (Map.Entry<String, List<LiteralTree>> literalOccurrences : sameLiteralOccurrences.entrySet()) {
      String value = literalOccurrences.getKey();
      List<LiteralTree> occurrences = literalOccurrences.getValue();

      if (occurrences.size() >= threshold && !ALLOWED_DUPLICATED_LITERALS.matcher(value).matches()) {
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
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    isPhpUnitTestCase = CheckUtils.isSubClassOfTestCase(tree);
    super.visitClassDeclaration(tree);
    isPhpUnitTestCase = false;
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Kind.REGULAR_STRING_LITERAL) && !isArrayKey(tree) && !isTranslationFunctionArgument(tree) && !isPhpUnitTestCase) {
      String literal = tree.value().replace("\\'", "'").replace("\\\"", "\"");
      String value = removeQuotesAndQuotesEscaping(literal);

      if (value.length() >= minimalLiteralLength) {
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

  private static String removeQuotesAndQuotesEscaping(String s) {
    var quote = s.charAt(0);
    return s.substring(1, s.length() - 1).replace("\\" + quote, String.valueOf(quote));
  }

  private static boolean isTranslationFunctionArgument(LiteralTree tree) {
    Tree parent = tree.getParent();
    if (parent == null) {
      return false;
    }
    if (parent.is(Kind.CALL_ARGUMENT)) {
      Tree grandParent = parent.getParent();
      if (grandParent != null && grandParent.is(Kind.FUNCTION_CALL)) {
        FunctionCallTree funcCall = (FunctionCallTree) grandParent;
        String funcName = CheckUtils.lowerCaseFunctionName(funcCall);
        return funcName != null && TRANSLATION_FUNCTIONS.contains(funcName);
      }
    }
    return false;
  }

  /**
   * Checks if a literal is used as an array key.
   * This includes:
   * - Keys in array initializers: ['key' => 'value']
   * - Keys in array access: $array['key']
   */
  private static boolean isArrayKey(LiteralTree tree) {
    Tree parent = tree.getParent();
    if (parent == null) {
      // Defensive, should not happen
      return false;
    }

    // Check if the literal is a key in an array pair: ['key' => 'value']
    if (parent.is(Kind.ARRAY_PAIR)) {
      ArrayPairTree arrayPair = (ArrayPairTree) parent;
      return tree.equals(arrayPair.key());
    }

    // Check if the literal is an offset in array access: $array['key']
    if (parent.is(Kind.ARRAY_ACCESS)) {
      ArrayAccessTree arrayAccess = (ArrayAccessTree) parent;
      return tree.equals(arrayAccess.offset());
    }

    return false;
  }

}
