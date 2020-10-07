package org.sonar.php.parser.declaration;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

public class FunctionCallArgumentTest {
  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.FUNCTION_CALL_ARGUMENT)
      .matches("$a")
      .matches("foo: $a")
      .matches("self::$a")
      .matches("bar:self::$a");
  }
}
