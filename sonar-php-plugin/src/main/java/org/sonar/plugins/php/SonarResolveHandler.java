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
package org.sonar.plugins.php;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.Version;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonarsource.analyzer.commons.SonarResolve;

final class SonarResolveHandler {

  private static final Logger LOG = LoggerFactory.getLogger(SonarResolveHandler.class);

  private SonarResolveHandler() {
  }

  static void handle(SensorContext context, InputFile inputFile, CompilationUnitTree tree, boolean apiSupportsIssueResolution) {
    List<SyntaxTrivia> trivias = collectTrivias(tree);
    int i = 0;
    while (i < trivias.size()) {
      SyntaxTrivia trivia = trivias.get(i);
      String text = trivia.text();
      if (isBlockComment(text)) {
        handleBlockComment(context, inputFile, trivia, apiSupportsIssueResolution);
        i++;
      } else if (isSingleLineComment(text)) {
        i += handleSingleLineComment(context, inputFile, trivias, i, apiSupportsIssueResolution);
      } else {
        i++;
      }
    }
  }

  private static void handleBlockComment(SensorContext context, InputFile inputFile, SyntaxTrivia trivia, boolean apiSupportsIssueResolution) {
    String text = trivia.text();
    String[] rawLines = text.split("(\r)?\n|\r", -1);
    int startLine = trivia.line();

    String firstLineContent = stripBlockCommentStart(rawLines[0]);
    List<NormalizedCommentLine> normalized = new ArrayList<>();
    normalized.add(new NormalizedCommentLine(startLine, firstLineContent));

    List<NormalizedCommentLine> continuationLines = new ArrayList<>();
    for (int j = 1; j < rawLines.length; j++) {
      String stripped = rawLines[j].stripLeading();
      if (!"*/".equals(stripped.stripTrailing())) {
        continuationLines.add(new NormalizedCommentLine(startLine + j, stripped));
      }
    }
    normalized.addAll(dedentContinuationLines(continuationLines, SonarResolveHandler::blockCommentPrefix));

    if (!startsWithIgnoreCase(firstLineContent, SonarResolve.KEYWORD)) {
      return;
    }
    if (!apiSupportsIssueResolution) {
      LOG.warn("{}:{}: sonar-resolve skipped: unsupported API", inputFile.filename(), startLine);
      return;
    }

    SonarResolve.StreamingParser driver = new SonarResolve.StreamingParser(startLine);
    consumeNormalizedLines(context, inputFile, startLine, driver, normalized);
  }

  private static int handleSingleLineComment(
    SensorContext context,
    InputFile inputFile,
    List<SyntaxTrivia> trivias,
    int index,
    boolean apiSupportsIssueResolution) {
    SyntaxTrivia trivia = trivias.get(index);
    int line = trivia.line();
    String directive = stripSingleLineCommentStart(trivia.text());

    if (!startsWithIgnoreCase(directive, SonarResolve.KEYWORD)) {
      return 1;
    }
    if (!apiSupportsIssueResolution) {
      LOG.warn("{}:{}: sonar-resolve skipped: unsupported API", inputFile.filename(), line);
      return 1;
    }

    SonarResolve.StreamingParser driver = new SonarResolve.StreamingParser(line);
    SonarResolve.StreamingParser.State state = driver.consumeLine(line, directive);

    int consumed = 1;
    List<NormalizedCommentLine> continuationLines = new ArrayList<>();
    if (state == SonarResolve.StreamingParser.State.INCOMPLETE) {
      int expectedLine = line + 1;
      for (int j = index + 1; j < trivias.size(); j++) {
        SyntaxTrivia next = trivias.get(j);
        if (next.line() != expectedLine || !isSingleLineComment(next.text())) {
          break;
        }
        String continuationContent = stripSingleLineCommentContinuationStart(next.text());
        if (startsWithIgnoreCase(continuationContent.stripLeading(), SonarResolve.KEYWORD)) {
          break;
        }
        continuationLines.add(new NormalizedCommentLine(next.line(), continuationContent));
        consumed++;
        expectedLine++;
      }
      continuationLines = dedentContinuationLines(continuationLines, SonarResolveHandler::leadingWhitespace);
    }

    consumeNormalizedLines(context, inputFile, line, driver, continuationLines);
    return consumed;
  }

