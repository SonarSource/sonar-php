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
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.LabelTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import java.util.Iterator;

public class LabelTreeImpl extends PHPTree implements LabelTree {

  private static final Kind KIND = Kind.LABEL;

  private final NameIdentifierTree label;
  private final InternalSyntaxToken colonToken;

  public LabelTreeImpl(NameIdentifierTree label, InternalSyntaxToken colonToken) {
    this.label = label;
    this.colonToken = colonToken;
  }

  @Override
  public NameIdentifierTree label() {
    return label;
  }

  @Override
  public SyntaxToken colonToken() {
    return colonToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(label, colonToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitLabel(this);
  }
}
