/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.utils.argumentmatching;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArgumentVerifierValueContainmentTest {

  @Test
  void argumentVerifierWithValue() {
    ArgumentVerifierValueContainment argumentVerifier = ArgumentVerifierValueContainment.builder()
      .raiseIssueOnMatch(false)
      .position(1)
      .values("VALUE")
      .build();

    assertThat(argumentVerifier.getValues()).isEqualTo(Set.of("value"));
    assertThat(argumentVerifier.isRaiseIssueOnMatch()).isFalse();
  }

  @Test
  void argumentVerifierWithName() {
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
  void argumentVerifierWithSet() {
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
  void argumentVerifierWithDefault() {
    ArgumentVerifierValueContainment argumentVerifier = ArgumentVerifierValueContainment.builder()
      .position(1)
      .values("VALUE")
      .build();

    assertThat(argumentVerifier.isRaiseIssueOnMatch()).isTrue();
  }

}
