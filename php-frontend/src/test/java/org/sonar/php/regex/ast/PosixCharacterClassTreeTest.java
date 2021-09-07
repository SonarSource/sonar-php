/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.regex.ast;


import org.junit.Test;
import org.sonarsource.analyzer.commons.regex.ast.AutomatonState;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassElementTree;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassTree;
import org.sonarsource.analyzer.commons.regex.ast.EscapedCharacterClassTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.regex.RegexParserTestUtils.assertKind;
import static org.sonar.php.regex.RegexParserTestUtils.assertSuccessfulParse;

public class PosixCharacterClassTreeTest {

  @Test
  public void posixCharacterClasses() {
    assertPosixClass("'/[[:alnum:]]/'", "alnum", false);
    assertPosixClass("'/[[:alpha:]]/'", "alpha", false);
    assertPosixClass("'/[[:ascii:]]/'", "ascii", false);
    assertPosixClass("'/[[:cntrl:]]/'", "cntrl", false);
    assertPosixClass("'/[[:digit:]]/'", "digit", false);
    assertPosixClass("'/[[:graph:]]/'", "graph", false);
    assertPosixClass("'/[[:lower:]]/'", "lower", false);
    assertPosixClass("'/[[:print:]]/'", "print", false);
    assertPosixClass("'/[[:punct:]]/'", "punct", false);
    assertPosixClass("'/[[:space:]]/'", "space", false);
    assertPosixClass("'/[[:upper:]]/'", "upper", false);
    assertPosixClass("'/[[:word:]]/'", "word", false);
    assertPosixClass("'/[[:xdigit:]]/'", "xdigit", false);
    assertPosixClass("'/[[:<:]]/'", "<", false);
    assertPosixClass("'/[[:>:]]/'", ">", false);

    assertPosixClass("'/[[:^alnum:]]/'", "alnum", true);
    assertPosixClass("'/[[:^alpha:]]/'", "alpha", true);
    assertPosixClass("'/[[:^ascii:]]/'", "ascii", true);
    assertPosixClass("'/[[:^cntrl:]]/'", "cntrl", true);
    assertPosixClass("'/[[:^digit:]]/'", "digit", true);
    assertPosixClass("'/[[:^graph:]]/'", "graph", true);
    assertPosixClass("'/[[:^lower:]]/'", "lower", true);
    assertPosixClass("'/[[:^print:]]/'", "print", true);
    assertPosixClass("'/[[:^punct:]]/'", "punct", true);
    assertPosixClass("'/[[:^space:]]/'", "space", true);
    assertPosixClass("'/[[:^upper:]]/'", "upper", true);
    assertPosixClass("'/[[:^word:]]/'", "word", true);
    assertPosixClass("'/[[:^xdigit:]]/'", "xdigit", true);
    assertPosixClass("'/[[:^<:]]/'", "<", true);
    assertPosixClass("'/[[:^>:]]/'", ">", true);
  }

  private void assertPosixClass(String regex, String expectedProperty, boolean isNegation) {
    RegexTree tree = assertSuccessfulParse(regex);
    assertPosixClass(tree, expectedProperty, isNegation);
  }

  private void assertPosixClass(RegexSyntaxElement tree, String expectedProperty, boolean isNegation) {
    assertThat(tree).isInstanceOf(CharacterClassTree.class);
    CharacterClassElementTree classElementTree = ((CharacterClassTree)tree).getContents();
    assertThat(classElementTree).isInstanceOf(PosixCharacterClassTree.class);
    PosixCharacterClassTree posixCharacterClass = (PosixCharacterClassTree) classElementTree;
    assertKind(CharacterClassElementTree.Kind.POSIX_CLASS, posixCharacterClass);

    assertThat(posixCharacterClass.property()).isNotNull().isEqualTo(expectedProperty);
    assertThat(posixCharacterClass.isNegation()).isEqualTo(isNegation);;
  }
}
