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
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedList;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.TreeVisitor;

import com.google.common.base.Functions;
import com.google.common.collect.Iterators;

public class ParameterListTreeImpl extends PHPTree implements ParameterListTree {

  private static final Kind KIND = Kind.PARAMETER_LIST;
  private InternalSyntaxToken openParenthesis;
  private SeparatedList<ParameterTree> parameters;
  private InternalSyntaxToken closeParenthesis;

  public ParameterListTreeImpl(
    InternalSyntaxToken openParenthesis, 
    SeparatedList<ParameterTree> parameters, 
    InternalSyntaxToken closeParenthesis
    ) {
      this.openParenthesis = openParenthesis;
      this.parameters = parameters;
      this.closeParenthesis = closeParenthesis;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesis;
  }
  
  @Override
  public SeparatedList<ParameterTree> parameters() {
    return parameters;
  }
  
  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesis;
  }
  
  @Override
  public Iterator<Tree> childrenIterator() {
    return Iterators.concat(
      Iterators.singletonIterator(openParenthesis),
      parameters.elementsAndSeparators(Functions.<ParameterTree>identity()),
      Iterators.singletonIterator(closeParenthesis));
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitParameterList(this);
  }

}
