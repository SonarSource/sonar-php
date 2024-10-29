package org.sonar.php.checks;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.CheckVerifier;

class HardCodedSecretCheckTest {

  @Test
  void test() throws Exception {
    CheckVerifier.verify(new HardCodedSecretCheck(), "HardCodedSecretCheck.php");
  }
}
