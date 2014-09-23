/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
import com.sonar.sslr.api.AstNode;
import org.apache.commons.lang.StringUtils;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.Map;

@Rule(
  key = "S1192",
  priority = Priority.MINOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
public class StringLiteralDuplicatedCheck extends SquidCheck<LexerlessGrammar> {

  private static final Integer MINIMAL_LITERAL_LENGTH = 5;

  private final Map<String, Integer> firstOccurrence = Maps.newHashMap();
  private final Map<String, Integer> literalsOccurrences = Maps.newHashMap();

  public static final int DEFAULT = 3;

  @RuleProperty(
    key = "threshold",
    defaultValue = "" + DEFAULT)
  int threshold = DEFAULT;

  @Override
  public void init() {
    subscribeTo(PHPGrammar.STRING_LITERAL);
  }

  @Override
  public void visitFile(AstNode node) {
    firstOccurrence.clear();
    literalsOccurrences.clear();
  }

  @Override
  public void visitNode(AstNode node) {
    String literal = node.getTokenOriginalValue();
    visitOccurrence(StringUtils.substring(literal, 1, literal.length() - 1), node.getTokenLine());
  }

  @Override
  public void leaveFile(AstNode node) {
    for (Map.Entry<String, Integer> literalOccurrences : literalsOccurrences.entrySet()) {
      Integer occurrences = literalOccurrences.getValue();

      if (occurrences >= threshold) {
        String literal = literalOccurrences.getKey();

        getContext().createLineViolation(this, "Define a constant instead of duplicating this literal \"{0}\" {1} times.", firstOccurrence.get(literal),
          literal, occurrences);
      }
    }
  }

  private void visitOccurrence(String literal, int line) {
    if (literal.length() >= MINIMAL_LITERAL_LENGTH) {
      if (!firstOccurrence.containsKey(literal)) {
        firstOccurrence.put(literal, line);
        literalsOccurrences.put(literal, 1);
      } else {
        int occurrences = literalsOccurrences.get(literal);
        literalsOccurrences.put(literal, occurrences + 1);
      }
    }
  }

}
