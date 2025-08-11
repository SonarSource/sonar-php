/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ConstantDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.DeclaredTypeTree;
import org.sonar.plugins.php.api.tree.declaration.DnfIntersectionTypeTree;
import org.sonar.plugins.php.api.tree.declaration.DnfTypeTree;
import org.sonar.plugins.php.api.tree.declaration.EnumDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.IntersectionTypeTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterListTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.declaration.PropertyHookListTree;
import org.sonar.plugins.php.api.tree.declaration.PropertyHookTree;
import org.sonar.plugins.php.api.tree.declaration.ReturnTypeClauseTree;
import org.sonar.plugins.php.api.tree.declaration.TypeNameTree;
import org.sonar.plugins.php.api.tree.declaration.TypeTree;
import org.sonar.plugins.php.api.tree.declaration.UnionTypeTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.ArrowFunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.CallableConvertTree;
import org.sonar.plugins.php.api.tree.expression.ComputedVariableTree;
import org.sonar.plugins.php.api.tree.expression.ExecutionOperatorTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.HeredocStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.expression.ListExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MatchClauseTree;
import org.sonar.plugins.php.api.tree.expression.MatchExpressionTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ReferenceVariableTree;
import org.sonar.plugins.php.api.tree.expression.SpreadArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ThrowExpressionTree;
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
import org.sonar.plugins.php.api.tree.statement.EnumCaseTree;
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

import static org.sonar.php.api.PHPKeyword.ABSTRACT;
import static org.sonar.php.api.PHPKeyword.ARRAY;
import static org.sonar.php.api.PHPKeyword.CALLABLE;
import static org.sonar.php.api.PHPKeyword.CASE;
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
import static org.sonar.php.api.PHPKeyword.READONLY;
import static org.sonar.php.api.PHPKeyword.STATIC;
import static org.sonar.php.api.PHPKeyword.TRAIT;
import static org.sonar.php.api.PHPKeyword.USE;
import static org.sonar.php.api.PHPKeyword.YIELD;
import static org.sonar.php.api.PHPPunctuator.AMPERSAND;
import static org.sonar.php.api.PHPPunctuator.ARROW;
import static org.sonar.php.api.PHPPunctuator.ATTRIBUTE_OPEN;
import static org.sonar.php.api.PHPPunctuator.COLON;
import static org.sonar.php.api.PHPPunctuator.COMMA;
import static org.sonar.php.api.PHPPunctuator.DEC;
import static org.sonar.php.api.PHPPunctuator.DIV;
import static org.sonar.php.api.PHPPunctuator.DOLLAR_LCURLY;
import static org.sonar.php.api.PHPPunctuator.DOT;
import static org.sonar.php.api.PHPPunctuator.DOUBLEARROW;
import static org.sonar.php.api.PHPPunctuator.DOUBLECOLON;
import static org.sonar.php.api.PHPPunctuator.ELLIPSIS;
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
import static org.sonar.php.api.PHPPunctuator.NULL_SAFE_ARROW;
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

