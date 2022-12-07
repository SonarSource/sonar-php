/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.plugins.php;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import java.util.List;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.php.compat.PhpFileImpl;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.php.cache.Cache;

public class SymbolScanner extends Scanner {

  private static final Logger LOG = Loggers.get(SymbolScanner.class);

  private final ActionParser<Tree> parser = PHPParserBuilder.createParser();
  private ProjectSymbolData projectSymbolData = new ProjectSymbolData();
  private final Cache cache;

  public SymbolScanner(SensorContext context, DurationStatistics statistics, Cache cacheContext) {
    super(context, statistics);
    this.cache = cacheContext;
  }

  @Override
  String name() {
    return "PHP symbol indexer";
  }

  @Override
  void execute(List<InputFile> files) {
    if (optimizedAnalysis) {
      projectSymbolData = cache.read();
    }
    super.execute(files);
    cache.write(projectSymbolData);
  }

  @Override
  void scanFile(InputFile file) {
    PhpFileImpl phpFile = new PhpFileImpl(file);
    try {
      CompilationUnitTree ast = (CompilationUnitTree) statistics.time("ProjectSymbolParsing", () -> parser.parse(phpFile.contents()));
      SymbolTableImpl symbolTable = statistics.time("ProjectSymbolTable", () -> SymbolTableImpl.create(ast, projectSymbolData, phpFile));
      symbolTable.classSymbolDatas().forEach(projectSymbolData::add);
      symbolTable.functionSymbolDatas().forEach(projectSymbolData::add);
    } catch (RecognitionException e) {
      LOG.debug("Parsing error in " + file);
    }
  }

  @Override
  void logException(Exception e, InputFile file) {
    LOG.debug("Unable to analyze file: " + file.toString(), e);
  }

  public ProjectSymbolData getProjectSymbolData() {
    return projectSymbolData;
  }
}
