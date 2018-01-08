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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.plugins.php.api.tree.Tree.Kind.REGULAR_STRING_LITERAL;

@Rule(key = "S1075")
public class HardCodedUriCheck extends PHPVisitorCheck {
  // Don't match scheme starting with php://
  private static final String SCHEME = "^(?!.*php)[a-zA-Z\\+\\.\\-]+";
  private static final String URI_REGEX = SCHEME + "://[^\\$]+";
  private static final Pattern URI_PATTERN = Pattern.compile(URI_REGEX);
  private static final Pattern VARIABLE_NAME_PATTERN = Pattern.compile("filename|path", Pattern.CASE_INSENSITIVE);
  private static final Set<String> WHITELIST = ImmutableSet.<String>builder()
    .add("basename")
    .add("chgrp")
    .add("chmod")
    .add("chown")
    .add("clearstatcache")
    .add("copy")
    .add("delete")
    .add("dirname")
    .add("disk_​free_​space")
    .add("disk_​total_​space")
    .add("diskfreespace")
    .add("fclose")
    .add("feof")
    .add("fflush")
    .add("fgetc")
    .add("fgetcsv")
    .add("fgets")
    .add("fgetss")
    .add("file_​exists")
    .add("file_​get_​contents")
    .add("file_​put_​contents")
    .add("file")
    .add("fileatime")
    .add("filectime")
    .add("filegroup")
    .add("fileinode")
    .add("filemtime")
    .add("fileowner")
    .add("fileperms")
    .add("filesize")
    .add("filetype")
    .add("flock")
    .add("fnmatch")
    .add("fopen")
    .add("fpassthru")
    .add("fputcsv")
    .add("fputs")
    .add("fread")
    .add("fscanf")
    .add("fseek")
    .add("fstat")
    .add("ftell")
    .add("ftruncate")
    .add("fwrite")
    .add("glob")
    .add("is_​dir")
    .add("is_​executable")
    .add("is_​file")
    .add("is_​link")
    .add("is_​readable")
    .add("is_​uploaded_​file")
    .add("is_​writable")
    .add("is_​writeable")
    .add("lchgrp")
    .add("lchown")
    .add("link")
    .add("linkinfo")
    .add("lstat")
    .add("mkdir")
    .add("move_​uploaded_​file")
    .add("parse_​ini_​file")
    .add("parse_​ini_​string")
    .add("pathinfo")
    .add("pclose")
    .add("popen")
    .add("readfile")
    .add("readlink")
    .add("realpath_​cache_​get")
    .add("realpath_​cache_​size")
    .add("realpath")
    .add("rename")
    .add("rewind")
    .add("rmdir")
    .add("set_​file_​buffer")
    .add("stat")
    .add("symlink")
    .add("tempnam")
    .add("tmpfile")
    .add("touch")
    .add("umask")
    .add("unlink").build();

  private static boolean isFileNameVariable(IdentifierTree variable) {
    return VARIABLE_NAME_PATTERN.matcher(variable.text()).find();
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = CheckUtils.getFunctionName(tree);
    if (functionName != null && (functionName.startsWith("http_") || WHITELIST.contains(functionName))) {
      tree.arguments().forEach(this::checkExpression);
    }
    super.visitFunctionCall(tree);
  }

  @Override
  public void visitVariableDeclaration(VariableDeclarationTree tree) {
    if (isFileNameVariable(tree.identifier())) {
      checkExpression(tree.initValue());
    }
    super.visitVariableDeclaration(tree);
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    if (tree.variable().is(Tree.Kind.VARIABLE_IDENTIFIER) && isFileNameVariable(((VariableIdentifierTree) tree.variable()).variableExpression())) {
      checkExpression(tree.value());
    }
    super.visitAssignmentExpression(tree);
  }

  private void checkExpression(@Nullable ExpressionTree expr) {
    if (expr != null && isHardcodedURI(expr)) {
      reportHardcodedURI(expr);
    }
  }

  private static boolean isHardcodedURI(ExpressionTree expr) {
    ExpressionTree newExpr = CheckUtils.skipParenthesis(expr);
    if (!newExpr.is(REGULAR_STRING_LITERAL)) {
      return false;
    }
    String stringLiteral = trimQuotes(((LiteralTree) newExpr).value());
    return URI_PATTERN.matcher(stringLiteral).find();
  }

  private static String trimQuotes(String value) {
    return value.substring(1, value.length());
  }

  private void reportHardcodedURI(ExpressionTree hardcodedURI) {
    context().newIssue(this, hardcodedURI, "Refactor your code to get this URI from a customizable parameter.");
  }


}
