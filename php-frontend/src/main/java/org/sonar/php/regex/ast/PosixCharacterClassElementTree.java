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
package org.sonar.php.regex.ast;

import javax.annotation.Nonnull;
import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.ast.AbstractRegexSyntaxElement;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassElementTree;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.IndexRange;
import org.sonarsource.analyzer.commons.regex.ast.RegexVisitor;
import org.sonarsource.analyzer.commons.regex.ast.SourceCharacter;

public class PosixCharacterClassElementTree extends AbstractRegexSyntaxElement implements CharacterClassElementTree {

  private final String property;
  private final FlagSet activeFlags;
  private final boolean isNegation;

  public PosixCharacterClassElementTree(RegexSource source, IndexRange range, boolean isNegation, String property, FlagSet activeFlags) {
    super(source, range);
    this.isNegation = isNegation;
    this.property = property;
    this.activeFlags = activeFlags;
  }

  public PosixCharacterClassElementTree(RegexSource source, SourceCharacter openBracket, SourceCharacter closingBracket, boolean isNegation, String property, FlagSet activeFlags) {
    this(source, openBracket.getRange().merge(closingBracket.getRange()), isNegation, property, activeFlags);
  }

  @Nonnull
  @Override
  public CharacterClassElementTree.Kind characterClassElementKind() {
    return CharacterClassElementTree.Kind.POSIX_CLASS;
  }

  @Override
  public void accept(RegexVisitor visitor) {
    // do nothing
  }

  public boolean isNegation() {
    return isNegation;
  }

  @Nonnull
  public String property() {
    return property;
  }

  @Nonnull
  @Override
  public FlagSet activeFlags() {
    return activeFlags;
  }
}
