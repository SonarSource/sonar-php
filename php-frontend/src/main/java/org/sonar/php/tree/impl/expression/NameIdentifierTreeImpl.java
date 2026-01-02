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
package org.sonar.php.tree.impl.expression;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import static java.util.Objects.requireNonNull;

public class NameIdentifierTreeImpl extends PHPTree implements NameIdentifierTree {

  private final InternalSyntaxToken nameToken;
  private static final Kind KIND = Kind.NAME_IDENTIFIER;

  public NameIdentifierTreeImpl(InternalSyntaxToken nameToken) {
    this.nameToken = requireNonNull(nameToken);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public SyntaxToken token() {
    return nameToken;
  }

  @Override
  public String text() {
    return token().text();
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(nameToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitNameIdentifier(this);
  }
}
