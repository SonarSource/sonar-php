/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

@Rule(key = HardCodedCredentialsCheck.KEY)
public class HardCodedCredentialsCheck extends PHPVisitorCheck {

  public static final String KEY = "S2068";
  private static final String MESSAGE = "'%s' detected in this variable name, review this potentially hardcoded credential.";
  private static final String MESSAGE_ARGUMENTS = "Review this hardcoded credential.";
  private static final String MESSAGE_URI = "detected URI with password, review this potentially hardcoded credential.";
  private static final String DEFAULT_CREDENTIAL_WORDS = "password,passwd,pwd";

  private static final String LITERAL_PATTERN_SUFFIX = "=(?!([\\?:']|%s))..";

  private static final int LITERAL_PATTERN_SUFFIX_LENGTH = LITERAL_PATTERN_SUFFIX.length();
  private static final Map<String, Parameter> CONNECT_FUNCTIONS = initializeConnectFunctionsMap();

  private static final QualifiedName LARAVEL_ENCRYPTER = qualifiedName("illuminate\\encryption\\encrypter");

  private static final String BIND_PASSWORD = "bind_password";
  private static final String PASSWD = "passwd";
  private static final String PASSWORD = "password";
  private static final String NEWPW = "newpw";
  private static final String ENCRYPTER_KEY = "key";

  @RuleProperty(
    key = "credentialWords",
    description = "Comma separated list of words identifying potential credentials",
    defaultValue = DEFAULT_CREDENTIAL_WORDS)
  public String credentialWords = DEFAULT_CREDENTIAL_WORDS;

  private List<Pattern> variablePatterns = null;
  private List<Pattern> literalPatterns = null;

  private static Map<String, Parameter> initializeConnectFunctionsMap() {
    Map<String, Parameter> connectFunctions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    connectFunctions.put("ldap_bind", new Parameter(BIND_PASSWORD, 2));
    connectFunctions.put("pdo", new Parameter(PASSWD, 2));
    connectFunctions.put("mysqli", new Parameter(PASSWD, 2));
    connectFunctions.put("mysqli_connect", new Parameter(PASSWD, 2));
    connectFunctions.put("mysql_connect", new Parameter(PASSWORD, 2));
    connectFunctions.put("oci_connect", new Parameter(PASSWORD, 1));
    connectFunctions.put("ldap_exop_passwd", new Parameter(NEWPW, 3));
    connectFunctions.put("mssql_connect", new Parameter(PASSWORD, 2));
    connectFunctions.put("odbc_connect", new Parameter(PASSWORD, 2));
    connectFunctions.put("db2_connect", new Parameter(PASSWORD, 2));
    connectFunctions.put("cubrid_connect", new Parameter(PASSWD, 4));
    connectFunctions.put("maxdb_connect", new Parameter(PASSWD, 2));
    connectFunctions.put("maxdb_change_user", new Parameter(PASSWORD, 2));
    connectFunctions.put("imap_open", new Parameter(PASSWORD, 2));
    connectFunctions.put("ifx_connect", new Parameter(PASSWORD, 2));
    connectFunctions.put("dbx_connect", new Parameter(PASSWORD, 4));
    connectFunctions.put("fbsql_pconnect", new Parameter(PASSWORD, 2));

    return connectFunctions;
  }

  private Stream<Pattern> variablePatterns() {
    if (variablePatterns == null) {
      variablePatterns = toPatterns("");
    }
    return variablePatterns.stream();
  }

  private Stream<Pattern> literalPatterns() {
    if (literalPatterns == null) {
      literalPatterns = toPatterns(LITERAL_PATTERN_SUFFIX);
    }
    return literalPatterns.stream();
  }

  private List<Pattern> toPatterns(String suffix) {
    return Stream.of(credentialWords.split(","))
      .map(String::trim)
      .map(word -> Pattern.compile(word + suffix, Pattern.CASE_INSENSITIVE))
      .collect(Collectors.toList());
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();
    String functionName = callee.toString();
    if (isLaravelEncryptConstructorCall(callee)) {
      checkArgument(tree, new Parameter(ENCRYPTER_KEY, 0));
    }
    if (CONNECT_FUNCTIONS.containsKey(functionName)) {
      checkArgument(tree, CONNECT_FUNCTIONS.get(functionName));
    }
    super.visitFunctionCall(tree);
  }

