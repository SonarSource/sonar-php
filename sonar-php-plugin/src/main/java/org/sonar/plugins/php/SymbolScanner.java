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
package org.sonar.plugins.php;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.DurationStatistics;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.php.cache.Cache;
import org.sonar.php.compat.PhpFileImpl;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.visitors.PhpFile;

public class SymbolScanner extends Scanner {

  private static final Logger LOG = LoggerFactory.getLogger(SymbolScanner.class);

  private final ActionParser<Tree> parser = PHPParserBuilder.createParser();
  private final ProjectSymbolData projectSymbolData = new ProjectSymbolData();
  private int symbolTablesFromCache = 0;

  SymbolScanner(SensorContext context, DurationStatistics statistics, Cache cache) {
    super(context, statistics, cache);
  }

  @Override
  void execute(List<InputFile> files) {
    super.execute(files);
    LOG.info("Cached information of global symbols will be used for {} out of {} files. Global symbols were recomputed for the remaining files.",
      symbolTablesFromCache,
      files.size());
  }

  public static SymbolScanner create(SensorContext context, DurationStatistics statistics, CacheContext cacheContext) {
    Cache cache = new Cache(cacheContext);
    return new SymbolScanner(context, statistics, cache);
  }

  @Override
  String name() {
    return "PHP symbol indexer";
  }

  @Override
  void scanFile(InputFile file) {
    SymbolTableImpl fileSymbolTable = null;
    if (fileCanBeSkipped(file)) {
      fileSymbolTable = readSymbolTableFromCache(file);
    }

    if (fileSymbolTable == null) {
      try {
        fileSymbolTable = createSymbolTable(file);
      } catch (RecognitionException e) {
        LOG.debug("Can not create symbols for file: {}", file.filename());
        return;
      }
    }

    fileSymbolTable.classSymbolDatas().forEach(projectSymbolData::add);
    fileSymbolTable.functionSymbolDatas().forEach(projectSymbolData::add);

    byte[] contentHash;
    try {
      contentHash = FileHashingUtils.inputFileContentHash(file);
    } catch (IllegalStateException ise) {
      LOG.debug("Failed to compute content hash for file {}", file.key());
      return;
    }
    cache.writeFileContentHash(file, contentHash);
    cache.writeFileSymbolTable(file, fileSymbolTable);
  }

  @CheckForNull
  private SymbolTableImpl readSymbolTableFromCache(InputFile file) {
    SymbolTableImpl fileSymbolTable = cache.read(file);
    if (fileSymbolTable != null) {
      symbolTablesFromCache++;
    }
    return fileSymbolTable;
  }

  private SymbolTableImpl createSymbolTable(InputFile file) throws RecognitionException {
    PhpFile phpFile = PhpFileImpl.create(file);
    CompilationUnitTree ast = (CompilationUnitTree) statistics.time("ProjectSymbolParsing", () -> parser.parse(phpFile.contents()));
    return statistics.time("ProjectSymbolTable", () -> SymbolTableImpl.create(ast, new ProjectSymbolData(), phpFile));
  }

  @Override
  void logException(Exception e, InputFile file) {
    LOG.debug("Unable to analyze file: {}", file, e);
  }

  public ProjectSymbolData getProjectSymbolData() {
    return projectSymbolData;
  }
}
