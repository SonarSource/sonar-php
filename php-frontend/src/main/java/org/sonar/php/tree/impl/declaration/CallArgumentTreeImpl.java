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
import javax.annotation.Nullable;
import org.sonar.php.parser.TreeFactory;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class CallArgumentTreeImpl extends PHPTree implements CallArgumentTree {
  private static final Kind KIND = Kind.CALL_ARGUMENT;

  @Nullable
  private final NameIdentifierTree name;
  @Nullable
  private final SyntaxToken nameSeparator;
  private final ExpressionTree value;

  public CallArgumentTreeImpl(@Nullable TreeFactory.Tuple<NameIdentifierTree, InternalSyntaxToken> nameAndToken, ExpressionTree value) {
    this.name = nameAndToken != null ? nameAndToken.first() : null;
    this.nameSeparator = nameAndToken != null ? nameAndToken.second() : null;
    this.value = value;
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

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(name, nameSeparator, value);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitCallArgument(this);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }
}
