/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
