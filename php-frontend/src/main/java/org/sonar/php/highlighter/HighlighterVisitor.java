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
package org.sonar.php.highlighter;

import com.google.common.collect.ImmutableList;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.ArrayList;
import java.util.List;

public class HighlighterVisitor extends PHPVisitorCheck {

  private static final ImmutableList<String> PHP_RESERVED_VARIABLES = ImmutableList.of(
    "__FUNCTION__",
    "__CLASS__",
    "__METHOD__",
    "__NAMESPACE__",
    "__DIR__",
    "__FILE__",
    "__LINE__",
    "$this");

  private static final ImmutableList<String> WORDS_TO_HIGHLIGHT = ImmutableList.<String>builder()
    .add(PHPKeyword.getKeywordValues())
    .addAll(PHP_RESERVED_VARIABLES).build();

  private final List<HighlightingData> highlightingDataList;
  private final SourceFileOffsets offsets;

  private HighlighterVisitor(SourceFileOffsets offsets) {
    this.highlightingDataList = new ArrayList<>();
    this.offsets = offsets;
  }

  public static List<HighlightingData> getHighlightData(Tree tree, SourceFileOffsets sourceFileOffsets) {
    HighlighterVisitor visitor = new HighlighterVisitor(sourceFileOffsets);
    visitor.scan(tree);
    return visitor.highlightingDataList;
  }

  @Override
  public void visitToken(SyntaxToken token) {
    if (token.is(Kind.TOKEN) && WORDS_TO_HIGHLIGHT.contains(token.text())) {
      highlight(token, "k");
    }

    super.visitToken(token);
  }

  @Override
  public void visitTrivia(SyntaxTrivia trivia) {
    if (trivia.text().startsWith("/**")) {
      highlight(trivia, "j");
    } else {
      highlight(trivia, "cd");
    }
  }

  @Override
  public void visitExpandableStringLiteral(ExpandableStringLiteralTree tree) {
    highlight(tree.openDoubleQuoteToken(), "s");
    highlight(tree.closeDoubleQuoteToken(), "s");

    super.visitExpandableStringLiteral(tree);
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Kind.REGULAR_STRING_LITERAL)) {
      highlight(tree.token(), "s");

    } else if (tree.is(Kind.NUMERIC_LITERAL)) {
      highlight(tree.token(), "c");
    }

    super.visitLiteral(tree);
  }

  @Override
  public void visitExpandableStringCharacters(ExpandableStringCharactersTree tree) {
    highlight(tree.token(), "s");
    super.visitExpandableStringCharacters(tree);
  }

  private void highlight(SyntaxToken token, String code) {
    highlightingDataList.add(new HighlightingData(offsets.startOffset(token), offsets.endOffset(token), code));
  }

}
