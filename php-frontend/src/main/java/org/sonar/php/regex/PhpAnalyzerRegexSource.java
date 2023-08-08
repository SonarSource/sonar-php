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
package org.sonar.php.regex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.LocationInFile;
import org.sonarsource.analyzer.commons.regex.ast.IndexRange;
import org.sonarsource.analyzer.commons.regex.php.PhpRegexSource;

public class PhpAnalyzerRegexSource extends PhpRegexSource {

  private static final IndexRange OPENER_RANGE = new IndexRange(-1, 0);

  private final int sourceLine;
  private final int sourceStartOffset;
  private final int[] lineStartOffsets;

  public PhpAnalyzerRegexSource(LiteralTree stringLiteral) {
    this(literalToString(stringLiteral), stringLiteral.value().charAt(0), stringLiteral.token());
  }

  private PhpAnalyzerRegexSource(String literalToString, char quote, SyntaxToken token) {
    super(stripDelimiters(literalToString.trim()), quote);
    String leadingWhitespaces = leadingWhitespaces(literalToString);
    int[] leadingWhitespaceLineStartOffsets = lineStartOffsets(leadingWhitespaces);
    sourceLine = token.line() + leadingWhitespaceLineStartOffsets.length - 1;
    int delimiterOffset = leadingWhitespaces.length() - leadingWhitespaceLineStartOffsets[leadingWhitespaceLineStartOffsets.length - 1];
    if (leadingWhitespaceLineStartOffsets.length == 1) {
      // usual case: no '\n' or '\r' before the opening delimiter
      sourceStartOffset = token.column() + delimiterOffset + 1 /* quote */ + 1 /* delimiter */;
    } else {
      sourceStartOffset = delimiterOffset + 1;
    }
    lineStartOffsets = lineStartOffsets(getSourceText());
  }

  private static String literalToString(LiteralTree literal) {
    if (literal.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      String literalValue = literal.value();
      return literalValue.substring(1, literalValue.length() - 1);
    }
    throw new IllegalArgumentException("Only string literals allowed");
  }

  private static String stripDelimiters(String pattern) {
    if (pattern.length() >= 2) {
      Character endDelimiter = PhpRegexUtils.getEndDelimiter(pattern);
      return pattern.substring(1, pattern.lastIndexOf(endDelimiter));
    }
    throw new IllegalArgumentException("Regular expression does not contain delimiters");
  }

  private static String leadingWhitespaces(String s) {
    int i = 0;
    while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
      i++;
    }
    return s.substring(0, i);
  }

  public LocationInFile locationInFileFor(IndexRange range) {
    if (OPENER_RANGE.equals(range)) {
      return new LocationInFileImpl(null, sourceLine, sourceStartOffset - 2, sourceLine, sourceStartOffset - 1);
    }

    int[] startLineAndOffset = lineAndOffset(range.getBeginningOffset());
    int[] endLineAndOffset = lineAndOffset(range.getEndingOffset());
    return new LocationInFileImpl(null, startLineAndOffset[0], startLineAndOffset[1], endLineAndOffset[0], endLineAndOffset[1]);
  }

  private int[] lineAndOffset(int index) {
    int line;
    int offset;
    int searchResult = Arrays.binarySearch(lineStartOffsets, index);
    if (searchResult >= 0) {
      line = sourceLine + searchResult;
      offset = 0;
    } else {
      line = sourceLine - searchResult - 2;
      offset = index - lineStartOffsets[-searchResult - 2];
    }
    if (line == sourceLine) {
      offset += sourceStartOffset;
    }
    return new int[] {line, offset};
  }

  private static int[] lineStartOffsets(String text) {
    List<Integer> lineStartOffsets = new ArrayList<>();
    lineStartOffsets.add(0);
    int length = text.length();
    int i = 0;
    while (i < length) {
      if (text.charAt(i) == '\n' || text.charAt(i) == '\r') {
        int nextLineStartOffset = i + 1;
        if (i < (length - 1) && text.charAt(i) == '\r' && text.charAt(i + 1) == '\n') {
          nextLineStartOffset = i + 2;
          i++;
        }
        lineStartOffsets.add(nextLineStartOffset);
      }
      i++;
    }
    return lineStartOffsets.stream().mapToInt(x -> x).toArray();
  }
}
