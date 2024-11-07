/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.CheckVerifier;

class HardCodedSecretCheckTest {

  @Test
  void shouldVerifyDifferentASTElements() {
    CheckVerifier.verify(new HardCodedSecretCheck(), "HardCodedSecretCheckAST.php");
  }

  @Test
  void shouldVerifyDifferentSecrets() {
    CheckVerifier.verify(new HardCodedSecretCheck(), "HardCodedSecretCheckSecrets.php");
  }

  @Test
  void shouldVerifyCustomSecretWords() {
    var check = new HardCodedSecretCheck();
    check.secretWords = "CUSTOM,app";
    CheckVerifier.verify(check, "HardCodedSecretCheckCustomWords.php");
  }

  @Test
  void shouldVerifyCustomRandomnessSensibility() {
    var check = new HardCodedSecretCheck();
    check.randomnessSensibility = 6.0;
    CheckVerifier.verify(check, "HardCodedSecretCheckCustomRandomnessSensibility.php");
  }
}
