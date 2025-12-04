/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.tree.impl;

import java.util.Iterator;
import java.util.List;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class ScriptTreeImpl extends PHPTree implements ScriptTree {

  private static final Kind KIND = Kind.SCRIPT;

  private final InternalSyntaxToken fileOpeningTagToken;
  private final List<StatementTree> statements;

  public ScriptTreeImpl(InternalSyntaxToken fileOpeningTagToken, List<StatementTree> statements) {
    this.fileOpeningTagToken = fileOpeningTagToken;
    this.statements = statements;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(fileOpeningTagToken),
      statements.iterator());
  }

  @Override
  public SyntaxToken fileOpeningTagToken() {
    return fileOpeningTagToken;
  }

  @Override
  public List<StatementTree> statements() {
    return statements;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitScript(this);
  }
}
