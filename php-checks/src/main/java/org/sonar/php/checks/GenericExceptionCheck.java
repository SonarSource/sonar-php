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
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Set;

@Rule(
  key = "S2201",
  name = "Generic exceptions ErrorException, RuntimeException and Exception should never be thrown",
  priority = Priority.MAJOR,
  tags = {PHPRuleTags.PITFAIL})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
public class GenericExceptionCheck extends SquidCheck<LexerlessGrammar> {

  public static final String MESSAGE = "Don't throw the base Exception.";

  private static final Set<String> RAW_EXCEPTIONS = ImmutableSet.of("ErrorException", "RuntimeException", "Exception");
  private static final String SEPARATOR = PHPPunctuator.NS_SEPARATOR.getValue();
  private String namespace;
  private ArrayList<String> uses;


  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.NAMESPACE_STATEMENT,
      PHPGrammar.USE_DECLARATION,
      PHPGrammar.THROW_STATEMENT
    );
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    namespace = "";
    uses = new ArrayList<String>();
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.NAMESPACE_STATEMENT)) {
      parseNamespace(astNode);
      return;
    }
    if (astNode.is(PHPGrammar.USE_DECLARATION)) {
      parseUse(astNode);
      return;
    }
    if (isBaseException(astNode)) {
      getContext().createLineViolation(this, MESSAGE, astNode);
    }
  }

  private void parseNamespace(AstNode astNode) {
    AstNode namespaceNode = astNode.getFirstChild(PHPGrammar.NAMESPACE_NAME);
    namespace = getStringValue(namespaceNode);
  }

  private void parseUse(AstNode astNode) {
    AstNode namespaceNode = astNode.getFirstChild(PHPGrammar.NAMESPACE_NAME);
    String namespace = getStringValue(namespaceNode);
    for (String exc : RAW_EXCEPTIONS) {
      if (namespace.equals(exc)) {
        AstNode idNode = astNode.getFirstChild(PHPGrammar.IDENTIFIER);
        String alias = idNode != null ? getStringValue(idNode) : namespace;
        uses.add(alias);
      }
    }
  }

  private boolean isBaseException(AstNode throwNode) {
    AstNode classNameNode = throwNode.getFirstDescendant(PHPGrammar.CLASS_NAME);
    if (classNameNode == null) {
      return false;
    }
    String className = getStringValue(classNameNode);
    if (className.startsWith(SEPARATOR)) {
      return RAW_EXCEPTIONS.contains(className.substring(1));
    }
    if (namespace.isEmpty()) {
      return RAW_EXCEPTIONS.contains(className);
    }

    return uses.contains(className);
  }

  private String getStringValue(AstNode classNameNode) {
    StringBuilder builder = new StringBuilder();
    for (Token t : classNameNode.getTokens()) {
      builder.append(t.getOriginalValue());
    }
    return builder.toString();
  }


}
