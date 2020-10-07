/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.php.tree.impl.declaration;

import org.sonar.php.parser.TreeFactory;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionCallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;

public class FunctionCallArgumentTreeImpl implements FunctionCallArgumentTree {
  @Nullable
  private final NameIdentifierTree name;
  @Nullable
  private final SyntaxToken nameSeparator;
  private final ExpressionTree value;

  public FunctionCallArgumentTreeImpl(@Nullable TreeFactory.Tuple<NameIdentifierTree, InternalSyntaxToken> nameAndToken, ExpressionTree value) {
    this.name = nameAndToken != null ? nameAndToken.first() : null;
    this.nameSeparator = nameAndToken != null ? nameAndToken.second() : null;
    this.value = value;
  }

  @Override
  public boolean is(Kind... kind) {
    return false;
  }

  @Override
  public void accept(VisitorCheck visitor) {

  }

  @Override
  public Kind getKind() {
    return null;
  }

  @Nullable
  @Override
  public Tree getParent() {
    return null;
  }

  @Nullable
  public NameIdentifierTree name() {
    return name;
  }

  @Nullable
  public SyntaxToken separator() {
    return nameSeparator;
  }

  public ExpressionTree value() {
    return value;
  }
}
