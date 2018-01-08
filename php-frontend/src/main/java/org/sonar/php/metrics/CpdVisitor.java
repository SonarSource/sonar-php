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

import java.util.ArrayList;
import java.util.List;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.InlineHTMLTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;

public class CpdVisitor extends PHPVisitorCheck {

  private List<CpdToken> cpdTokens = new ArrayList<>();

  private static final String NORMALIZED_NUMERIC_LITERAL = "$NUMBER";
  private static final String NORMALIZED_CHARACTER_LITERAL = "$CHARS";

  @Override
  public void visitScript(ScriptTree tree) {
    // skip opening tag
    this.scan(tree.statements());
  }

  @Override
  public void visitInlineHTML(InlineHTMLTree tree) {
    // skip
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Kind.NUMERIC_LITERAL)) {
      addToken(tree.token(), NORMALIZED_NUMERIC_LITERAL);

    } else if (tree.is(Kind.REGULAR_STRING_LITERAL, Kind.NOWDOC_LITERAL)) {
      addToken(tree.token(), NORMALIZED_CHARACTER_LITERAL);

    } else {
      super.visitLiteral(tree);
    }
  }

  @Override
  public void visitExpandableStringCharacters(ExpandableStringCharactersTree tree) {
    addToken(tree.token(), NORMALIZED_CHARACTER_LITERAL);
  }

  @Override
  public void visitToken(SyntaxToken token) {
    if (((InternalSyntaxToken) token).isEOF()) {
      return;
    }

    addToken(token, token.text());
  }

  private void addToken(SyntaxToken token, String text) {
    cpdTokens.add(new CpdToken(token, text));
  }

  @Override
  public void visitUseStatement(UseStatementTree tree) {
    // do not enter (in order to avoid use statement tokens be considered in duplication detection)
  }

  public List<CpdToken> getCpdTokens(PhpFile file, CompilationUnitTree tree) {
    super.analyze(file, tree);
    return cpdTokens;
  }

  public static class CpdToken {

    private SyntaxToken syntaxToken;
    private String image;

    public CpdToken(SyntaxToken syntaxToken, String image) {
      this.syntaxToken = syntaxToken;
      this.image = image;
    }

    public SyntaxToken syntaxToken() {
      return syntaxToken;
    }

    public String image() {
      return image;
    }
  }
}
