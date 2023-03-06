/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.plugins.php.api.visitors;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public class IssueLocation {

  @Nullable
  private final String filePath;
  private final int startLine;
  private final int startLineOffset;
  private final int endLine;
  private final int endLineOffset;
  private final String message;

  private IssueLocation(SyntaxToken firstToken, SyntaxToken lastToken, @Nullable String message) {
    this.filePath = null;
    this.startLine = firstToken.line();
    this.startLineOffset = firstToken.column();
    this.endLine = lastToken.endLine();
    this.endLineOffset = lastToken.endColumn();
    this.message = message;
  }

  public IssueLocation(Tree tree, @Nullable String message) {
    this(tree, tree, message);
  }

  public IssueLocation(Tree startTree, Tree endTree, @Nullable String message) {
    this(((PHPTree) startTree).getFirstToken(), ((PHPTree) endTree).getLastToken(), message);
  }

  public IssueLocation(LocationInFile locationInFile, @Nullable String message) {
    this.filePath = locationInFile.filePath();
    this.startLine = locationInFile.startLine();
    this.startLineOffset = locationInFile.startLineOffset();
    this.endLine = locationInFile.endLine();
    this.endLineOffset = locationInFile.endLineOffset();
    this.message = message;
  }

  public int startLine() {
    return startLine;
  }

  public int startLineOffset() {
    return startLineOffset;
  }

  public int endLine() {
    return endLine;
  }

  public int endLineOffset() {
    return endLineOffset;
  }

  @Nullable
  public String message() {
    return message;
  }

  @CheckForNull
  public String filePath() {
    return filePath;
  }
}
