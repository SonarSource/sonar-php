/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.tree.visitors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.symbols.SymbolTable;

class FrameworkDetectionVisitorTest {
  @Test
  void shouldDetectDrupalByUseClause() {
    var code = """
      <?php

      use Drupal\\Core\\Entity\\EntityInterface;
      use Symfony\\Component\\HttpFoundation\\Request;
      """;
    var tree = ParsingTestUtils.parseSource(code);
    var visitor = new FrameworkDetectionVisitor();

    tree.accept(visitor);

    Assertions.assertThat(visitor.getFramework()).isEqualTo(SymbolTable.Framework.DRUPAL);
  }

  @Test
  void shouldDetectYiiByUseClause() {
    var code = """
      <?php

      use yii\\base\\Model;
      use Symfony\\Component\\HttpFoundation\\Request;
      """;
    var tree = ParsingTestUtils.parseSource(code);
    var visitor = new FrameworkDetectionVisitor();

    tree.accept(visitor);

    Assertions.assertThat(visitor.getFramework()).isEqualTo(SymbolTable.Framework.YII);
  }

  @Test
  void shouldReturnEmptyWithoutDrupalUseClause() {
    var code = """
      <?php

      use Symfony\\Component\\HttpFoundation\\Request;
      use Symfony\\Component\\HttpFoundation\\Response;
      """;
    var tree = ParsingTestUtils.parseSource(code);
    var visitor = new FrameworkDetectionVisitor();

    tree.accept(visitor);

    Assertions.assertThat(visitor.getFramework()).isEqualTo(SymbolTable.Framework.EMPTY);
  }
}
