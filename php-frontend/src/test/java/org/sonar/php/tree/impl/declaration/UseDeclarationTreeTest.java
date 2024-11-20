/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.tree.impl.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;

import static org.assertj.core.api.Assertions.assertThat;

class UseDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  void withoutAlias() {
    UseClauseTree tree = parse("\\ns1\\ns2\\name", PHPLexicalGrammar.USE_CLAUSE);
    assertThat(tree.is(Kind.USE_CLAUSE)).isTrue();
    assertThat(tree.namespaceName().is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(tree.asToken()).isNull();
    assertThat(tree.alias()).isNull();
  }

  @Test
  void withAlias() {
    UseClauseTree tree = parse("\\ns1\\ns2\\name as alias1", PHPLexicalGrammar.USE_CLAUSE);
    assertThat(tree.is(Kind.USE_CLAUSE)).isTrue();
    assertThat(tree.namespaceName().is(Kind.NAMESPACE_NAME)).isTrue();
    assertThat(tree.asToken().text()).isEqualTo("as");
    assertThat(tree.alias().text()).isEqualTo("alias1");
  }

}
