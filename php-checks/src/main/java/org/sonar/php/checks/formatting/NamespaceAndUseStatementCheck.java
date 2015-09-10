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
package org.sonar.php.checks.formatting;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import org.sonar.php.checks.FormattingStandardCheck;
import org.sonar.php.parser.PHPGrammar;

import java.util.List;

public class NamespaceAndUseStatementCheck {

  private List<AstNode> useNodes = Lists.newArrayList();

  public void visitNode(FormattingStandardCheck formattingCheck, AstNode node) {
    if (node.is(PHPGrammar.NAMESPACE_STATEMENT)) {
      checkBlankLineAfterNamespace(formattingCheck, node);

    } else if (node.is(PHPGrammar.USE_STATEMENT)) {
      useNodes.add(node);
      AstNode nextNode = node.getNextAstNode().getFirstChild();

      if (nextNode == null || nextNode.isNot(PHPGrammar.USE_STATEMENT)) {
        checkUsesAreBeforeNamespace(formattingCheck, nextNode);
        checkBlankLineAfterUses(formattingCheck, node);
        useNodes.clear();
      }
    }
  }

  public void leaveFile() {
    useNodes.clear();
  }

  private void checkBlankLineAfterUses(FormattingStandardCheck formattingCheck, AstNode useStatement) {
    if (formattingCheck.hasUseBlankLine && isNotFollowedWithBlankLine(useStatement) && useStatement.getParent().getNextSibling() != null) {
      formattingCheck.reportIssue("Add a blank line after this \"use\" declaration.", Iterables.getLast(useNodes));
    }
  }

  private void checkUsesAreBeforeNamespace(FormattingStandardCheck formattingCheck, AstNode nextNode) {
    if (formattingCheck.isUseAfterNamespace && nextNode != null && nextNode.is(PHPGrammar.NAMESPACE_STATEMENT)) {
      formattingCheck.reportIssue("Move the use declarations after the namespace declarations.", useNodes.get(0));
    }
  }

  private void checkBlankLineAfterNamespace(FormattingStandardCheck formattingCheck, AstNode namespaceNode) {
    if (formattingCheck.hasNamespaceBlankLine && isNotFollowedWithBlankLine(namespaceNode)) {
      formattingCheck.reportIssue("Add a blank line after this \"namespace " + getNamespaceName(namespaceNode) + "\" declaration.", namespaceNode);
    }
  }

  /**
   * Returns true when there is either token or comment on node's next line.
   */
  private boolean isNotFollowedWithBlankLine(AstNode node) {
    int nextNodeLine = node.getTokenLine() + 1;
    boolean isNotFollowedWithBlankLine = false;

    // Checking for comment: is allowed on the same line as the node declaration or on next line + 1.
    for (Trivia t : node.getNextAstNode().getToken().getTrivia()) {
      int line = t.getToken().getLine();
      isNotFollowedWithBlankLine |= line == nextNodeLine;
    }

    return isNotFollowedWithBlankLine || node.getNextAstNode().getTokenLine() == nextNodeLine;
  }

  private Object getNamespaceName(AstNode namespaceNode) {
    AstNode namespaceName = namespaceNode.getFirstChild(PHPGrammar.NAMESPACE_NAME);
    StringBuilder builder = new StringBuilder();

    if (namespaceName != null) {
      for (Token t : namespaceName.getTokens()) {
        builder.append(t.getOriginalValue());
      }
    }

    return builder.toString();
  }

}
