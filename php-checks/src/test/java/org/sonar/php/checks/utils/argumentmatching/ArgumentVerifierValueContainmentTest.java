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
package org.sonar.php.checks.utils.argumentmatching;

import java.util.Set;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArgumentVerifierValueContainmentTest {

  @Test
  public void argumentVerifierWithValue() {
    ArgumentVerifierValueContainment argumentVerifier = ArgumentVerifierValueContainment.builder()
      .raiseIssueOnMatch(false)
      .position(1)
      .values("VALUE")
      .build();

    assertThat(argumentVerifier.getValues()).isEqualTo(Set.of("value"));
    assertThat(argumentVerifier.isRaiseIssueOnMatch()).isFalse();
  }

  @Test
  public void argumentVerifierWithName() {
    ArgumentVerifierValueContainment argumentVerifier = ArgumentVerifierValueContainment.builder()
      .position(1)
      .name("name")
      .values("VALUE")
      .build();

    assertThat(argumentVerifier.getName()).isEqualTo("name");
    assertThat(argumentVerifier.getValues()).containsOnly("value");
    assertThat(argumentVerifier.getPosition()).isEqualTo(1);
    assertThat(argumentVerifier.isRaiseIssueOnMatch()).isTrue();
  }

  @Test
  public void argumentVerifierWithSet() {
    ArgumentVerifierValueContainment argumentVerifier = ArgumentVerifierValueContainment.builder()
      .position(1)
      .values(Set.of("VALUE"))
      .raiseIssueOnMatch(true)
      .build();

    assertThat(argumentVerifier.getValues()).isEqualTo(Set.of("value"));
    assertThat(argumentVerifier.isRaiseIssueOnMatch()).isTrue();
    assertThat(argumentVerifier.getName()).isNull();
    assertThat(argumentVerifier.getPosition()).isEqualTo(1);
  }

  @Test
  public void argumentVerifierWithDefault() {
    ArgumentVerifierValueContainment argumentVerifier = ArgumentVerifierValueContainment.builder()
      .position(1)
      .values("VALUE")
      .build();

    assertThat(argumentVerifier.isRaiseIssueOnMatch()).isTrue();
  }

}
