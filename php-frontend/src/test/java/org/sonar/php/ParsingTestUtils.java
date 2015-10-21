/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.typed.ActionParser;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;

import java.io.File;

public class ParsingTestUtils {

  protected ActionParser<Tree> p = PHPParserBuilder.createParser(PHPLexicalGrammar.COMPILATION_UNIT, Charsets.UTF_8);

  protected CompilationUnitTree parse(String filename) {
    File file = new File("src/test/resources/", filename);

    ActionParser<Tree> parser = PHPParserBuilder.createParser(Charsets.UTF_8);
    return (CompilationUnitTree) parser.parse(file);
  }
}
