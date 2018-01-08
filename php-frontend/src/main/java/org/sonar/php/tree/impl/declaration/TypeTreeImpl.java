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
package org.sonar.php.tree.impl.declaration;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.TypeNameTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class TypeTreeImpl extends PHPTree implements TypeTree {

  private static final Kind KIND = Kind.TYPE;

  private final SyntaxToken questionMarkToken;

  private final TypeNameTree typeName;

  public TypeTreeImpl(@Nullable SyntaxToken questionMarkToken, TypeNameTree typeName) {
    this.questionMarkToken = questionMarkToken;
    this.typeName = typeName;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.forArray(questionMarkToken, typeName);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitType(this);
  }

  @Override
  @Nullable
  public SyntaxToken questionMarkToken() {
    return questionMarkToken;
  }

  @Override
  public TypeNameTree typeName() {
    return typeName;
  }

}
