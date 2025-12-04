/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

class PropertyHookTest {

  @Test
  void shouldMatchPropertyHooks() {
    assertThat(PHPLexicalGrammar.PROPERTY_HOOK)
      .matches("get;")
      .matches("set;")
      .matches("#[A1(4)] get;")
      .matches("final get;")
      .matches("&get;")
      .matches("&get => $this->a;")
      .matches("set => 123;")
      .matches("get => $this->a;")
      .matches("get {}")
      .matches("get { return 42; }")
      .matches("get { return $this->a+1; }")
      .matches("set { $this->a = $value -1; }")
      .matches("set(A|string $value) { $this->a = $value -1; }")
      .matches("final set($value) => $value - 1;")
      .matches("set (A $a) { $this->arr[] = $a; $this->a = $a; }")
      .matches("get { return implode(', ', array_map(fn (Author $author) => $author->name,$this->authors,)); }")

      .notMatches("private get;")
      .notMatches("public get;")
      .notMatches("protected get;")
      .notMatches("set => 42; 42;")
      .notMatches("name;")
      .notMatches("name => 123;")
      .notMatches("name;");
  }

}
