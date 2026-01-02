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
package org.sonar.php.tree.impl.statement;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class ForStatementTreeTest extends PHPTreeModelTest {

  @Test
  void standardSyntax() {
    ForStatementTree tree = parse("for ($a; ; $c, $b) {}", PHPLexicalGrammar.FOR_STATEMENT);

    assertThat(tree.is(Kind.FOR_STATEMENT)).isTrue();
    assertThat(tree.forToken().text()).isEqualTo("for");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.init()).hasSize(1);
    assertThat(tree.condition()).isEmpty();
    assertThat(tree.update()).hasSize(2);
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(tree.colonToken()).isNull();
    assertThat(tree.endforToken()).isNull();
    assertThat(tree.eosToken()).isNull();
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(Kind.BLOCK)).isTrue();
    assertThat(tree.eosToken()).isNull();
  }

  @Test
  void alternativeSyntax() {
    ForStatementTree tree = parse("for (; ;) : endfor ;", PHPLexicalGrammar.FOR_STATEMENT);

    assertThat(tree.is(Kind.ALTERNATIVE_FOR_STATEMENT)).isTrue();
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.init()).isEmpty();
    assertThat(tree.condition()).isEmpty();
    assertThat(tree.update()).isEmpty();
    assertThat(tree.colonToken()).isNotNull();
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(tree.endforToken()).isNotNull();
    assertThat(tree.eosToken()).isNotNull();
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }
}
