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
package org.sonar.php.checks.utils;

import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.ArrayList;
import java.util.List;

public class TokenVisitor extends PHPVisitorCheck {

  List<SyntaxToken> tokens = new ArrayList<>();

  public static List<SyntaxToken> tokens(Tree tree) {
    TokenVisitor visitor = new TokenVisitor(tree);
    return visitor.getTokens();
  }

  public TokenVisitor(Tree tree) {
    this.scan(tree);
  }

  private List<SyntaxToken> getTokens() {
    return tokens;
  }

  @Override
  public void visitToken(SyntaxToken token) {
    super.visitToken(token);
    if (token.is(Kind.TOKEN)) {
      tokens.add(token);
    }
  }

  public SyntaxToken prevToken(SyntaxToken token) {
    for (int i = 0; i < tokens.size(); i++) {
      if (token.equals(tokens.get(i))) {
        if (i > 0) {
          return tokens.get(i - 1);
        } else {
          break;
        }
      }
    }
    return null;
  }

  public SyntaxToken nextToken(SyntaxToken token) {
    for (int i = 0; i < tokens.size(); i++) {
      if (token.equals(tokens.get(i))) {
        if (i < tokens.size() - 1) {
          return tokens.get(i + 1);
        } else {
          break;
        }
      }
    }
    return null;
  }
}
