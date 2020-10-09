package org.sonar.php.tree.impl.declaration;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeTreeTest extends PHPTreeModelTest {
  @Test
  public void simple_attribute() throws Exception {
    AttributeTree tree = parse("A", PHPLexicalGrammar.ATTRIBUTE);

    assertThat(tree.is(Tree.Kind.ATTRIBUTE)).isTrue();
    assertThat(tree.name()).hasToString("A");
  }

  @Test
  public void with_arguments() throws Exception {
    // TODO: add named argument when rebased.
    AttributeTree tree = parse("A($x, $y)", PHPLexicalGrammar.ATTRIBUTE);

    assertThat(tree.is(Tree.Kind.ATTRIBUTE)).isTrue();
    assertThat(tree.name()).hasToString("A");

    assertThat(tree.arguments()).hasSize(1);
  }
}
