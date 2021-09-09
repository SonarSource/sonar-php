/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
import org.sonar.plugins.php.api.visitors.LocationInFile;
import org.sonarsource.analyzer.commons.regex.CharacterParser;
import org.sonarsource.analyzer.commons.regex.RegexDialect;
import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.ast.IndexRange;

public class PhpRegexSource implements RegexSource {

  private final String sourceText;
  private final int sourceLine;
  private final int sourceStartOffset;
  private final char quote;
  private final int[] lineStartOffsets;

  public PhpRegexSource(LiteralTree stringLiteral) {
    quote = stringLiteral.value().charAt(0);
    String stringWithoutQuotes = literalToString(stringLiteral);
    sourceText = stripDelimiters(stringWithoutQuotes.trim());
    sourceLine = stringLiteral.token().line();
    sourceStartOffset = sourceStartOffset(stringLiteral, stringWithoutQuotes);
    lineStartOffsets = lineStartOffsets(sourceText);
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

  @Override
  public CharacterParser createCharacterParser() {
    if (quote == '\'') {
      return PhpStringCharacterParser.forSingleQuotedString(this);
    }
    return PhpStringCharacterParser.forDoubleQuotedString(this);
  }

  @Override
  public String getSourceText() {
    return sourceText;
  }

  @Override
  public RegexDialect dialect() {
    return RegexDialect.PHP;
  }

  public LocationInFile locationInFileFor(IndexRange range) {
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
      offset = index - lineStartOffsets[- searchResult - 2];
    }
    if (line == sourceLine) {
      offset += sourceStartOffset;
    }
    return new int[] { line, offset };
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

  // When calculating the start offset of the source, we have to take leading spaces into account.
  // If there are truncated spaces, we simply add their number to the offset.
  // To also have the possibility to calculate the offset when the source is empty,
  // we have to add the first delimiter to the offset.
  private int sourceStartOffset(LiteralTree tree, String stringWithoutQuotes) {
    int skipLeadingWhiteSpaces = sourceText.isEmpty() ? 2 : (stringWithoutQuotes.indexOf(sourceText) + 1);
    return tree.token().column() + skipLeadingWhiteSpaces;
  }
}
