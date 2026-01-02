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
package org.sonar.php.tree.impl.statement;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.LabelTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

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
    return IteratorUtils.iteratorOf(label, colonToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitLabel(this);
  }
}
