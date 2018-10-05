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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.visitors.AssignmentExpressionVisitor;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = SSLCertificatesVerificationDisabledCheck.KEY)
public class SSLCertificatesVerificationDisabledCheck extends PHPVisitorCheck {
  public static final String KEY = "S4830";

  private static final String CURL_SETOPT = "curl_setopt";
  private static final String CURLOPT_SSL_VERIFYHOST = "CURLOPT_SSL_VERIFYHOST";
  private static final String CURLOPT_SSL_VERIFYPEER = "CURLOPT_SSL_VERIFYPEER";

  private static final String MESSAGE = "Activate SSL/TLS certificates chain of trust verification.";
  private AssignmentExpressionVisitor assignmentExpressionVisitor;
  private HashMap<Kind, Set<String>> verifyHostCompliantValues;
  private HashMap<Kind, Set<String>> verifyPeerCompliantValues;

  @Override
  public void init() {
    verifyHostCompliantValues = new HashMap<>();
    verifyHostCompliantValues.put(Kind.NUMERIC_LITERAL, ImmutableSet.of("2"));
    verifyHostCompliantValues.put(Kind.REGULAR_STRING_LITERAL, ImmutableSet.of("\'2\'", "\"2\""));


    verifyPeerCompliantValues = new HashMap<>();
    verifyPeerCompliantValues.put(Kind.BOOLEAN_LITERAL, ImmutableSet.of("true", "TRUE"));
    verifyPeerCompliantValues.put(Kind.NUMERIC_LITERAL, ImmutableSet.of("1"));
    verifyPeerCompliantValues.put(Kind.REGULAR_STRING_LITERAL, ImmutableSet.of("\'1\'", "\"1\""));
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    assignmentExpressionVisitor = new AssignmentExpressionVisitor(context().symbolTable());
    tree.accept(assignmentExpressionVisitor);
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = CheckUtils.getFunctionName(tree);
    List<ExpressionTree> arguments = tree.arguments();

    // Detect curl_setopt function usage
    // http://php.net/manual/fr/function.curl-setopt.php
    if (CURL_SETOPT.equals(functionName) && arguments.size() > 2) {
      ExpressionTree optionArgument = arguments.get(1);
      ExpressionTree valueArgument = arguments.get(2);

      this.nameOf(optionArgument).ifPresent(name -> {
          if(name.equals(CURLOPT_SSL_VERIFYHOST)) {
            this.checkCURLSSLVerify(valueArgument, verifyHostCompliantValues);
          } else if(name.equals(CURLOPT_SSL_VERIFYPEER)) {
            this.checkCURLSSLVerify(valueArgument, verifyPeerCompliantValues);
          }
        }
      );
    }

    // super method must be called in order to visit function call node's children
    super.visitFunctionCall(tree);
  }

  private Optional<String> nameOf(Tree tree) {
    String name = CheckUtils.nameOf(tree);
    return name != null ? Optional.of(name) : Optional.empty();
  }

  private void checkCURLSSLVerify(ExpressionTree expressionTree, HashMap<Kind, Set<String>> compliantValues) {

    boolean isCompliant = false;
    ExpressionTree curlOptValue = getAssignedValue(expressionTree);
    for(Map.Entry<Kind, Set<String>> entry: compliantValues.entrySet()) {
      Kind kind = entry.getKey();
      Set<String> values = entry.getValue();
      if(curlOptValue.is(kind) && curlOptValue instanceof LiteralTree) {
        String value = ((LiteralTree) curlOptValue).value();
        isCompliant = values.contains(value);
      }
    }

    if (!isCompliant) {
      context().newIssue(this, expressionTree, MESSAGE);
    }
  }

  private void checkCURLSSLVerifyHost(ExpressionTree expressionTree) {

    ExpressionTree curlOptValue = getAssignedValue(expressionTree);
    boolean verifyHostEnabled = false;
    if (curlOptValue.is(Kind.NUMERIC_LITERAL)) {
      // Detect 2 integer value
      String value = ((LiteralTree) curlOptValue).value();
      verifyHostEnabled = value.equals("2");
    } else if (curlOptValue.is(Kind.REGULAR_STRING_LITERAL)) {
      // Detect '2' or "2" string characters
      ImmutableSet twoStringLiteral = ImmutableSet.of("\'2\'", "\"2\"");
      verifyHostEnabled = twoStringLiteral.contains(((LiteralTree) curlOptValue).value());
    }

    if (!verifyHostEnabled) {
      context().newIssue(this, expressionTree, MESSAGE);
    }
  }

  private void checkCURLSSLVerifyPeer(ExpressionTree expressionTree) {

    ExpressionTree curlOptValue = getAssignedValue(expressionTree);
    boolean verifyPeerEnabled = false;
    if (curlOptValue.is(Tree.Kind.BOOLEAN_LITERAL)) {
      // Detect true or TRUE boolean values
      ImmutableSet trueStringLiteral = ImmutableSet.of("true", "TRUE");
      verifyPeerEnabled = trueStringLiteral.contains(((LiteralTree) curlOptValue).value());

    } else if (curlOptValue.is(Kind.NUMERIC_LITERAL)) {
      // Detect 1 integer value
      String value = ((LiteralTree) curlOptValue).value();
      verifyPeerEnabled = value.equals("1");
    } else if (curlOptValue.is(Kind.REGULAR_STRING_LITERAL)) {
      // Detect '1' or "1" string characters
      ImmutableSet twoStringLiteral = ImmutableSet.of("\'1\'", "\"1\"");
      verifyPeerEnabled = twoStringLiteral.contains(((LiteralTree) curlOptValue).value());
    }

    if (!verifyPeerEnabled) {
      context().newIssue(this, expressionTree, MESSAGE);
    }
  }

  private ExpressionTree getAssignedValue(ExpressionTree value) {
    if (value.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      Symbol valueSymbol = context().symbolTable().getSymbol(value);
      return assignmentExpressionVisitor
        .getUniqueAssignedValue(valueSymbol)
        .orElse(value);
    }
    return value;
  }
}
