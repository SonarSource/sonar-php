/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.checks.utils;

import org.junit.Test;
import org.sonar.php.checks.utils.FunctionArgumentCheck.ArgumentMatcher;
import org.sonar.php.checks.utils.FunctionArgumentCheck.ArgumentVerifier;
import org.sonar.php.utils.collections.SetUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class FunctionArgumentCheckTest {

  @Test
  public void argument_indicator_with_string() {
    ArgumentMatcher argumentMatcher = new ArgumentMatcher(1, null, "VALUE");

    assertThat(argumentMatcher.getValues()).isEqualTo(SetUtils.immutableSetOf("value"));
    assertThat(argumentMatcher.getPosition()).isEqualTo(1);
    assertThat(argumentMatcher.getName()).isNull();
  }

  @Test
  public void argument_indicator_with_set() {
    ArgumentMatcher argumentMatcher = new ArgumentMatcher(1, "argumentName", SetUtils.immutableSetOf("VALUE"));

    assertThat(argumentMatcher.getValues()).isEqualTo(SetUtils.immutableSetOf("value"));
    assertThat(argumentMatcher.getName()).isEqualTo("argumentName");
  }

  @Test
  public void argument_verifier_with_value() {
    ArgumentVerifier argumentVerifier = new ArgumentVerifier(1, "VALUE", false);

    assertThat(argumentVerifier.getValues()).isEqualTo(SetUtils.immutableSetOf("value"));
    assertThat(argumentVerifier.isRaiseIssueOnMatch()).isFalse();
  }

  @Test
  public void argument_verifier_with_name() {
    ArgumentVerifier argumentVerifier = new ArgumentVerifier(1, "name", "VALUE");

    assertThat(argumentVerifier.getName()).isEqualTo("name");
  }

  @Test
  public void argument_verifier_with_set() {
    ArgumentVerifier argumentVerifier = new ArgumentVerifier(1, SetUtils.immutableSetOf("VALUE"), true);

    assertThat(argumentVerifier.getValues()).isEqualTo(SetUtils.immutableSetOf("value"));
    assertThat(argumentVerifier.isRaiseIssueOnMatch()).isTrue();
  }

  @Test
  public void argument_verifier_with_default() {
    ArgumentVerifier argumentVerifier = new ArgumentVerifier(1, "VALUE");

    assertThat(argumentVerifier.isRaiseIssueOnMatch()).isTrue();
  }

}
