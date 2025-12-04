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
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class ForEachStatementTreeTest extends PHPTreeModelTest {

  @Test
  void standardSyntax() {
    ForEachStatementTree tree = parse("foreach ($a as $b) {}", PHPLexicalGrammar.FOREACH_STATEMENT);

    assertThat(tree.is(Kind.FOREACH_STATEMENT)).isTrue();
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.expression().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(tree.asToken().text()).isEqualTo("as");
    assertThat(tree.value().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(tree.key()).isNull();
    assertThat(tree.doubleArrowToken()).isNull();
    assertThat(tree.colonToken()).isNull();
    assertThat(tree.endforeachToken()).isNull();
    assertThat(tree.eosToken()).isNull();
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(Kind.BLOCK)).isTrue();
    assertThat(tree.eosToken()).isNull();
  }

  @Test
  void alternativeSyntax() {
    ForEachStatementTree tree = parse("foreach ($a as $b) : {} {} endforeach ;", PHPLexicalGrammar.FOREACH_STATEMENT);

    assertThat(tree.is(Kind.ALTERNATIVE_FOREACH_STATEMENT)).isTrue();
    assertThat(tree.openParenthesisToken().text()).isEqualTo("(");
    assertThat(tree.expression().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(tree.asToken().text()).isEqualTo("as");
    assertThat(tree.value().is(Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(tree.key()).isNull();
    assertThat(tree.doubleArrowToken()).isNull();
    assertThat(tree.closeParenthesisToken().text()).isEqualTo(")");
    assertThat(tree.colonToken().text()).isEqualTo(":");
    assertThat(tree.endforeachToken().text()).isEqualTo("endforeach");
    assertThat(tree.eosToken().text()).isEqualTo(";");
    assertThat(tree.statements()).hasSize(2);
    assertThat(tree.eosToken().text()).isEqualTo(";");
  }

  @Test
  void syntaxWithKey() {
    ForEachStatementTree tree = parse("foreach ($a as $key => $b) {}", PHPLexicalGrammar.FOREACH_STATEMENT);
    assertThat(tree.key()).isNotNull();
    assertThat(tree.doubleArrowToken()).isNotNull();
  }

}
