/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
import org.sonar.plugins.php.api.tree.expression.ComputedVariableTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ComputedVariableTreeImpl extends PHPTree implements ComputedVariableTree {

  private static final Kind KIND = Kind.COMPUTED_VARIABLE_NAME;

  private final InternalSyntaxToken openCurly;
  private final ExpressionTree expression;
  private final InternalSyntaxToken closeCurly;

  public ComputedVariableTreeImpl(InternalSyntaxToken openCurly, ExpressionTree expression, InternalSyntaxToken closeCurly) {
    this.openCurly = openCurly;
    this.expression = expression;
    this.closeCurly = closeCurly;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public SyntaxToken openCurlyBraceToken() {
    return openCurly;
  }

  @Override
  public ExpressionTree variableExpression() {
    return expression;
  }

  @Override
  public SyntaxToken closeCurlyBraceToken() {
    return closeCurly;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(openCurly, expression, closeCurly);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitComputedVariable(this);
  }

}
