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

import java.io.FileNotFoundException;
import org.junit.Test;
import org.sonar.plugins.php.CheckVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HardCodedCredentialsInFunctionCallsCheckTest {

  @Test
  public void test() throws Exception {
    CheckVerifier.verify(new HardCodedCredentialsInFunctionCallsCheck(), "HardCodedCredentialsInFunctionCallsCheck.php");
  }

  @Test
  public void parseResourceThrowsException() {
    assertThatThrownBy(() -> HardCodedCredentialsInFunctionCallsCheck.JsonSensitiveFunctionsReader.parseResource("no_valid_file_location" +
      ".json")).isInstanceOf(FileNotFoundException.class);
  }

  @Test
  public void toIntegerReturnsNull() {
    Integer integer = HardCodedCredentialsInFunctionCallsCheck.JsonSensitiveFunctionsReader.toInteger("string");
    assertThat(integer).isNull();
  }
}