  private boolean isLaravelEncryptConstructorCall(ExpressionTree expressionTree) {
    return expressionTree.is(Kind.NAMESPACE_NAME) && getFullyQualifiedName((NamespaceNameTree) expressionTree).equals(LARAVEL_ENCRYPTER);
  }

  private void checkArgument(FunctionCallTree tree, Parameter parameter) {
    Optional<CallArgumentTree> argument = CheckUtils.argument(tree, parameter.name, parameter.position);

    if (!argument.isPresent()) {
      return;
    }

    ExpressionTree arg = argument.get().value();
    if (arg.is(Kind.REGULAR_STRING_LITERAL) && !isEmptyStringLiteral((LiteralTree) arg)) {
      context().newIssue(this, arg, MESSAGE_ARGUMENTS);
    }
  }

  @Override
  public void visitLiteral(LiteralTree literal) {
    checkForCredentialQuery(literal);
    checkForCredentialUri(literal);

    super.visitLiteral(literal);
  }

  private void checkForCredentialQuery(LiteralTree literal) {
    literalPatterns()
      .filter(pattern -> pattern.matcher(literal.token().text()).find())
      .findAny().ifPresent(pattern -> addIssue(pattern, literal));
  }

  private void checkForCredentialUri(LiteralTree literal) {
    String possibleUrl = CheckUtils.trimQuotes(literal.value());
    URI uri = null;

    try {
      uri = new URI(possibleUrl);
    } catch (URISyntaxException e) {
      return;
    }

    if (uri.getUserInfo() != null) {
      String userInfo = uri.getUserInfo();
      Matcher m = Pattern.compile("(\\S+):(\\S+)").matcher(userInfo);
      if (m.find() && !m.group(1).equals(m.group(2)) && !userInfo.equals("user:password") && !userInfo.equals("username:password")) {
        context().newIssue(this, literal, MESSAGE_URI);
      }
    }
  }

  @Override
  public void visitVariableDeclaration(VariableDeclarationTree declaration) {
    checkVariable((declaration.identifier()).token(), declaration.initValue());
    super.visitVariableDeclaration(declaration);
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree assignment) {
    checkVariable(((PHPTree) assignment.variable()).getLastToken(), assignment.value());
    super.visitAssignmentExpression(assignment);
  }

  private void checkVariable(SyntaxToken reportTree, @Nullable Tree assignedValue) {
    if (assignedValue != null && assignedValue.is(Kind.REGULAR_STRING_LITERAL) && !isEmptyStringLiteral((LiteralTree) assignedValue)) {
      variablePatterns().filter(pattern -> pattern.matcher(reportTree.text()).find()).findAny().ifPresent(pattern -> checkAssignedValue(pattern, reportTree, assignedValue));
    }
  }

  private void checkAssignedValue(Pattern pattern, SyntaxToken reportTree, Tree assignedValue) {
    if (!pattern.matcher(assignedValue.toString()).find()) {
      addIssue(pattern, reportTree);
    }
  }

  private static boolean isEmptyStringLiteral(LiteralTree literal) {
    return literal.value().substring(1, literal.value().length() - 1).isEmpty();
  }

  private void addIssue(Pattern pattern, Tree tree) {
    context().newIssue(this, tree, String.format(MESSAGE, cleanedPattern(pattern.pattern())));
  }

  private static String cleanedPattern(String pattern) {
    if (pattern.endsWith(LITERAL_PATTERN_SUFFIX)) {
      return pattern.substring(0, pattern.length() - LITERAL_PATTERN_SUFFIX_LENGTH);
    }
    return pattern;
  }

  private static class Parameter {
    private @Nullable String name;
    private int position;

    public Parameter(String name, int position) {
      this.name = name;
      this.position = position;
    }

  }
}
