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
import org.sonar.php.tree.impl.SeparatedList;
import static org.sonar.php.api.PHPKeyword.LIST;
import static org.sonar.php.api.PHPKeyword.YIELD;
import static org.sonar.php.api.PHPPunctuator.ARROW;
import static org.sonar.php.api.PHPPunctuator.COMMA;
import static org.sonar.php.api.PHPPunctuator.DOLLAR_LCURLY;
import static org.sonar.php.api.PHPPunctuator.DOUBLEARROW;
import static org.sonar.php.api.PHPPunctuator.EQU;
import static org.sonar.php.api.PHPPunctuator.LBRACKET;
import static org.sonar.php.api.PHPPunctuator.LCURLYBRACE;
import static org.sonar.php.api.PHPPunctuator.LPARENTHESIS;
import static org.sonar.php.api.PHPPunctuator.RBRACKET;
import static org.sonar.php.api.PHPPunctuator.RCURLYBRACE;
import static org.sonar.php.api.PHPPunctuator.RPARENTHESIS;

import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.impl.statement.ForEachStatementTreeImpl.ForEachStatementHeader;
import org.sonar.php.tree.impl.statement.ForStatementTreeImpl.ForStatementHeader;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.EmptyStatementTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.GotoStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.LabelTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ComputedVariableTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.ListExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.expression.YieldExpressionTree;

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
            THROW_STATEMENT(),
            IF_STATEMENT(),
//            WHILE_STATEMENT(),
//            DO_WHILE_STATEMENT(),
            FOREACH_STATEMENT(),
            FOR_STATEMENT(),
//            SWITCH_STATEMENT(),
            BREAK_STATEMENT(),
            CONTINUE_STATEMENT(),
            RETURN_STATEMENT(),
            EMPTY_STATEMENT(),
//            YIELD_STATEMENT(),
//            GLOBAL_STATEMENT(),
//            STATIC_STATEMENT(),
//            ECHO_STATEMENT // is represented by function call
            TRY_STATEMENT(),
//            DECLARE_STATEMENT(),  // requires variable_declaration
            GOTO_STATEMENT(),
