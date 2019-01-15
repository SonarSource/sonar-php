/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.php.checks.utils.namespace;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.declaration.NamespaceNameTreeImpl;
import org.sonar.php.tree.impl.expression.NameIdentifierTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.checks.utils.namespace.QualifiedName.create;
import static org.sonar.php.checks.utils.namespace.QualifiedName.qualifiedName;

public class QualifiedNameTest {

  @Test(expected = IllegalStateException.class)
  public void illegal_qualified_name() {
    create();
  }

  @Test
  public void simple_create() {
    QualifiedName qualifiedName1 = create("A", "B", "C");
    assertThat(qualifiedName1.asList()).containsExactly("A", "B", "C");
    assertThat(qualifiedName1.firstPart()).isEqualTo("A");
    assertThat(qualifiedName1).isEqualTo(qualifiedName1);
    assertThat(qualifiedName1).isEqualTo(create("A", "B", "C"));
    assertThat(qualifiedName1.hashCode()).isEqualTo(create("A", "B", "C").hashCode());
    assertThat(qualifiedName1).isNotEqualTo(null);

    QualifiedName qualifiedName2 = create("D");
    assertThat(qualifiedName2.asList()).containsExactly("D");
    assertThat(qualifiedName2.firstPart()).isEqualTo("D");
    assertThat(qualifiedName2).isNotEqualTo(qualifiedName1);
    assertThat(qualifiedName2).isNotEqualTo(qualifiedName2.asList());

    QualifiedName withOriginalName1 = qualifiedName1.withOriginalName(qualifiedName2);
    assertThat(withOriginalName1.asList()).containsExactly("D", "B", "C");
    assertThat(withOriginalName1).isEqualTo(create("D", "B", "C"));
    assertThat(withOriginalName1).isNotEqualTo(qualifiedName1);

    QualifiedName withOriginalName2 = qualifiedName2.withOriginalName(qualifiedName1);
    assertThat(withOriginalName2.asList()).containsExactly("A", "B", "C");
    assertThat(withOriginalName2).isEqualTo(qualifiedName1);
  }

  @Test
  public void create_from_qualified_names() {
    QualifiedName qualifiedName1 = create("A", "B");
    QualifiedName qualifiedName2 = create("C", "D");

    QualifiedName qualifiedName3 = create(qualifiedName1, qualifiedName2);
    assertThat(qualifiedName3).isEqualTo(create("A", "B", "C", "D"));
  }

  @Test
  public void qualified_name() {
    QualifiedName qualifiedName1 = create("A", "B");
    QualifiedName qualifiedName2 = qualifiedName("A\\B");

    assertThat(qualifiedName1).isEqualTo(qualifiedName2);
  }

  @Test
  public void create_from_tree() {
    NameIdentifierTree identifierTree1 = new NameIdentifierTreeImpl(token("A"));
    NameIdentifierTree identifierTree2 = new NameIdentifierTreeImpl(token("B"));
    NameIdentifierTree identifierTree3 = new NameIdentifierTreeImpl(token("C"));
    List<NameIdentifierTree> list = Arrays.asList(identifierTree1, identifierTree2);
    List<SyntaxToken> separatorList = Arrays.asList(token("\\"), token("\\"));
    SeparatedListImpl<NameIdentifierTree> namespaces = new SeparatedListImpl<>(list, separatorList);
    NamespaceNameTree namespaceNameTree = new NamespaceNameTreeImpl(null, namespaces, identifierTree3);

    QualifiedName qualifiedName1 = create(namespaceNameTree);
    assertThat(qualifiedName1).isEqualTo(create("A", "B", "C"));
  }

  @Test
  public void equals_corner_case() {
    assertThat(create("A", "B", "X")).isNotEqualTo(create("A", "B", "C"));
  }

  private static InternalSyntaxToken token(String text) {
    return new InternalSyntaxToken(0, 0, text, null, 0, false);
  }

}