// Ignore uppercase method names warning
@SuppressWarnings("java:S100")
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
        b.optional(f.newTuple(b.token(EQU), EXPRESSION()))));
  }

  public VariableDeclarationTree CONST_VAR() {
    return b.<VariableDeclarationTree>nonterminal(PHPLexicalGrammar.CONSTANT_VAR).is(
      f.constDeclaration(
        b.token(PHPLexicalGrammar.IDENTIFIER),
        b.token(EQU),
        EXPRESSION()));
  }

  public VariableDeclarationTree VARIABLE_DECLARATION() {
    return b.<VariableDeclarationTree>nonterminal(PHPLexicalGrammar.VARIABLE_DECLARATION).is(
      f.variableDeclaration(
        b.token(PHPLexicalGrammar.REGULAR_VAR_IDENTIFIER),
        b.optional(f.newTuple(b.token(EQU), EXPRESSION()))));
  }

  public NamespaceNameTree NAMESPACE_NAME() {
    return b.<NamespaceNameTree>nonterminal(PHPLexicalGrammar.NAMESPACE_NAME).is(
      b.firstOf(
        NAMESPACE_NAME_WITHOUT_SINGLE_KEYWORD(),
        f.namespaceName(
          b.token(PHPLexicalGrammar.IDENTIFIER_OR_KEYWORD))));
  }

  public NamespaceNameTree NAMESPACE_NAME_WITHOUT_SINGLE_KEYWORD() {
    return b.<NamespaceNameTree>nonterminal(PHPLexicalGrammar.NAMESPACE_NAME_WITHOUT_SINGLE_KEYWORD).is(
      b.firstOf(
        f.namespaceName(
          f.newTuple(
            b.token(PHPPunctuator.NS_SEPARATOR),
            b.token(PHPLexicalGrammar.IDENTIFIER_OR_KEYWORD)),
          b.zeroOrMore(f.newTuple(
            b.token(PHPLexicalGrammar.NS_SEPARATOR_WITHOUT_SPACE),
            b.token(PHPLexicalGrammar.IDENTIFIER_OR_KEYWORD)))),
        f.namespaceName(
          b.token(PHPLexicalGrammar.IDENTIFIER_OR_KEYWORD),
          b.oneOrMore(f.newTuple(
            b.token(PHPLexicalGrammar.NS_SEPARATOR_WITHOUT_SPACE),
            b.token(PHPLexicalGrammar.IDENTIFIER_OR_KEYWORD)))),
        f.namespaceName(
          b.token(PHPLexicalGrammar.IDENTIFIER))));
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
        b.token(PHPKeyword.FUNCTION)));
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
        b.zeroOrMore(ATTRIBUTE_GROUP()),
        b.zeroOrMore(b.firstOf(b.token(READONLY), b.token(ABSTRACT), b.token(FINAL))),
        b.token(CLASS),
        NAME_IDENTIFIER(),
        b.optional(f.newTuple(b.token(EXTENDS), f.classNamespaceName(NAMESPACE_NAME()))),
        b.optional(f.newTuple(b.token(IMPLEMENTS), INTERFACE_LIST())),
        b.token(LCURLYBRACE),
        b.zeroOrMore(CLASS_MEMBER()),
        b.token(RCURLYBRACE)));
  }

  public ClassDeclarationTree TRAIT_DECLARATION() {
    return b.<ClassDeclarationTree>nonterminal(PHPLexicalGrammar.TRAIT_DECLARATION).is(
      f.traitDeclaration(
        b.zeroOrMore(ATTRIBUTE_GROUP()),
        b.token(TRAIT),
        NAME_IDENTIFIER(),
        b.token(LCURLYBRACE),
        b.zeroOrMore(CLASS_MEMBER()),
        b.token(RCURLYBRACE)));
  }

  public ClassDeclarationTree INTERFACE_DECLARATION() {
    return b.<ClassDeclarationTree>nonterminal(PHPLexicalGrammar.INTERFACE_DECLARATION).is(
      f.interfaceDeclaration(
        b.zeroOrMore(ATTRIBUTE_GROUP()),
        b.token(INTERFACE),
        NAME_IDENTIFIER(),
        b.optional(f.newTuple(b.token(EXTENDS), INTERFACE_LIST())),
        b.token(LCURLYBRACE),
        b.zeroOrMore(CLASS_MEMBER()),
        b.token(RCURLYBRACE)));
  }

  public EnumDeclarationTree ENUM_DECLARATION() {
    return b.<EnumDeclarationTree>nonterminal(PHPLexicalGrammar.ENUM_DECLARATION).is(
      f.enumDeclaration(
        b.zeroOrMore(ATTRIBUTE_GROUP()),
        b.token(PHPLexicalGrammar.ENUM),
        NAME_IDENTIFIER(),
        b.optional(f.newTuple(b.token(COLON), TYPE())),
        b.optional(f.newTuple(b.token(IMPLEMENTS), INTERFACE_LIST())),
        b.token(LCURLYBRACE),
        b.zeroOrMore(ENUM_MEMBER()),
        b.token(RCURLYBRACE)));
  }

  /**
   * In contrast to class declarations, enums cannot contain properties. They do allow enum cases as an addition.
   */
  public ClassMemberTree ENUM_MEMBER() {
    return b.<ClassMemberTree>nonterminal(PHPLexicalGrammar.ENUM_MEMBER).is(
      b.firstOf(
        METHOD_DECLARATION(),
        CLASS_CONSTANT_DECLARATION(),
        USE_TRAIT_DECLARATION(),
        ENUM_CASE()));
  }

  public EnumCaseTree ENUM_CASE() {
    return b.<EnumCaseTree>nonterminal(PHPLexicalGrammar.ENUM_CASE).is(
      f.enumCase(
        b.zeroOrMore(ATTRIBUTE_GROUP()),
        b.token(CASE),
        NAME_IDENTIFIER_OR_KEYWORD(),
        b.optional(f.newTuple(b.token(EQU), EXPRESSION())),
        EOS()));
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
      b.firstOf(
        f.classConstantDeclaration(
          b.zeroOrMore(ATTRIBUTE_GROUP()),
          b.zeroOrMore(CLASS_CONST_MODIFIER()),
          b.token(PHPKeyword.CONST),
          MEMBER_CONST_DECLARATION(),
          b.zeroOrMore(f.newTuple(b.token(COMMA), MEMBER_CONST_DECLARATION())),
          EOS()),
        f.classConstantDeclarationWithTypeHint(
          b.zeroOrMore(ATTRIBUTE_GROUP()),
          b.zeroOrMore(CLASS_CONST_MODIFIER()),
          b.token(PHPKeyword.CONST),
          DECLARED_TYPE(),
          MEMBER_CONST_DECLARATION(),
          b.zeroOrMore(f.newTuple(b.token(COMMA), MEMBER_CONST_DECLARATION())),
          EOS())));
  }

  public SyntaxToken CLASS_CONST_MODIFIER() {
    return b.<SyntaxToken>nonterminal(PHPLexicalGrammar.CLASS_CONST_MODIFIER).is(
      b.firstOf(
        VISIBILITY_MODIFIER(),
        b.token(PHPKeyword.FINAL)));
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
      b.firstOf(
        f.classVariableDeclaration(
          b.zeroOrMore(ATTRIBUTE_GROUP()),
          b.firstOf(
            f.singleToken(b.token(PHPKeyword.VAR)),
            b.oneOrMore(MEMBER_MODIFIER())),
          b.optional(DECLARED_TYPE()),
          VARIABLE_DECLARATION(),
          PROPERTY_HOOK_LIST()),
        f.classVariableDeclaration(
          b.zeroOrMore(ATTRIBUTE_GROUP()),
          b.firstOf(
            f.singleToken(b.token(PHPKeyword.VAR)),
            b.oneOrMore(MEMBER_MODIFIER())),
          b.optional(DECLARED_TYPE()),
          VARIABLE_DECLARATION(),
          b.zeroOrMore(f.newTuple(b.token(COMMA), VARIABLE_DECLARATION())),
          EOS())));
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
        b.token(PHPLexicalGrammar.ASYMMETRIC_VISIBILITY_MODIFIER),
        VISIBILITY_MODIFIER(),
        b.token(PHPKeyword.READONLY),
        b.token(PHPKeyword.STATIC),
        b.token(PHPKeyword.ABSTRACT),
        b.token(PHPKeyword.FINAL)));
  }

  public MethodDeclarationTree METHOD_DECLARATION() {
    return b.<MethodDeclarationTree>nonterminal(PHPLexicalGrammar.METHOD_DECLARATION).is(
      f.methodDeclaration(
        b.zeroOrMore(ATTRIBUTE_GROUP()),
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
        b.zeroOrMore(ATTRIBUTE_GROUP()),
        b.token(PHPKeyword.FUNCTION),
        b.optional(b.token(PHPPunctuator.AMPERSAND)),
        NAME_IDENTIFIER(),
        PARAMETER_LIST(),
        b.optional(RETURN_TYPE_CLAUSE()),
        BLOCK()));
  }

  public ReturnTypeClauseTree RETURN_TYPE_CLAUSE() {
    return b.<ReturnTypeClauseTree>nonterminal(PHPLexicalGrammar.RETURN_TYPE_CLAUSE).is(
      f.returnTypeClause(b.token(COLON), DECLARED_TYPE()));
  }

  public ParameterListTree PARAMETER_LIST() {
    return b.<ParameterListTree>nonterminal(PHPLexicalGrammar.PARAMETER_LIST).is(
      f.parameterList(
        b.token(PHPPunctuator.LPARENTHESIS),
        b.optional(
          f.newTuple(
            f.newTuple(
              PARAMETER(),
              b.zeroOrMore(
                f.newTuple(
                  b.token(PHPPunctuator.COMMA),
                  PARAMETER()))),
            b.optional(b.token(COMMA)))),
        b.token(PHPPunctuator.RPARENTHESIS)));
  }

  public ParameterTree PARAMETER() {
    return b.<ParameterTree>nonterminal(PHPLexicalGrammar.PARAMETER).is(
      f.parameter(
        b.zeroOrMore(ATTRIBUTE_GROUP()),
        b.zeroOrMore(
          b.firstOf(b.token(PHPLexicalGrammar.ASYMMETRIC_VISIBILITY_MODIFIER), VISIBILITY_MODIFIER(), b.token(PHPKeyword.READONLY))),
        b.optional(DECLARED_TYPE()),
        b.optional(b.token(PHPPunctuator.AMPERSAND)),
        b.optional(b.token(PHPPunctuator.ELLIPSIS)),
        b.token(PHPLexicalGrammar.REGULAR_VAR_IDENTIFIER),
        b.optional(
          f.newTuple(
            b.token(PHPPunctuator.EQU),
            EXPRESSION())),
        b.optional(PROPERTY_HOOK_LIST())));
  }

  public SeparatedListImpl<NamespaceNameTree> INTERFACE_LIST() {
    return b.<SeparatedListImpl<NamespaceNameTree>>nonterminal(PHPLexicalGrammar.INTERFACE_LIST).is(
      f.interfaceList(
        f.classNamespaceName(NAMESPACE_NAME()),
        b.zeroOrMore(f.newTuple(b.token(COMMA), f.classNamespaceName(NAMESPACE_NAME())))));
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
          b.token(STATIC),
          b.token(PHPLexicalGrammar.MIXED),
          b.token(PHPLexicalGrammar.SELF),
          b.token(PHPLexicalGrammar.PARENT),
          b.token(PHPLexicalGrammar.ITERABLE),
          b.token(PHPLexicalGrammar.OBJECT),
          b.token(PHPLexicalGrammar.BOOL),
          b.token(PHPLexicalGrammar.FLOAT),
          b.token(PHPLexicalGrammar.INT),
          b.token(PHPLexicalGrammar.STRING))),
        f.classNamespaceName(NAMESPACE_NAME_WITHOUT_SINGLE_KEYWORD())));
  }

  public UnionTypeTree UNION_TYPE() {
    return b.<UnionTypeTree>nonterminal(PHPLexicalGrammar.UNION_TYPE).is(
      f.unionType(
        TYPE(),
        b.oneOrMore(f.newTuple(b.token(PHPPunctuator.OR), TYPE()))));
  }

  public IntersectionTypeTree INTERSECTION_TYPE() {
    return b.<IntersectionTypeTree>nonterminal(PHPLexicalGrammar.INTERSECTION_TYPE).is(
      f.intersectionType(
        TYPE(),
        b.oneOrMore(f.newTuple(b.token(PHPPunctuator.AMPERSAND), TYPE()))));
  }

  /**
   * We don't want to match UNION_TYPE as DNF_TYPE, so we need this more complex grammar to ensure we have a mix of union and intersection:
   * DNF_TYPE -> (TYPE "|")* DNF_INTERSECTION_TYPE ("|" (TYPE | DNF_INTERSECTION_TYPE))*
   */
  public DnfTypeTree DNF_TYPE() {
    return b.<DnfTypeTree>nonterminal(PHPLexicalGrammar.DNF_TYPE).is(
      b.firstOf(
        f.dnfType(
          b.zeroOrMore(f.newTuple(TYPE(), b.token(PHPPunctuator.OR))),
          DNF_INTERSECTION_TYPE(),
          b.zeroOrMore(f.newTuple(b.token(PHPPunctuator.OR), b.firstOf(TYPE(), DNF_INTERSECTION_TYPE()))))));
  }

  public DnfIntersectionTypeTree DNF_INTERSECTION_TYPE() {
    return b.<DnfIntersectionTypeTree>nonterminal(PHPLexicalGrammar.DNF_INTERSECTION_TYPE).is(
      f.dnfIntersectionType(
        b.token(PHPPunctuator.LPARENTHESIS),
        TYPE(),
        b.oneOrMore(f.newTuple(b.token(PHPPunctuator.AMPERSAND), TYPE())),
        b.token(PHPPunctuator.RPARENTHESIS)));
  }

  public DeclaredTypeTree DECLARED_TYPE() {
    return b.<DeclaredTypeTree>nonterminal(PHPLexicalGrammar.DECLARED_TYPE).is(
      b.firstOf(DNF_TYPE(), UNION_TYPE(), INTERSECTION_TYPE(), TYPE()));
  }

  public PropertyHookListTree PROPERTY_HOOK_LIST() {
    return b.<PropertyHookListTree>nonterminal(PHPLexicalGrammar.PROPERTY_HOOK_LIST).is(
      f.propertyHookList(
        b.token(LCURLYBRACE),
        b.oneOrMore(PROPERTY_HOOK()),
        b.token(RCURLYBRACE)));
  }

  public PropertyHookTree PROPERTY_HOOK() {
    return b.<PropertyHookTree>nonterminal(PHPLexicalGrammar.PROPERTY_HOOK).is(
      f.propertyHook(
        b.zeroOrMore(ATTRIBUTE_GROUP()),
        b.optional(b.token(PHPKeyword.FINAL)),
        b.optional(b.token(PHPPunctuator.AMPERSAND)),
        PROPERTY_HOOK_FUNCTION_NAME(),
        b.optional(PARAMETER_LIST()),
        b.optional(b.token(DOUBLEARROW)),
        b.firstOf(
          EOS(),
          BLOCK(),
          EXPRESSION_STATEMENT())));
  }

  public InternalSyntaxToken PROPERTY_HOOK_FUNCTION_NAME() {
    return b.<InternalSyntaxToken>nonterminal(PHPLexicalGrammar.PROPERTY_HOOK_FUNCTION_NAME).is(
      b.firstOf(
        b.token(PHPLexicalGrammar.GET),
        b.token(PHPLexicalGrammar.SET)));
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
        ENUM_DECLARATION(),
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
          EOS()),
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
        b.optional(f.newTuple(b.token(EQU), EXPRESSION()))));
  }

  public DeclareStatementTree DECLARE_STATEMENT() {
    return b.<DeclareStatementTree>nonterminal(PHPLexicalGrammar.DECLARE_STATEMENT).is(
      b.firstOf(
        f.shortDeclareStatement(
          DECLARE_STATEMENT_HEAD(),
          EOS()),
        f.declareStatementWithOneStatement(
          DECLARE_STATEMENT_HEAD(),
          STATEMENT()),
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
        ENUM_DECLARATION(),
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
        // PHP 7.3: last argument can be suffixed with an extra comma
        b.optional(b.token(COMMA)),
        b.token(RPARENTHESIS),
        EOS()));
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
          b.token(PHPPunctuator.RCURLYBRACE)),
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
          b.zeroOrMore(INNER_STATEMENT())),
        f.defaultClause(
          b.token(PHPKeyword.DEFAULT),
          b.firstOf(b.token(PHPPunctuator.COLON), b.token(PHPPunctuator.SEMICOLON)),
          b.zeroOrMore(INNER_STATEMENT()))));
  }

  public MatchClauseTree MATCH_CLAUSE() {
    return b.<MatchClauseTree>nonterminal(PHPLexicalGrammar.MATCH_CLAUSE).is(
      b.firstOf(
        f.matchConditionClause(
          EXPRESSION(),
          b.zeroOrMore(f.newTuple(b.token(PHPPunctuator.COMMA), EXPRESSION())),
          b.optional(b.token(PHPPunctuator.COMMA)),
          b.token(DOUBLEARROW),
          EXPRESSION()),
        f.matchDefaultClause(
          b.token(PHPKeyword.DEFAULT),
          b.optional(b.token(PHPPunctuator.COMMA)),
          b.token(DOUBLEARROW),
          EXPRESSION())));
  }

  public MatchExpressionTree MATCH_EXPRESSION() {
    return b.<MatchExpressionTree>nonterminal(PHPLexicalGrammar.MATCH_EXPRESSION).is(
      f.matchExpression(
        b.token(PHPKeyword.MATCH),
        PARENTHESIZED_EXPRESSION(),
        b.token(PHPPunctuator.LCURLYBRACE),
        MATCH_CLAUSE(),
        b.zeroOrMore(f.newTuple(b.token(PHPPunctuator.COMMA), MATCH_CLAUSE())),
        b.optional(b.token(PHPPunctuator.COMMA)),
        b.token(PHPPunctuator.RCURLYBRACE)));
  }

  public WhileStatementTree WHILE_STATEMENT() {
    return b.<WhileStatementTree>nonterminal(PHPLexicalGrammar.WHILE_STATEMENT).is(
      b.firstOf(
        f.whileStatement(
          b.token(PHPKeyword.WHILE),
          PARENTHESIZED_EXPRESSION(),
          STATEMENT()),
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
      f.throwStatement(THROW_EXPRESSION(), EOS()));
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
        f.classNamespaceName(NAMESPACE_NAME_WITHOUT_SINGLE_KEYWORD()),
        b.zeroOrMore(f.newTuple(b.token(PHPPunctuator.OR), f.classNamespaceName(NAMESPACE_NAME_WITHOUT_SINGLE_KEYWORD()))),
        b.optional(b.token(PHPLexicalGrammar.REGULAR_VAR_IDENTIFIER)),
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
      b.firstOf(
        f.castExpression(
          b.token(PHPPunctuator.LPARENTHESIS),
          b.firstOf(
            b.token(PHPKeyword.ARRAY),
            b.token(PHPKeyword.UNSET),
            b.token(PHPLexicalGrammar.INTEGER),
            b.token(PHPLexicalGrammar.INT),
            b.token(PHPLexicalGrammar.DOUBLE),
            b.token(PHPLexicalGrammar.FLOAT),
            b.token(PHPLexicalGrammar.REAL),
            b.token(PHPLexicalGrammar.STRING),
            b.token(PHPLexicalGrammar.OBJECT),
            b.token(PHPLexicalGrammar.BOOLEAN),
            b.token(PHPLexicalGrammar.BOOL),
            b.token(PHPLexicalGrammar.BINARY)),
          b.token(PHPPunctuator.RPARENTHESIS),
          UNARY_EXPR()),
        PREFIXED_BINARY_CAST()));
  }

  public ExpressionTree PREFIXED_BINARY_CAST() {
    return b.<ExpressionTree>nonterminal(Kind.PREFIXED_CAST_EXPRESSION).is(
      f.prefixedCastExpression(
        b.firstOf(
          b.token(PHPPunctuator.LOWER_BINARY_CAST_PREFIX),
          b.token(PHPPunctuator.UPPER_BINARY_CAST_PREFIX)),
        b.firstOf(
          STRING_LITERAL(),
          NOWDOC_STRING_LITERAL(),
          HEREDOC_STRING_LITERAL())));
  }

  public ExpressionTree UNARY_EXPR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.UNARY_EXPR).is(
      b.firstOf(
        MATCH_EXPRESSION(),
        YIELD_EXPRESSION(),
        THROW_EXPRESSION(),
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
          POWER_EXPR())));
  }

  public ExpressionTree POWER_EXPR() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.POWER_EXPR).is(
      f.powerExpr(
        b.firstOf(
          CAST_EXPR(),
          ASSIGNMENT_EXPRESSION(),
          MATCH_EXPRESSION(),
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

  // Seemingly infinite recursion is correctly handled by SSLR
  @SuppressWarnings("javabugs:S2190")
  public ExpressionTree CONDITIONAL_EXPR() {
    return b.<ExpressionTree>nonterminal(Kind.CONDITIONAL_EXPRESSION).is(
      f.completeConditionalExpr(
        NULL_COALESCING_EXPRESSION(),
        b.zeroOrMore(
          f.newConditionalExpr(b.token(QUERY), b.optional(CONDITIONAL_EXPR()), b.token(COLON), NULL_COALESCING_EXPRESSION()))));
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
        YIELD_SCALAR(),
        NOWDOC_STRING_LITERAL(),
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
          EXECUTION_OPERATOR(),
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

  public ExecutionOperatorTree EXECUTION_OPERATOR() {
    return b.<ExecutionOperatorTree>nonterminal(Kind.EXECUTION_OPERATOR).is(
      f.executionOperator(f.expandableStringLiteral(
        b.token(PHPLexicalGrammar.SPACING),
        b.token(PHPLexicalGrammar.BACKTICK),
        b.oneOrMore(
          b.firstOf(
            STRING_CHARACTERS_EXECUTION(),
            ENCAPSULATED_STRING_VARIABLE())),
        b.token(PHPLexicalGrammar.BACKTICK))));
  }

  public HeredocStringLiteralTree HEREDOC_STRING_LITERAL() {
    return b.<HeredocStringLiteralTree>nonterminal(Kind.HEREDOC_LITERAL).is(
      f.heredocStringLiteral(b.token(PHPLexicalGrammar.HEREDOC)));
  }

  public LiteralTree NOWDOC_STRING_LITERAL() {
    return b.<LiteralTree>nonterminal(Kind.NOWDOC_LITERAL).is(
      f.nowdocLiteral(b.token(PHPLexicalGrammar.NOWDOC)));
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
            b.firstOf(b.token(ARROW), b.token(NULL_SAFE_ARROW)),
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

  public ExpressionTree STRING_CHARACTERS_EXECUTION() {
    return b.<ExpandableStringCharactersTree>nonterminal().is(
      f.expandableStringCharacters(b.token(PHPLexicalGrammar.STRING_CHARACTERS_EXECUTION)));
  }

  public ExpressionTree HEREDOC_STRING_CHARACTERS() {
    return b.<ExpandableStringCharactersTree>nonterminal(Kind.HEREDOC_STRING_CHARACTERS).is(
      f.heredocStringCharacters(b.token(PHPLexicalGrammar.HEREDOC_STRING_CHARACTERS)));
  }

  public ThrowExpressionTree THROW_EXPRESSION() {
    return b.<ThrowExpressionTree>nonterminal(Kind.THROW_EXPRESSION).is(
      f.throwExpression(b.token(PHPKeyword.THROW), EXPRESSION()));
  }

  public YieldExpressionTree YIELD_EXPRESSION() {
    return b.<YieldExpressionTree>nonterminal(Kind.YIELD_EXPRESSION).is(
      b.firstOf(
        f.yieldExpressionWithKey(b.token(YIELD), EXPRESSION(), b.token(DOUBLEARROW), EXPRESSION()),
        f.yieldFromExpression(b.token(YIELD), b.token(PHPLexicalGrammar.FROM), EXPRESSION()),
        f.yieldExpression(b.token(YIELD), EXPRESSION())));
  }

  public YieldExpressionTree YIELD_SCALAR() {
    return b.<YieldExpressionTree>nonterminal(PHPLexicalGrammar.YIELD_SCALAR).is(
      f.yieldExpression(b.token(YIELD)));
  }

  public ParenthesisedExpressionTree PARENTHESIZED_EXPRESSION() {
    return b.<ParenthesisedExpressionTree>nonterminal(Kind.PARENTHESISED_EXPRESSION).is(
      f.parenthesizedExpression(
        b.token(LPARENTHESIS),
        EXPRESSION(),
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
        b.token(RPARENTHESIS)));
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
            REFERENCE_VARIABLE(),
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
        COMPOUND_VARIABLE()));
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
        NAMESPACE_NAME_WITHOUT_SINGLE_KEYWORD(),
        VARIABLE_WITHOUT_OBJECTS(),
        PARENTHESIZED_EXPRESSION(),
        NEW_EXPRESSION()));
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
      LIST_EXPRESSION_ASSIGNMENT(),
      ARRAY_DESTRUCTURING_ASSIGNMENT()));
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
        b.token(PHPPunctuator.NULL_COALESCING_EQU),

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
        b.token(ELLIPSIS),
        EXPRESSION()));
  }

  public FunctionCallTree FUNCTION_CALL_ARGUMENT_LIST() {
    return b.<FunctionCallTree>nonterminal(PHPLexicalGrammar.FUNCTION_CALL_PARAMETER_LIST).is(
      f.functionCallParameterList(
        b.token(LPARENTHESIS),
        ARGUMENTS(),
        b.token(RPARENTHESIS)));
  }

  public SeparatedListImpl<CallArgumentTree> ARGUMENTS() {
    return b.<SeparatedListImpl<CallArgumentTree>>nonterminal().is(
      f.arguments(b.optional(f.argumentsList(
        FUNCTION_CALL_ARGUMENT(),
        b.zeroOrMore(f.newTuple(b.token(COMMA), FUNCTION_CALL_ARGUMENT())),
        // PHP 7.3: last argument can be suffixed with an extra comma
        b.optional(b.token(COMMA))))));
  }

  public CallArgumentTree FUNCTION_CALL_ARGUMENT() {
    return b.<CallArgumentTree>nonterminal(PHPLexicalGrammar.FUNCTION_CALL_ARGUMENT).is(
      b.firstOf(
        f.functionCallArgument(b.optional(f.newTuple(NAME_IDENTIFIER_OR_KEYWORD(), b.token(COLON))),
          b.firstOf(
            REFERENCE_VARIABLE(),
            SPREAD_ARGUMENT(),
            EXPRESSION())),
        f.functionCallArgument(
          b.firstOf(
            REFERENCE_VARIABLE(),
            SPREAD_ARGUMENT(),
            EXPRESSION()))));
  }

  public CallableConvertTree CALLABLE_CONVERT() {
    return b.<CallableConvertTree>nonterminal(PHPLexicalGrammar.CALLABLE_CONVERT).is(
      f.callableConvert(
        b.token(LPARENTHESIS),
        b.token(ELLIPSIS),
        b.token(RPARENTHESIS)));
  }

  public ExpressionTree SPECIAL_CALL() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.SPECIAL_CALL).is(
      f.memberExpression(
        b.firstOf(
          f.nullLiteral(b.token(PHPLexicalGrammar.NULL)),
          ARRAY_INITIALIZER(),
          STRING_LITERAL()),
        b.firstOf(FUNCTION_CALL_ARGUMENT_LIST(), CALLABLE_CONVERT())));
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
            ALTERNATIVE_DIMENSIONAL_OFFSET(),
            FUNCTION_CALL_ARGUMENT_LIST(),
            CALLABLE_CONVERT()))));
  }

  public MemberAccessTree OBJECT_MEMBER_ACCESS() {
    return b.<MemberAccessTree>nonterminal(PHPLexicalGrammar.OBJECT_MEMBER_ACCESS).is(
      f.objectMemberAccess(
        b.firstOf(b.token(ARROW), b.token(NULL_SAFE_ARROW)),
        b.firstOf(
          VARIABLE_WITHOUT_OBJECTS(),
          NAME_IDENTIFIER_OR_KEYWORD(),
          COMPUTED_VARIABLE_NAME())));
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
        b.optional(b.token(COMMA)),
        b.token(RPARENTHESIS)));
  }

  public VariableTree LEXICAL_VARIABLE() {
    return b.<VariableTree>nonterminal(PHPLexicalGrammar.LEXICAL_VARIABLE).is(
      f.lexicalVariable(
        b.optional(b.token(AMPERSAND)),
        VARIABLE_IDENTIFIER()));
  }

  public FunctionCallTree INTERNAL_FUNCTION() {
    return b.<FunctionCallTree>nonterminal(PHPLexicalGrammar.INTERNAL_FUNCTION).is(
      b.firstOf(
        f.internalFunction(
          b.token(PHPLexicalGrammar.ISSET),
          b.token(LPARENTHESIS),
          EXPRESSION(),
          b.zeroOrMore(f.newTuple(b.token(COMMA), EXPRESSION())),
          // PHP 7.3: last argument can be suffixed with an extra comma
          b.optional(b.token(COMMA)),
          b.token(RPARENTHESIS)),

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
    return b.<ArrayInitializerTree>nonterminal(PHPLexicalGrammar.ARRAY_INITIALIZER).is(
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
        // Match nested arrays early to avoid running into a combinatoric explosion
        f.arrayPair1(ARRAY_INITIALIZER()),
        f.arrayPair2(b.token(ELLIPSIS), EXPRESSION()),
        f.arrayPair1(EXPRESSION(), b.optional(f.newTuple(b.token(DOUBLEARROW), b.firstOf(REFERENCE_VARIABLE(), EXPRESSION())))),
        f.arrayPair2(REFERENCE_VARIABLE())));
  }

  public FunctionExpressionTree FUNCTION_EXPRESSION() {
    return b.<FunctionExpressionTree>nonterminal(Kind.FUNCTION_EXPRESSION).is(
      f.functionExpression(
        b.zeroOrMore(ATTRIBUTE_GROUP()),
        b.optional(b.token(STATIC)),
        b.token(FUNCTION),
        b.optional(b.token(AMPERSAND)),
        PARAMETER_LIST(),
        b.optional(LEXICAL_VARIABLES()),
        b.optional(RETURN_TYPE_CLAUSE()),
        BLOCK()));
  }

  public ArrowFunctionExpressionTree ARROW_FUNCTION_EXPRESSION() {
    return b.<ArrowFunctionExpressionTree>nonterminal(Kind.ARROW_FUNCTION_EXPRESSION).is(
      f.arrowFunctionExpression(
        b.zeroOrMore(ATTRIBUTE_GROUP()),
        b.optional(b.token(STATIC)),
        b.token(PHPKeyword.FN),
        b.optional(b.token(AMPERSAND)),
        PARAMETER_LIST(),
        b.optional(RETURN_TYPE_CLAUSE()),
        b.token(DOUBLEARROW),
        EXPRESSION()));
  }

  public NewExpressionTree NEW_EXPRESSION() {
    return b.<NewExpressionTree>nonterminal(Kind.NEW_EXPRESSION).is(
      f.newExpression(b.token(NEW), b.firstOf(PARENTHESIZED_EXPRESSION(), NEW_OBJECT_EXPRESSION(), ANONYMOUS_CLASS())));
  }

  public ExpressionTree NEW_OBJECT_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(PHPLexicalGrammar.NEW_OBJECT_EXPRESSION).is(
      f.newObjectExpression(
        PRIMARY_EXPRESSION(),
        b.zeroOrMore(
          b.firstOf(
            OBJECT_MEMBER_ACCESS(),
            NEW_OBJECT_CLASS_FIELD_ACCESS(),
            DIMENSIONAL_OFFSET(),
            ALTERNATIVE_DIMENSIONAL_OFFSET())),
        b.optional(FUNCTION_CALL_ARGUMENT_LIST())));
  }

  public MemberAccessTree NEW_OBJECT_CLASS_FIELD_ACCESS() {
    return b.<MemberAccessTree>nonterminal(PHPLexicalGrammar.NEW_OBJECT_CLASS_FIELD_ACCESS).is(
      f.classMemberAccess(b.token(DOUBLECOLON), VARIABLE_WITHOUT_OBJECTS()));
  }

  public AnonymousClassTree ANONYMOUS_CLASS() {
    return b.<AnonymousClassTree>nonterminal(Kind.ANONYMOUS_CLASS).is(
      f.anonymousClass(
        b.zeroOrMore(ATTRIBUTE_GROUP()),
        b.optional(b.token(READONLY)),
        b.token(CLASS),
        b.optional(b.token(PHPPunctuator.LPARENTHESIS)),
        ARGUMENTS(),
        b.optional(b.token(PHPPunctuator.RPARENTHESIS)),
        b.optional(f.newTuple(b.token(EXTENDS), f.classNamespaceName(NAMESPACE_NAME()))),
        b.optional(f.newTuple(b.token(IMPLEMENTS), INTERFACE_LIST())),
        b.token(LCURLYBRACE),
        b.zeroOrMore(CLASS_MEMBER()),
        b.token(RCURLYBRACE)));
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
          SPECIAL_CALL(),
          f.combinedScalarOffset(ARRAY_INITIALIZER(), b.zeroOrMore(DIMENSIONAL_OFFSET())),
          FUNCTION_EXPRESSION(),
          ARROW_FUNCTION_EXPRESSION(),
          COMMON_SCALAR(),
          MEMBER_EXPRESSION(),
          NEW_EXPRESSION(),
          EXIT_EXPRESSION(),
          INTERNAL_FUNCTION()),
        b.optional(b.firstOf(
          b.token(INC),
          b.token(DEC),
          f.newTuple(b.token(INSTANCEOF), MEMBER_EXPRESSION())))));
  }

  /**
   * [ END ] Expression
   */

  public AttributeTree ATTRIBUTE() {
    return b.<AttributeTree>nonterminal(PHPLexicalGrammar.ATTRIBUTE).is(
      f.attribute(
        NAMESPACE_NAME(),
        b.optional(FUNCTION_CALL_ARGUMENT_LIST())));
  }

  public SeparatedList<AttributeTree> ATTRIBUTE_LIST() {
    return b.<SeparatedList<AttributeTree>>nonterminal().is(
      f.attributeList(
        ATTRIBUTE(),
        b.zeroOrMore(f.newTuple(b.token(COMMA), ATTRIBUTE())),
        b.optional(b.token(COMMA))));
  }

  public AttributeGroupTree ATTRIBUTE_GROUP() {
    return b.<AttributeGroupTree>nonterminal(PHPLexicalGrammar.ATTRIBUTE_GROUP).is(
      f.attributeGroup(
        b.token(ATTRIBUTE_OPEN),
        ATTRIBUTE_LIST(),
        b.token(RBRACKET)));
  }
}
