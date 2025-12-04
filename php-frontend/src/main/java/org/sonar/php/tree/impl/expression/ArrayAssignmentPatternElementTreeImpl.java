/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.tree.impl.expression;

import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ArrayAssignmentPatternElementTreeImpl extends PHPTree implements ArrayAssignmentPatternElementTree {

  private static final Kind KIND = Kind.ARRAY_ASSIGNMENT_PATTERN_ELEMENT;
  private final ExpressionTree key;
  private final InternalSyntaxToken doubleArrow;
  private final Tree variable;

  public ArrayAssignmentPatternElementTreeImpl(ExpressionTree key, InternalSyntaxToken doubleArrow, Tree variable) {
    this.key = key;
    this.doubleArrow = doubleArrow;
    this.variable = variable;
  }

  public ArrayAssignmentPatternElementTreeImpl(Tree variable) {
    this(null, null, variable);
  }

  @Nullable
  @Override
  public ExpressionTree key() {
    return key;
  }

  @Nullable
  @Override
  public SyntaxToken doubleArrowToken() {
    return doubleArrow;
  }

  @Override
  public Tree variable() {
    return variable;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(key, doubleArrow, variable);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitArrayAssignmentPatternElement(this);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }
}
