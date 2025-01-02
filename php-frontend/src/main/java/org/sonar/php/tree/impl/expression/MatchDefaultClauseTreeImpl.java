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

import com.sonar.sslr.api.typed.Optional;
import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.MatchDefaultClauseTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class MatchDefaultClauseTreeImpl extends PHPTree implements MatchDefaultClauseTree {

  private static final Kind KIND = Kind.MATCH_DEFAULT_CLAUSE;

  private final SyntaxToken defaultToken;
  @Nullable
  private final SyntaxToken trailingComma;
  private final SyntaxToken doubleArrowToken;
  private final ExpressionTree expression;

  public MatchDefaultClauseTreeImpl(SyntaxToken defaultToken, Optional<SyntaxToken> trailingComma, SyntaxToken doubleArrowToken, ExpressionTree expression) {
    this.defaultToken = defaultToken;
    this.trailingComma = trailingComma.orNull();
    this.doubleArrowToken = doubleArrowToken;
    this.expression = expression;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitMatchDefaultClause(this);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public SyntaxToken defaultToken() {
    return defaultToken;
  }

  @Nullable
  @Override
  public SyntaxToken trailingComma() {
    return trailingComma;
  }

  @Override
  public SyntaxToken doubleArrowToken() {
    return doubleArrowToken;
  }

  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(defaultToken, trailingComma, doubleArrowToken, expression);
  }

}
