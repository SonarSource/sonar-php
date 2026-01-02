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
import org.sonar.plugins.php.api.tree.statement.DeclareStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class DeclareStatementTreeTest extends PHPTreeModelTest {

  @Test
  void shortSyntax() {
    DeclareStatementTree tree = parse("declare (a);", PHPLexicalGrammar.DECLARE_STATEMENT);

    assertThat(tree.is(Kind.DECLARE_STATEMENT)).isTrue();
    assertThat(tree.declareToken().text()).isEqualTo("declare");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.directives()).hasSize(1);
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(tree.colonToken()).isNull();
    assertThat(tree.endDeclareToken()).isNull();
    assertThat(tree.eosToken()).isNotNull();
    assertThat(tree.statements()).isEmpty();
  }

  @Test
  void oneStatementSyntax() {
    DeclareStatementTree tree = parse("declare (a = $a, b = $b) {}", PHPLexicalGrammar.DECLARE_STATEMENT);

    assertThat(tree.is(Kind.DECLARE_STATEMENT)).isTrue();
    assertThat(tree.declareToken().text()).isEqualTo("declare");
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.directives()).hasSize(2);
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(tree.colonToken()).isNull();
    assertThat(tree.endDeclareToken()).isNull();
    assertThat(tree.eosToken()).isNull();
    assertThat(tree.statements()).hasSize(1);
  }

  @Test
  void alternativeSyntax() {
    DeclareStatementTree tree = parse("declare (a) : {} {} enddeclare ;", PHPLexicalGrammar.DECLARE_STATEMENT);

    assertThat(tree.is(Kind.DECLARE_STATEMENT)).isTrue();
    assertThat(tree.directives()).hasSize(1);
    assertThat(tree.colonToken()).isNotNull();
    assertThat(tree.endDeclareToken()).isNotNull();
    assertThat(tree.eosToken()).isNotNull();
    assertThat(tree.statements()).hasSize(2);
  }

}
