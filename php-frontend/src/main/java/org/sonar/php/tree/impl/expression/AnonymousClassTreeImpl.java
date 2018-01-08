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
package org.sonar.php.tree.impl.expression;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class AnonymousClassTreeImpl extends PHPTree implements AnonymousClassTree {

  private static final Kind KIND = Kind.ANONYMOUS_CLASS;

  private final SyntaxToken classToken;
  private final SyntaxToken openParenthesisToken;
  private final SeparatedList<ExpressionTree> arguments;
  private final SyntaxToken closeParenthesisToken;
  private final SyntaxToken extendsToken;
  private final NamespaceNameTree superClass;
  private final SyntaxToken implementsToken;
  private final SeparatedListImpl<NamespaceNameTree> superInterfaces;
  private final SyntaxToken openCurlyBraceToken;
  private final List<ClassMemberTree> members;
  private final SyntaxToken closeCurlyBraceToken;

  public AnonymousClassTreeImpl(
    SyntaxToken classToken,
    @Nullable SyntaxToken openParenthesisToken, SeparatedList<ExpressionTree> arguments, @Nullable SyntaxToken closeParenthesisToken,
    @Nullable SyntaxToken extendsToken, @Nullable NamespaceNameTree superClass,
    @Nullable SyntaxToken implementsToken, @Nullable SeparatedListImpl<NamespaceNameTree> superInterfaces,
    SyntaxToken openCurlyBraceToken,
    List<ClassMemberTree> members,
    SyntaxToken closeCurlyBraceToken
  ) {
    this.classToken = classToken;
    this.openParenthesisToken = openParenthesisToken;
    this.arguments = arguments;
    this.closeParenthesisToken = closeParenthesisToken;
    this.extendsToken = extendsToken;
    this.superClass = superClass;
    this.implementsToken = implementsToken;
    this.superInterfaces = superInterfaces;
    this.openCurlyBraceToken = openCurlyBraceToken;
    this.members = members;
    this.closeCurlyBraceToken = closeCurlyBraceToken;
  }

  @Override
  public SyntaxToken classToken() {
    return classToken;
  }

  @Nullable
  @Override
  public SyntaxToken openParenthesisToken() {
    return openCurlyBraceToken;
  }

  @Override
  public SeparatedList<ExpressionTree> arguments() {
    return arguments;
  }

  @Nullable
  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeCurlyBraceToken;
  }

  @Nullable
  @Override
  public SyntaxToken extendsToken() {
    return extendsToken;
  }

  @Nullable
  @Override
  public NamespaceNameTree superClass() {
    return superClass;
  }

  @Nullable
  @Override
  public SyntaxToken implementsToken() {
    return implementsToken;
  }

  @Override
  public SeparatedList<NamespaceNameTree> superInterfaces() {
    return superInterfaces;
  }

  @Override
  public SyntaxToken openCurlyBraceToken() {
    return openCurlyBraceToken;
  }

  @Override
  public List<ClassMemberTree> members() {
    return members;
  }

  @Override
  public SyntaxToken closeCurlyBraceToken() {
    return closeCurlyBraceToken;
  }

  @Nullable
  @Override
  public MethodDeclarationTree fetchConstructor() {
    MethodDeclarationTree constructor = null;

    for (ClassMemberTree member : members) {
      if (member.is(Kind.METHOD_DECLARATION)) {
        MethodDeclarationTree method = (MethodDeclarationTree) member;
        String methodName = method.name().text();

        if (PHP5_CONSTRUCTOR_NAME.equalsIgnoreCase(methodName)) {
          constructor = method;
        }
      }
    }

    return constructor;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.forArray(classToken, openParenthesisToken),
      arguments.elementsAndSeparators(),
      Iterators.forArray(closeParenthesisToken, extendsToken, superClass, implementsToken),
      superInterfaces.elementsAndSeparators(),
      Iterators.singletonIterator(openCurlyBraceToken),
      members.iterator(),
      Iterators.singletonIterator(closeCurlyBraceToken)
    );
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitAnonymousClass(this);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }
}
