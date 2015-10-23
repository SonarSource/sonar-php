/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleLinearWithOffsetRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.Map;

@Rule(
  key = StringLiteralDuplicatedCheck.KEY,
  name = "String literals should not be duplicated",
  priority = Priority.MINOR,
  tags = {Tags.DESIGN})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.DATA_RELIABILITY)
@SqaleLinearWithOffsetRemediation(coeff = "2min", offset = "2min", effortToFixDescription = "per duplicate instance")
public class StringLiteralDuplicatedCheck extends PHPVisitorCheck {

  public static final String KEY = "S1192";
  private static final String MESSAGE = "Define a constant instead of duplicating this literal \"%s\" %s times.";

  private static final Integer MINIMAL_LITERAL_LENGTH = 5;

  private final Map<String, Integer> firstOccurrenceLines = Maps.newHashMap();
  private final Map<String, Integer> sameLiteralOccurrencesCounter = Maps.newHashMap();

  public static final int DEFAULT = 3;

  @RuleProperty(
    key = "threshold",
    defaultValue = "" + DEFAULT)
  int threshold = DEFAULT;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    firstOccurrenceLines.clear();
    sameLiteralOccurrencesCounter.clear();

    super.visitCompilationUnit(tree);

    finish();
  }

  private void finish() {
    for (Map.Entry<String, Integer> literalOccurrences : sameLiteralOccurrencesCounter.entrySet()) {
      Integer occurrences = literalOccurrences.getValue();

      if (occurrences >= threshold) {
        String literal = literalOccurrences.getKey();
        String message = String.format(MESSAGE, literal, occurrences);
        context().newIssue(KEY, message).line(firstOccurrenceLines.get(literal)).cost(occurrences);
      }
    }
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Kind.REGULAR_STRING_LITERAL)) {
      String literal = tree.value();
      String value = StringUtils.substring(literal, 1, literal.length() - 1);

      if (value.length() >= MINIMAL_LITERAL_LENGTH) {

        if (!sameLiteralOccurrencesCounter.containsKey(value)) {
          sameLiteralOccurrencesCounter.put(value, 1);
          firstOccurrenceLines.put(value, ((PHPTree) tree).getLine());

        } else {
          int occurrences = sameLiteralOccurrencesCounter.get(value);
          sameLiteralOccurrencesCounter.put(value, occurrences + 1);
        }
      }
    }
  }

}
