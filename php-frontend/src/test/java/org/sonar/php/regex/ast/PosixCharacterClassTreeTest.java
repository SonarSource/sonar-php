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


import java.util.List;
import org.junit.Test;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassElementTree;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassTree;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassUnionTree;
import org.sonarsource.analyzer.commons.regex.ast.CharacterRangeTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexBaseVisitor;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexVisitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.sonar.php.regex.RegexParserTestUtils.assertKind;
import static org.sonar.php.regex.RegexParserTestUtils.assertSuccessfulParse;
import static org.sonar.php.regex.RegexParserTestUtils.assertType;

public class PosixCharacterClassTreeTest {

  private static final RegexBaseVisitor BASE_VISITOR = new RegexBaseVisitor();

  @Test
  public void posixCharacterClassElements() {
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

  @Test
  public void posixCharacterClassElements_within_union() {
    RegexTree tree = assertSuccessfulParse("'/[[:alnum:]0-9]/'");
    CharacterClassTree characterClass = assertType(CharacterClassTree.class, tree);
    CharacterClassUnionTree characterClassUnion = assertType(CharacterClassUnionTree.class, characterClass.getContents());

    List<CharacterClassElementTree> classElementTrees = characterClassUnion.getCharacterClasses();
    assertThat(classElementTrees).hasSize(2);
    assertType(PosixCharacterClassElementTree.class, classElementTrees.get(0));
    assertType(CharacterRangeTree.class, classElementTrees.get(1));
  }

  @Test
  public void nonPosixCharacterClassElements() {
    assertNonPosixClass("'/[[:alpha]]/'");
    assertNonPosixClass("'/[[alpha]]/'");
  }

  private void assertPosixClass(String regex, String expectedProperty, boolean isNegation) {
    RegexTree tree = assertSuccessfulParse(regex);
    assertPosixClass(tree, expectedProperty, isNegation);
  }

  private void assertPosixClass(RegexSyntaxElement tree, String expectedProperty, boolean isNegation) {
    CharacterClassTree characterClass = assertType(CharacterClassTree.class, tree);
    PosixCharacterClassElementTree posixCharacterClassElement = assertType(PosixCharacterClassElementTree.class, characterClass.getContents());
    assertKind(CharacterClassElementTree.Kind.POSIX_CLASS, posixCharacterClassElement);
    assertThat(posixCharacterClassElement.property()).isNotNull().isEqualTo(expectedProperty);
    assertThat(posixCharacterClassElement.activeFlags().isEmpty()).isTrue();

    CharacterClassElementTree classElementTree = spy(posixCharacterClassElement);
    BASE_VISITOR.visitInCharClass(classElementTree);
    verify(classElementTree).accept(BASE_VISITOR);
  }

  private void assertNonPosixClass(String regex) {
    RegexTree tree = assertSuccessfulParse(regex);
    assertNonPosixClass(tree);
  }

  private void assertNonPosixClass(RegexSyntaxElement tree) {
    assertThat(tree).isInstanceOf(CharacterClassTree.class);
    CharacterClassElementTree classElementTree = ((CharacterClassTree)tree).getContents();
    assertThat(classElementTree).isNotInstanceOf(PosixCharacterClassElementTree.class);
  }
}
