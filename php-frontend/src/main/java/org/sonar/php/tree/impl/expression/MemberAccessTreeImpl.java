/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.declaration.ClassNamespaceNameTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import static org.sonar.php.api.PHPPunctuator.NULL_SAFE_ARROW;

public class MemberAccessTreeImpl extends PHPTree implements MemberAccessTree {

  private final Kind kind;

  private ExpressionTree object;
  private final InternalSyntaxToken accessToken;
  private final Tree member;

  public MemberAccessTreeImpl(Kind kind, InternalSyntaxToken accessToken, Tree member) {
    this.kind = kind;
    this.accessToken = accessToken;
    this.member = member;
  }

  public MemberAccessTree complete(ExpressionTree object) {
    if (kind == Kind.CLASS_MEMBER_ACCESS && object.is(Kind.NAMESPACE_NAME)) {
      object = new ClassNamespaceNameTreeImpl((NamespaceNameTree) object);
    }
    this.object = object;

    return this;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public ExpressionTree object() {
    return object;
  }

  @Override
  public SyntaxToken accessToken() {
    return accessToken;
  }

  @Override
  public Tree member() {
    return member;
  }

  @Override
  public boolean isStatic() {
    return PHPPunctuator.DOUBLECOLON.getValue().equals(accessToken.text());
  }

  @Override
  public boolean isNullSafeObjectAccess() {
    return accessToken.text().equals(NULL_SAFE_ARROW.getValue());
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(object, accessToken, member);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitMemberAccess(this);
  }

}
