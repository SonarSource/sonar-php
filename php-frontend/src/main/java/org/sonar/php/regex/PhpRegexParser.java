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
import javax.annotation.Nullable;
import org.sonar.php.regex.ast.ConditionalSubpatternsTree;
import org.sonar.php.regex.ast.ReferenceConditionTree;
import org.sonar.php.regex.ast.PosixCharacterClassElementTree;
import org.sonarsource.analyzer.commons.regex.RegexParser;
import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassElementTree;
import org.sonarsource.analyzer.commons.regex.ast.CharacterTree;
import org.sonarsource.analyzer.commons.regex.ast.DisjunctionTree;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.GroupTree;
import org.sonarsource.analyzer.commons.regex.ast.IndexRange;
import org.sonarsource.analyzer.commons.regex.ast.LookAroundTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.SequenceTree;
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
  protected CharacterClassElementTree parseCharacterClassElement(boolean isAtBeginning) {
    if (characters.lookAhead(1) == ':') {
      SourceCharacter openingBracket = characters.getCurrent();
      boolean isNegation = characters.lookAhead(2) == '^';
      Map<String, String> posixLookup = isNegation ? POSIX_CHARACTER_CLASS_NEGATION_LOOKUP : POSIX_CHARACTER_CLASS_LOOKUP;
      Optional<Map.Entry<String, String>> posixClass = posixLookup.entrySet().stream()
        .filter(posix -> characters.currentIs(posix.getKey())).findFirst();
      if (posixClass.isPresent()) {
        characters.moveNext(posixClass.get().getKey().length());
        return new PosixCharacterClassElementTree(source, openingBracket, characters.getCurrent(), isNegation, posixClass.get().getValue(), activeFlags);
      }
    }
    return super.parseCharacterClassElement(isAtBeginning);
  }

  @Override
  protected GroupTree parseNonCapturingGroup(SourceCharacter openingParen) {
    if (characters.currentIs("?R)")) {
      characters.moveNext();
    }
    if (characters.currentIs("?(")) {
      return parseConditionalSubpattern(openingParen);
    }
    return super.parseNonCapturingGroup(openingParen);
  }

  private GroupTree parseConditionalSubpattern(SourceCharacter openingParen) {
    // Discard '?'
    characters.moveNext();
    GroupTree condition = parseCondition();
    RegexTree subpattern = parseDisjunction();
    SourceCharacter closingParen = characters.getCurrent();
    characters.moveNext();
    if (subpattern.is(RegexTree.Kind.DISJUNCTION)) {
      if (((DisjunctionTree) subpattern).getAlternatives().size() > 2) {
        error("More than two alternatives in the subpattern");
      }
      DisjunctionTree disjunction = (DisjunctionTree) subpattern;
      return new ConditionalSubpatternsTree(source, openingParen, closingParen, condition, disjunction.getAlternatives().get(0),
        disjunction.getOrOperators().get(0), disjunction.getAlternatives().get(1), activeFlags);
    } else {
      return new ConditionalSubpatternsTree(source, openingParen, closingParen, condition, subpattern, activeFlags);
    }
  }

  private GroupTree parseCondition() {
    SourceCharacter openingParen = characters.getCurrent();
    characters.moveNext();
    if (characters.currentIs("?=")) {
      characters.moveNext(2);
      return finishGroup(openingParen, (range, inner) -> LookAroundTree.positiveLookAhead(source, range, inner, activeFlags));
    } else if (characters.currentIs("?<=")) {
      characters.moveNext(3);
      return finishGroup(openingParen, (range, inner) -> LookAroundTree.positiveLookBehind(source, range, inner, activeFlags));
    } else if (characters.currentIs("?!")) {
      characters.moveNext(2);
      return finishGroup(openingParen, (range, inner) -> LookAroundTree.negativeLookAhead(source, range, inner, activeFlags));
    } else if (characters.currentIs("?<!")) {
      characters.moveNext(3);
      return finishGroup(openingParen, (range, inner) -> LookAroundTree.negativeLookBehind(source, range, inner, activeFlags));
    } else if (characters.currentIs("+")) {
      // Skip '+' as first character since it would be identified as quantifier at the beginning of a sequence
      CharacterTree plus = readCharacter();
      return finishGroup(openingParen, (range, inner) -> reference(source, range, plus, inner, activeFlags));
    } else {
      // TODO Allow only valid conditions: signed sequence of digits or 'R'
      return finishGroup(openingParen, (range, inner) -> reference(source, range, null, inner, activeFlags));
    }
  }

  public ReferenceConditionTree reference(RegexSource source, IndexRange range, @Nullable CharacterTree plus, RegexTree inner, FlagSet activeFlags) {
    StringBuilder reference = new StringBuilder();
    if (plus != null) {
      reference.append('+');
    }
    if (inner.is(RegexTree.Kind.CHARACTER)) {
      reference.append(((CharacterTree) inner).characterAsString());
    } else if (inner.is(RegexTree.Kind.SEQUENCE)){
      ((SequenceTree) inner).getItems().stream()
        .filter(CharacterTree.class::isInstance)
        .map(i -> ((CharacterTree) i).characterAsString())
        .forEach(reference::append);
    } else {
      error("Conditional subpattern has invalid condition.");
    }
    return new ReferenceConditionTree(source, range, reference.toString(), activeFlags);
  }
}
