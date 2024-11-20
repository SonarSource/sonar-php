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
package org.sonar.php.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

class CompilationUnitTreeTest extends PHPTreeModelTest {

  @Test
  void test() {
    CompilationUnitTree tree = parse("<?php $a;", PHPLexicalGrammar.COMPILATION_UNIT);

    assertThat(tree.is(Kind.COMPILATION_UNIT)).isTrue();
    assertThat(tree.script().fileOpeningTagToken().text()).isEqualTo("<?php");
    assertThat(tree.script().statements()).hasSize(1);
    assertThat(tree.eofToken().line()).isEqualTo(1);
  }

  @Test
  void withoutStatement() {
    CompilationUnitTree tree = parse("<?php", PHPLexicalGrammar.COMPILATION_UNIT);

    assertThat(tree.is(Kind.COMPILATION_UNIT)).isTrue();
    assertThat(tree.script().fileOpeningTagToken().text()).isEqualTo("<?php");
    assertThat(tree.script().statements()).isEmpty();
  }

  @Test
  void empty() {
    CompilationUnitTree tree = parse("", PHPLexicalGrammar.COMPILATION_UNIT);

    assertThat(tree.is(Kind.COMPILATION_UNIT)).isTrue();
    assertThat(tree.script()).isNull();
    assertThat(tree.eofToken().line()).isEqualTo(1);
  }

  @Test
  void withoutPhp() {
    CompilationUnitTree tree = parse("hello world\n", PHPLexicalGrammar.COMPILATION_UNIT);

    assertThat(tree.is(Kind.COMPILATION_UNIT)).isTrue();
    assertThat(tree.script().fileOpeningTagToken().text()).isEqualTo("hello world\n");
    assertThat(tree.eofToken().line()).isEqualTo(2);
  }

}
