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
package org.sonar.php.parser;

import com.sonar.sslr.api.typed.GrammarBuilder;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.tree.impl.SeparatedListImpl;
import org.sonar.php.tree.impl.expression.HeredocStringLiteralTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.impl.statement.DeclareStatementTreeImpl.DeclareStatementHead;
import org.sonar.php.tree.impl.statement.ForEachStatementTreeImpl.ForEachStatementHeader;
import org.sonar.php.tree.impl.statement.ForStatementTreeImpl.ForStatementHeader;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ConstantDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.declaration.TypeNameTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ComputedVariableTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.HeredocStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.expression.ListExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ReferenceVariableTree;
import org.sonar.plugins.php.api.tree.expression.SpreadArgumentTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.expression.YieldExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;
import org.sonar.plugins.php.api.tree.statement.DeclareStatementTree;
import org.sonar.plugins.php.api.tree.statement.DoWhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.ElseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.EmptyStatementTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionListStatementTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.tree.statement.GlobalStatementTree;
import org.sonar.plugins.php.api.tree.statement.GotoStatementTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.tree.statement.InlineHTMLTree;
import org.sonar.plugins.php.api.tree.statement.LabelTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.StatementTree;
import org.sonar.plugins.php.api.tree.statement.StaticStatementTree;
import org.sonar.plugins.php.api.tree.statement.SwitchCaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.SwitchStatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.tree.statement.TraitAliasTree;
import org.sonar.plugins.php.api.tree.statement.TraitMethodReferenceTree;
import org.sonar.plugins.php.api.tree.statement.TraitPrecedenceTree;
import org.sonar.plugins.php.api.tree.statement.TryStatementTree;
import org.sonar.plugins.php.api.tree.statement.UnsetVariableStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseTraitDeclarationTree;
import org.sonar.plugins.php.api.tree.statement.WhileStatementTree;
import org.sonar.plugins.php.api.tree.statement.YieldStatementTree;

import static org.sonar.php.api.PHPKeyword.ABSTRACT;
import static org.sonar.php.api.PHPKeyword.ARRAY;
import static org.sonar.php.api.PHPKeyword.CALLABLE;
import static org.sonar.php.api.PHPKeyword.CLASS;
import static org.sonar.php.api.PHPKeyword.DIE;
import static org.sonar.php.api.PHPKeyword.ECHO;
import static org.sonar.php.api.PHPKeyword.EXIT;
import static org.sonar.php.api.PHPKeyword.EXTENDS;
import static org.sonar.php.api.PHPKeyword.FINAL;
import static org.sonar.php.api.PHPKeyword.FUNCTION;
import static org.sonar.php.api.PHPKeyword.HALT_COMPILER;
import static org.sonar.php.api.PHPKeyword.IMPLEMENTS;
import static org.sonar.php.api.PHPKeyword.INSTANCEOF;
import static org.sonar.php.api.PHPKeyword.INTERFACE;
import static org.sonar.php.api.PHPKeyword.LIST;
import static org.sonar.php.api.PHPKeyword.NEW;
import static org.sonar.php.api.PHPKeyword.STATIC;
import static org.sonar.php.api.PHPKeyword.TRAIT;
import static org.sonar.php.api.PHPKeyword.USE;
import static org.sonar.php.api.PHPKeyword.YIELD;
import static org.sonar.php.api.PHPPunctuator.AMPERSAND;
import static org.sonar.php.api.PHPPunctuator.ARROW;
import static org.sonar.php.api.PHPPunctuator.COLON;
import static org.sonar.php.api.PHPPunctuator.COMMA;
import static org.sonar.php.api.PHPPunctuator.DEC;
import static org.sonar.php.api.PHPPunctuator.DIV;
import static org.sonar.php.api.PHPPunctuator.DOLLAR_LCURLY;
import static org.sonar.php.api.PHPPunctuator.DOT;
import static org.sonar.php.api.PHPPunctuator.DOUBLEARROW;
import static org.sonar.php.api.PHPPunctuator.DOUBLECOLON;
import static org.sonar.php.api.PHPPunctuator.ELIPSIS;
import static org.sonar.php.api.PHPPunctuator.EQU;
import static org.sonar.php.api.PHPPunctuator.EQUAL;
import static org.sonar.php.api.PHPPunctuator.EQUAL2;
import static org.sonar.php.api.PHPPunctuator.GE;
import static org.sonar.php.api.PHPPunctuator.GT;
import static org.sonar.php.api.PHPPunctuator.INC;
import static org.sonar.php.api.PHPPunctuator.LBRACKET;
import static org.sonar.php.api.PHPPunctuator.LCURLYBRACE;
import static org.sonar.php.api.PHPPunctuator.LE;
import static org.sonar.php.api.PHPPunctuator.LPARENTHESIS;
import static org.sonar.php.api.PHPPunctuator.LT;
import static org.sonar.php.api.PHPPunctuator.MINUS;
import static org.sonar.php.api.PHPPunctuator.MOD;
import static org.sonar.php.api.PHPPunctuator.NOTEQUAL;
import static org.sonar.php.api.PHPPunctuator.NOTEQUAL2;
import static org.sonar.php.api.PHPPunctuator.NOTEQUALBIS;
import static org.sonar.php.api.PHPPunctuator.NS_SEPARATOR;
import static org.sonar.php.api.PHPPunctuator.PLUS;
import static org.sonar.php.api.PHPPunctuator.QUERY;
import static org.sonar.php.api.PHPPunctuator.RBRACKET;
import static org.sonar.php.api.PHPPunctuator.RCURLYBRACE;
import static org.sonar.php.api.PHPPunctuator.RPARENTHESIS;
import static org.sonar.php.api.PHPPunctuator.SL;
import static org.sonar.php.api.PHPPunctuator.SPACESHIP;
import static org.sonar.php.api.PHPPunctuator.SR;
import static org.sonar.php.api.PHPPunctuator.STAR;
import static org.sonar.php.api.PHPPunctuator.STAR_STAR;

public class PHPGrammar {

  private final GrammarBuilder<InternalSyntaxToken> b;
  private final TreeFactory f;

  public PHPGrammar(GrammarBuilder<InternalSyntaxToken> b, TreeFactory f) {
    this.b = b;
    this.f = f;
  }

  public CompilationUnitTree COMPILATION_UNIT() {
    return b.<CompilationUnitTree>nonterminal(PHPLexicalGrammar.COMPILATION_UNIT).is(
      f.compilationUnit(b.optional(SCRIPT()), b.optional(b.token(PHPLexicalGrammar.SPACING)), b.token(PHPLexicalGrammar.EOF)));
  }

  public ScriptTree SCRIPT() {
    return b.<ScriptTree>nonterminal(PHPLexicalGrammar.SCRIPT).is(
      b.firstOf(
        f.script(b.token(PHPLexicalGrammar.FILE_OPENING_TAG), b.zeroOrMore(TOP_STATEMENT())),
        f.script(b.token(PHPLexicalGrammar.ANYTHING_BUT_START_TAG))));
  }

  /**
   * [ START ] Declaration
   */

  public VariableDeclarationTree MEMBER_CONST_DECLARATION() {
    return b.<VariableDeclarationTree>nonterminal(PHPLexicalGrammar.MEMBER_CONST_DECLARATION).is(
      f.memberConstDeclaration(
        b.token(PHPLexicalGrammar.IDENTIFIER_OR_KEYWORD),
        b.optional(f.newTuple(b.token(EQU), STATIC_SCALAR()))));
  }

  public VariableDeclarationTree CONST_VAR() {
    return b.<VariableDeclarationTree>nonterminal(PHPLexicalGrammar.CONSTANT_VAR).is(
      f.constDeclaration(
        b.token(PHPLexicalGrammar.IDENTIFIER),
        b.token(EQU),
        STATIC_SCALAR()));
  }

  public VariableDeclarationTree VARIABLE_DECLARATION() {
    return b.<VariableDeclarationTree>nonterminal(PHPLexicalGrammar.VARIABLE_DECLARATION).is(
      f.variableDeclaration(
        b.token(PHPLexicalGrammar.REGULAR_VAR_IDENTIFIER),
        b.optional(f.newTuple(b.token(EQU), STATIC_SCALAR()))));
  }

  public NamespaceNameTree NAMESPACE_NAME() {
    return b.<NamespaceNameTree>nonterminal(PHPLexicalGrammar.NAMESPACE_NAME).is(
      b.firstOf(
        f.namespaceName(
          b.oneOrMore(f.newTuple(
            b.token(PHPPunctuator.NS_SEPARATOR),
            b.token(PHPLexicalGrammar.IDENTIFIER)))),
        f.namespaceName(
          b.firstOf(b.token(PHPLexicalGrammar.IDENTIFIER), b.token(PHPKeyword.NAMESPACE)),
          b.zeroOrMore(f.newTuple(
            b.token(PHPPunctuator.NS_SEPARATOR),
            b.token(PHPLexicalGrammar.IDENTIFIER))))));
  }

  public UseClauseTree GROUP_USE_CLAUSE() {
    return b.<UseClauseTree>nonterminal(PHPLexicalGrammar.GROUP_USE_CLAUSE).is(
      f.groupUseClause(
        b.optional(USE_TYPE()),
        NAMESPACE_NAME(),
        b.optional(
          f.newTuple(
            b.token(PHPKeyword.AS),
            b.token(PHPLexicalGrammar.IDENTIFIER)))));
  }

