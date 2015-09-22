/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.impl.declaration;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.UseDeclarationsTree;

public class UseDeclarationsTreeTest extends PHPTreeModelTest {

  @Test
  public void single_declaration() throws Exception {
    UseDeclarationsTree tree = parse("use \\ns1\\ns2\\name;", PHPLexicalGrammar.USE_DECLARATIONS);
    assertThat(tree.is(Kind.USE_DECLARATIONS)).isTrue();
    assertThat(tree.useTypeToken()).isNull();
    assertThat(tree.declarations()).hasSize(1);
  }

  @Test
  public void multiple_declarations() throws Exception {
    UseDeclarationsTree tree = parse("use \\ns1\\ns2\\name, \\ns1\\ns2\\name2;", PHPLexicalGrammar.USE_DECLARATIONS);
    assertThat(tree.is(Kind.USE_DECLARATIONS)).isTrue();
    assertThat(tree.useTypeToken()).isNull();
    assertThat(tree.declarations()).hasSize(2);
  }
  
  @Test
  public void const_token() throws Exception {
    UseDeclarationsTree tree = parse("use const \\ns1\\ns2\\name;", PHPLexicalGrammar.USE_DECLARATIONS);
    assertThat(tree.useTypeToken().text()).isEqualTo("const");
  }

}
