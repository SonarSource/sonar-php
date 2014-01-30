/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
 * dev@sonar.codehaus.org
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
package org.sonar.plugins.php.core;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.impl.Lexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.PhpConstants;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

public class PhpLexerSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PhpLexerSensor.class);
  private FileLinesContextFactory fileLinesContextFactory;

  /**
   * @param noSonarFilter
   */
  public PhpLexerSensor(FileLinesContextFactory fileLinesContextFactory) {
    this.fileLinesContextFactory = fileLinesContextFactory;
  }

  /**
   * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
   */
  public void analyse(Project project, SensorContext context) {
    ProjectFileSystem fileSystem = project.getFileSystem();
    List<InputFile> sourceFiles = fileSystem.mainFiles(Php.KEY);
    for (InputFile file : sourceFiles) {
      org.sonar.api.resources.File phpFile = org.sonar.api.resources.File.fromIOFile(file.getFile(), project);
      if (phpFile != null) {
        try {
          analyseSourceCode(project, phpFile, file.getFile(), context);
        } catch (IOException e) {
          LOG.error("Unabale to compute metrics for file " + file.getRelativePath());
        }
      }
    }
  }

  protected void analyseSourceCode(Project project, org.sonar.api.resources.File phpFile, File file, SensorContext context) throws IOException {
    Charset sourceCharset = project.getFileSystem().getSourceCharset();

    String content = Files.toString(file, sourceCharset);
    String[] lines = content.split("(\r)?\n|\r", -1);

    context.saveMeasure(phpFile, CoreMetrics.LINES, Double.valueOf(lines.length));
    context.saveMeasure(phpFile, CoreMetrics.FILES, 1.0);
    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(phpFile);

    final Set<Integer> linesOfCode = Sets.newHashSet();
    final Set<Integer> linesOfComments = Sets.newHashSet();

    computePerLineMetrics(file, sourceCharset, lines.length, fileLinesContext, linesOfCode, linesOfComments);

    context.saveMeasure(phpFile, CoreMetrics.NCLOC, Double.valueOf(linesOfCode.size()));
    context.saveMeasure(phpFile, CoreMetrics.COMMENT_LINES, Double.valueOf(linesOfComments.size()));

  }

  @VisibleForTesting
  void computePerLineMetrics(File file, Charset sourceCharset, int fileLength, FileLinesContext fileLinesContext,
      final Set<Integer> linesOfCode, final Set<Integer> linesOfComments) {
    PhpParserConfiguration config = PhpParserConfiguration.builder().setCharset(sourceCharset).build();
    Lexer lexer = PhpLexer.create(config);

    List<Token> tokens = lexer.lex(file);
    for (Token token : tokens) {
      if (token.getType().equals(GenericTokenType.EOF)) {
        break;
      }

      linesOfCode.add(token.getLine());
      List<Trivia> trivias = token.getTrivia();
      for (Trivia trivia : trivias) {
        if (trivia.isComment()) {
          int firstLine = trivia.getToken().getLine();
          int lineCount = trivia.getToken().getValue().split("(\r)?\n|\r", -1).length;
          for (int i = firstLine; i < lineCount + firstLine; i++) {
            linesOfComments.add(i);
          }
        }
      }
    }

    for (int line = 1; line <= fileLength; line++) {
      fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, linesOfCode.contains(line) ? 1 : 0);
      fileLinesContext.setIntValue(CoreMetrics.COMMENT_LINES_DATA_KEY, line, linesOfComments.contains(line) ? 1 : 0);
    }
    fileLinesContext.save();
  }

  /**
   * @see org.sonar.api.batch.CheckProject#shouldExecuteOnProject(org.sonar.api.resources.Project)
   */
  public boolean shouldExecuteOnProject(Project project) {
    return Php.KEY.equals(project.getLanguageKey());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "PHP Lexer Sensor";
  }
}