  public InternalSyntaxToken USE_TYPE() {
    return b.<InternalSyntaxToken>nonterminal().is(
      b.firstOf(
        b.token(PHPKeyword.CONST),
        b.token(PHPKeyword.FUNCTION))
    );
  }

  public UseClauseTree USE_CLAUSE() {
    return b.<UseClauseTree>nonterminal(PHPLexicalGrammar.USE_CLAUSE).is(
      f.useClause(
        NAMESPACE_NAME(),
        b.optional(
          f.newTuple(
            b.token(PHPKeyword.AS),
            b.token(PHPLexicalGrammar.IDENTIFIER)))));
  }

  public ClassDeclarationTree CLASS_DECLARATION() {
    return b.<ClassDeclarationTree>nonterminal(PHPLexicalGrammar.CLASS_DECLARATION).is(
      f.classDeclaration(
        b.optional(b.firstOf(b.token(ABSTRACT), b.token(FINAL))),
        b.token(CLASS),
        NAME_IDENTIFIER(),
        b.optional(f.newTuple(b.token(EXTENDS), NAMESPACE_NAME())),
        b.optional(f.newTuple(b.token(IMPLEMENTS), INTERFACE_LIST())),
        b.token(LCURLYBRACE),
        b.zeroOrMore(CLASS_MEMBER()),
        b.token(RCURLYBRACE)));
  }

  public ClassDeclarationTree TRAIT_DECLARATION() {
    return b.<ClassDeclarationTree>nonterminal(PHPLexicalGrammar.TRAIT_DECLARATION).is(
      f.traitDeclaration(
        b.token(TRAIT),
        NAME_IDENTIFIER(),
        b.token(LCURLYBRACE),
        b.zeroOrMore(CLASS_MEMBER()),
        b.token(RCURLYBRACE)));
  }

  public ClassDeclarationTree INTERFACE_DECLARATION() {
    return b.<ClassDeclarationTree>nonterminal(PHPLexicalGrammar.INTERFACE_DECLARATION).is(
      f.interfaceDeclaration(
        b.token(INTERFACE),
        NAME_IDENTIFIER(),
        b.optional(f.newTuple(b.token(EXTENDS), INTERFACE_LIST())),
        b.token(LCURLYBRACE),
        b.zeroOrMore(CLASS_MEMBER()),
        b.token(RCURLYBRACE)));
  }


  public ClassMemberTree CLASS_MEMBER() {
    return b.<ClassMemberTree>nonterminal(PHPLexicalGrammar.CLASS_MEMBER).is(
      b.firstOf(
        METHOD_DECLARATION(),
        CLASS_VARIABLE_DECLARATION(),
        CLASS_CONSTANT_DECLARATION(),
        USE_TRAIT_DECLARATION()));
  }

  public ClassPropertyDeclarationTree CLASS_CONSTANT_DECLARATION() {
    return b.<ClassPropertyDeclarationTree>nonterminal(PHPLexicalGrammar.CLASS_CONSTANT_DECLARATION).is(
      f.classConstantDeclaration(
        b.optional(VISIBILITY_MODIFIER()),
        b.token(PHPKeyword.CONST),
        MEMBER_CONST_DECLARATION(),
        b.zeroOrMore(f.newTuple(b.token(COMMA), MEMBER_CONST_DECLARATION())),
        EOS()));
  }

  public ConstantDeclarationTree CONSTANT_DECLARATION() {
    return b.<ConstantDeclarationTree>nonterminal(PHPLexicalGrammar.CONSTANT_DECLARATION).is(
      f.constantDeclaration(
        b.token(PHPKeyword.CONST),
        CONST_VAR(),
        b.zeroOrMore(f.newTuple(b.token(COMMA), CONST_VAR())),
        EOS()));
  }

  public ClassPropertyDeclarationTree CLASS_VARIABLE_DECLARATION() {
    return b.<ClassPropertyDeclarationTree>nonterminal(PHPLexicalGrammar.CLASS_VARIABLE_DECLARATION).is(
      f.classVariableDeclaration(
        b.firstOf(
          f.singleToken(b.token(PHPKeyword.VAR)),
          b.oneOrMore(MEMBER_MODIFIER())),
        VARIABLE_DECLARATION(),
        b.zeroOrMore(f.newTuple(b.token(COMMA), VARIABLE_DECLARATION())),
        EOS()));
  }

  public SyntaxToken VISIBILITY_MODIFIER() {
    return b.<SyntaxToken>nonterminal(PHPLexicalGrammar.VISIBILITY_MODIFIER).is(
      b.firstOf(
        b.token(PHPKeyword.PUBLIC),
        b.token(PHPKeyword.PROTECTED),
        b.token(PHPKeyword.PRIVATE)));
  }

  public SyntaxToken MEMBER_MODIFIER() {
    return b.<SyntaxToken>nonterminal(PHPLexicalGrammar.MEMBER_MODIFIER).is(
      b.firstOf(
        VISIBILITY_MODIFIER(),
        b.token(PHPKeyword.STATIC),
        b.token(PHPKeyword.ABSTRACT),
        b.token(PHPKeyword.FINAL)));
  }

  public MethodDeclarationTree METHOD_DECLARATION() {
    return b.<MethodDeclarationTree>nonterminal(PHPLexicalGrammar.METHOD_DECLARATION).is(
      f.methodDeclaration(
        b.zeroOrMore(MEMBER_MODIFIER()),
        b.token(PHPKeyword.FUNCTION),
        b.optional(b.token(PHPPunctuator.AMPERSAND)),
        NAME_IDENTIFIER_OR_KEYWORD(),
        PARAMETER_LIST(),
        b.optional(RETURN_TYPE_CLAUSE()),
        b.firstOf(
          EOS(),
          BLOCK())));
  }

  public FunctionDeclarationTree FUNCTION_DECLARATION() {
    return b.<FunctionDeclarationTree>nonterminal(PHPLexicalGrammar.FUNCTION_DECLARATION).is(
      f.functionDeclaration(
        b.token(PHPKeyword.FUNCTION),
        b.optional(b.token(PHPPunctuator.AMPERSAND)),
        NAME_IDENTIFIER(),
        PARAMETER_LIST(),
        b.optional(RETURN_TYPE_CLAUSE()),
        BLOCK()));
  }

  public ReturnTypeClauseTree RETURN_TYPE_CLAUSE() {
    return b.<ReturnTypeClauseTree>nonterminal(PHPLexicalGrammar.RETURN_TYPE_CLAUSE).is(
      f.returnTypeClause(b.token(COLON), TYPE())
    );
  }

  public ParameterListTree PARAMETER_LIST() {
    return b.<ParameterListTree>nonterminal(PHPLexicalGrammar.PARAMETER_LIST).is(
      f.parameterList(
        b.token(PHPPunctuator.LPARENTHESIS),
        b.optional(
          f.newTuple(
            PARAMETER(),
            b.zeroOrMore(
              f.newTuple(
                b.token(PHPPunctuator.COMMA),
                PARAMETER())))),
        b.token(PHPPunctuator.RPARENTHESIS)));
  }

  public ParameterTree PARAMETER() {
    return b.<ParameterTree>nonterminal(PHPLexicalGrammar.PARAMETER).is(
      f.parameter(
        b.optional(TYPE()),
        b.optional(b.token(PHPPunctuator.AMPERSAND)),
        b.optional(b.token(PHPPunctuator.ELIPSIS)),
        b.token(PHPLexicalGrammar.REGULAR_VAR_IDENTIFIER),
        b.optional(
          f.newTuple(
            b.token(PHPPunctuator.EQU),
            STATIC_SCALAR()))));
  }

  public SeparatedListImpl<NamespaceNameTree> INTERFACE_LIST() {
    return b.<SeparatedListImpl<NamespaceNameTree>>nonterminal(PHPLexicalGrammar.INTERFACE_LIST).is(
      f.interfaceList(
        NAMESPACE_NAME(),
        b.zeroOrMore(f.newTuple(b.token(COMMA), NAMESPACE_NAME()))));
  }

  public UseTraitDeclarationTree USE_TRAIT_DECLARATION() {
    return b.<UseTraitDeclarationTree>nonterminal(Kind.USE_TRAIT_DECLARATION).is(
      b.firstOf(
        f.useTraitDeclaration(
          b.token(PHPKeyword.USE),
          INTERFACE_LIST(),
          EOS()),
        f.useTraitDeclaration(
          b.token(PHPKeyword.USE),
          INTERFACE_LIST(),
          b.token(LCURLYBRACE),
          b.zeroOrMore(
            b.firstOf(
              TRAIT_PRECEDENCE(),
              TRAIT_ALIAS())),
          b.token(RCURLYBRACE))));
  }

  public TraitPrecedenceTree TRAIT_PRECEDENCE() {
    return b.<TraitPrecedenceTree>nonterminal(PHPLexicalGrammar.TRAIT_PRECEDENCE).is(
      f.traitPrecedence(
        TRAIT_METHOD_REFERENCE_FULLY_QUALIFIED(),
        b.token(PHPKeyword.INSTEADOF),
        INTERFACE_LIST(),
        EOS()));
  }

