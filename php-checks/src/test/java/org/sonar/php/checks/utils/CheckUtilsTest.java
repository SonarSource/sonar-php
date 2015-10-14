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
package org.sonar.php.checks.utils;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.typed.ActionParser;
import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.fest.assertions.Assertions.assertThat;

public class CheckUtilsTest {

  private ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT, Charsets.UTF_8);

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
    return CheckUtils.areSyntacticallyEquivalent(parse(toParse1), parse(toParse2));
  }

  @Test
  public void asString() {
    ActionParser<Tree> listParser = PHPParserBuilder.createParser(Kind.LIST_EXPRESSION, Charsets.UTF_8);
    assertThat(CheckUtils.asString(listParser.parse("list(a, ,)"))).isEqualTo("list(a, ,)");
    assertThat(CheckUtils.asString(listParser.parse("list()"))).isEqualTo("list()");
    assertThat(CheckUtils.asString(listParser.parse("list(a, b)"))).isEqualTo("list(a, b)");
  }

  private Tree parse(String toParse) {
    return toParse == null ? null : parser.parse(toParse);
  }

}
