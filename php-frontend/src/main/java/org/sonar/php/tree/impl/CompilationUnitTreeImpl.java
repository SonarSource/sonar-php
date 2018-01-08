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
package org.sonar.php.tree.impl;

import com.google.common.collect.Iterators;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

import javax.annotation.Nullable;
import java.util.Iterator;

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
    return Iterators.forArray(script, eofToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitCompilationUnit(this);
  }
}