  public TraitAliasTree TRAIT_ALIAS() {
    return b.<TraitAliasTree>nonterminal(PHPLexicalGrammar.TRAIT_ALIAS).is(
      b.firstOf(
        f.traitAlias(
          TRAIT_METHOD_REFERENCE(),
          b.token(PHPKeyword.AS),
          b.optional(MEMBER_MODIFIER()),
          NAME_IDENTIFIER_OR_KEYWORD(),
          EOS()),
        f.traitAlias(
          TRAIT_METHOD_REFERENCE(),
          b.token(PHPKeyword.AS),
          MEMBER_MODIFIER(),
          EOS())));
  }

  public TraitMethodReferenceTree TRAIT_METHOD_REFERENCE() {
    return b.<TraitMethodReferenceTree>nonterminal(PHPLexicalGrammar.TRAIT_METHOD_REFERENCE).is(
      b.firstOf(
        TRAIT_METHOD_REFERENCE_FULLY_QUALIFIED(),
        f.traitMethodReference(
          b.token(PHPLexicalGrammar.IDENTIFIER_OR_KEYWORD))));
  }

  public TraitMethodReferenceTree TRAIT_METHOD_REFERENCE_FULLY_QUALIFIED() {
    return b.<TraitMethodReferenceTree>nonterminal(PHPLexicalGrammar.TRAIT_METHOD_REFERENCE_FULLY_QUALIFIED).is(
      f.traitMethodReference(
        NAMESPACE_NAME(),
        b.token(PHPPunctuator.DOUBLECOLON),
        b.token(PHPLexicalGrammar.IDENTIFIER_OR_KEYWORD)));
  }

  public TypeTree TYPE() {
    return b.<TypeTree>nonterminal(PHPLexicalGrammar.TYPE).is(
      f.type(
        b.optional(b.token(PHPPunctuator.QUERY)),
        TYPE_NAME()));
  }

  public TypeNameTree TYPE_NAME() {
    return b.<TypeNameTree>nonterminal(PHPLexicalGrammar.TYPE_NAME).is(
      b.firstOf(
        f.builtInType(b.firstOf(
          b.token(ARRAY),
          b.token(CALLABLE),
          b.token(PHPLexicalGrammar.SELF),
          b.token(PHPLexicalGrammar.BOOL),
          b.token(PHPLexicalGrammar.FLOAT),
          b.token(PHPLexicalGrammar.INT),
          b.token(PHPLexicalGrammar.STRING))),
        NAMESPACE_NAME()
      ));
  }

  /**
   * [ END ] Declaration
   */

  /**
   * [ START ] Statement
   */

  public StatementTree TOP_STATEMENT() {
    return b.<StatementTree>nonterminal(PHPLexicalGrammar.TOP_STATEMENT).is(
      b.firstOf(
        CLASS_DECLARATION(),
        TRAIT_DECLARATION(),
        FUNCTION_DECLARATION(),
        INTERFACE_DECLARATION(),
        NAMESPACE_STATEMENT(),
        GROUP_USE_STATEMENT(),
        USE_STATEMENT(),
        CONSTANT_DECLARATION(),
        HALT_COMPILER_STATEMENT(),
        STATEMENT()));
  }

  public ExpressionStatementTree HALT_COMPILER_STATEMENT() {
    return b.<ExpressionStatementTree>nonterminal().is(
      f.haltCompilerStatement(
        b.token(HALT_COMPILER),
        b.token(LPARENTHESIS),
        b.token(RPARENTHESIS),
        EOS()));
  }

  public NamespaceStatementTree NAMESPACE_STATEMENT() {
    return b.<NamespaceStatementTree>nonterminal(PHPLexicalGrammar.NAMESPACE_STATEMENT).is(
      b.firstOf(
        f.namespaceStatement(
          b.token(PHPKeyword.NAMESPACE),
          NAMESPACE_NAME(),
          EOS()
        ),
        f.blockNamespaceStatement(
          b.token(PHPKeyword.NAMESPACE),
          b.optional(NAMESPACE_NAME()),
          b.token(LCURLYBRACE),
          b.zeroOrMore(TOP_STATEMENT()),
          b.token(RCURLYBRACE))));
  }

  public UseStatementTree USE_STATEMENT() {
    return b.<UseStatementTree>nonterminal(PHPLexicalGrammar.USE_STATEMENT).is(
      f.useStatement(
        b.token(PHPKeyword.USE),
        b.optional(USE_TYPE()),
        USE_CLAUSE(),
        b.zeroOrMore(f.newTuple(b.token(PHPPunctuator.COMMA), USE_CLAUSE())),
        EOS()));
  }

  public UseStatementTree GROUP_USE_STATEMENT() {
    return b.<UseStatementTree>nonterminal(Kind.GROUP_USE_STATEMENT).is(
      f.groupUseStatement(
        b.token(PHPKeyword.USE),
        b.optional(USE_TYPE()),
        NAMESPACE_NAME(),
        b.token(NS_SEPARATOR),
        b.token(LCURLYBRACE),
        GROUP_USE_CLAUSE(),
        b.zeroOrMore(f.newTuple(b.token(PHPPunctuator.COMMA), GROUP_USE_CLAUSE())),
        b.optional(b.token(COMMA)),
        b.token(RCURLYBRACE),
        EOS()));
  }

  public StatementTree STATEMENT() {
    return b.<StatementTree>nonterminal(PHPLexicalGrammar.STATEMENT).is(
      b.firstOf(
        BLOCK(),
        THROW_STATEMENT(),
        IF_STATEMENT(),
        WHILE_STATEMENT(),
        DO_WHILE_STATEMENT(),
        FOREACH_STATEMENT(),
        FOR_STATEMENT(),
        SWITCH_STATEMENT(),
        BREAK_STATEMENT(),
        CONTINUE_STATEMENT(),
        RETURN_STATEMENT(),
        EMPTY_STATEMENT(),
        YIELD_STATEMENT(),
        GLOBAL_STATEMENT(),
        STATIC_STATEMENT(),
        ECHO_STATEMENT(),
        TRY_STATEMENT(),
        DECLARE_STATEMENT(),
        GOTO_STATEMENT(),
        INLINE_HTML(),
        UNSET_VARIABLE_STATEMENT(),
        EXPRESSION_STATEMENT(),
        LABEL(),
        EXPRESSION_LIST_STATEMENT()));
  }

  public ExpressionStatementTree ECHO_STATEMENT() {
    return b.<ExpressionStatementTree>nonterminal(PHPLexicalGrammar.ECHO_STATEMENT).is(
      f.echoStatement(
        b.token(ECHO),
        ARGUMENTS(),
        EOS()));
  }

  public StaticStatementTree STATIC_STATEMENT() {
    return b.<StaticStatementTree>nonterminal(PHPLexicalGrammar.STATIC_STATEMENT).is(
      f.staticStatement(
        b.token(STATIC),
        STATIC_VAR(),
        b.zeroOrMore(f.newTuple(b.token(COMMA), STATIC_VAR())),
        EOS()));
  }

  public VariableDeclarationTree STATIC_VAR() {
    return b.<VariableDeclarationTree>nonterminal(PHPLexicalGrammar.STATIC_VAR).is(
      f.staticVar(
        b.token(PHPLexicalGrammar.REGULAR_VAR_IDENTIFIER),
        b.optional(f.newTuple(b.token(EQU), STATIC_SCALAR()))));
  }

  public DeclareStatementTree DECLARE_STATEMENT() {
    return b.<DeclareStatementTree>nonterminal(PHPLexicalGrammar.DECLARE_STATEMENT).is(
      b.firstOf(
        f.shortDeclareStatement(
          DECLARE_STATEMENT_HEAD(),
          EOS()
        ),
        f.declareStatementWithOneStatement(
          DECLARE_STATEMENT_HEAD(),
          STATEMENT()
        ),
        f.alternativeDeclareStatement(
          DECLARE_STATEMENT_HEAD(),
          b.token(COLON),
          b.zeroOrMore(INNER_STATEMENT()),
          b.token(PHPKeyword.ENDDECLARE),
          EOS())));
  }

  public DeclareStatementHead DECLARE_STATEMENT_HEAD() {
    return b.<DeclareStatementHead>nonterminal().is(
      f.declareStatementHead(
        b.token(PHPKeyword.DECLARE),
        b.token(LPARENTHESIS),
        MEMBER_CONST_DECLARATION(),
        b.zeroOrMore(f.newTuple(b.token(COMMA), MEMBER_CONST_DECLARATION())),
        b.token(RPARENTHESIS)));
  }

  public InlineHTMLTree INLINE_HTML() {
    return b.<InlineHTMLTree>nonterminal(PHPLexicalGrammar.INLINE_HTML_STATEMENT).is(
      f.inlineHTML(b.token(PHPLexicalGrammar.INLINE_HTML)));
  }

  public StatementTree INNER_STATEMENT() {
    return b.<StatementTree>nonterminal(PHPLexicalGrammar.INNER_STATEMENT).is(
      b.firstOf(
        FUNCTION_DECLARATION(),
        CLASS_DECLARATION(),
        TRAIT_DECLARATION(),
        INTERFACE_DECLARATION(),
        STATEMENT()));
  }


  public GlobalStatementTree GLOBAL_STATEMENT() {
    return b.<GlobalStatementTree>nonterminal(PHPLexicalGrammar.GLOBAL_STATEMENT).is(
      f.globalStatement(
        b.token(PHPKeyword.GLOBAL),
        GLOBAL_VAR(),
        b.zeroOrMore(f.newTuple(b.token(COMMA), GLOBAL_VAR())),
        EOS()));
  }

