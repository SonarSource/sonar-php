/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
package org.sonar.php.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.assertj.core.api.Assertions.assertThat;

class ScriptTreeTest extends PHPTreeModelTest {

  @Test
  void scriptWithoutStatement() {
    ScriptTree tree = parse("<?php", PHPLexicalGrammar.SCRIPT);

    assertThat(tree.is(Kind.SCRIPT)).isTrue();
    assertThat(tree.fileOpeningTagToken().text()).isEqualTo("<?php");
    assertThat(tree.statements()).isEmpty();
  }

  @Test
  void scriptAspStyleNotSupported() {
    // Support removed in SONARPHP-1087
    ScriptTree tree = parse("<% $a; %> <br/>", PHPLexicalGrammar.SCRIPT);

    assertThat(tree.is(Kind.SCRIPT)).isTrue();
    assertThat(tree.fileOpeningTagToken().text()).isEqualTo("<% $a; %> <br/>");
    assertThat(tree.statements()).isEmpty();
  }

  @Test
  void scriptWithStatement() {
    ScriptTree tree = parse("<?php $a;", PHPLexicalGrammar.SCRIPT);

    assertThat(tree.is(Kind.SCRIPT)).isTrue();
    assertThat(tree.fileOpeningTagToken().text()).isEqualTo("<?php");
    assertThat(tree.statements()).hasSize(1);
    assertThat(expressionToString(tree.statements().get(0))).isEqualTo("$a;");
  }

}
