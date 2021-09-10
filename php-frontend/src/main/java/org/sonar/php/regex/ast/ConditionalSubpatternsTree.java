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
import javax.annotation.Nullable;
import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.GroupTree;
import org.sonarsource.analyzer.commons.regex.ast.IndexRange;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexVisitor;
import org.sonarsource.analyzer.commons.regex.ast.SourceCharacter;

public class ConditionalSubpatternsTree extends GroupTree {

  private final GroupTree condition;
  private final RegexTree yesPattern;
  @Nullable
  private final SourceCharacter pipe;
  @Nullable
  private final RegexTree noPattern;

  public ConditionalSubpatternsTree(RegexSource source, SourceCharacter openingParen, SourceCharacter closingParen, GroupTree condition,
    RegexTree yesPattern, FlagSet activeFlags) {
    this(source, openingParen, closingParen, condition, yesPattern, null, null, activeFlags);
  }

  public ConditionalSubpatternsTree(RegexSource source, SourceCharacter openingParen, SourceCharacter closingParen, GroupTree condition,
    RegexTree yesPattern, @Nullable SourceCharacter pipe, @Nullable RegexTree noPattern, FlagSet activeFlags) {
    this(source, openingParen.getRange().merge(closingParen.getRange()), condition, yesPattern, pipe, noPattern, activeFlags);
  }

  public ConditionalSubpatternsTree(RegexSource source, IndexRange range, GroupTree condition, RegexTree yesPattern, @Nullable SourceCharacter pipe,
    @Nullable RegexTree noPattern, FlagSet activeFlags) {
    super(source, Kind.CONDITIONAL_SUBPATTERNS, null, range, activeFlags);
    this.condition = condition;
    this.yesPattern = yesPattern;
    this.pipe = pipe;
    this.noPattern = noPattern;
  }

  @Override
  public void accept(RegexVisitor visitor) {
    if (visitor instanceof PhpRegexBaseVisitor) {
      ((PhpRegexBaseVisitor) visitor).visitConditionalSubpatterns(this);
    }
  }

  @Nonnull
  @Override
  public TransitionType incomingTransitionType() {
    return TransitionType.EPSILON;
  }

  public GroupTree getCondition() {
    return condition;
  }

  public RegexTree getYesPattern() {
    return yesPattern;
  }

  @Nullable
  public SourceCharacter getPipe() {
    return pipe;
  }

  @Nullable
  public RegexTree getNoPattern() {
    return noPattern;
  }
}