  private static void consumeNormalizedLines(
    SensorContext context,
    InputFile inputFile,
    int directiveLine,
    SonarResolve.StreamingParser driver,
    List<NormalizedCommentLine> lines) {
    for (NormalizedCommentLine line : lines) {
      SonarResolve.StreamingParser.State state = driver.consumeLine(line.lineNumber(), line.lineContent());
      if (state == SonarResolve.StreamingParser.State.COMPLETE || state == SonarResolve.StreamingParser.State.INVALID) {
        break;
      }
    }
    if (driver.state() == SonarResolve.StreamingParser.State.INCOMPLETE) {
      driver.finish();
    }
    if (driver.state() == SonarResolve.StreamingParser.State.COMPLETE) {
      submit(context, inputFile, driver.result());
    } else if (driver.state() == SonarResolve.StreamingParser.State.INVALID) {
      LOG.warn("{}:{}: {}", inputFile.filename(), directiveLine, driver.errorMessage());
    }
  }

  private static void submit(SensorContext context, InputFile inputFile, SonarResolve sr) {
    context.newIssueResolution()
      .on(inputFile)
      .at(inputFile.selectLine(sr.targetLine()))
      .status(sr.status())
      .forRules(sr.ruleKeys())
      .comment(sr.justification())
      .save();
  }

  static boolean apiSupportsIssueResolution(SensorContext context) {
    return context.runtime().getApiVersion().isGreaterThanOrEqual(Version.create(13, 5));
  }

  private static List<SyntaxTrivia> collectTrivias(CompilationUnitTree tree) {
    List<SyntaxTrivia> result = new ArrayList<>();
    new PHPVisitorCheck() {
      @Override
      public void visitToken(SyntaxToken token) {
        result.addAll(token.trivias());
        super.visitToken(token);
      }
    }.visitCompilationUnit(tree);
    return result;
  }

  private static boolean isBlockComment(String text) {
    return text.startsWith("/*");
  }

  private static boolean isSingleLineComment(String text) {
    return text.startsWith("//") || text.startsWith("#");
  }

  private static boolean startsWithIgnoreCase(String text, String prefix) {
    return text.regionMatches(true, 0, prefix, 0, prefix.length());
  }

  private static String stripBlockCommentStart(String line) {
    String normalized = line.stripLeading();
    if (normalized.startsWith("/*")) {
      return normalized.substring(2).stripLeading();
    }
    return line;
  }

  private static String stripSingleLineCommentStart(String text) {
    if (text.startsWith("//")) {
      return text.substring(2).stripLeading();
    }
    if (text.startsWith("#")) {
      return text.substring(1).stripLeading();
    }
    return text;
  }

  private static String stripSingleLineCommentContinuationStart(String text) {
    if (text.startsWith("//")) {
      return text.substring(2);
    }
    if (text.startsWith("#")) {
      return text.substring(1);
    }
    return text;
  }

  private static List<NormalizedCommentLine> dedentContinuationLines(
    List<NormalizedCommentLine> lines,
    UnaryOperator<String> prefixExtractor) {
    if (lines.isEmpty()) {
      return lines;
    }
    String commonPrefix = prefixExtractor.apply(lines.get(0).lineContent());
    for (int i = 1; i < lines.size(); i++) {
      commonPrefix = commonPrefix(commonPrefix, prefixExtractor.apply(lines.get(i).lineContent()));
      if (commonPrefix.isEmpty()) {
        return lines;
      }
    }
    List<NormalizedCommentLine> result = new ArrayList<>();
    for (NormalizedCommentLine line : lines) {
      result.add(new NormalizedCommentLine(line.lineNumber(), line.lineContent().substring(commonPrefix.length())));
    }
    return result;
  }

  private static String blockCommentPrefix(String line) {
    if (!line.startsWith("*")) {
      return "";
    }
    return "*" + leadingWhitespace(line.substring(1));
  }

  private static String leadingWhitespace(String line) {
    int i = 0;
    while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
      i++;
    }
    return line.substring(0, i);
  }

  private static String commonPrefix(String left, String right) {
    int i = 0;
    while (i < left.length() && i < right.length() && left.charAt(i) == right.charAt(i)) {
      i++;
    }
    return left.substring(0, i);
  }

  private record NormalizedCommentLine(int lineNumber, String lineContent) {
  }
}
