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
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

public class ScriptTreeTest extends PHPTreeModelTest {

  @Test
  public void script_without_statement() throws Exception {
    ScriptTree tree = parse("<?php", PHPLexicalGrammar.SCRIPT);

    assertThat(tree.is(Kind.SCRIPT)).isTrue();
    assertThat(tree.fileOpeningTagToken().text()).isEqualTo("<?php");
    assertThat(tree.statements()).hasSize(0);
  }

  @Test
  public void script_asp_style() throws Exception {
    ScriptTree tree = parse("<% $a; %> <br/>", PHPLexicalGrammar.SCRIPT);

    assertThat(tree.is(Kind.SCRIPT)).isTrue();
    assertThat(tree.fileOpeningTagToken().text()).isEqualTo("<%");
    assertThat(tree.statements()).hasSize(2);

    tree = parse("<%  %>", PHPLexicalGrammar.SCRIPT);

    assertThat(tree.fileOpeningTagToken().text()).isEqualTo("<%");
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(Kind.INLINE_HTML)).isTrue();
    
    tree = parse("<% ", PHPLexicalGrammar.SCRIPT);

    assertThat(tree.fileOpeningTagToken().text()).isEqualTo("<%");
    assertThat(tree.statements()).hasSize(0);
  }

  @Test
  public void script_with_statement() throws Exception {
    ScriptTree tree = parse("<?php $a;", PHPLexicalGrammar.SCRIPT);

    assertThat(tree.is(Kind.SCRIPT)).isTrue();
    assertThat(tree.fileOpeningTagToken().text()).isEqualTo("<?php");
    assertThat(tree.statements()).hasSize(1);
    assertThat(expressionToString(tree.statements().get(0))).isEqualTo("$a;");
  }

}
