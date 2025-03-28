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
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class UseClauseTreeImpl extends PHPTree implements UseClauseTree {

  private static final Kind KIND = Kind.USE_CLAUSE;
  private final InternalSyntaxToken useTypeToken;
  private final NamespaceNameTree namespaceName;
  private final InternalSyntaxToken asToken;
  private final NameIdentifierTree alias;

  public UseClauseTreeImpl(@Nullable InternalSyntaxToken useTypeToken, NamespaceNameTree namespaceName, InternalSyntaxToken asToken, NameIdentifierTree alias) {
    this.useTypeToken = useTypeToken;
    this.namespaceName = namespaceName;
    this.asToken = asToken;
    this.alias = alias;
  }

  public UseClauseTreeImpl(@Nullable InternalSyntaxToken useTypeToken, NamespaceNameTree namespaceName) {
    this.useTypeToken = useTypeToken;
    this.namespaceName = namespaceName;
    this.asToken = null;
    this.alias = null;
  }

  @Nullable
  @Override
  public SyntaxToken useTypeToken() {
    return useTypeToken;
  }

  @Override
  public NamespaceNameTree namespaceName() {
    return namespaceName;
  }

  @Nullable
  @Override
  public SyntaxToken asToken() {
    return asToken;
  }

  @Nullable
  @Override
  public NameIdentifierTree alias() {
    return alias;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(useTypeToken, namespaceName, asToken, alias);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitUseClause(this);
  }

}
