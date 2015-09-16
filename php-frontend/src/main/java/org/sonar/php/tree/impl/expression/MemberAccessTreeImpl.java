/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.impl.expression;

import java.util.Iterator;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import com.google.common.collect.Iterators;

public class MemberAccessTreeImpl extends PHPTree implements MemberAccessTree {

  private final Kind KIND = Kind.OBJECT_MEMBER_ACCESS;

  private ExpressionTree object;
  private final InternalSyntaxToken accessToken;
  private final ExpressionTree member;


  public MemberAccessTreeImpl(InternalSyntaxToken accessToken, ExpressionTree member) {
    this.accessToken = accessToken;
    this.member = member;
  }

  public MemberAccessTree complete(ExpressionTree object) {
    this.object = object;

    return this;
  }

  @Override
  public Kind getKind() {
    return KIND;
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
  public ExpressionTree member() {
    return member;
  }

  @Override
  public boolean isStatic() {
    return false;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(object, accessToken, member);
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitMemberAccess(this);
  }

}
