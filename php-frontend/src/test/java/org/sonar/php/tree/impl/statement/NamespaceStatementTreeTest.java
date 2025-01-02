/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class NamespaceStatementTreeTest extends PHPTreeModelTest {

  @Test
  void standardSyntax() {
    NamespaceStatementTree tree = parse("namespace NS;", PHPLexicalGrammar.NAMESPACE_STATEMENT);

    assertThat(tree.is(Kind.NAMESPACE_STATEMENT)).isTrue();
    assertThat(tree.namespaceToken().text()).isEqualTo("namespace");
    assertThat(expressionToString(tree.namespaceName())).isEqualTo("NS");
    assertThat(tree.eosToken().text()).isEqualTo(";");

    assertThat(tree.statements()).isEmpty();
    assertThat(tree.openCurlyBrace()).isNull();
    assertThat(tree.closeCurlyBrace()).isNull();
  }

  @Test
  void blockSyntax() {
    NamespaceStatementTree tree = parse("namespace { $a; }", PHPLexicalGrammar.NAMESPACE_STATEMENT);

    assertThat(tree.is(Kind.NAMESPACE_STATEMENT)).isTrue();
    assertThat(tree.namespaceName()).isNull();

    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.openCurlyBrace()).isNotNull();
    assertThat(tree.closeCurlyBrace()).isNotNull();
  }

}
