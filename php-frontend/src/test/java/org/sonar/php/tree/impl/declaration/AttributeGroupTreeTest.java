package org.sonar.php.tree.impl.declaration;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeGroupTreeTest extends PHPTreeModelTest {
  @Test
  public void simple_group() throws Exception {
    AttributeGroupTree tree = parse("#[A,B]", PHPLexicalGrammar.ATTRIBUTE_GROUP);

    assertThat(tree.is(Tree.Kind.ATTRIBUTE_GROUP)).isTrue();
    assertThat(tree.attributes()).hasSize(2);
    assertThat(tree.startToken()).hasToString("#[");
    assertThat(tree.endToken()).hasToString("]");
  }
}
