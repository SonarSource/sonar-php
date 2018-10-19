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
package org.sonar.php.tree;

import java.util.EnumSet;
import org.junit.Test;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.impl.CompilationUnitTreeImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.tree.TreeUtils.findAncestorWithKind;
import static org.sonar.php.tree.TreeUtils.isDescendant;

public class TreeUtilsTest {

  @Test
  public void test_isDescendant() {
    Tree tree = PHPParserBuilder.createParser().parse("<?= for(;;) {} ?>");
    StatementTree statementTree = ((CompilationUnitTree) tree).script().statements().get(0);
    assertThat(isDescendant(statementTree, tree)).isTrue();
    assertThat(isDescendant(tree, statementTree)).isFalse();

    assertThat(isDescendant(tree, tree)).isTrue();
    assertThat(isDescendant(statementTree, statementTree)).isTrue();

  }

  @Test
  public void test_findAncestorWithKind() {
    Tree tree = PHPParserBuilder.createParser().parse("<?= function foo() {for(;;) {} } ?>");
    FunctionDeclarationTree func = (FunctionDeclarationTree) ((CompilationUnitTreeImpl) tree).script().statements().get(0);
    StatementTree statementTree = func.body().statements().get(0);
    assertThat(findAncestorWithKind(statementTree, EnumSet.of(Tree.Kind.FUNCTION_DECLARATION))).isEqualTo(func);
    assertThat(findAncestorWithKind(statementTree, EnumSet.of(Tree.Kind.SCRIPT, Tree.Kind.FUNCTION_DECLARATION))).isEqualTo(func);
    assertThat(findAncestorWithKind(statementTree, singletonList(Tree.Kind.WHILE_STATEMENT))).isNull();

    assertThat(findAncestorWithKind(func, singletonList(Tree.Kind.FUNCTION_DECLARATION))).isEqualTo(func);
  }

}
