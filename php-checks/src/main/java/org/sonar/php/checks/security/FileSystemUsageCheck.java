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
package org.sonar.php.checks.security;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S4797")
public class FileSystemUsageCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure this file handling is safe here.";

  private static final Map<String, FunctionArgsMatcher> FILE_SYSTEM_FUNCTIONS = buildFileSystemFunctions();

  private static Map<String, FunctionArgsMatcher> buildFileSystemFunctions() {
    HashMap<String, FunctionArgsMatcher> map = new HashMap<>();
    map.put("chgrp", new FunctionArgsMatcher(2, 2, false, 0));
    map.put("chmod", new FunctionArgsMatcher(2, 2, false, 0));
    map.put("chown", new FunctionArgsMatcher(2, 2, false, 0));
    map.put("copy", new FunctionArgsMatcher(2, 2, true, 0, 1));
    map.put("delete", new FunctionArgsMatcher(1, 1, false, 0));
    map.put("file", new FunctionArgsMatcher(1, 2, true, 0));
    map.put("file_get_contents", new FunctionArgsMatcher(1, 2, true, 0));
    map.put("file_put_contents", new FunctionArgsMatcher(2, 3, true, 0));
    map.put("fopen", new FunctionArgsMatcher(2, 3, true, 0));
    map.put("lchgrp", new FunctionArgsMatcher(2, 2, false, 0));
    map.put("lchown", new FunctionArgsMatcher(2, 2, false, 0));
    map.put("move_uploaded_file", new FunctionArgsMatcher(2, 2, false, 0, 1));
    map.put("parse_ini_file", new FunctionArgsMatcher(1, 3, true, 0));
    map.put("readfile", new FunctionArgsMatcher(1, 2, true, 0));
    map.put("rmdir", new FunctionArgsMatcher(1, 1, false, 0));
    map.put("tmpfile", new FunctionArgsMatcher(0, 0, true));
    map.put("unlink", new FunctionArgsMatcher(1, 1, false, 0));
    return map;
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (tree.callee().is(Kind.NAMESPACE_NAME)) {
      String qualifiedName = ((NamespaceNameTree) tree.callee()).qualifiedName();
      FunctionArgsMatcher argsMatcher = FILE_SYSTEM_FUNCTIONS.get(qualifiedName);
      if (argsMatcher != null && argsMatcher.matches(tree)) {
        context().newIssue(this, tree, MESSAGE);
      }
    }
    super.visitFunctionCall(tree);
  }

  private static class FunctionArgsMatcher {

    private static final Pattern NETWORK_OR_PHP_SCHEME = Pattern.compile("(^|/)(http|https|ftp|ftps|ssh\\d?(\\.\\w+?)?|php)://");

    private final int minCount;
    private final int maxCount;
    private final boolean matchesHardcodedPath;
    private final int[] pathIndexes;

    private FunctionArgsMatcher(int minCount, int maxCount, boolean matchesHardcodedPath, int... pathIndexes) {
      this.minCount = minCount;
      this.maxCount = maxCount;
      this.matchesHardcodedPath = matchesHardcodedPath;
      this.pathIndexes = pathIndexes;
    }

    private boolean matches(FunctionCallTree function) {
      int argumentCount = function.arguments().size();
      boolean pathMatches = pathIndexes.length == 0 || hasFileSystemPath(function.arguments());
      return minCount <= argumentCount && argumentCount <= maxCount && pathMatches;
    }

    private boolean hasFileSystemPath(SeparatedList<ExpressionTree> arguments) {
      boolean hasPath = false;
      for (int pathIndex : pathIndexes) {
        if (pathIndex < arguments.size()) {
          ExpressionTree pathExpression = arguments.get(pathIndex);
          if (!pathExpression.is(Kind.REGULAR_STRING_LITERAL)) {
            hasPath = true;
          } else if (matchesHardcodedPath) {
            String path = CheckUtils.trimQuotes((LiteralTree) pathExpression);
            hasPath |= !NETWORK_OR_PHP_SCHEME.matcher(path).find();
          }
        }
      }
      return hasPath;
    }
  }
}
