package org.sonar.php.parser.declaration;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

public class AttributeGroupTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.ATTRIBUTE_GROUP)
      .matches("#[A]")
      .matches("#[A,B]")
      .matches("#[A,B,]")
      .matches("#[A,B,]")
      .matches("#[A()]")
      .matches("#[A($x)]")
      .matches("#[A(x:$x)]")
      .matches("#[A(x:$x), B()]")
      .notMatches("#[]")
    ;
  }

}
