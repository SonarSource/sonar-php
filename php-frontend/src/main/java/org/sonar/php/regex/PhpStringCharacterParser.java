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

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonarsource.analyzer.commons.regex.CharacterParser;
import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.ast.IndexRange;
import org.sonarsource.analyzer.commons.regex.ast.SourceCharacter;

public abstract class PhpStringCharacterParser implements CharacterParser {

  final String sourceText;
  final int textLength;
  protected final RegexSource source;
  protected int index = 0;
  /**
   * Will be null if and only if the end of input has been reached
   */
  @Nullable
  private SourceCharacter current;

  private PhpStringCharacterParser(RegexSource source) {
    this.source = source;
    this.sourceText = source.getSourceText();
    this.textLength = sourceText.length();
    moveNext();
  }

  public static CharacterParser forSingleQuotedString(RegexSource source) {
    return new SingleQuotedStringCharacterParser(source);
  }

  public static CharacterParser forDoubleQuotedString(RegexSource source) {
    return new DoubleQuotedStringCharacterParser(source);
  }

  @Override
  public void resetTo(int index) {
    this.index = index;
    moveNext();
  }

  @Override
  public void moveNext() {
    if (index >= textLength) {
      current = null;
      return;
    }
    current = parsePhpCharacter();
  }

  @Override
  @Nonnull
  public SourceCharacter getCurrent() {
    if (current == null) {
      throw new NoSuchElementException();
    }
    return current;
  }

  @Override
  public boolean isAtEnd() {
    return current == null;
  }

  @CheckForNull
  private SourceCharacter parsePhpCharacter() {
    char ch = sourceText.charAt(index);
    if (ch == '\\') {
      if (index + 1 >= textLength) {
        return createCharAndUpdateIndex('\\', 1);
      }
      return parsePhpEscapeSequence();
    }
    return createCharAndUpdateIndex(ch, 1);
  }

  abstract SourceCharacter parsePhpEscapeSequence();

  SourceCharacter createCharAndUpdateIndex(char ch, int length) {
    int startIndex = index;
    index += length;
    return new SourceCharacter(source, new IndexRange(startIndex, index), ch, length > 1);
  }

  private static class SingleQuotedStringCharacterParser extends PhpStringCharacterParser {

    private SingleQuotedStringCharacterParser(RegexSource source) {
      super(source);
    }

    SourceCharacter parsePhpEscapeSequence() {
      char charAfterBackslash = sourceText.charAt(index + 1);
      if (charAfterBackslash == '\'') {
        return createCharAndUpdateIndex('\'', 2);
      } else if (charAfterBackslash == '\\') {
        return createCharAndUpdateIndex('\\', 2);
      } else {
        return createCharAndUpdateIndex('\\', 1);
      }
    }
  }

  private static class DoubleQuotedStringCharacterParser extends PhpStringCharacterParser {

    private static final Pattern UNICODE_PATTERN = Pattern.compile("\\Au\\{([0-9A-Fa-f]+)}");
    private static final Pattern HEX_PATTERN = Pattern.compile("\\Ax([0-9A-Fa-f]{1,2})");
    private static final Pattern OCTAL_PATTERN = Pattern.compile("\\A([0-7]{1,3})");

    private DoubleQuotedStringCharacterParser(RegexSource source) {
      super(source);
    }

    SourceCharacter parsePhpEscapeSequence() {
      char charAfterBackslash = sourceText.charAt(index + 1);
      switch (charAfterBackslash) {
        case '\\':
          return createCharAndUpdateIndex('\\', 2);
        case '"':
          return createCharAndUpdateIndex('"', 2);
        case 'n':
          return createCharAndUpdateIndex('\n', 2);
        case 'r':
          return createCharAndUpdateIndex('\r', 2);
        case 't':
          return createCharAndUpdateIndex('\t', 2);
        case 'f':
          return createCharAndUpdateIndex('\f', 2);
        case 'e':
          return createCharAndUpdateIndex('\u001b', 2);
        case 'v':
          return createCharAndUpdateIndex('\u000b', 2);
        case '$':
          return createCharAndUpdateIndex('$', 2);
        case 'u':
          Matcher unicodeMatcher = UNICODE_PATTERN.matcher(sourceText.substring(index + 1));
          if (unicodeMatcher.find()) {
            String hexValue = unicodeMatcher.group(1);
            return createCharAndUpdateIndex((char) Integer.parseInt(hexValue, 16), hexValue.length() + 4);
          }
          return createCharAndUpdateIndex('\\', 1);
        case 'x':
          Matcher hexMatcher = HEX_PATTERN.matcher(sourceText.substring(index + 1));
          if (hexMatcher.find()) {
            String hexValue = hexMatcher.group(1);
            return createCharAndUpdateIndex((char) Integer.parseInt(hexValue, 16), hexValue.length() + 2);
          }
          return createCharAndUpdateIndex('\\', 1);
        default:
          Matcher octalMatcher = OCTAL_PATTERN.matcher(sourceText.substring(index + 1));
          if (octalMatcher.find()) {
            String octalValue = octalMatcher.group(1);
            return createCharAndUpdateIndex((char) Integer.parseInt(octalValue, 8), octalValue.length() + 1);
          }
          return createCharAndUpdateIndex('\\', 1);
      }
    }
  }
}
