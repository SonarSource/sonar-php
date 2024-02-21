/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.tree.impl.expression;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.php.symbols.FunctionSymbol;
import org.sonar.php.symbols.UnknownFunctionSymbol;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class FunctionCallTreeImpl extends PHPTree implements FunctionCallTree {

  private static final Kind KIND = Kind.FUNCTION_CALL;
  private static final QualifiedName UNKNOWN_FUNCTION_NAME = QualifiedName.qualifiedName("<unknown_function>");
  private ExpressionTree callee;
  private final InternalSyntaxToken openParenthesisToken;
  private final SeparatedListImpl<ExpressionTree> arguments;
  private final SeparatedList<CallArgumentTree> callArguments;
  private final InternalSyntaxToken closeParenthesisToken;
  private FunctionSymbol symbol = new UnknownFunctionSymbol(UNKNOWN_FUNCTION_NAME);

  public FunctionCallTreeImpl(ExpressionTree callee, InternalSyntaxToken openParenthesisToken, SeparatedListImpl<CallArgumentTree> callArguments,
    InternalSyntaxToken closeParenthesisToken) {
    this.callee = callee;
    this.openParenthesisToken = openParenthesisToken;
    this.arguments = argumentsValueList(callArguments);
    this.callArguments = callArguments;
    this.closeParenthesisToken = closeParenthesisToken;
  }

  public FunctionCallTreeImpl(ExpressionTree callee, SeparatedListImpl<CallArgumentTree> callArguments) {
    this.callee = callee;
    this.openParenthesisToken = null;
    this.arguments = argumentsValueList(callArguments);
    this.callArguments = callArguments;
    this.closeParenthesisToken = null;
  }

  public FunctionCallTreeImpl(InternalSyntaxToken openParenthesisToken, SeparatedListImpl<CallArgumentTree> callArguments, InternalSyntaxToken closeParenthesisToken) {
    this.openParenthesisToken = openParenthesisToken;
    this.arguments = argumentsValueList(callArguments);
    this.callArguments = callArguments;
    this.closeParenthesisToken = closeParenthesisToken;
  }

  public FunctionCallTreeImpl complete(ExpressionTree callee) {
    this.callee = callee;

    return this;
  }

  private static SeparatedListImpl<ExpressionTree> argumentsValueList(SeparatedListImpl<CallArgumentTree> arguments) {
    List<ExpressionTree> argumentValues = arguments.stream()
      .map(CallArgumentTree::value)
      .collect(Collectors.toList());

    return new SeparatedListImpl<>(argumentValues, arguments.getSeparators());
  }

  @Override
  public ExpressionTree callee() {
    return callee;
  }

  @Nullable
  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesisToken;
  }

  /**
   * @deprecated since 3.11 . Use {@link #callArguments()} instead.
   */
  @Deprecated
  @Override
  public SeparatedListImpl<ExpressionTree> arguments() {
    return arguments;
  }

  @Override
  public SeparatedList<CallArgumentTree> callArguments() {
    return callArguments;
  }

  @Nullable
  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesisToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(callee),
      IteratorUtils.iteratorOf(openParenthesisToken),
      callArguments.elementsAndSeparators(),
      IteratorUtils.iteratorOf(closeParenthesisToken));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitFunctionCall(this);
  }

  public FunctionSymbol symbol() {
    return symbol;
  }

  public void setSymbol(FunctionSymbol symbol) {
    this.symbol = symbol;
  }
}
