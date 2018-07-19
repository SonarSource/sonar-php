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
package org.sonar.plugins.php.api.visitors;

import com.google.common.annotations.Beta;
import java.util.List;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;

@Beta
public interface CheckContext {

  /**
   * @return the top tree node of the current file AST representation.
   */
  CompilationUnitTree tree();

  /**
   *
   * <p> To set line and cost use {@link Issue#line(int)} and {@link Issue#cost(double)}. Note, that these calls could be chained.
   * <pre>
   *   newIssue(myCheck, "Some message")
   *     .line(105)
   *     .cost(3);
   * </pre>
   *
   * @param check the instance of the rule for which issue should be created
   * @param message message of the issue
   * @return instance of Issue
   *
   * @deprecated since 2.9. Use {@link CheckContext#newIssue(PHPCheck, Tree, String)}, {@link CheckContext#newLineIssue(PHPCheck, int, String)} or
   * {@link CheckContext#newFileIssue(PHPCheck, String)}
   */
  @Deprecated
  Issue newIssue(PHPCheck check, String message);

  /**
   *
   * <p> To add secondary locations and cost use {@link PreciseIssue#secondary(Tree, String)} and {@link PreciseIssue#cost(double)}. Note, that these calls could be chained.
   * <pre>
   *   newIssue(myCheck, primaryTree, "Primary message")
   *     .secondary(secondaryTree1, "Secondary message")
   *     .secondary(secondaryTree2, null)
   *     .cost(3);
   * </pre>
   *
   * @param check the instance of the rule for which issue should be created
   * @param tree primary location for issue
   * @param message primary message of the issue
   * @return issue with precise location
   */
  PreciseIssue newIssue(PHPCheck check, Tree tree, String message);

  /**
   *
   * <p> To add secondary locations and cost use {@link PreciseIssue#secondary(Tree, String)} and {@link PreciseIssue#cost(double)}. Note, that these calls could be chained.
   * <pre>
   *   newIssue(myCheck, primaryLocationStartTree, primaryLocationEndTree, "Primary message")
   *     .secondary(secondaryTree1, "Secondary message")
   *     .secondary(secondaryTree2, null)
   *     .cost(3);
   * </pre>
   *
   * @param check the instance of the rule for which issue should be created
   * @param startTree start of this tree will be the start of primary location of the issue
   * @param endTree end of this tree will be the end of primary location of the issue
   * @param message primary message of the issue
   * @return issue with precise location
   */
  PreciseIssue newIssue(PHPCheck check, Tree startTree, Tree endTree, String message);

  /**
   *
   * <p> To add cost use {@link LineIssue#cost(double)}.
   * <pre>
   *   newLineIssue(myCheck, 42, "Message")
   *     .cost(3);
   * </pre>
   *
   * @param check the instance of the rule for which issue should be created
   * @param line position of the issue in the file
   * @param message message of the issue
   * @return issue with line location
   */
  LineIssue newLineIssue(PHPCheck check, int line, String message);

  /**
   *
   * <p> To add cost use {@link FileIssue#cost(double)}.
   * <pre>
   *   newFileIssue(myCheck, "Message")
   *     .cost(3);
   * </pre>
   *
   * @param check the instance of the rule for which issue should be created
   * @param message message of the issue
   * @return issue at file level
   */
  FileIssue newFileIssue(PHPCheck check, String message);

  List<PhpIssue> getIssues();

  SymbolTable symbolTable();

  /**
   * @return the current file
   */
  PhpFile getPhpFile();
}
