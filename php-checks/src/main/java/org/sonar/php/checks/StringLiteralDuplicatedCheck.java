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

@Rule(key = "S1192")
public class StringLiteralDuplicatedCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Define a constant instead of duplicating this literal \"%s\" %s times.";
  private static final String SECONDARY_MESSAGE = "Duplication.";

  private static final Pattern ALLOWED_DUPLICATED_LITERALS = Pattern.compile("^[a-zA-Z][_\\-\\w]+$");

  private final Map<String, LiteralTree> firstOccurrenceTrees = new HashMap<>();
  private final Map<String, List<LiteralTree>> sameLiteralOccurrences = new HashMap<>();

  public static final int THRESHOLD_DEFAULT = 3;
  public static final int MINIMAL_LITERAL_LENGTH_DEFAULT = 5;

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