  public VariableTree GLOBAL_VAR() {
    return b.<VariableTree>nonterminal(PHPLexicalGrammar.GLOBAL_VAR).is(
      f.globalVar(
        b.zeroOrMore(b.token(PHPLexicalGrammar.VARIABLE_VARIABLE_DOLLAR)),
        COMPOUND_VARIABLE()));
  }

  public UnsetVariableStatementTree UNSET_VARIABLE_STATEMENT() {
    return b.<UnsetVariableStatementTree>nonterminal(PHPLexicalGrammar.UNSET_VARIABLE_STATEMENT).is(
      f.unsetVariableStatement(
        b.token(PHPKeyword.UNSET),
        b.token(LPARENTHESIS),
        MEMBER_EXPRESSION(),
        b.zeroOrMore(f.newTuple(b.token(COMMA), MEMBER_EXPRESSION())),
        b.token(RPARENTHESIS),
        EOS()));
  }

  public YieldStatementTree YIELD_STATEMENT() {
    return b.<YieldStatementTree>nonterminal(PHPLexicalGrammar.YIELD_STATEMENT).is(
      f.yieldStatement(YIELD_EXPRESSION(), EOS()));
  }

  public SwitchStatementTree SWITCH_STATEMENT() {
    return b.<SwitchStatementTree>nonterminal(PHPLexicalGrammar.SWITCH_STATEMENT).is(
      b.firstOf(
        f.switchStatement(
          b.token(PHPKeyword.SWITCH),
          PARENTHESIZED_EXPRESSION(),
          b.token(PHPPunctuator.LCURLYBRACE),
          b.optional(b.token(PHPPunctuator.SEMICOLON)),
          b.zeroOrMore(SWITCH_CASE_CLAUSE()),
          b.token(PHPPunctuator.RCURLYBRACE)
        ),
        f.alternativeSwitchStatement(
          b.token(PHPKeyword.SWITCH),
          PARENTHESIZED_EXPRESSION(),
          b.token(PHPPunctuator.COLON),
          b.optional(b.token(PHPPunctuator.SEMICOLON)),
          b.zeroOrMore(SWITCH_CASE_CLAUSE()),
          b.token(PHPKeyword.ENDSWITCH),
          EOS())));
  }

  public SwitchCaseClauseTree SWITCH_CASE_CLAUSE() {
    return b.<SwitchCaseClauseTree>nonterminal(PHPLexicalGrammar.SWITCH_CASE_CLAUSE).is(
      b.firstOf(
        f.caseClause(
          b.token(PHPKeyword.CASE),
          EXPRESSION(),
          b.firstOf(b.token(PHPPunctuator.COLON), b.token(PHPPunctuator.SEMICOLON)),
          b.zeroOrMore(INNER_STATEMENT())
        ),
        f.defaultClause(
          b.token(PHPKeyword.DEFAULT),
          b.firstOf(b.token(PHPPunctuator.COLON), b.token(PHPPunctuator.SEMICOLON)),
          b.zeroOrMore(INNER_STATEMENT()))));
  }

  public WhileStatementTree WHILE_STATEMENT() {
    return b.<WhileStatementTree>nonterminal(PHPLexicalGrammar.WHILE_STATEMENT).is(
      b.firstOf(
        f.whileStatement(
          b.token(PHPKeyword.WHILE),
          PARENTHESIZED_EXPRESSION(),
          STATEMENT()
        ),
        f.alternativeWhileStatement(
          b.token(PHPKeyword.WHILE),
          PARENTHESIZED_EXPRESSION(),
          b.token(PHPPunctuator.COLON),
          b.zeroOrMore(INNER_STATEMENT()),
          b.token(PHPKeyword.ENDWHILE),
          EOS())));
  }

  public DoWhileStatementTree DO_WHILE_STATEMENT() {
    return b.<DoWhileStatementTree>nonterminal(PHPLexicalGrammar.DO_WHILE_STATEMENT).is(
      f.doWhileStatement(
        b.token(PHPKeyword.DO),
        STATEMENT(),
        b.token(PHPKeyword.WHILE),
        PARENTHESIZED_EXPRESSION(),
        EOS()));
  }

  public IfStatementTree IF_STATEMENT() {
    return b.<IfStatementTree>nonterminal(PHPLexicalGrammar.IF_STATEMENT).is(
      b.firstOf(STANDARD_IF_STATEMENT(), ALTERNATIVE_IF_STATEMENT()));
  }

  public IfStatementTree STANDARD_IF_STATEMENT() {
    return b.<IfStatementTree>nonterminal(PHPLexicalGrammar.STANDARD_IF_STATEMENT).is(
      f.ifStatement(
        b.token(PHPKeyword.IF),
        PARENTHESIZED_EXPRESSION(),
        STATEMENT(),
        b.zeroOrMore(ELSEIF_CLAUSE()),
        b.optional(ELSE_CLAUSE())));
  }

  public IfStatementTree ALTERNATIVE_IF_STATEMENT() {
    return b.<IfStatementTree>nonterminal(PHPLexicalGrammar.ALTERNATIVE_IF_STATEMENT).is(
      f.alternativeIfStatement(
        b.token(PHPKeyword.IF),
        PARENTHESIZED_EXPRESSION(),
        b.token(PHPPunctuator.COLON),
        b.zeroOrMore(INNER_STATEMENT()),
        b.zeroOrMore(ALTERNATIVE_ELSEIF_CLAUSE()),
        b.optional(ALTERNATIVE_ELSE_CLAUSE()),
        b.token(PHPKeyword.ENDIF),
        EOS()));
  }

  public ElseClauseTree ELSE_CLAUSE() {
    return b.<ElseClauseTree>nonterminal(PHPLexicalGrammar.ELSE_CLAUSE).is(
      f.elseClause(b.token(PHPKeyword.ELSE), STATEMENT()));
  }

  public ElseifClauseTree ELSEIF_CLAUSE() {
    return b.<ElseifClauseTree>nonterminal(PHPLexicalGrammar.ELSEIF_CLAUSE).is(
      f.elseifClause(
        b.token(PHPKeyword.ELSEIF),
        PARENTHESIZED_EXPRESSION(),
        STATEMENT()));
  }

  public ElseClauseTree ALTERNATIVE_ELSE_CLAUSE() {
    return b.<ElseClauseTree>nonterminal(PHPLexicalGrammar.ALTERNATIVE_ELSE_CLAUSE).is(
      f.alternativeElseClause(
        b.token(PHPKeyword.ELSE),
        b.token(PHPPunctuator.COLON),
        b.zeroOrMore(INNER_STATEMENT())));
  }

  public ElseifClauseTree ALTERNATIVE_ELSEIF_CLAUSE() {
    return b.<ElseifClauseTree>nonterminal(PHPLexicalGrammar.ALTERNATIVE_ELSEIF_CLAUSE).is(
      f.alternativeElseifClause(
        b.token(PHPKeyword.ELSEIF),
        PARENTHESIZED_EXPRESSION(),
        b.token(PHPPunctuator.COLON),
        b.zeroOrMore(INNER_STATEMENT())));
  }

  public ForStatementTree FOR_STATEMENT() {
    return b.<ForStatementTree>nonterminal(PHPLexicalGrammar.FOR_STATEMENT).is(
      b.firstOf(
        f.forStatement(
          FOR_STATEMENT_HEADER(),
          STATEMENT()),
        f.forStatementAlternative(
          FOR_STATEMENT_HEADER(),
          b.token(PHPPunctuator.COLON),
          b.zeroOrMore(INNER_STATEMENT()),
          b.token(PHPKeyword.ENDFOR),
          EOS())));
  }

  public ForStatementHeader FOR_STATEMENT_HEADER() {
    return b.<ForStatementHeader>nonterminal().is(
      f.forStatementHeader(
        b.token(PHPKeyword.FOR), b.token(PHPPunctuator.LPARENTHESIS),
        b.optional(FOR_EXPR()),
        b.token(PHPPunctuator.SEMICOLON),
        b.optional(FOR_EXPR()),
        b.token(PHPPunctuator.SEMICOLON),
        b.optional(FOR_EXPR()),
        b.token(PHPPunctuator.RPARENTHESIS)));
  }

  public SeparatedListImpl<ExpressionTree> FOR_EXPR() {
    return b.<SeparatedListImpl<ExpressionTree>>nonterminal(PHPLexicalGrammar.FOR_EXPR).is(
      f.forExpr(
        EXPRESSION(),
        b.zeroOrMore(f.newTuple(
          b.token(PHPPunctuator.COMMA),
          EXPRESSION()))));
  }

  public ForEachStatementTree FOREACH_STATEMENT() {
    return b.<ForEachStatementTree>nonterminal(PHPLexicalGrammar.FOREACH_STATEMENT).is(
      b.firstOf(
        f.forEachStatement(FOREACH_STATEMENT_HEADER(), STATEMENT()),
        f.forEachStatementAlternative(
          FOREACH_STATEMENT_HEADER(),
          b.token(PHPPunctuator.COLON),
          b.zeroOrMore(INNER_STATEMENT()),
          b.token(PHPKeyword.ENDFOREACH),
          EOS())));
  }

