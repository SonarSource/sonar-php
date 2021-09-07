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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.php.regex.ast.PosixCharacterClassTree;
import org.sonarsource.analyzer.commons.regex.RegexParser;
import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassElementTree;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.SourceCharacter;

public class PhpRegexParser extends RegexParser {

  private static final String POSIX_CHARACTER_CLASS_PATTERN = "[:%s%s:]";
  private static final Set<String> POSIX_CHARACTER_CLASSES = new HashSet<>(Arrays.asList(
    "alnum", "alpha", "ascii", "blank", "cntrl", "digit", "graph", "lower", "print", "punct", "space", "upper", "word", "xdigit", "<", ">"
  ));
  private static final Map<String, String> POSIX_CHARACTER_CLASS_LOOKUP = posixCharacterClassMap(false);
  private static final Map<String, String> POSIX_CHARACTER_CLASS_NEGATION_LOOKUP = posixCharacterClassMap(true);
  private static Map<String, String> posixCharacterClassMap(boolean negative) {
    return POSIX_CHARACTER_CLASSES.stream()
      .collect(Collectors.toMap(posix -> String.format(POSIX_CHARACTER_CLASS_PATTERN, negative ? "^" : "", posix), posix -> posix));
  }

  public PhpRegexParser(RegexSource source, FlagSet initialFlags) {
    super(source, initialFlags);
  }

  @Override
  protected CharacterClassElementTree parsePosixClassOrCharacterClass() {
    if (characters.lookAhead(1) == ':') {
      SourceCharacter openingBracket = characters.getCurrent();
      boolean isNegation = characters.lookAhead(2) == '^';
      Map<String, String> posixLookup = isNegation ? POSIX_CHARACTER_CLASS_NEGATION_LOOKUP : POSIX_CHARACTER_CLASS_LOOKUP;
      Optional<Map.Entry<String, String>> posixClass = posixLookup.entrySet().stream()
        .filter(posix -> characters.currentIs(posix.getKey())).findFirst();
      if (posixClass.isPresent()) {
        characters.moveNext(posixClass.get().getKey().length());
        return new PosixCharacterClassTree(source, openingBracket, characters.getCurrent(), posixClass.get().getValue(), isNegation, activeFlags);
      }
    }
    return parseCharacterClass();
  }

}
