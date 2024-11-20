/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.tree.impl.expression;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.SpreadArgumentTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class SpreadArgumentTreeImpl extends PHPTree implements SpreadArgumentTree {

  private static final Kind KIND = Kind.SPREAD_ARGUMENT;
  private final InternalSyntaxToken ellipsisToken;
  private final ExpressionTree argument;

  public SpreadArgumentTreeImpl(InternalSyntaxToken ellipsisToken, ExpressionTree argument) {
    this.ellipsisToken = ellipsisToken;
    this.argument = argument;
  }

  @Override
  public SyntaxToken ellipsisToken() {
    return ellipsisToken;
  }

  @Override
  public ExpressionTree argument() {
    return argument;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(ellipsisToken, argument);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitSpreadArgument(this);
  }

}
