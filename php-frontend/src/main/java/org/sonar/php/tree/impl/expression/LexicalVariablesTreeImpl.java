/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.tree.impl.expression;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class LexicalVariablesTreeImpl extends PHPTree implements LexicalVariablesTree {

  private static final Kind KIND = Kind.LEXICAL_VARIABLES;

  private final InternalSyntaxToken useToken;
  private final InternalSyntaxToken openParenthesisToken;
  private final SeparatedListImpl variables;
  private final InternalSyntaxToken closeParenthesisToken;

  public LexicalVariablesTreeImpl(InternalSyntaxToken useToken, InternalSyntaxToken openParenthesisToken, SeparatedListImpl variables, InternalSyntaxToken closeParenthesisToken) {
    this.useToken = useToken;
    this.openParenthesisToken = openParenthesisToken;
    this.variables = variables;
    this.closeParenthesisToken = closeParenthesisToken;
  }

  @Override
  public SyntaxToken useToken() {
    return useToken;
  }

  @Override
  public SyntaxToken openParenthesisToken() {
    return openParenthesisToken;
  }

  @Override
  public SeparatedListImpl<VariableTree> variables() {
    return variables;
  }

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
      IteratorUtils.iteratorOf(useToken),
      IteratorUtils.iteratorOf(openParenthesisToken),
      variables.elementsAndSeparators(),
      IteratorUtils.iteratorOf(closeParenthesisToken));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitLexicalVariables(this);
  }

}
