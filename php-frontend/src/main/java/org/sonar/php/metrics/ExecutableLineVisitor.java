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
package org.sonar.php.metrics;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.visitors.PHPTreeSubscriber;

class ExecutableLineVisitor extends PHPTreeSubscriber {
  private final Set<Integer> executableLines = new HashSet<>();

  ExecutableLineVisitor(Tree tree) {
    scanTree(tree);
  }

  @Override
  public void visitNode(Tree tree) {
    executableLines.add(((PHPTree) tree).getLine());
  }

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(
      Kind.RETURN_STATEMENT,
      Kind.CONTINUE_STATEMENT,
      Kind.BREAK_STATEMENT,
      Kind.GOTO_STATEMENT,
      Kind.THROW_STATEMENT,
      Kind.EMPTY_STATEMENT,
      Kind.YIELD_STATEMENT,
      Kind.GLOBAL_STATEMENT,
      Kind.STATIC_STATEMENT,
      Kind.TRY_STATEMENT,
      Kind.SWITCH_STATEMENT,
      Kind.IF_STATEMENT,
      Kind.WHILE_STATEMENT,
      Kind.DO_WHILE_STATEMENT,
      Kind.FOREACH_STATEMENT,
      Kind.FOR_STATEMENT,
      Kind.DECLARE_STATEMENT,
      Kind.EXPRESSION_STATEMENT,
      Kind.UNSET_VARIABLE_STATEMENT
    );
  }

  public Set<Integer> getExecutableLines() {
    return executableLines;
  }
}
