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
package org.sonar.php.tree.impl.statement;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class WhileStatementTreeTest extends PHPTreeModelTest {

  @Test
  void standardSyntax() {
    WhileStatementTree tree = parse("while ($a) {}", PHPLexicalGrammar.WHILE_STATEMENT);

    assertThat(tree.is(Kind.WHILE_STATEMENT)).isTrue();
    assertThat(tree.whileToken().text()).isEqualTo("while");
    assertThat(tree.condition().is(Kind.PARENTHESISED_EXPRESSION)).isTrue();
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(Kind.BLOCK)).isTrue();

    assertThat(tree.colonToken()).isNull();
    assertThat(tree.endWhileToken()).isNull();
    assertThat(tree.eosToken()).isNull();
  }

  @Test
  void alternativeSyntax() {
    WhileStatementTree tree = parse("while ($a) : endwhile ;", PHPLexicalGrammar.WHILE_STATEMENT);

    assertThat(tree.is(Kind.ALTERNATIVE_WHILE_STATEMENT)).isTrue();
    assertThat(tree.whileToken().text()).isEqualTo("while");
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.colonToken().text()).isEqualTo(":");
    assertThat(tree.endWhileToken().text()).isEqualTo("endwhile");
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

}