  public ForEachStatementHeader FOREACH_STATEMENT_HEADER() {
    return b.<ForEachStatementHeader>nonterminal().is(
      f.forEachStatementHeader(
        b.token(PHPKeyword.FOREACH), b.token(PHPPunctuator.LPARENTHESIS),
        EXPRESSION(), b.token(PHPKeyword.AS),
        b.optional(f.newTuple(FOREACH_VARIABLE(), b.token(PHPPunctuator.DOUBLEARROW))), FOREACH_VARIABLE(),
        b.token(PHPPunctuator.RPARENTHESIS)));
  }

  public ExpressionTree FOREACH_VARIABLE() {
    return b.<ExpressionTree>nonterminal().is(
      b.firstOf(REFERENCE_VARIABLE(), MEMBER_EXPRESSION(), LIST_EXPRESSION(), ARRAY_ASSIGNMENT_PATTERN()));
  }

  public ThrowStatementTree THROW_STATEMENT() {
    return b.<ThrowStatementTree>nonterminal(PHPLexicalGrammar.THROW_STATEMENT).is(
      f.throwStatement(b.token(PHPKeyword.THROW), EXPRESSION(), EOS()));
  }

  public EmptyStatementTree EMPTY_STATEMENT() {
    return b.<EmptyStatementTree>nonterminal(PHPLexicalGrammar.EMPTY_STATEMENT).is(
      f.emptyStatement(b.token(PHPPunctuator.SEMICOLON)));
  }

  public ReturnStatementTree RETURN_STATEMENT() {
    return b.<ReturnStatementTree>nonterminal(PHPLexicalGrammar.RETURN_STATEMENT).is(
      f.returnStatement(b.token(PHPKeyword.RETURN), b.optional(EXPRESSION()), EOS()));
  }

  public ContinueStatementTree CONTINUE_STATEMENT() {
    return b.<ContinueStatementTree>nonterminal(PHPLexicalGrammar.CONTINUE_STATEMENT).is(
      f.continueStatement(b.token(PHPKeyword.CONTINUE), b.optional(EXPRESSION()), EOS()));
  }

  public BreakStatementTree BREAK_STATEMENT() {
    return b.<BreakStatementTree>nonterminal(PHPLexicalGrammar.BREAK_STATEMENT).is(
      f.breakStatement(b.token(PHPKeyword.BREAK), b.optional(EXPRESSION()), EOS()));
  }

  public TryStatementTree TRY_STATEMENT() {
    return b.<TryStatementTree>nonterminal(PHPLexicalGrammar.TRY_STATEMENT).is(
      f.tryStatement(
        b.token(PHPKeyword.TRY),
        BLOCK(),
        b.zeroOrMore(CATCH_BLOCK()),
        b.optional(f.newTuple(b.token(PHPKeyword.FINALLY), BLOCK()))));
  }

  public CatchBlockTree CATCH_BLOCK() {
    return b.<CatchBlockTree>nonterminal(PHPLexicalGrammar.CATCH_BLOCK).is(
      f.catchBlock(
        b.token(PHPKeyword.CATCH),
        b.token(PHPPunctuator.LPARENTHESIS),
        NAMESPACE_NAME(),
        b.zeroOrMore(f.newTuple(b.token(PHPPunctuator.OR), NAMESPACE_NAME())),
        b.token(PHPLexicalGrammar.REGULAR_VAR_IDENTIFIER),
        b.token(PHPPunctuator.RPARENTHESIS),
        BLOCK()));
  }

  public BlockTree BLOCK() {
    return b.<BlockTree>nonterminal(PHPLexicalGrammar.BLOCK).is(
      f.block(
        b.token(PHPPunctuator.LCURLYBRACE),
        b.zeroOrMore(INNER_STATEMENT()),
        b.token(PHPPunctuator.RCURLYBRACE)));
  }

  public GotoStatementTree GOTO_STATEMENT() {
    return b.<GotoStatementTree>nonterminal(PHPLexicalGrammar.GOTO_STATEMENT).is(
      f.gotoStatement(b.token(PHPKeyword.GOTO), b.token(PHPLexicalGrammar.IDENTIFIER), EOS()));
  }

  public ExpressionStatementTree EXPRESSION_STATEMENT() {
    return b.<ExpressionStatementTree>nonterminal(PHPLexicalGrammar.EXPRESSION_STATEMENT).is(
      f.expressionStatement(EXPRESSION(), EOS()));
  }

  public ExpressionListStatementTree EXPRESSION_LIST_STATEMENT() {
    return b.<ExpressionListStatementTree>nonterminal(PHPLexicalGrammar.EXPRESSION_LIST_STATEMENT).is(
      f.expressionListStatement(
        EXPRESSION(),
        b.zeroOrMore(f.newTuple(b.token(PHPPunctuator.COMMA), EXPRESSION())),
        EOS()));
  }

  public LabelTree LABEL() {
    return b.<LabelTree>nonterminal(PHPLexicalGrammar.LABEL).is(
      f.label(b.token(PHPLexicalGrammar.IDENTIFIER), b.token(PHPPunctuator.COLON)));
  }

  public InternalSyntaxToken EOS() {
    return b.<InternalSyntaxToken>nonterminal(PHPLexicalGrammar.EOS).is(
      b.firstOf(b.token(PHPPunctuator.SEMICOLON), b.token(PHPLexicalGrammar.INLINE_HTML)));
  }

  /**
   * [ END ] Statement
   */

  /**
   * [ START ] Expression
   */

