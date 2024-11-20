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
package org.sonar.php.parser.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class ClassDeclarationTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.CLASS_DECLARATION)
      .matches("class C {}")
      .matches("class match {}")
      .matches("class Enum {}")
      .matches("abstract class C {}")
      .matches("final class C {}")

      .matches("class C extends A {}")
      .matches("class C implements B {}")
      .matches("class C extends A implements B {}")
      .matches("#[A1(1)] class C {}")

      .notMatches("class A extends B, C {}")
      .notMatches("class readonly {}");
  }
}
