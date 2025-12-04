/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class TokenVisitor extends PHPVisitorCheck {

  private static final Set<String> PHP_KEYWORD_VALUES = new HashSet<>(Arrays.asList(PHPKeyword.getKeywordValues()));

  List<SyntaxToken> tokens = new ArrayList<>();

  public TokenVisitor(Tree tree) {
    this.scan(tree);
  }

  public static List<SyntaxToken> tokens(Tree tree) {
    TokenVisitor visitor = new TokenVisitor(tree);
    return visitor.getTokens();
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

  @Nullable
  public SyntaxToken firstKeyword() {
    return tokens.stream()
      .filter(t -> PHP_KEYWORD_VALUES.contains(t.text().toLowerCase(Locale.ROOT)))
      .findFirst()
      .orElse(null);
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
