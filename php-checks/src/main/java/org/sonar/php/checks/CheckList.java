/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import java.util.List;

public class CheckList {

  public static final String REPOSITORY_KEY = "php";

  public static final String SONAR_WAY_PROFILE = "Sonar way";

  private CheckList() {
  }

  public static List<Class> getChecks() {
    return ImmutableList.<Class>of(
      EvalUseCheck.class,
      TooManyCasesInSwitchCheck.class,
      EmptyStatementCheck.class,
      IfConditionAlwaysTrueOrFalseCheck.class,
      CollapsibleIfStatementCheck.class,
      SwitchCaseTooBigCheck.class,
      TooManyReturnCheck.class,
      FunctionNameCheck.class,
      ReturnOfBooleanExpressionCheck.class,
      BooleanEqualityComparisonCheck.class,
      VariableVariablesCheck.class,
      SwitchWithoutDefaultCheck.class,
      AtLeastThreeCasesInSwitchCheck.class,
      ForHidingWhileCheck.class,
      TrailingCommentCheck.class,
      ElseIfWithoutElseCheck.class,
      TooManyLinesInFileCheck.class,
      LineLengthCheck.class,
      EmptyNestedBlockCheck.class,
      NonEmptyCaseWithoutBreakCheck.class,
      DeprecatedPredefinedVariablesUseCheck.class,
      NestedControlFlowDepthCheck.class,
      StringLiteralDuplicatedCheck.class,
      FunctionComplexityCheck.class,
      TabCharacterCheck.class,
      FileHeaderCheck.class,
      LeftCurlyBraceEndsLineCheck.class,
      AlwaysUseCurlyBracesCheck.class,
      RightCurlyBraceStartsLineCheck.class,
      GotoUseCheck.class,
      ClassNameCheck.class,
      FieldNameCheck.class,
      UnusedFunctionParametersCheck.class,
      ExpressionComplexityCheck.class,
      ForLoopCounterChangedCheck.class,
      ConstantNameCheck.class,
      TooManyLinesInFunctionCheck.class,
      TooManyMethodsInClassCheck.class,
      ClassComplexityCheck.class,
      TodoTagPresenceCheck.class,
      FixmeTagPresenceCheck.class,
      ConstructorDeclarationCheck.class,
      CallParentConstructorCheck.class,
      TrailingWhitespaceCheck.class,
      MissingNewLineAtEOFCheck.class,
      UnusedPrivateFieldCheck.class,
      UnusedLocalVariableCheck.class,
      OneStatementPerLineCheck.class,
      ClassCouplingCheck.class,
      LeftCurlyBraceStartsLineCheck.class,
      IncrementDecrementInSubExpressionCheck.class,
      LocalVariableShadowsClassFieldCheck.class,
      LocalVariableAndParameterNameCheck.class,
      ImmediatelyReturnedVariableCheck.class,
      CommentedOutCodeCheck.class,
      ElseIfSequenceKeywordUsageCheck.class,
      ExitOrDieUsageCheck.class,
      VarKeywordUsageCheck.class,
      MissingMethodVisibilityCheck.class,
      KeywordsAndConstantsNotLowerCaseCheck.class,
      OnePropertyDeclarationPerStatementCheck.class,
      ModifiersOrderCheck.class,
      ClosingTagInFullPHPFileCheck.class,
      NonLFCharAsEOLCheck.class,
      ArgumentWithDefaultValueNotLastCheck.class,
      FormattingStandardCheck.class,
      OpeningPHPTagCheck.class,
      PerlStyleCommentsUsageCheck.class,
      TooManyLinesInClassCheck.class,
      PhpSapiNameFunctionUsageCheck.class,
      EchoWithParenthesisCheck.class,
      SilencedErrorsCheck.class,
      TooManyFieldsInClassCheck.class,
      ThisVariableUsageInStaticContextCheck.class,
      ReferenceInFunctionCallCheck.class,
      LogicalWordOperatorUsageCheck.class,
      DuplicatedFunctionArgumentCheck.class,
      CodeFollowingJumpStatementCheck.class,
      RedundantFinalCheck.class,
      UnusedPrivateMethodCheck.class,
      FunctionDefineOutsideClassCheck.class,
      SelfKeywordUsageCheck.class,
      FileWithSymbolsAndSideEffectsCheck.class,
      GlobalKeywordAndArrayUsageCheck.class,
      MoreThanOneClassInFileCheck.class,
      LowerCaseColorCheck.class,
      NullDereferenceInConditionalCheck.class,
      DirectlyAccessingSuperGlobalCheck.class,
      RequireInsteadOfRequireOnceCheck.class,
      InlineHTMLInFileCheck.class,
      ConcatenatedStringLiteralCheck.class,
      NestedFunctionDepthCheck.class,
      CharacterBeforeOpeningPHPTagCheck.class,
      MethodNameReturningBooleanCheck.class,
      FileNameCheck.class,
      GenericExceptionCheck.class,
      ConstructorDependencyInversionCheck.class,
      PHP5DeprecatedFunctionUsageCheck.class,
      OverridingMethodSimplyCallParentCheck.class,
      TooManyFunctionParametersCheck.class,
      UselessExpressionStatementCheck.class,
      SelfAssignmentCheck.class,
      UselessObjectCreationCheck.class,
      DuplicateConditionCheck.class,
      DuplicateBranchImplementationCheck.class,
      InterfaceNameCheck.class,
      CallToIniSetCheck.class,
      SleepFunctionUsageCheck.class,
      HardCodedCredentialsCheck.class,
      MultilineBlocksCurlyBracesCheck.class,
      ParsingErrorCheck.class,
      IdenticalOperandsInBinaryExpressionCheck.class);
  }
}
