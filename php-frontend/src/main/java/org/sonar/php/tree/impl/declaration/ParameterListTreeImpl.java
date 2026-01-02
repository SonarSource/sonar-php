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
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ParameterListTreeImpl extends PHPTree implements ParameterListTree {

  private static final Kind KIND = Kind.PARAMETER_LIST;
  private InternalSyntaxToken openParenthesis;
  private SeparatedListImpl<ParameterTree> parameters;
  private InternalSyntaxToken closeParenthesis;

  public ParameterListTreeImpl(
    InternalSyntaxToken openParenthesis,
    SeparatedListImpl<ParameterTree> parameters,
    InternalSyntaxToken closeParenthesis) {
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
  public SeparatedListImpl<ParameterTree> parameters() {
    return parameters;
  }

  @Override
  public SyntaxToken closeParenthesisToken() {
    return closeParenthesis;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(openParenthesis),
      parameters.elementsAndSeparators(),
      IteratorUtils.iteratorOf(closeParenthesis));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitParameterList(this);
  }

}
