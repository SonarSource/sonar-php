package org.sonar.php.tree.impl.statement;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumCaseTreeTest extends PHPTreeModelTest {

  @Test
  public void simple_case() {
    EnumCaseTreeImpl tree = parse("case A;", PHPLexicalGrammar.ENUM_CASE);

    assertThat(tree.is(Tree.Kind.ENUM_CASE)).isTrue();
    assertThat(tree.childrenIterator()).hasSize(3);
    assertThat(tree.caseToken()).hasToString("case");
    assertThat(tree.name()).hasToString("A");
    assertThat(tree.eosToken()).hasToString(";");
  }
}
