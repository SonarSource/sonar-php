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
package org.sonar.php.tree.impl.lexical;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

public class InternalSyntaxTokenTest extends PHPTreeModelTest {

  @Test
  public void inline_html() throws Exception {
    InternalSyntaxToken tree = parse("?>", PHPLexicalGrammar.INLINE_HTML);

    assertThat(tree.is(Kind.INLINE_HTML_TOKEN)).isTrue();
    assertThat(tree.text()).isEqualTo("?>");
  }

  @Test
  public void token() throws Exception {
    InternalSyntaxToken tree = parse("foo", PHPLexicalGrammar.IDENTIFIER);

    assertThat(tree.is(Kind.TOKEN)).isTrue();
    assertThat(tree.text()).isEqualTo("foo");
  }

}