//            INLINE_HTML,   // ???
//            UNSET_VARIABLE_STATEMENT(),  // requires MEMBER_EXPRESSION
            EXPRESSION_STATEMENT(),
            LABEL()
        ));
  }

  public IfStatementTree IF_STATEMENT() {
    return b.<IfStatementTree>nonterminal(PHPLexicalGrammar.IF_STATEMENT)
        .is(b.firstOf(STANDARD_IF_STATEMENT(), ALTERNATIVE_IF_STATEMENT()));
  }

  public IfStatementTree STANDARD_IF_STATEMENT() {
    return b.<IfStatementTree>nonterminal(PHPLexicalGrammar.STANDARD_IF_STATEMENT)
        .is(f.ifStatement(
            b.token(PHPKeyword.IF),
            //fixme (Lena) : should be PARENTHESIS_EXPRESSION
            EXPRESSION(),
            STATEMENT(),
            b.zeroOrMore(ELSEIF_CLAUSE()),
            b.optional(ELSE_CLAUSE())
        ));
  }

  public IfStatementTree ALTERNATIVE_IF_STATEMENT() {
    return b.<IfStatementTree>nonterminal(PHPLexicalGrammar.ALTERNATIVE_IF_STATEMENT)
        .is(f.alternativeIfStatement(
            b.token(PHPKeyword.IF),
            //fixme (Lena) : should be PARENTHESIS_EXPRESSION
            EXPRESSION(),
            b.token(PHPPunctuator.COLON),
            //fixme (Lena) : should be INNER_STATEMENT_LIST
            b.zeroOrMore(STATEMENT()),
            b.zeroOrMore(ALTERNATIVE_ELSEIF_CLAUSE()),
            b.optional(ALTERNATIVE_ELSE_CLAUSE()),
            b.token(PHPKeyword.ENDIF),
            EOS()
        ));
  }

  public ElseClauseTree ELSE_CLAUSE() {
    return b.<ElseClauseTree>nonterminal(PHPLexicalGrammar.ELSE_CLAUSE)
        .is(f.elseClause(b.token(PHPKeyword.ELSE), STATEMENT()));
  }

  public ElseifClauseTree ELSEIF_CLAUSE() {
    return b.<ElseifClauseTree>nonterminal(PHPLexicalGrammar.ELSEIF_CLAUSE)
        .is(f.elseifClause(
            b.token(PHPKeyword.ELSEIF),
            //fixme (Lena) : should be PARENTHESIS_EXPRESSION
            EXPRESSION(),
            STATEMENT()
        ));
  }

  public ElseClauseTree ALTERNATIVE_ELSE_CLAUSE() {
    return b.<ElseClauseTree>nonterminal(PHPLexicalGrammar.ALTERNATIVE_ELSE_CLAUSE)
        .is(f.alternativeElseClause(
            b.token(PHPKeyword.ELSE),
            b.token(PHPPunctuator.COLON),
            //fixme (Lena) : should be INNER_STATEMENT_LIST
            b.zeroOrMore(STATEMENT())
        ));
  }

  public ElseifClauseTree ALTERNATIVE_ELSEIF_CLAUSE() {
    return b.<ElseifClauseTree>nonterminal(PHPLexicalGrammar.ALTERNATIVE_ELSEIF_CLAUSE)
        .is(f.alternativeElseifClause(
            b.token(PHPKeyword.ELSEIF),
            //fixme (Lena) : should be PARENTHESIS_EXPRESSION
            EXPRESSION(),
            b.token(PHPPunctuator.COLON),
            //fixme (Lena) : should be INNER_STATEMENT_LIST
            b.zeroOrMore(STATEMENT())
        ));
  }

  public ForStatementTree FOR_STATEMENT() {
    return b.<ForStatementTree>nonterminal(PHPLexicalGrammar.FOR_STATEMENT)
        .is(b.firstOf(
            f.forStatement(
                FOR_STATEMENT_HEADER(),
                STATEMENT()
            ),
            f.forStatementAlternative(
                FOR_STATEMENT_HEADER(),
                b.token(PHPPunctuator.COLON),
                //fixme (Lena) : should be INNER_STATEMENT_LIST
                b.zeroOrMore(STATEMENT()),
                b.token(PHPKeyword.ENDFOR),
                EOS()
            ))
        );
  }

  public ForStatementHeader FOR_STATEMENT_HEADER() {
    return b.<ForStatementHeader>nonterminal()
        .is(f.forStatementHeader(
            b.token(PHPKeyword.FOR), b.token(PHPPunctuator.LPARENTHESIS),
            b.optional(FOR_EXPR()),
            b.token(PHPPunctuator.SEMICOLON),
            b.optional(FOR_EXPR()),
            b.token(PHPPunctuator.SEMICOLON),
            b.optional(FOR_EXPR()),
            b.token(PHPPunctuator.RPARENTHESIS)
        ));
  }

  public SeparatedList<ExpressionTree> FOR_EXPR() {
    return b.<SeparatedList<ExpressionTree>>nonterminal(PHPLexicalGrammar.FOR_EXRR)
        .is(f.forExpr(
            EXPRESSION(),
            b.zeroOrMore(f.newTuple12(b.token(PHPPunctuator.COMMA), EXPRESSION()))
        ));
  }

  public ForEachStatementTree FOREACH_STATEMENT() {
    return b.<ForEachStatementTree>nonterminal(PHPLexicalGrammar.FOREACH_STATEMENT)
        .is(b.firstOf(
            f.forEachStatement(FOREACH_STATEMENT_HEADER(), STATEMENT()),
            f.forEachStatementAlternative(
                FOREACH_STATEMENT_HEADER(),
                b.token(PHPPunctuator.COLON),
                //fixme (Lena) : should be INNER_STATEMENT_LIST
                b.zeroOrMore(STATEMENT()),
                b.token(PHPKeyword.ENDFOREACH),
                EOS()
            ))
        );
  }

  public ForEachStatementHeader FOREACH_STATEMENT_HEADER() {
    return b.<ForEachStatementHeader>nonterminal()
        .is(f.forEachStatementHeader(
            b.token(PHPKeyword.FOREACH), b.token(PHPPunctuator.LPARENTHESIS),
            EXPRESSION(), b.token(PHPKeyword.AS),
            // fixme (Lena) : both EXPRESSION() should be FOREACH_VARIABLE
            b.optional(f.newTuple10(EXPRESSION(), b.token(PHPPunctuator.DOUBLEARROW))), EXPRESSION(),
            b.token(PHPPunctuator.RPARENTHESIS)
        ));
  }

  public ThrowStatementTree THROW_STATEMENT() {
    return b.<ThrowStatementTree>nonterminal(PHPLexicalGrammar.THROW_STATEMENT)
        .is(f.throwStatement(b.token(PHPKeyword.THROW), EXPRESSION(), EOS()));
  }

  public EmptyStatementTree EMPTY_STATEMENT() {
    return b.<EmptyStatementTree>nonterminal(PHPLexicalGrammar.EMPTY_STATEMENT)
        .is(f.emptyStatement(b.token(PHPPunctuator.SEMICOLON)));
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
        .is(f.block(
            b.token(PHPPunctuator.LCURLYBRACE),
            //fixme (Lena) : should be INNER_STATEMENT_LIST
            b.zeroOrMore(STATEMENT()),
            b.token(PHPPunctuator.RCURLYBRACE)
        ));
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

  public ExpressionTree COMMON_SCALAR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.COMMON_SCALAR)
      .is(b.firstOf(
        f.heredocLiteral(b.token(PHPLexicalGrammar.HEREDOC)),
        NUMERIC_LITERAL(),
        STRING_LITERAL(),
        f.booleanLiteral(b.token(PHPLexicalGrammar.BOOLEAN_LITERAL)),
        f.nullLiteral(b.token(PHPLexicalGrammar.NULL)),
        f.magicConstantLiteral(b.firstOf(
          b.token(PHPLexicalGrammar.CLASS_CONSTANT),
          b.token(PHPLexicalGrammar.FILE_CONSTANT),
          b.token(PHPLexicalGrammar.DIR_CONSTANT),
          b.token(PHPLexicalGrammar.FUNCTION_CONSTANT),
          b.token(PHPLexicalGrammar.LINE_CONSTANT),
          b.token(PHPLexicalGrammar.METHOD_CONSTANT),
          b.token(PHPLexicalGrammar.NAMESPACE_CONSTANT),
          b.token(PHPLexicalGrammar.TRAIT_CONSTANT)))));
  }

  public LiteralTree NUMERIC_LITERAL() {
    return b.<LiteralTree>nonterminal(Kind.NUMERIC_LITERAL)
      .is(f.numericLiteral(b.token(PHPLexicalGrammar.NUMERIC_LITERAL)));
  }

  public ExpressionTree STRING_LITERAL() {
    return b.<ExpressionTree>nonterminal()
      .is(b.firstOf(
        f.regularStringLiteral(b.token(PHPLexicalGrammar.REGULAR_STRING_LITERAL)),
        EXPANDABLE_STRING_LITERAL()
        ));
  }

  public ExpandableStringLiteralTree EXPANDABLE_STRING_LITERAL() {
    return b.<ExpandableStringLiteralTree>nonterminal(Kind.EXPANDABLE_STRING_LITERAL)
      .is(f.expandableStringLiteral(
        b.token(PHPLexicalGrammar.SPACING),
        b.token(PHPLexicalGrammar.DOUBLE_QUOTE),
        b.oneOrMore(
          b.firstOf(
            ENCAPSULATED_STRING_VARIABLE(),
            EXPANDABLE_STRING_CHARACTERS()
            )
          ),
        b.token(PHPLexicalGrammar.DOUBLE_QUOTE)
        ));
  }

  public ExpressionTree ENCAPSULATED_STRING_VARIABLE() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.ENCAPS_VAR)
      .is(b.firstOf(
        ENCAPSULATED_SEMI_COMPLEX_VARIABLE(),
        ENCAPSULATED_SIMPLE_VARIABLE(),
        ENCAPSULATED_COMPLEX_VARIABLE()
        )
      );
  }

  public ExpressionTree ENCAPSULATED_COMPLEX_VARIABLE() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.COMPLEX_ENCAPS_VARIABLE)
      .is(f.encapsulatedComplexVariable(
        b.token(LCURLYBRACE),
        b.token(PHPLexicalGrammar.NEXT_IS_DOLLAR),
        EXPRESSION(),
        b.token(RCURLYBRACE)
        ));
  }

  public ExpressionTree ENCAPSULATED_SEMI_COMPLEX_VARIABLE() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.SEMI_COMPLEX_ENCAPS_VARIABLE)
      .is(f.encapsulatedSemiComplexVariable(
        b.token(DOLLAR_LCURLY),
        b.firstOf(
          EXPRESSION(),
          f.expressionRecovery(b.token(PHPLexicalGrammar.SEMI_COMPLEX_RECOVERY_EXPRESSION))),
        b.token(RCURLYBRACE)));
  }

  public ExpressionTree ENCAPSULATED_SIMPLE_VARIABLE() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.SIMPLE_ENCAPS_VARIABLE)
      .is(
        f.encapsulatedSimpleVar(ENCAPSULATED_VARIABLE_IDENTIFIER(),
          b.optional(b.firstOf(
            f.expandableArrayAccess(
              b.token(LBRACKET),
              b.firstOf(IDENTIFIER(), NUMERIC_LITERAL(), ENCAPSULATED_VARIABLE_IDENTIFIER()),
              b.token(RBRACKET)),
            f.expandableObjectMemberAccess(b.token(ARROW), IDENTIFIER()))))
      );
  }

  public IdentifierTree IDENTIFIER() {
    return b.<IdentifierTree>nonterminal(Kind.IDENTIFIER)
      .is(f.identifier(b.token(PHPLexicalGrammar.IDENTIFIER)));
  }

  public VariableIdentifierTree ENCAPSULATED_VARIABLE_IDENTIFIER() {
    return b.<VariableIdentifierTree>nonterminal(PHPLexicalGrammar.ENCAPS_VAR_IDENTIFIER)
      // variable identifiers encapsulated into strings literal does not allowed line terminator spacing,
      // so here using "WHITESPACE" instead of SPACING.
      .is(f.encapsulatedVariableIdentifier(b.token(PHPLexicalGrammar.WHITESPACES), b.token(PHPLexicalGrammar.VARIABLE_IDENTIFIER)));
  }

  public ExpressionTree EXPANDABLE_STRING_CHARACTERS() {
    return b.<ExpandableStringCharactersTree>nonterminal(Kind.EXPANDABLE_STRING_CHARACTERS)
      .is(f.expandableStringCharacters(b.token(PHPLexicalGrammar.STRING_WITH_ENCAPS_VAR_CHARACTERS)));
  }

  public YieldExpressionTree YIELD_EXPRESSION() {
    return b.<YieldExpressionTree>nonterminal(Kind.YIELD_EXPRESSION)
      .is(f.yieldExpression(
        b.token(YIELD),
        EXPRESSION(),
        b.optional(f.newTuple1(b.token(DOUBLEARROW), EXPRESSION()))
        ));
  }

  public ParenthesisedExpressionTree PARENTHESIZED_EXPRESSION() {
    return b.<ParenthesisedExpressionTree>nonterminal(Kind.PARENTHESISED_EXPRESSION)
      .is(f.parenthesizedExpression(
        b.token(LPARENTHESIS),
        b.firstOf(
          YIELD_EXPRESSION(),
          EXPRESSION()),
        b.token(RPARENTHESIS)
        ));
  }

  public AssignmentExpressionTree LIST_EXPRESSION_ASSIGNMENT() {
    return b.<AssignmentExpressionTree>nonterminal()
      .is(f.listExpressionAssignment(LIST_EXPRESSION(), b.token(EQU), EXPRESSION()));
  }

  public ListExpressionTree LIST_EXPRESSION() {
    return b.<ListExpressionTree>nonterminal(Kind.LIST_EXPRESSION)
      .is(f.listExpression(
        b.token(LIST),
        b.token(LPARENTHESIS),
        b.optional(f.newTuple7(
          LIST_ELEMENT(),
          b.zeroOrMore(f.newTuple3(b.token(COMMA), LIST_ELEMENT())))),
        b.token(RPARENTHESIS))
      );
  }

  public ExpressionTree LIST_ELEMENT() {
    return b.<ExpressionTree>nonterminal()
      .is(b.firstOf(
        EXPRESSION(), // FIXME => /!\ replace with MEMBER_EXPRESSION
        LIST_EXPRESSION()));
  }

  public ComputedVariableTree COMPUTED_VARIABLE_NAME() {
    return b.<ComputedVariableTree>nonterminal(Kind.COMPUTED_VARIABLE_NAME)
      .is(f.computedVariableName(
        b.token(LCURLYBRACE),
        EXPRESSION(),
        b.token(RCURLYBRACE)));

  }

  public VariableIdentifierTree VARIABLE_IDENTIFIER() {
    return b.<VariableIdentifierTree>nonterminal(Kind.VARIABLE_IDENTIFIER)
      .is(f.variableIdentifier(b.token(PHPLexicalGrammar.REGULAR_VAR_IDENTIFIER)));
  }

  public VariableTree COMPOUND_VARIABLE() {
    return b.<VariableTree>nonterminal(Kind.COMPOUND_VARIABLE_NAME)
      .is(
        b.firstOf(
          VARIABLE_IDENTIFIER(),
          f.compoundVariable(b.token(DOLLAR_LCURLY), EXPRESSION(), b.token(RCURLYBRACE))));
  }

  public ExpressionTree VARIABLE_WITHOUT_OBJECTS() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.VARIABLE_WITHOUT_OBJECTS)
      .is(f.variableWithoutObjects(
        b.zeroOrMore(b.token(PHPLexicalGrammar.VARIABLE_VARIABLE_DOLLAR)),
        COMPOUND_VARIABLE(),
        b.zeroOrMore(b.firstOf(
          DIMENSIONAL_OFFSET(),
          ALTERNATIVE_DIMENSIONAL_OFFSET()
        ))));
  }

  public ArrayAccessTree ALTERNATIVE_DIMENSIONAL_OFFSET() {
    return b.<ArrayAccessTree>nonterminal()
      .is(f.alternativeDimensionalOffset(
        b.token(LCURLYBRACE),
        b.optional(EXPRESSION()),
        b.token(RCURLYBRACE)
      ));
  }

  public ArrayAccessTree DIMENSIONAL_OFFSET() {
    return b.<ArrayAccessTree>nonterminal()
      .is(f.dimensionalOffset(
        b.token(LBRACKET),
        b.optional(EXPRESSION()),
        b.token(RBRACKET)
      ));
  }

  /**
   * [ END ] Expression
   */

}
