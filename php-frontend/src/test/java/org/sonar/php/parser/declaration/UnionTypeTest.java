package org.sonar.php.parser.declaration;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

public class UnionTypeTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.UNION_TYPE)
      .matches("int|array")
      .matches("foo|bar|array")
    ;
  }
}
