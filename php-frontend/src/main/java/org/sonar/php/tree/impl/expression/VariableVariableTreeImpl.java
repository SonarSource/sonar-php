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
package org.sonar.php.tree.impl.expression;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableVariableTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class VariableVariableTreeImpl extends PHPTree implements VariableVariableTree {

  private static final Kind KIND = Kind.VARIABLE_VARIABLE;
  private final List<SyntaxToken> dollars;
  private final ExpressionTree variable;

  public VariableVariableTreeImpl(List<InternalSyntaxToken> dollars, ExpressionTree variable) {
    this.dollars = Collections.unmodifiableList(dollars);
    this.variable = variable;
  }

  @Override
  public List<SyntaxToken> dollarTokens() {
    return dollars;
  }

  @Override
  public ExpressionTree variableExpression() {
    return variable;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(dollars.iterator(), IteratorUtils.iteratorOf(variable));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitVariableVariable(this);
  }

}
