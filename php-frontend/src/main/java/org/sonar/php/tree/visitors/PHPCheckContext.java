/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.visitors;

import com.google.common.collect.ImmutableList;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.Issue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PHPCheckContext implements CheckContext {

  private final File file;
  private final CompilationUnitTree tree;
  private final SymbolTable symbolTable;
  private List<Issue> issues;

  public PHPCheckContext(File file, CompilationUnitTree tree) {
    this(file, tree, SymbolTableImpl.create(tree));
  }

  public PHPCheckContext(File file, CompilationUnitTree tree, SymbolTable symbolTable) {
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
  public PHPIssue newIssue(String ruleKey, String message) {
    PHPIssue issue = new PHPIssue(ruleKey, message);
    issues.add(issue);

    return issue;
  }

  @Override
  public File file() {
    return file;
  }

  @Override
  public ImmutableList<Issue> getIssues() {
    return ImmutableList.copyOf(issues);
  }

  @Override
  public SymbolTable symbolTable() {
    return symbolTable;
  }

}
