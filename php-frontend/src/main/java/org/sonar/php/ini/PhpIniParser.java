/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.ini;

import com.sonar.sslr.api.RecognitionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.php.ini.tree.Directive;
import org.sonar.php.ini.tree.PhpIniFile;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PhpFile;

public class PhpIniParser {

  public PhpIniFile parse(PhpFile file) {
    return parse(file.contents());
  }

  public PhpIniFile parse(String content) {
    List<Directive> directives = new ArrayList<>();
    String[] lines = content.split("\\r?\\n");
    int lineNumber = 1;
    for (String line : lines) {
      Directive directive = parseLine(line, lineNumber);
      if (directive != null) {
        directives.add(directive);
      }
      lineNumber++;
    }
    return new PhpIniFileImpl(directives);
  }

  private static Directive parseLine(String line, int lineNumber) {
    int equalSignIndex = -1;
    boolean insideString = false;
    boolean afterBackSlash = false;

    int currentCharIndex = 0;
    while (currentCharIndex < line.length()) {
      char currentChar = line.charAt(currentCharIndex);
      if (!insideString) {
        if (currentChar == '=') {
          checkState(equalSignIndex < 0, lineNumber, line);
          equalSignIndex = currentCharIndex;
        }
        if (currentChar == ';') {
          break;
        }
      }
      if (currentChar == '"' && !afterBackSlash) {
        insideString = !insideString;
      }
      afterBackSlash = currentChar == '\\';
      currentCharIndex++;
    }

    return createDirective(line, lineNumber, equalSignIndex, currentCharIndex);
  }

  private static void checkState(boolean condition, int lineNumber, String line) {
    if (!condition) {
      throw new RecognitionException(lineNumber, "Cannot parse directive at line " + lineNumber + ":\n" + line);
    }
  }

  private static Directive createDirective(String line, int lineNumber, int equalSignIndex, int endIndex) {
    if (equalSignIndex < 0) {
      return null;
    }
    String name = line.substring(0, equalSignIndex);
    String value = line.substring(equalSignIndex + 1, endIndex);
    checkState(!name.trim().isEmpty(), lineNumber, line);
    return new DirectiveImpl(lineNumber, name, value);
  }

  private static class PhpIniFileImpl implements PhpIniFile {

    private final List<Directive> directives;
    private final Map<String, List<Directive>> directivesByName = new HashMap<>();

    public PhpIniFileImpl(List<Directive> directives) {
      this.directives = Collections.unmodifiableList(directives);
      for (Directive directive : directives) {
        directivesByName.computeIfAbsent(directive.name().text(), key -> new ArrayList<>()).add(directive);
      }
    }

    @Override
    public List<Directive> directives() {
      return directives;
    }

    @Override
    public List<Directive> directivesForName(String directiveName) {
      return directivesByName.getOrDefault(directiveName, Collections.emptyList());
    }

  }

  private static class DirectiveImpl implements Directive {

    private final SyntaxToken name;
    private final SyntaxToken equalSign;
    private final SyntaxToken value;

    public DirectiveImpl(int lineNumber, String name, String value) {
      this.name = createToken(lineNumber, name, 0);
      this.equalSign = createToken(lineNumber, "=", name.length());
      this.value = createToken(lineNumber, value, name.length() + 1);
    }

    private static InternalSyntaxToken createToken(int lineNumber, String untrimmed, int offsetInLine) {
      int column = numberOfLeadingWhiteSpaces(untrimmed) + offsetInLine + 1;
      return new InternalSyntaxToken(lineNumber, column, untrimmed.trim(), Collections.emptyList(), 0, false);
    }

    private static int numberOfLeadingWhiteSpaces(String string) {
      for (int i = 0; i < string.length(); i++) {
        if (!Character.isSpaceChar(string.charAt(i))) {
          return i;
        }
      }
      return string.length();
    }

    @Override
    public SyntaxToken name() {
      return name;
    }

    @Override
    public SyntaxToken equalSign() {
      return equalSign;
    }

    @Override
    public SyntaxToken value() {
      return value;
    }

  }

}