  public ExpressionTree CAST_EXPR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.CAST_TYPE).is(
      f.castExpression(
        b.token(PHPPunctuator.LPARENTHESIS),
        b.firstOf(
          b.token(PHPKeyword.ARRAY),
          b.token(PHPKeyword.UNSET),
          b.token(PHPLexicalGrammar.INTEGER),
          b.token(PHPLexicalGrammar.INT),
          b.token(PHPLexicalGrammar.DOUBLE),
          b.token(PHPLexicalGrammar.FLOAT),
          b.token(PHPLexicalGrammar.STRING),
          b.token(PHPLexicalGrammar.OBJECT),
          b.token(PHPLexicalGrammar.BOOLEAN),
          b.token(PHPLexicalGrammar.BOOL),
          b.token(PHPLexicalGrammar.BINARY)),
        b.token(PHPPunctuator.RPARENTHESIS),
        UNARY_EXPR()));
  }

  public ExpressionTree UNARY_EXPR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.UNARY_EXPR).is(
        f.prefixExpr(
          b.zeroOrMore(
          b.firstOf(
            b.token(PHPPunctuator.INC),
            b.token(PHPPunctuator.DEC),
            b.token(PHPPunctuator.PLUS),
            b.token(PHPPunctuator.MINUS),
            b.token(PHPPunctuator.TILDA),
            b.token(PHPPunctuator.BANG),
            b.token(PHPPunctuator.AT))),
          POWER_EXPR()));
  }

  public ExpressionTree POWER_EXPR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.POWER_EXPR).is(
      f.powerExpr(
        b.firstOf(
          CAST_EXPR(),
          ASSIGNMENT_EXPRESSION(),
          POSTFIX_EXPRESSION()),
        b.zeroOrMore(f.newTuple(
          b.token(STAR_STAR),
          UNARY_EXPR()))));
  }

  public ExpressionTree MULTIPLICATIVE_EXPR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.MULTIPLICATIVE_EXPR).is(
      f.binaryExpression(
        UNARY_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.firstOf(b.token(STAR), b.token(DIV), b.token(MOD)),
          UNARY_EXPR()))));
  }

  public ExpressionTree ADDITIVE_EXPR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.ADDITIVE_EXPR).is(
      f.binaryExpression(
        MULTIPLICATIVE_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.firstOf(b.token(PLUS), b.token(MINUS), b.token(DOT)),
          MULTIPLICATIVE_EXPR()))));
  }

  public ExpressionTree SHIFT_EXPR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.SHIFT_EXPR).is(
      f.binaryExpression(
        ADDITIVE_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.firstOf(b.token(SL), b.token(SR)),
          ADDITIVE_EXPR()))));
  }

  public ExpressionTree RELATIONAL_EXPR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.RELATIONAL_EXPR).is(
      f.binaryExpression(
        SHIFT_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.firstOf(b.token(LE), b.token(GE), b.token(LT), b.token(GT)),
          SHIFT_EXPR()))));
  }

  public ExpressionTree EQUALITY_EXPR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.EQUALITY_EXPR).is(
      f.binaryExpression(
        RELATIONAL_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.firstOf(b.token(NOTEQUAL2), b.token(NOTEQUAL), b.token(EQUAL2), b.token(EQUAL), b.token(NOTEQUALBIS), b.token(SPACESHIP)),
          RELATIONAL_EXPR()))));
  }

  public ExpressionTree BITWISE_AND_EXPR() {
    return b.<ExpressionTree>nonterminal(Kind.BITWISE_AND).is(
      f.binaryExpression(
        EQUALITY_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.token(PHPPunctuator.AMPERSAND),
          EQUALITY_EXPR()))));
  }

  public ExpressionTree BITWISE_XOR_EXPR() {
    return b.<ExpressionTree>nonterminal(Kind.BITWISE_XOR).is(
      f.binaryExpression(
        BITWISE_AND_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.token(PHPPunctuator.XOR),
          BITWISE_AND_EXPR()))));
  }

  public ExpressionTree BITWISE_OR_EXPR() {
    return b.<ExpressionTree>nonterminal(Kind.BITWISE_OR).is(
      f.binaryExpression(
        BITWISE_XOR_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.token(PHPPunctuator.OR),
          BITWISE_XOR_EXPR()))));
  }

  public ExpressionTree CONDITIONAL_AND_EXPR() {
    return b.<ExpressionTree>nonterminal(Kind.CONDITIONAL_AND).is(
      f.binaryExpression(
        BITWISE_OR_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.token(PHPPunctuator.ANDAND),
          BITWISE_OR_EXPR()))));
  }

  public ExpressionTree CONDITIONAL_OR_EXPR() {
    return b.<ExpressionTree>nonterminal(Kind.CONDITIONAL_OR).is(
      f.binaryExpression(
        CONDITIONAL_AND_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.token(PHPPunctuator.OROR),
          CONDITIONAL_AND_EXPR()))));
  }

  public ExpressionTree NULL_COALESCING_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(Kind.NULL_COALESCING_EXPRESSION).is(
      f.nullCoalescingExpr(
        CONDITIONAL_OR_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.token(PHPPunctuator.NULL_COALESCE),
          CONDITIONAL_OR_EXPR()))));
  }

  public ExpressionTree CONDITIONAL_EXPR() {
    return b.<ExpressionTree>nonterminal(Kind.CONDITIONAL_EXPRESSION).is(
      f.completeConditionalExpr(
        NULL_COALESCING_EXPRESSION(),
        b.zeroOrMore(
          f.newConditionalExpr(b.token(QUERY), b.optional(CONDITIONAL_EXPR()), b.token(COLON), NULL_COALESCING_EXPRESSION())
        )));
  }

  public ExpressionTree ALTERNATIVE_CONDITIONAL_AND_EXPR() {
    return b.<ExpressionTree>nonterminal(Kind.ALTERNATIVE_CONDITIONAL_AND).is(
      f.binaryExpression(
        // note that ASSIGNMENT_EXPRESSION is skipped even though it has lower priority than CONDITIONAL_EXPR,
        // this is because ASSIGNMENT_EXPRESSION can only have variable on lhs
        // so $a && $b = 1 is parsed as $a && ($b = 1) despite && having higher priority than =
        CONDITIONAL_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.firstOf(b.token(PHPKeyword.AND)),
          CONDITIONAL_EXPR()))));
  }

  public ExpressionTree CONDITIONAL_XOR_EXPR() {
    return b.<ExpressionTree>nonterminal(Kind.ALTERNATIVE_CONDITIONAL_XOR).is(
      f.binaryExpression(
        ALTERNATIVE_CONDITIONAL_AND_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.token(PHPKeyword.XOR),
          ALTERNATIVE_CONDITIONAL_AND_EXPR()))));
  }

  public ExpressionTree ALTERNATIVE_CONDITIONAL_OR_EXPR() {
    return b.<ExpressionTree>nonterminal(Kind.ALTERNATIVE_CONDITIONAL_OR).is(
      f.binaryExpression(
        CONDITIONAL_XOR_EXPR(),
        b.zeroOrMore(f.newTuple(
          b.firstOf(b.token(PHPKeyword.OR)),
          CONDITIONAL_XOR_EXPR()))));
  }

  public ExpressionTree EXPRESSION() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.EXPRESSION).is(
      ALTERNATIVE_CONDITIONAL_OR_EXPR());
  }

  public ExpressionTree COMMON_SCALAR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.COMMON_SCALAR).is(
      b.firstOf(
        NUMERIC_LITERAL(),
        STRING_LITERAL(),
        f.nowdocLiteral(b.token(PHPLexicalGrammar.NOWDOC)),
        HEREDOC_STRING_LITERAL(),
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
    return b.<LiteralTree>nonterminal(Kind.NUMERIC_LITERAL).is(
      f.numericLiteral(b.token(PHPLexicalGrammar.NUMERIC_LITERAL)));
  }

  public ExpressionTree STRING_LITERAL() {
    return b.<ExpressionTree>nonterminal().is(
      f.stringLiteral(
        b.firstOf(
          f.regularStringLiteral(b.token(PHPLexicalGrammar.REGULAR_STRING_LITERAL)),
          EXPANDABLE_STRING_LITERAL()),
        b.optional(
          b.firstOf(
            DIMENSIONAL_OFFSET(),
            ALTERNATIVE_DIMENSIONAL_OFFSET()))));
  }

  public ExpandableStringLiteralTree EXPANDABLE_STRING_LITERAL() {
    return b.<ExpandableStringLiteralTree>nonterminal(Kind.EXPANDABLE_STRING_LITERAL).is(
      f.expandableStringLiteral(
        b.token(PHPLexicalGrammar.SPACING),
        b.token(PHPLexicalGrammar.DOUBLE_QUOTE),
        b.oneOrMore(
          b.firstOf(
            EXPANDABLE_STRING_CHARACTERS(),
            ENCAPSULATED_STRING_VARIABLE())),
        b.token(PHPLexicalGrammar.DOUBLE_QUOTE)));
  }

  public HeredocStringLiteralTree HEREDOC_STRING_LITERAL() {
    return b.<HeredocStringLiteralTree>nonterminal(Kind.HEREDOC_LITERAL).is(
      f.heredocStringLiteral(b.token(PHPLexicalGrammar.HEREDOC)));
  }

  public HeredocStringLiteralTreeImpl.HeredocBody HEREDOC_BODY() {
    return b.<HeredocStringLiteralTreeImpl.HeredocBody>nonterminal(PHPLexicalGrammar.HEREDOC_BODY).is(
      f.heredocBodyTree(b.oneOrMore(b.firstOf(
        HEREDOC_STRING_CHARACTERS(),
        ENCAPSULATED_STRING_VARIABLE()))));
  }

  public ExpressionTree ENCAPSULATED_STRING_VARIABLE() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.ENCAPS_VAR).is(
      b.firstOf(
        ENCAPSULATED_SEMI_COMPLEX_VARIABLE(),
        ENCAPSULATED_SIMPLE_VARIABLE(),
        ENCAPSULATED_COMPLEX_VARIABLE()));
  }

  public ExpressionTree ENCAPSULATED_COMPLEX_VARIABLE() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.COMPLEX_ENCAPS_VARIABLE).is(
      f.encapsulatedComplexVariable(
        b.token(LCURLYBRACE),
        b.token(PHPLexicalGrammar.NEXT_IS_DOLLAR),
        EXPRESSION(),
        b.token(RCURLYBRACE)));
  }

  public ExpressionTree ENCAPSULATED_SEMI_COMPLEX_VARIABLE() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.SEMI_COMPLEX_ENCAPS_VARIABLE).is(
      f.encapsulatedSemiComplexVariable(
        b.token(DOLLAR_LCURLY),
        b.firstOf(
          EXPRESSION(),
          f.expressionRecovery(b.token(PHPLexicalGrammar.SEMI_COMPLEX_RECOVERY_EXPRESSION))),
        b.token(RCURLYBRACE)));
  }

  public ExpressionTree ENCAPSULATED_SIMPLE_VARIABLE() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.SIMPLE_ENCAPS_VARIABLE).is(
      f.encapsulatedSimpleVar(ENCAPSULATED_VARIABLE_IDENTIFIER(),
        b.optional(b.firstOf(
          f.expandableArrayAccess(
            b.token(LBRACKET),
            b.firstOf(NAME_IDENTIFIER(), NUMERIC_LITERAL(), ENCAPSULATED_VARIABLE_IDENTIFIER()),
            b.token(RBRACKET)),
          f.expandableObjectMemberAccess(
            b.token(ARROW),
            NAME_IDENTIFIER())))));
  }

  public NameIdentifierTree NAME_IDENTIFIER() {
    return b.<NameIdentifierTree>nonterminal(Kind.NAME_IDENTIFIER).is(
      f.identifier(b.token(PHPLexicalGrammar.IDENTIFIER)));
  }

  public NameIdentifierTree NAME_IDENTIFIER_OR_KEYWORD() {
    return b.<NameIdentifierTree>nonterminal(PHPGrammarRuleKey.NAME_IDENTIFIER_OR_KEYWORD).is(
      f.identifierOrKeyword(b.token(PHPLexicalGrammar.IDENTIFIER_OR_KEYWORD)));
  }

  public VariableIdentifierTree ENCAPSULATED_VARIABLE_IDENTIFIER() {
    return b.<VariableIdentifierTree>nonterminal(PHPLexicalGrammar.ENCAPS_VAR_IDENTIFIER).is(
      // variable identifiers encapsulated into strings literal does not allowed line terminator spacing,
      // so here using "WHITESPACE" instead of SPACING.
      f.encapsulatedVariableIdentifier(b.token(PHPLexicalGrammar.WHITESPACES), b.token(PHPLexicalGrammar.VARIABLE_IDENTIFIER)));
  }

  public ExpressionTree EXPANDABLE_STRING_CHARACTERS() {
    return b.<ExpandableStringCharactersTree>nonterminal(Kind.EXPANDABLE_STRING_CHARACTERS).is(
      f.expandableStringCharacters(b.token(PHPLexicalGrammar.STRING_WITH_ENCAPS_VAR_CHARACTERS)));
  }

  public ExpressionTree HEREDOC_STRING_CHARACTERS() {
    return b.<ExpandableStringCharactersTree>nonterminal(Kind.HEREDOC_STRING_CHARACTERS).is(
      f.heredocStringCharacters(b.token(PHPLexicalGrammar.HEREDOC_STRING_CHARACTERS)));
  }

  public YieldExpressionTree YIELD_EXPRESSION() {
    return b.<YieldExpressionTree>nonterminal(Kind.YIELD_EXPRESSION).is(
      b.firstOf(
        f.yieldExpressionWithKey(b.token(YIELD), EXPRESSION(), b.token(DOUBLEARROW), EXPRESSION()),
        f.yieldFromExpression(b.token(YIELD), b.token(PHPLexicalGrammar.FROM), EXPRESSION()),
        f.yieldExpression(b.token(YIELD), b.optional(EXPRESSION()))));
  }

  public ParenthesisedExpressionTree PARENTHESIZED_EXPRESSION() {
    return b.<ParenthesisedExpressionTree>nonterminal(Kind.PARENTHESISED_EXPRESSION).is(
      f.parenthesizedExpression(
        b.token(LPARENTHESIS),
        b.firstOf(
          YIELD_EXPRESSION(),
          EXPRESSION()),
        b.token(RPARENTHESIS)));
  }

  public AssignmentExpressionTree LIST_EXPRESSION_ASSIGNMENT() {
    return b.<AssignmentExpressionTree>nonterminal(PHPLexicalGrammar.LIST_EXPRESSION_ASSIGNMENT).is(
      f.listExpressionAssignment(LIST_EXPRESSION(), b.token(EQU), EXPRESSION()));
  }

  public ListExpressionTree LIST_EXPRESSION() {
    return b.<ListExpressionTree>nonterminal(Kind.LIST_EXPRESSION)
      .is(f.listExpression(
          b.token(LIST),
          b.token(LPARENTHESIS),
          b.optional(ARRAY_ASSIGNMENT_PATTERN_ELEMENT()),
          b.zeroOrMore(f.newTuple(b.token(COMMA), b.optional(ARRAY_ASSIGNMENT_PATTERN_ELEMENT()))),
          b.token(RPARENTHESIS))
      );
  }

  public AssignmentExpressionTree ARRAY_DESTRUCTURING_ASSIGNMENT() {
    return b.<AssignmentExpressionTree>nonterminal(PHPLexicalGrammar.ARRAY_DESTRUCTURING_ASSIGNMENT).is(
      f.arrayDestructuringAssignment(ARRAY_ASSIGNMENT_PATTERN(), b.token(EQU), EXPRESSION()));
  }

  public ArrayAssignmentPatternTree ARRAY_ASSIGNMENT_PATTERN() {
    return b.<ArrayAssignmentPatternTree>nonterminal(Kind.ARRAY_ASSIGNMENT_PATTERN)
      .is(
        b.firstOf(
          f.arrayAssignmentPattern(
            b.token(LBRACKET),
            b.oneOrMore(f.newTuple(b.token(COMMA), b.optional(ARRAY_ASSIGNMENT_PATTERN_ELEMENT()))),
            b.token(RBRACKET)),
          f.arrayAssignmentPattern(
            b.token(LBRACKET),
            ARRAY_ASSIGNMENT_PATTERN_ELEMENT(),
            b.zeroOrMore(f.newTuple(b.token(COMMA), b.optional(ARRAY_ASSIGNMENT_PATTERN_ELEMENT()))),
            b.token(RBRACKET))));
  }

  public ArrayAssignmentPatternElementTree ARRAY_ASSIGNMENT_PATTERN_ELEMENT() {
    return b.<ArrayAssignmentPatternElementTree>nonterminal(PHPLexicalGrammar.ARRAY_ASSIGNMENT_PATTERN_ELEMENT)
      .is(
        f.arrayAssignmentPatternElement(
          b.optional(
            f.newTuple(
              EXPRESSION(),
              b.token(DOUBLEARROW))),
          b.firstOf(
            MEMBER_EXPRESSION(),
            LIST_EXPRESSION(),
            ARRAY_ASSIGNMENT_PATTERN())));
  }

  public ComputedVariableTree COMPUTED_VARIABLE_NAME() {
    return b.<ComputedVariableTree>nonterminal(Kind.COMPUTED_VARIABLE_NAME).is(
      f.computedVariableName(
        b.token(LCURLYBRACE),
        EXPRESSION(),
        b.token(RCURLYBRACE)));
  }

  public VariableIdentifierTree VARIABLE_IDENTIFIER() {
    return b.<VariableIdentifierTree>nonterminal(Kind.VARIABLE_IDENTIFIER).is(
      f.variableIdentifier(b.token(PHPLexicalGrammar.REGULAR_VAR_IDENTIFIER)));
  }

  public VariableTree COMPOUND_VARIABLE() {
    return b.<VariableTree>nonterminal(Kind.COMPOUND_VARIABLE_NAME).is(
      b.firstOf(
        VARIABLE_IDENTIFIER(),
        f.compoundVariable(b.token(DOLLAR_LCURLY), EXPRESSION(), b.token(RCURLYBRACE))));
  }

  public ExpressionTree VARIABLE_WITHOUT_OBJECTS() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.VARIABLE_WITHOUT_OBJECTS).is(
      f.variableWithoutObjects(
        b.zeroOrMore(b.token(PHPLexicalGrammar.VARIABLE_VARIABLE_DOLLAR)),
        COMPOUND_VARIABLE(),
        b.zeroOrMore(b.firstOf(
          DIMENSIONAL_OFFSET(),
          ALTERNATIVE_DIMENSIONAL_OFFSET()))));
  }

  public ArrayAccessTree ALTERNATIVE_DIMENSIONAL_OFFSET() {
    return b.<ArrayAccessTree>nonterminal().is(
      f.alternativeDimensionalOffset(
        b.token(LCURLYBRACE),
        b.optional(EXPRESSION()),
        b.token(RCURLYBRACE)));
  }

  public ArrayAccessTree DIMENSIONAL_OFFSET() {
    return b.<ArrayAccessTree>nonterminal(PHPLexicalGrammar.DIMENSIONAL_OFFSET).is(
      f.dimensionalOffset(
        b.token(LBRACKET),
        b.optional(EXPRESSION()),
        b.token(RBRACKET)));
  }

  public ExpressionTree PRIMARY_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.PRIMARY_EXPRESSION).is(
      b.firstOf(
        f.newStaticIdentifier(b.token(STATIC)),
        NAMESPACE_NAME(),
        VARIABLE_WITHOUT_OBJECTS(),
        PARENTHESIZED_EXPRESSION()));
  }

  public AssignmentExpressionTree ASSIGNMENT_BY_REFERENCE() {
    return b.<AssignmentExpressionTree>nonterminal(PHPLexicalGrammar.ASSIGNMENT_BY_REFERENCE).is(
      f.assignmentByReference(
        MEMBER_EXPRESSION(),
        b.token(PHPPunctuator.EQU), b.token(AMPERSAND),
        b.firstOf(NEW_EXPRESSION(), MEMBER_EXPRESSION())));
  }

  public AssignmentExpressionTree ASSIGNMENT_EXPRESSION() {
    return b.<AssignmentExpressionTree>nonterminal(PHPLexicalGrammar.ASSIGNMENT_EXPRESSION).is(b.firstOf(
      f.assignmentExpression(MEMBER_EXPRESSION(), ASSIGNMENT_OPERATOR(), CONDITIONAL_EXPR()),
      ASSIGNMENT_BY_REFERENCE(),
      ARRAY_DESTRUCTURING_ASSIGNMENT()
    ));
  }

  public InternalSyntaxToken ASSIGNMENT_OPERATOR() {
    return b.<InternalSyntaxToken>nonterminal(PHPLexicalGrammar.ASSIGNMENT_OPERATOR).is(
      b.firstOf(
        b.token(PHPPunctuator.EQU),

        b.token(PHPPunctuator.STAR_EQU),
        b.token(PHPPunctuator.STAR_STAR_EQU),
        b.token(PHPPunctuator.DIVEQUAL),
        b.token(PHPPunctuator.MOD_EQU),
        b.token(PHPPunctuator.PLUS_EQU),
        b.token(PHPPunctuator.MINUS_EQU),
        b.token(PHPPunctuator.SL_EQU),
        b.token(PHPPunctuator.SR_EQU),
        b.token(PHPPunctuator.CONCATEQUAL),

        b.token(PHPPunctuator.ANDEQUAL),
        b.token(PHPPunctuator.XOR_EQU),
        b.token(PHPPunctuator.OR_EQU)));
  }

  public ReferenceVariableTree REFERENCE_VARIABLE() {
    return b.<ReferenceVariableTree>nonterminal(Kind.REFERENCE_VARIABLE).is(
      f.referenceVariable(
        b.token(AMPERSAND),
        MEMBER_EXPRESSION()));
  }

  public SpreadArgumentTree SPREAD_ARGUMENT() {
    return b.<SpreadArgumentTree>nonterminal(Kind.SPREAD_ARGUMENT).is(
      f.spreadArgument(
        b.token(ELIPSIS),
        EXPRESSION()));
  }

  public FunctionCallTree FUNCTION_CALL_ARGUMENT_LIST() {
    return b.<FunctionCallTree>nonterminal(PHPLexicalGrammar.FUNCTION_CALL_PARAMETER_LIST).is(
      f.functionCallParameterList(
        b.token(LPARENTHESIS),
        ARGUMENTS(),
        b.token(RPARENTHESIS)));
  }

  public SeparatedListImpl<ExpressionTree> ARGUMENTS() {
    return b.<SeparatedListImpl<ExpressionTree>>nonterminal().is(
      f.arguments(b.optional(f.newTuple(
        FUNCTION_CALL_ARGUMENT(), b.zeroOrMore(f.newTuple(b.token(COMMA), FUNCTION_CALL_ARGUMENT())))))
    );
  }

  public ExpressionTree FUNCTION_CALL_ARGUMENT() {
    return b.<ExpressionTree>nonterminal().is(
      b.firstOf(
        REFERENCE_VARIABLE(),
        SPREAD_ARGUMENT(),
        EXPRESSION(),
        YIELD_EXPRESSION()));
  }

  public ExpressionTree MEMBER_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.MEMBER_EXPRESSION).is(
      f.memberExpression(
        PRIMARY_EXPRESSION(),
        b.zeroOrMore(
          b.firstOf(
            OBJECT_MEMBER_ACCESS(),
            CLASS_MEMBER_ACCESS(),
            DIMENSIONAL_OFFSET(),
            FUNCTION_CALL_ARGUMENT_LIST()))));
  }

  public MemberAccessTree OBJECT_MEMBER_ACCESS() {
    return b.<MemberAccessTree>nonterminal(PHPLexicalGrammar.OBJECT_MEMBER_ACCESS).is(
      f.objectMemberAccess(
        b.token(ARROW),
        b.firstOf(
          VARIABLE_WITHOUT_OBJECTS(),
          OBJECT_DIMENSIONAL_LIST(),
          NAME_IDENTIFIER_OR_KEYWORD())));
  }

  public ExpressionTree OBJECT_DIMENSIONAL_LIST() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.OBJECT_DIM_LIST).is(
      f.objectDimensionalList(
        b.firstOf(
          NAME_IDENTIFIER(),
          f.variableName(b.token(PHPLexicalGrammar.KEYWORDS)),
          COMPUTED_VARIABLE_NAME()),
        b.zeroOrMore(
          b.firstOf(
            ALTERNATIVE_DIMENSIONAL_OFFSET(),
            DIMENSIONAL_OFFSET()))));
  }

  public MemberAccessTree CLASS_MEMBER_ACCESS() {
    return b.<MemberAccessTree>nonterminal(PHPLexicalGrammar.CLASS_MEMBER_ACCESS).is(
      f.classMemberAccess(
        b.token(DOUBLECOLON),
        b.firstOf(
          VARIABLE_WITHOUT_OBJECTS(),
          NAME_IDENTIFIER_OR_KEYWORD(),
          b.token(CLASS),
          COMPUTED_VARIABLE_NAME())));
  }

  public LexicalVariablesTree LEXICAL_VARIABLES() {
    return b.<LexicalVariablesTree>nonterminal(Kind.LEXICAL_VARIABLES).is(
      f.lexicalVariables(
        b.token(USE),
        b.token(LPARENTHESIS),
        LEXICAL_VARIABLE(),
        b.zeroOrMore(f.newTuple(b.token(COMMA), LEXICAL_VARIABLE())),
        b.token(RPARENTHESIS)));
  }

  public VariableTree LEXICAL_VARIABLE() {
    return b.<VariableTree>nonterminal(PHPLexicalGrammar.LEXICAL_VARIABLE).is(
      f.lexicalVariable(
        b.optional(b.token(AMPERSAND)),
        VARIABLE_IDENTIFIER()));
  }

  public ExpressionTree STATIC_SCALAR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.STATIC_SCALAR).is(
      b.firstOf(
        ARRAY_INITIALIZER(),
        EXPRESSION()));
  }

  public FunctionCallTree INTERNAL_FUNCTION() {
    return b.<FunctionCallTree>nonterminal(PHPLexicalGrammar.INTERNAL_FUNCTION).is(
      b.firstOf(
        f.internalFunction(b.token(PHPLexicalGrammar.ISSET), b.token(LPARENTHESIS), EXPRESSION(), b.zeroOrMore(f.newTuple(b.token(COMMA), EXPRESSION())), b.token(RPARENTHESIS)),

        f.internalFunction(
          b.firstOf(
            b.token(PHPLexicalGrammar.EMPTY),
            b.token(PHPLexicalGrammar.EVAL)),
          b.token(LPARENTHESIS), EXPRESSION(), b.token(RPARENTHESIS)),

        f.internalFunction(
          b.firstOf(
            b.token(PHPLexicalGrammar.INCLUDE_ONCE),
            b.token(PHPLexicalGrammar.INCLUDE),
            b.token(PHPLexicalGrammar.REQUIRE_ONCE),
            b.token(PHPLexicalGrammar.REQUIRE),
            b.token(PHPLexicalGrammar.CLONE),
            b.token(PHPLexicalGrammar.PRINT)),
          EXPRESSION())));
  }

  public ArrayInitializerTree ARRAY_INITIALIZER() {
    return b.<ArrayInitializerTree>nonterminal(PHPLexicalGrammar.ARRAY_INIALIZER).is(
      b.firstOf(
        f.newArrayInitFunction(b.token(ARRAY), b.token(LPARENTHESIS), b.optional(ARRAY_PAIR_LIST()), b.token(RPARENTHESIS)),
        f.newArrayInitBracket(b.token(LBRACKET), b.optional(ARRAY_PAIR_LIST()), b.token(RBRACKET))));
  }

  public SeparatedListImpl<ArrayPairTree> ARRAY_PAIR_LIST() {
    return b.<SeparatedListImpl<ArrayPairTree>>nonterminal(PHPLexicalGrammar.ARRAY_PAIR_LIST).is(
      f.arrayInitializerList(
        ARRAY_PAIR(),
        b.zeroOrMore(f.newTuple(b.token(COMMA), ARRAY_PAIR())),
        b.optional(b.token(COMMA))));
  }

  public ArrayPairTree ARRAY_PAIR() {
    return b.<ArrayPairTree>nonterminal(Kind.ARRAY_PAIR).is(
      b.firstOf(
        f.arrayPair1(EXPRESSION(), b.optional(f.newTuple(b.token(DOUBLEARROW), b.firstOf(REFERENCE_VARIABLE(), EXPRESSION())))),
        f.arrayPair2(REFERENCE_VARIABLE())));
  }

  public FunctionExpressionTree FUNCTION_EXPRESSION() {
    return b.<FunctionExpressionTree>nonterminal(Kind.FUNCTION_EXPRESSION).is(
      f.functionExpression(
        b.optional(b.token(STATIC)),
        b.token(FUNCTION),
        b.optional(b.token(AMPERSAND)),
        PARAMETER_LIST(),
        b.optional(LEXICAL_VARIABLES()),
        b.optional(RETURN_TYPE_CLAUSE()),
        BLOCK()));
  }

  public NewExpressionTree NEW_EXPRESSION() {
    return b.<NewExpressionTree>nonterminal(Kind.NEW_EXPRESSION).is(
      f.newExpression(b.token(NEW), b.firstOf(MEMBER_EXPRESSION(), ANONYMOUS_CLASS())));
  }

  public AnonymousClassTree ANONYMOUS_CLASS() {
    return b.<AnonymousClassTree>nonterminal(Kind.ANONYMOUS_CLASS).is(
      f.anonymousClass(
        b.token(CLASS),
        b.optional(b.token(PHPPunctuator.LPARENTHESIS)),
        ARGUMENTS(),
        b.optional(b.token(PHPPunctuator.RPARENTHESIS)),
        b.optional(f.newTuple(b.token(EXTENDS), NAMESPACE_NAME())),
        b.optional(f.newTuple(b.token(IMPLEMENTS), INTERFACE_LIST())),
        b.token(LCURLYBRACE),
        b.zeroOrMore(CLASS_MEMBER()),
        b.token(RCURLYBRACE))
    );
  }

  public FunctionCallTree EXIT_EXPRESSION() {
    return b.<FunctionCallTree>nonterminal(PHPLexicalGrammar.EXIT_EXPRESSION).is(
      f.completeExitExpression(
        b.firstOf(
          b.token(EXIT),
          b.token(DIE)),
        b.optional(f.newExitExpression(
          b.token(LPARENTHESIS),
          b.optional(EXPRESSION()),
          b.token(RPARENTHESIS)))));
  }

  public ExpressionTree POSTFIX_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.POSTFIX_EXPR).is(
      f.postfixExpression(
        b.firstOf(
          f.combinedScalarOffset(ARRAY_INITIALIZER(), b.zeroOrMore(DIMENSIONAL_OFFSET())),
          FUNCTION_EXPRESSION(),
          COMMON_SCALAR(),
          MEMBER_EXPRESSION(),
          NEW_EXPRESSION(),
          EXIT_EXPRESSION(),
          LIST_EXPRESSION_ASSIGNMENT(),
          INTERNAL_FUNCTION()),
        b.optional(b.firstOf(
          b.token(INC),
          b.token(DEC),
          f.newTuple(b.token(INSTANCEOF), MEMBER_EXPRESSION())))));
  }

  /**
   * [ END ] Expression
   */

}
