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
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExecutionOperatorTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ExecutionOperatorTreeImpl extends PHPTree implements ExecutionOperatorTree {

  private static final Kind KIND = Kind.EXECUTION_OPERATOR;

  private final ExpandableStringLiteralTree literal;

  public ExecutionOperatorTreeImpl(ExpandableStringLiteralTree literal) {
    this.literal = literal;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(literal);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitExecutionOperator(this);
  }

  @Override
  public ExpandableStringLiteralTree literal() {
    return literal;
  }
}
