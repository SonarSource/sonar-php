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
package org.sonar.php.symbols;

import com.sonar.sslr.api.typed.ActionParser;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;

public class SymbolTestUtils {

  private static final ActionParser<Tree> PARSER = PHPParserBuilder.createParser();

  public static CompilationUnitTree parse(String... lines) {
    String source = String.join("\n", lines);
    TestFile file = new TestFile(source, "file1.php");
    CompilationUnitTree ast = (CompilationUnitTree) PARSER.parse(source);
    SymbolTableImpl.create(ast, new ProjectSymbolData(), file);
    return ast;
  }
}
