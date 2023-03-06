/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import org.junit.Test;
import org.sonar.plugins.php.CheckVerifier;


public class ClearTextProtocolsCheckTest {

  @Test
  public void string_literals() {
    CheckVerifier.verify(new ClearTextProtocolsCheck(), "security/clearTextProtocolsCheck/stringLiterals.php");
  }

  @Test
  public void ftp() {
    CheckVerifier.verify(new ClearTextProtocolsCheck(), "security/clearTextProtocolsCheck/ftp.php");
  }

  @Test
  public void laravel_mail() {
    CheckVerifier.verify(new ClearTextProtocolsCheck(), "security/clearTextProtocolsCheck/laravel/config/mail.php");
  }

  @Test
  public void swift_mailer() {
    CheckVerifier.verify(new ClearTextProtocolsCheck(), "security/clearTextProtocolsCheck/swiftMailer.php");
  }

  @Test
  public void php_mailer() {
    CheckVerifier.verify(new ClearTextProtocolsCheck(), "security/clearTextProtocolsCheck/phpMailer.php");
  }

  @Test
  public void wordpress() {
    CheckVerifier.verify(new ClearTextProtocolsCheck(), "wordpress/WordPressForceSslCheck/wp-config.php");
  }
}
