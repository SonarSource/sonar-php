/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ExpandableStringCharactersTreeImpl extends PHPTree implements ExpandableStringCharactersTree {

  private final Kind kind;
  private final SyntaxToken token;

  public ExpandableStringCharactersTreeImpl(Kind kind, SyntaxToken token) {
    this.kind = kind;
    this.token = token;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public String value() {
    return token.text();
  }

  @Override
  public SyntaxToken token() {
    return token;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(token);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitExpandableStringCharacters(this);
  }
}
