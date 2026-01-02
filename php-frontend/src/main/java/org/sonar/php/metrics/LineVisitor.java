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
package org.sonar.php.metrics;

import java.util.HashSet;
import java.util.Set;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class LineVisitor extends PHPVisitorCheck {

  private Set<Integer> lines = new HashSet<>();

  private LineVisitor(Tree tree) {
    tree.accept(this);
  }

  public LineVisitor(CompilationUnitTree tree) {
    this.visitCompilationUnit(tree);
  }

  public static int linesOfCode(Tree tree) {
    return new LineVisitor(tree).getLinesOfCodeNumber();
  }

  @Override
  public void visitScript(ScriptTree tree) {
    // ignore opening tag
    this.scan(tree.statements());
  }

  @Override
  public void visitToken(SyntaxToken token) {
    boolean isEOF = ((InternalSyntaxToken) token).isEOF();

    if (token.is(Tree.Kind.TOKEN) && !isEOF) {
      String[] tokenLines = token.text().split("\n", -1);
      for (int i = token.line(); i < token.line() + tokenLines.length; i++) {
        lines.add(i);
      }

    }
  }

  public int getLinesOfCodeNumber() {
    return lines.size();
  }

  public Set<Integer> getLinesOfCode() {
    return lines;
  }

}
