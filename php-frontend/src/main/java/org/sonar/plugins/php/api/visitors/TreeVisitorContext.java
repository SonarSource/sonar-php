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
package org.sonar.plugins.php.api.visitors;

import com.google.common.annotations.Beta;
import org.sonar.plugins.php.api.tree.Tree;

import java.io.File;

@Beta
public interface TreeVisitorContext {

  /**
   * @return the top tree node of the current file AST representation.
   */
  //fixme ScriptTree
  Tree getTopTree();

  /**
   * Creates an issue.
   *
   * @param check instance of the check that creates the issue.
   * @param tree the tree on which the issue should be raise. Means the issue will be raised on its corresponding line in the source code.
   * @param message the issue message.
   */
  void addIssue(PHPCheck check, Tree tree, String message);

  /**
   * Creates an issue.
   *
   * @param check instance of the check that create the issue
   * @param line source line on which the issue should be raised
   * @param message the issue message
   */
  void addIssue(PHPCheck check, int line, String message);

  /**
   * Creates an issue.
   *
   * @param check instance of the check that create the issue
   * @param tree the tree on which the issue should be raise. Means the issue will be raised on its corresponding line in the source code.
   * @param message the issue message
   * @param cost specific remediation cost for the issue, used to compute the technical debt
   */
  void addIssue(PHPCheck check, Tree tree, String message, double cost);

  /**
   * Creates an issue.
   *
   * @param check instance of the check that create the issue
   * @param line source line on which the issue should be raised
   * @param message the issue message
   * @param cost specific remediation cost for the issue, used to compute the technical debt
   */
  void addIssue(PHPCheck check, int line, String message, double cost);

  /**
   * Creates an issue at a file level.
   *
   * @param check instance of the check that create the issue
   * @param message the issue message
   */
  void addFileIssue(PHPCheck check, String message);

  /**
   * @return the current file
   */
  File getFile();

  /**
   * Fetch project property
   *
   * @param name property key
   *
   * @return the value for the given key
   */
  String[] getPropertyValues(String name);

}