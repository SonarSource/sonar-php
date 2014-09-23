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
package org.sonar.php.parser;

import com.sonar.sslr.api.Rule;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.sonar.sslr.tests.Assertions;

public class RuleTest {

  protected final LexerlessGrammar g = PHPGrammar.createGrammar();
  protected Rule rule;

  protected void setTestedRule(GrammarRuleKey ruleKey) {
    rule = g.rule(ruleKey);
    // FIXME: below
    // Needs to override because of the introduction of FILE_OPENING_TAG token, corresponding to the first PHP opening tag encountered
    // Needs optional because of recursive rule that will expect the opening tag token before each recursion otherwise.
    // rule.override(GrammarFunctions.Standard.opt(PHPTagsChannel.FILE_OPENING_TAG), ((RuleDefinition) rule).getExpression());
  }

  protected void matches(String input) {
    Assertions.assertThat(rule)
      .matches(input);
  }

  protected void notMatches(String input) {
    Assertions.assertThat(rule)
      .notMatches(input);
  }

}
