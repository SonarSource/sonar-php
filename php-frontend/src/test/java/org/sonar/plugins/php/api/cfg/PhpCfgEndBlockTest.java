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
package org.sonar.plugins.php.api.cfg;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PhpCfgEndBlockTest extends PHPTreeModelTest {

  @Test
  void cannotAddElement() {
    PhpCfgEndBlock endBlock = new PhpCfgEndBlock();
    Tree tree = parse("array()", PHPLexicalGrammar.ARRAY_INITIALIZER);

    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> {
      endBlock.addElement(tree);
    });
  }

  @Test
  void cannotReplaceSuccessors() {
    PhpCfgEndBlock endBlock = new PhpCfgEndBlock();
    Map<PhpCfgBlock, PhpCfgBlock> map = new HashMap<>();
    map.put(endBlock, endBlock);

    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> {
      endBlock.replaceSuccessors(map);
    });
  }
}
