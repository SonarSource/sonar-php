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

class PropertyHookListTest {

  @Test
  void shouldMatchPropertyHookList() {
    assertThat(PHPLexicalGrammar.PROPERTY_HOOK_LIST)
      .matches("{ set; }")
      .matches("{ get => 123; }")
      .matches("{ get; set; }")
      .matches("{ get {} set; }")
      .matches("{ set; get {} }")
      .matches("{ &get; }")
      .matches("{ get { return implode(', ', array_map(fn (Author $author) => $author->name,$this->authors,)); } }")

      .matches("{ set; set; set; }")

      .notMatches("{ name; } ")
      .notMatches("{ name;")
      .notMatches("name; } ")
      .notMatches("{ get; name; } ")
      .notMatches("{ name; set; } ")
      .notMatches("{ name => 123; }")
      .notMatches("get;");
  }

}
