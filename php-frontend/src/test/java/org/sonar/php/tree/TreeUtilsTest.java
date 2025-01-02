/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.tree;

import com.sonar.sslr.api.typed.ActionParser;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.impl.CompilationUnitTreeImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.tree.TreeUtils.findAncestorWithKind;
import static org.sonar.php.tree.TreeUtils.hasAnnotation;
import static org.sonar.php.tree.TreeUtils.isDescendant;

class TreeUtilsTest {

  @Test
  void testIsDescendant() {
    Tree tree = PHPParserBuilder.createParser().parse("<?= for(;;) {} ?>");
    StatementTree statementTree = ((CompilationUnitTree) tree).script().statements().get(0);
    assertThat(isDescendant(statementTree, tree)).isTrue();
    assertThat(isDescendant(tree, statementTree)).isFalse();

    assertThat(isDescendant(tree, tree)).isTrue();
    assertThat(isDescendant(statementTree, statementTree)).isTrue();

  }

  @Test
  void testFindAncestorWithKind() {
    Tree tree = PHPParserBuilder.createParser().parse("<?= function foo() {for(;;) {} } ?>");
    FunctionDeclarationTree func = (FunctionDeclarationTree) ((CompilationUnitTreeImpl) tree).script().statements().get(0);
    StatementTree statementTree = func.body().statements().get(0);
    assertThat(findAncestorWithKind(statementTree, EnumSet.of(Tree.Kind.FUNCTION_DECLARATION))).isEqualTo(func);
    assertThat(findAncestorWithKind(statementTree, EnumSet.of(Tree.Kind.SCRIPT, Tree.Kind.FUNCTION_DECLARATION))).isEqualTo(func);
    assertThat(findAncestorWithKind(statementTree, singletonList(Tree.Kind.WHILE_STATEMENT))).isNull();

    assertThat(findAncestorWithKind(func, singletonList(Tree.Kind.FUNCTION_DECLARATION))).isEqualTo(func);
    assertThat(findAncestorWithKind(func, Tree.Kind.FUNCTION_DECLARATION)).isEqualTo(func);
    assertThat(findAncestorWithKind(func, Tree.Kind.SCRIPT, Tree.Kind.FUNCTION_DECLARATION)).isEqualTo(func);
  }

  @Test
  void testHasAnnotationOfFunction() {
    ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);
    FunctionDeclarationTree tree = (FunctionDeclarationTree) parser.parse("/**\n * @annotation\n */\nfunction foo(){}");
    assertThat(hasAnnotation(tree, "@annotation")).isTrue();
    assertThat(hasAnnotation(tree, "annotation")).isTrue();
    assertThat(hasAnnotation(tree, "other_annotation")).isFalse();

    tree = (FunctionDeclarationTree) parser.parse("/**\n * annotation\n */\nfunction foo(){}");
    assertThat(hasAnnotation(tree, "@annotation")).isFalse();
    assertThat(hasAnnotation(tree, "annotation")).isFalse();
  }
}
