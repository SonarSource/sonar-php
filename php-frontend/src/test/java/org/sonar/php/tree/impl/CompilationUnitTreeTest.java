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
package org.sonar.php.tree.impl;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

public class CompilationUnitTreeTest extends PHPTreeModelTest {

  @Test
  public void test() throws Exception {
    CompilationUnitTree tree = parse("<?php $a;", PHPLexicalGrammar.COMPILATION_UNIT);

    assertThat(tree.is(Kind.COMPILATION_UNIT)).isTrue();
    assertThat(tree.script().fileOpeningTagToken().text()).isEqualTo("<?php");
    assertThat(tree.script().statements()).hasSize(1);
    assertThat(tree.eofToken().line()).isEqualTo(1);
  }

  @Test
  public void without_statement() throws Exception {
    CompilationUnitTree tree = parse("<?php", PHPLexicalGrammar.COMPILATION_UNIT);

    assertThat(tree.is(Kind.COMPILATION_UNIT)).isTrue();
    assertThat(tree.script().fileOpeningTagToken().text()).isEqualTo("<?php");
    assertThat(tree.script().statements()).isEmpty();
  }

  @Test
  public void empty() throws Exception {
    CompilationUnitTree tree = parse("", PHPLexicalGrammar.COMPILATION_UNIT);

    assertThat(tree.is(Kind.COMPILATION_UNIT)).isTrue();
    assertThat(tree.script()).isNull();
    assertThat(tree.eofToken().line()).isEqualTo(1);
  }

  @Test
  public void without_php() throws Exception {
    CompilationUnitTree tree = parse("hello world\n", PHPLexicalGrammar.COMPILATION_UNIT);

    assertThat(tree.is(Kind.COMPILATION_UNIT)).isTrue();
    assertThat(tree.script().fileOpeningTagToken().text()).isEqualTo("hello world\n");
    assertThat(tree.eofToken().line()).isEqualTo(2);
  }

}
