/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import org.apache.commons.lang.StringUtils;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.lexer.PHPLexer;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;
import java.util.Set;

@Rule(
  key = "S1200",
  priority = Priority.MAJOR)
public class ClassCouplingCheck extends SquidCheck<LexerlessGrammar> {

  public static final int DEFAULT = 20;
  private Set<String> types = Sets.newHashSet();
  private static final Set<String> DOC_TAGS = ImmutableSet.of(
    "@var", "@global", "@staticvar", "@throws", "@param", "@return");

  private static final Set<String> EXCLUDED_TYPES = Sets.newTreeSet(String.CASE_INSENSITIVE_ORDER);

  static {
    EXCLUDED_TYPES.addAll(ImmutableSet.of(
      "INTEGER", "INT", "DOUBLE", "FLOAT",
      "STRING", "ARRAY", "OBJECT", "BOOLEAN",
      "BOOL", "BINARY", "NULL", "MIXED"));
  }

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  public int max = DEFAULT;

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.CLASS_DECLARATION,
      PHPGrammar.NEW_EXPR);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    types.clear();
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.CLASS_DECLARATION)) {
      retrieveCoupledTypes(astNode);
    } else {
      retrieveInstantiatedClassName(astNode);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.CLASS_DECLARATION)) {
      int nbType = types.size();

      if (nbType > max) {
        getContext().createLineViolation(this,
          "Split this class into smaller and more specialized ones to reduce its dependencies on other classes from {0} to the maximum authorized {1} or less.",
          astNode, nbType, max);
      }
      types.clear();
    }
  }

  private void retrieveCoupledTypes(AstNode classDeclaration) {
    for (AstNode classStatement : classDeclaration.getChildren(PHPGrammar.CLASS_STATEMENT)) {
      AstNode stmt = classStatement.getFirstChild();

      if (stmt.is(PHPGrammar.CLASS_VARIABLE_DECLARATION, PHPGrammar.CLASS_CONSTANT_DECLARATION)) {
        retrieveTypeFromDoc(stmt);
      } else if (stmt.is(PHPGrammar.METHOD_DECLARATION)) {
        retrieveTypeFromDoc(stmt);
        retrieveTypeFromParameter(stmt);
      } else {
        continue;
      }
    }
  }

  private void retrieveTypeFromParameter(AstNode methodDeclaration) {
    AstNode parameterList = methodDeclaration.getFirstChild(PHPGrammar.PARAMETER_LIST);

    if (parameterList != null) {
      for (AstNode parameter : parameterList.getChildren(PHPGrammar.PARAMETER)) {
        AstNode classType = parameter.getFirstChild(PHPGrammar.OPTIONAL_CLASS_TYPE);

        if (classType != null && classType.getFirstChild().is(PHPGrammar.FULLY_QUALIFIED_CLASS_NAME)) {
          types.add(getClassName(classType.getFirstChild()));
        }
      }
    }
  }

  private void retrieveTypeFromDoc(AstNode varDeclaration) {
    Token varDecToken = varDeclaration.getToken();

    for (Trivia comment : varDecToken.getTrivia()) {
      for (String line : comment.getToken().getValue().split("[" + PHPLexer.LINE_TERMINATOR + "]++")) {
        retrieveTypeFromCommentLine(line);
      }
    }
  }

  private void retrieveTypeFromCommentLine(String line) {
    String[] commentLine = line.trim().split("[" + PHPLexer.WHITESPACE + "]++");

    if (commentLine.length > 2 && DOC_TAGS.contains(commentLine[1])) {
      for (String type : commentLine[2].split("\\|")) {
        type = StringUtils.removeEnd(type, "[]");

        if (!EXCLUDED_TYPES.contains(type)) {
          types.add(type);
        }
      }
    }
  }

  private void retrieveInstantiatedClassName(AstNode astNode) {
    String className = getInstantiatedClassName(astNode);
    if (className != null) {
      types.add(className);
    }
  }

  private String getInstantiatedClassName(AstNode newExpr) {
    AstNode variable = newExpr.getFirstChild(PHPGrammar.MEMBER_EXPRESSION);
    AstNode classNameNode = variable.getFirstChild();

    if (classNameNode.is(PHPKeyword.NAMESPACE)) {
      return getClassName(variable);
    } else if (classNameNode.is(PHPGrammar.CLASS_NAME, PHPGrammar.IDENTIFIER) && classNameNode.getFirstChild().isNot(PHPKeyword.STATIC)) {
      return getClassName(classNameNode);
    } else {
      return null;
    }
  }

  private String getClassName(AstNode expr) {
    StringBuilder builder = new StringBuilder();

    for (Token token : expr.getTokens()) {
      if (token.getType() == PHPPunctuator.LPARENTHESIS) {
        break;
      }
      if (token.getType() != PHPKeyword.NAMESPACE) {
        builder.append(token.getOriginalValue());
      }
    }
    return builder.toString();
  }

}
