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
package org.sonar.php.tree.visitors;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.FileIssue;
import org.sonar.plugins.php.api.visitors.IssueLocation;
import org.sonar.plugins.php.api.visitors.LineIssue;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

public class PHPCheckContext implements CheckContext {

  private final PhpFile file;
  private final CompilationUnitTree tree;
  private final SymbolTable symbolTable;
  private List<PhpIssue> issues;

  public PHPCheckContext(PhpFile file, CompilationUnitTree tree) {
    this(file, tree, SymbolTableImpl.create(tree));
  }

  public PHPCheckContext(PhpFile file, CompilationUnitTree tree, SymbolTable symbolTable) {
    this.file = file;
    this.tree = tree;
    this.symbolTable = symbolTable;
    this.issues = new ArrayList<>();
  }

  @Override
  public CompilationUnitTree tree() {
    return tree;
  }

  @Override
  public LegacyIssue newIssue(PHPCheck check, String message) {
    LegacyIssue issue = new LegacyIssue(check, message);
    issues.add(issue);

    return issue;
  }

  @Override
  public PreciseIssue newIssue(PHPCheck check, Tree tree, String message) {
    PreciseIssue issue = new PreciseIssue(check, new IssueLocation(tree, message));
    issues.add(issue);

    return issue;
  }

  @Override
  public PreciseIssue newIssue(PHPCheck check, Tree startTree, Tree endTree, String message) {
    PreciseIssue issue = new PreciseIssue(check, new IssueLocation(startTree, endTree, message));
    issues.add(issue);

    return issue;
  }

  @Override
  public LineIssue newLineIssue(PHPCheck check, int line, String message) {
    LineIssue issue = new LineIssue(check, line, message);
    issues.add(issue);

    return issue;
  }

  @Override
  public FileIssue newFileIssue(PHPCheck check, String message) {
    FileIssue issue = new FileIssue(check, message);
    issues.add(issue);

    return issue;
  }

  @Override
  public PhpFile getPhpFile() {
    return file;
  }

  @Override
  public ImmutableList<PhpIssue> getIssues() {
    return ImmutableList.copyOf(issues);
  }

  @Override
  public SymbolTable symbolTable() {
    return symbolTable;
  }

}
