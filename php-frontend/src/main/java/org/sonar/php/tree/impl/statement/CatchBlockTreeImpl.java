/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
package org.sonar.php.tree.impl.statement;

import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class CatchBlockTreeImpl extends PHPTree implements CatchBlockTree {

  private static final Kind KIND = Kind.CATCH_BLOCK;

  private final InternalSyntaxToken catchToken;
  private final InternalSyntaxToken lParenthesis;
  private final SeparatedList<NamespaceNameTree> exceptionTypes;
  private final VariableIdentifierTreeImpl variable;
  private final InternalSyntaxToken rParenthsis;
  private final BlockTree block;

  public CatchBlockTreeImpl(
    InternalSyntaxToken catchToken,
    InternalSyntaxToken lParenthesis,
    SeparatedList<NamespaceNameTree> exceptionTypes,
    @Nullable VariableIdentifierTreeImpl variable,
    InternalSyntaxToken rParenthsis,
    BlockTree block) {
    this.catchToken = catchToken;
    this.lParenthesis = lParenthesis;
    this.exceptionTypes = exceptionTypes;
    this.variable = variable;
    this.rParenthsis = rParenthsis;
    this.block = block;
  }

  @Override
  public SyntaxToken catchToken() {
    return catchToken;
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return lParenthesis;
  }

  @Override
  public SeparatedList<NamespaceNameTree> exceptionTypes() {
    return exceptionTypes;
  }

  @Nullable
  @Override
  public VariableIdentifierTree variable() {
    return variable;
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return rParenthsis;
  }

  @Override
  public BlockTree block() {
    return block;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitCatchBlock(this);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(catchToken, lParenthesis),
      exceptionTypes.elementsAndSeparators(),
      IteratorUtils.iteratorOf(variable, rParenthsis, block));
  }

}
