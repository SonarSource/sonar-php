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
package org.sonar.php.parser;

import com.sonar.sslr.api.typed.GrammarBuilder;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.GotoStatementTree;
import org.sonar.plugins.php.api.tree.statement.LabelTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;

public class NewPHPGrammar {

  private final GrammarBuilder<InternalSyntaxToken> b;
  private final TreeFactory f;

  public NewPHPGrammar(GrammarBuilder<InternalSyntaxToken> b, TreeFactory f) {
    this.b = b;
    this.f = f;
  }

  /**
   * [ START ] Declaration
   */

  public NamespaceNameTree NAMESPACE_NAME() {
    return b.<NamespaceNameTree>nonterminal(PHPLexicalGrammar.NAMESPACE_NAME)
        .is(b.firstOf(
          f.namespaceName(
            b.optional(b.token(PHPPunctuator.NS_SEPARATOR)),
            b.zeroOrMore(f.newTuple4(
              b.firstOf(b.token(PHPLexicalGrammar.IDENTIFIER)),
              b.token(PHPPunctuator.NS_SEPARATOR)
            )),
            b.token(PHPLexicalGrammar.IDENTIFIER)),
          f.namespaceName(
              b.token(PHPKeyword.NAMESPACE),
              b.token(PHPPunctuator.NS_SEPARATOR),
              b.zeroOrMore(f.newTuple6(
                  b.firstOf(b.token(PHPLexicalGrammar.IDENTIFIER)),
                  b.token(PHPPunctuator.NS_SEPARATOR)
              )),
              b.token(PHPLexicalGrammar.IDENTIFIER)))
    );
  }

  /**
   * [ END ] Declaration
   */


  /**
   * [ START ] Statement
   */

  public StatementTree STATEMENT() {
    return b.<StatementTree>nonterminal(PHPLexicalGrammar.STATEMENT)
        .is(b.firstOf(
            BLOCK(),
//            ALTERNATIVE_IF_STATEMENT(),
//            THROW_STATEMENT(),
//            IF_STATEMENT(),
//            WHILE_STATEMENT(),
//            DO_WHILE_STATEMENT(),
//            FOREACH_STATEMENT(),
//            FOR_STATEMENT(),
//            SWITCH_STATEMENT(),
            BREAK_STATEMENT(),
            CONTINUE_STATEMENT(),
            RETURN_STATEMENT(),
//            EMPTY_STATEMENT(),
//            YIELD_STATEMENT(),
//            GLOBAL_STATEMENT(),
//            STATIC_STATEMENT(),
//            ECHO_STATEMENT // is represented by function call
            TRY_STATEMENT(),
//            DECLARE_STATEMENT(),
            GOTO_STATEMENT(),
//            INLINE_HTML,   // ???
//            UNSET_VARIABLE_STATEMENT(),
            EXPRESSION_STATEMENT(),
            LABEL()
        ));
  }

  public ReturnStatementTree RETURN_STATEMENT() {
    return b.<ReturnStatementTree>nonterminal(PHPLexicalGrammar.RETURN_STATEMENT)
        .is(f.returnStatement(b.token(PHPKeyword.RETURN), b.optional(EXPRESSION()), EOS()));
  }

  public ContinueStatementTree CONTINUE_STATEMENT() {
    return b.<ContinueStatementTree>nonterminal(PHPLexicalGrammar.CONTINUE_STATEMENT)
        .is(f.continueStatement(b.token(PHPKeyword.CONTINUE), b.optional(EXPRESSION()), EOS()));
  }

  public BreakStatementTree BREAK_STATEMENT() {
    return b.<BreakStatementTree>nonterminal(PHPLexicalGrammar.BREAK_STATEMENT)
        .is(f.breakStatement(b.token(PHPKeyword.BREAK), b.optional(EXPRESSION()), EOS()));
  }

  public TryStatementTree TRY_STATEMENT() {
    return b.<TryStatementTree>nonterminal(PHPLexicalGrammar.TRY_STATEMENT)
        .is(f.tryStatement(
            b.token(PHPKeyword.TRY),
            BLOCK(),
            b.zeroOrMore(CATCH_BLOCK()),
            b.optional(f.newTuple2(b.token(PHPKeyword.FINALLY), BLOCK()))));
  }

  public CatchBlockTree CATCH_BLOCK() {
    return b.<CatchBlockTree>nonterminal(PHPLexicalGrammar.CATCH_BLOCK)
        .is(f.catchBlock(
            b.token(PHPKeyword.CATCH),
            b.token(PHPPunctuator.LPARENTHESIS),
            NAMESPACE_NAME(),
            b.token(PHPLexicalGrammar.REGULAR_VAR_IDENTIFIER),
            b.token(PHPPunctuator.RPARENTHESIS),
            BLOCK()
        ));

  }

  public BlockTree BLOCK() {
    return b.<BlockTree>nonterminal(PHPLexicalGrammar.BLOCK)
        .is(f.block(b.token(PHPPunctuator.LCURLYBRACE), b.zeroOrMore(STATEMENT()), b.token(PHPPunctuator.RCURLYBRACE)));
  }

  public GotoStatementTree GOTO_STATEMENT() {
    return b.<GotoStatementTree>nonterminal(PHPLexicalGrammar.GOTO_STATEMENT)
        .is(f.gotoStatement(b.token(PHPKeyword.GOTO), b.token(PHPLexicalGrammar.IDENTIFIER), EOS()));
  }

  public ExpressionStatementTree EXPRESSION_STATEMENT() {
    return b.<ExpressionStatementTree>nonterminal(PHPLexicalGrammar.EXPRESSION_STATEMENT)
        .is(f.expressionStatement(EXPRESSION(), EOS()));
  }

  public LabelTree LABEL() {
    return b.<LabelTree>nonterminal(PHPLexicalGrammar.LABEL)
        .is(f.label(b.token(PHPLexicalGrammar.IDENTIFIER), b.token(PHPPunctuator.COLON)));
  }

  public InternalSyntaxToken EOS() {
    return b.<InternalSyntaxToken>nonterminal(PHPLexicalGrammar.EOS)
        .is(b.firstOf(b.token(PHPPunctuator.SEMICOLON), b.token(PHPLexicalGrammar.INLINE_HTML)));
  }

  /**
   * [ END ] Statement
   */

  public ExpressionTree EXPRESSION() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.EXPRESSION)
        .is(f.expression(b.token(PHPLexicalGrammar.REGULAR_VAR_IDENTIFIER)));
  }

  /**
   * [ START ] Expression
   */

  /**
   * [ END ] Expression
   */

}
