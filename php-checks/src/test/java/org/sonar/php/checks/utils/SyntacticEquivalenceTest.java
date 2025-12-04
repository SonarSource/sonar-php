/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.checks.utils;

import com.sonar.sslr.api.typed.ActionParser;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonarsource.analyzer.commons.checks.coverage.UtilityClass;

import static org.assertj.core.api.Assertions.assertThat;

class SyntacticEquivalenceTest {

  private ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);

  @Test
  void utilityClass() throws Exception {
    UtilityClass.assertGoodPractice(SyntacticEquivalence.class);
  }

  @Test
  void areSyntacticallyEquivalent() throws Exception {
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

  private boolean areSyntacticallyEquivalent(@Nullable String toParse1, @Nullable String toParse2) {
    return SyntacticEquivalence.areSyntacticallyEquivalent(parse(toParse1), parse(toParse2));
  }

  @CheckForNull
  private Tree parse(@Nullable String toParse) {
    return toParse == null ? null : parser.parse(toParse);
  }

}
