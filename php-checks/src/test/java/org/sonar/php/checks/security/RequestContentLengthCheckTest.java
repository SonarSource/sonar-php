/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.security;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.CheckVerifier;

class RequestContentLengthCheckTest {
  @Test
  void symfonyFile() {
    CheckVerifier.verify(new RequestContentLengthCheck(), "security/requestContentLengthCheck/symfonyFile.php");
  }

  @Test
  void laravelRequest() {
    CheckVerifier.verify(new RequestContentLengthCheck(), "security/requestContentLengthCheck/laravelRequest.php");
  }

  @Test
  void laravelFormRequest() {
    CheckVerifier.verify(new RequestContentLengthCheck(), "security/requestContentLengthCheck/laravelFormRequest.php");
  }

  @Test
  void laravelValidator() {
    CheckVerifier.verify(new RequestContentLengthCheck(), "security/requestContentLengthCheck/laravelValidator.php");
  }
}
