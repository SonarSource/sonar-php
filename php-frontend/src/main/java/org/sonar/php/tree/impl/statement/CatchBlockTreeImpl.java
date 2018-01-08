/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.tree.impl.statement;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
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
      VariableIdentifierTreeImpl variable,
      InternalSyntaxToken rParenthsis,
      BlockTree block
  ) {
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
    return Iterators.concat(
      Iterators.forArray(catchToken, lParenthesis),
      exceptionTypes.elementsAndSeparators(),
      Iterators.forArray(variable, rParenthsis, block));
  }

}
