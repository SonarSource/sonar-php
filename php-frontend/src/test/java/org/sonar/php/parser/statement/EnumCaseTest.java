package org.sonar.php.parser.statement;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

public class EnumCaseTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.ENUM_CASE)
      .matches("case A;")
      .notMatches("case A")
    ;
  }
}
