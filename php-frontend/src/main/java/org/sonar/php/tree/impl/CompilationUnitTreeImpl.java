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
package org.sonar.php.tree.impl;

import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class CompilationUnitTreeImpl extends PHPTree implements CompilationUnitTree {

  private static final Kind KIND = Kind.COMPILATION_UNIT;

  private final ScriptTree script;
  private final InternalSyntaxToken eofToken;

  public CompilationUnitTreeImpl(@Nullable ScriptTree script, InternalSyntaxToken eofToken) {
    this.script = script;
    this.eofToken = eofToken;
  }

  @Nullable
  @Override
  public ScriptTree script() {
    return script;
  }

  @Override
  public SyntaxToken eofToken() {
    return eofToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(script, eofToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitCompilationUnit(this);
  }
}
