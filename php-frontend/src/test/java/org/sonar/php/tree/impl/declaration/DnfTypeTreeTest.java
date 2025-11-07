/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.BuiltInTypeTree;
import org.sonar.plugins.php.api.tree.declaration.DnfIntersectionTypeTree;
import org.sonar.plugins.php.api.tree.declaration.DnfTypeTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class DnfTypeTreeTest extends PHPTreeModelTest {

  @Test
  void shouldParseDnfType() {
    DnfTypeTree dnfType = parse("int|null|(A&B)", PHPLexicalGrammar.DNF_TYPE);
    assertThat(dnfType.isSimple()).isFalse();
    assertThat(dnfType.types()).hasSize(3);
    var elementsAndSeparators = dnfType.types().elementsAndSeparators();

    // Checking individual elements of int|null|(A&B)
    var element1 = (TypeTree) elementsAndSeparators.next();
    assertThat(element1.is(Tree.Kind.TYPE)).isTrue();
    assertThat(element1.typeName().is(Tree.Kind.BUILT_IN_TYPE)).isTrue();
    assertThat(((BuiltInTypeTree) element1.typeName()).token().text()).isEqualTo("int");

    var separator1 = (SyntaxToken) elementsAndSeparators.next();
    assertThat(separator1.is(Tree.Kind.TOKEN)).isTrue();
    assertThat(separator1.text()).isEqualTo("|");

    var element2 = (TypeTree) elementsAndSeparators.next();
    assertThat(element2.is(Tree.Kind.TYPE)).isTrue();
    assertThat(element2.typeName().is(Tree.Kind.NAMESPACE_NAME)).isTrue();
    assertThat(((NamespaceNameTree) element2.typeName()).name().text()).isEqualTo("null");

    var separator2 = (SyntaxToken) elementsAndSeparators.next();
    assertThat(separator2.is(Tree.Kind.TOKEN)).isTrue();
    assertThat(separator2.text()).isEqualTo("|");

    var element3 = (DnfIntersectionTypeTree) elementsAndSeparators.next();
    assertThat(element3.is(Tree.Kind.DNF_INTERSECTION_TYPE)).isTrue();
    assertThat(element3.isSimple()).isFalse();
    assertThat(element3.openParenthesisToken().text()).isEqualTo("(");
    assertThat(element3.types()).hasSize(2);
    assertThat(element3.closedParenthesisToken().text()).isEqualTo(")");

    // Checking individual elements of A&B
    var dnfIntersectionElements = element3.types().elementsAndSeparators();

    var dnfIntersectionElement1 = (TypeTree) dnfIntersectionElements.next();
    assertThat(dnfIntersectionElement1.typeName().is(Tree.Kind.NAMESPACE_NAME)).isTrue();
    assertThat(((NamespaceNameTree) dnfIntersectionElement1.typeName()).name().text()).isEqualTo("A");

    var dnfIntersectionSeparator = (SyntaxToken) dnfIntersectionElements.next();
    assertThat(dnfIntersectionSeparator.is(Tree.Kind.TOKEN)).isTrue();
    assertThat(dnfIntersectionSeparator.text()).isEqualTo("&");

    var dnfIntersectionElement2 = (TypeTree) dnfIntersectionElements.next();
    assertThat(dnfIntersectionElement2.typeName().is(Tree.Kind.NAMESPACE_NAME)).isTrue();
    assertThat(((NamespaceNameTree) dnfIntersectionElement2.typeName()).name().text()).isEqualTo("B");
  }
}
