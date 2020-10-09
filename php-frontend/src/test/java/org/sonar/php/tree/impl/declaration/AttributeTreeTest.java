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
  public void with_arguments_and_fqn() throws Exception {
    AttributeTree tree = parse("\\A\\B\\C($x, y: $y)", PHPLexicalGrammar.ATTRIBUTE);

    assertThat(tree.is(Tree.Kind.ATTRIBUTE)).isTrue();
    assertThat(tree.name()).hasToString("\\A\\B\\C");

    assertThat(tree.arguments()).hasSize(2);
    assertThat(tree.arguments().get(0).name()).isNull();
    assertThat(tree.arguments().get(1).name()).hasToString("y");
  }
}
