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
package org.sonar.php.checks;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = StringLiteralDuplicatedCheck.KEY)
public class StringLiteralDuplicatedCheck extends PHPVisitorCheck {

  public static final String KEY = "S1192";
  private static final String MESSAGE = "Define a constant instead of duplicating this literal \"%s\" %s times.";

  private static final Integer MINIMAL_LITERAL_LENGTH = 5;

  private final Map<String, LiteralTree> firstOccurrenceTrees = Maps.newHashMap();
  private final Map<String, List<LiteralTree>> sameLiteralOccurrences = Maps.newHashMap();

  public static final int DEFAULT = 3;

  @RuleProperty(
    key = "threshold",
    defaultValue = "" + DEFAULT)
  int threshold = DEFAULT;

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
        PreciseIssue issue = context().newIssue(this, firstOccurrenceTrees.get(literal), message).cost(occurrences.size());
        occurrences.forEach(occurrence -> issue.secondary(occurrence, null));
      }
    }
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Kind.REGULAR_STRING_LITERAL)) {
      String literal = tree.value();
      String value = StringUtils.substring(literal, 1, literal.length() - 1);

      if (value.length() >= MINIMAL_LITERAL_LENGTH) {

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
