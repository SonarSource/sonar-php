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
package org.sonar.php.checks.utils;

import com.sonar.sslr.api.typed.ActionParser;
import com.sonarsource.checks.coverage.UtilityClass;
import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;

public class SyntacticEquivalenceTest {

  private ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);

  @Test
  public void utility_class() throws Exception {
    UtilityClass.assertGoodPractice(SyntacticEquivalence.class);
  }

  @Test
  public void areSyntacticallyEquivalent() throws Exception {
    assertThat(areSyntacticallyEquivalent(null, null)).isTrue();
    assertThat(areSyntacticallyEquivalent(null, "1;")).isFalse();
    assertThat(areSyntacticallyEquivalent("$x;", null)).isFalse();
    assertThat(areSyntacticallyEquivalent("$x;", "1;")).isFalse();
    assertThat(areSyntacticallyEquivalent("$x;", "$x;")).isTrue();
    assertThat(areSyntacticallyEquivalent("$x;", "$y;")).isFalse();
    assertThat(areSyntacticallyEquivalent("switch ($a) {case 1:}", "switch ($a) {case 1:}")).isTrue();
    assertThat(areSyntacticallyEquivalent("switch ($a) {case 1:}", "switch ($a) {case 1: case2:}")).isFalse();
    assertThat(areSyntacticallyEquivalent("switch ($a) {case 1: case2:}", "switch ($a) {case 1:}")).isFalse();
  }

  private boolean areSyntacticallyEquivalent(String toParse1, String toParse2) throws Exception {
    return SyntacticEquivalence.areSyntacticallyEquivalent(parse(toParse1), parse(toParse2));
  }

  private Tree parse(String toParse) {
    return toParse == null ? null : parser.parse(toParse);
  }

}
