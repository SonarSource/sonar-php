package org.sonar.php.tree.impl.declaration;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.EnumDeclarationTree;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumDeclarationTreeTest extends PHPTreeModelTest {

  @Test
  public void simple_enum_with_no_cases() {
    EnumDeclarationTreeImpl tree = parse("enum A {}", PHPLexicalGrammar.ENUM_DECLARATION);

    assertThat(tree.is(Tree.Kind.ENUM_DECLARATION)).isTrue();
    assertThat(tree.childrenIterator()).hasSize(4);
    assertThat(tree.name()).hasToString("A");
    assertThat(tree.openCurlyBraceToken()).hasToString("{");
    assertThat(tree.cases()).isEmpty();
    assertThat(tree.closeCurlyBraceToken()).hasToString("}");
  }

  @Test
  public void simple_enum_with_cases() {
    EnumDeclarationTree tree = parse("enum A {case A;\ncase B;}", PHPLexicalGrammar.ENUM_DECLARATION);
    assertThat(tree.is(Tree.Kind.ENUM_DECLARATION)).isTrue();
    assertThat(tree.name()).hasToString("A");
    assertThat(tree.cases()).hasSize(2);
    assertThat(tree.cases().get(0).name()).hasToString("A");
    assertThat(tree.cases().get(1).name()).hasToString("B");
  }
}
