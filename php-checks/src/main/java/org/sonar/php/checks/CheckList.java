/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.sonar.php.checks.phpini.AllowUrlCheck;
import org.sonar.php.checks.phpini.CgiForceRedirectCheck;
import org.sonar.php.checks.phpini.EnableDlCheck;
import org.sonar.php.checks.phpini.FileUploadsCheck;
import org.sonar.php.checks.phpini.OpenBasedirCheck;
import org.sonar.php.checks.phpini.SessionUseTransSidCheck;
import org.sonar.php.checks.security.ChangingAccessibilityCheck;
import org.sonar.php.checks.security.CommandLineArgumentCheck;
import org.sonar.php.checks.security.CookieDomainCheck;
import org.sonar.php.checks.security.CookieSensitiveDataCheck;
import org.sonar.php.checks.security.CryptographicHashCheck;
import org.sonar.php.checks.security.DataEncryptionCheck;
import org.sonar.php.checks.security.LDAPAuthenticatedConnectionCheck;
import org.sonar.php.checks.security.LoggerConfigurationCheck;
import org.sonar.php.checks.security.PermissionsControlCheck;
import org.sonar.php.checks.security.QueryUsageCheck;
import org.sonar.php.checks.security.RegexUsageCheck;
import org.sonar.php.checks.security.SessionFixationCheck;
import org.sonar.php.checks.security.SignallingProcessCheck;
import org.sonar.php.checks.security.SocketUsageCheck;
import org.sonar.php.checks.security.StandardInputUsageCheck;

public class CheckList {

  public static final String REPOSITORY_KEY = "php";

  private CheckList() {
  }

  public static List<Class> getChecks() {
    return ImmutableList.<Class>of(
      AliasFunctionUsageCheck.class,
      AllBranchesIdenticalCheck.class,
      AlwaysUseCurlyBracesCheck.class,
      ArgumentWithDefaultValueNotLastCheck.class,
      ArrayCountableCountCheck.class,
      AssignmentInSubExpressionCheck.class,
      AtLeastThreeCasesInSwitchCheck.class,
      AvoidDESCheck.class,
      BooleanEqualityComparisonCheck.class,
      CakePhpDebugModeCheck.class,
      CallParentConstructorCheck.class,
      CallToIniSetCheck.class,
      CatchRethrowingCheck.class,
      ChangingAccessibilityCheck.class,
      CharacterBeforeOpeningPHPTagCheck.class,
      ClassComplexityCheck.class,
      ClassCouplingCheck.class,
      ClassNameCheck.class,
      ClosingTagInFullPHPFileCheck.class,
      CodeFollowingJumpStatementCheck.class,
      CollapsibleIfStatementCheck.class,
      CommandLineArgumentCheck.class,
      CommentedOutCodeCheck.class,
      ConcatenatedStringLiteralCheck.class,
      ConditionalIndentationCheck.class,
      ConditionalOnNewLineCheck.class,
      ConsistentFunctionReturnCheck.class,
      ConstantNameCheck.class,
      ConstructorDeclarationCheck.class,
      ConstructorDependencyInversionCheck.class,
      CookieDomainCheck.class,
      CookieSensitiveDataCheck.class,
      CookiesSecureCheck.class,
      CryptographicHashCheck.class,
      CryptographicKeySizeCheck.class,
      DataEncryptionCheck.class,
      DeadStoreCheck.class,
      DeprecatedPredefinedVariablesUseCheck.class,
      DirectlyAccessingSuperGlobalCheck.class,
      DuplicateBranchImplementationCheck.class,
      DuplicateConditionCheck.class,
      DuplicatedArgumentCheck.class,
      DuplicatedFunctionArgumentCheck.class,
      DuplicatedMethodCheck.class,
      EchoWithParenthesisCheck.class,
      ElseIfSequenceKeywordUsageCheck.class,
      ElseIfWithoutElseCheck.class,
      EmptyDatabasePasswordCheck.class,
      EmptyNestedBlockCheck.class,
      EmptyStatementCheck.class,
      EvalUseCheck.class,
      ExitOrDieUsageCheck.class,
      ExpressionComplexityCheck.class,
      FieldNameCheck.class,
      FileHeaderCheck.class,
      FileNameCheck.class,
      FileWithSymbolsAndSideEffectsCheck.class,
      FixmeTagPresenceCheck.class,
      ForHidingWhileCheck.class,
      ForLoopCounterChangedCheck.class,
      ForLoopIncrementSignCheck.class,
      FormattingStandardCheck.class,
      FunctionCognitiveComplexityCheck.class,
      FunctionComplexityCheck.class,
      FunctionDefineOutsideClassCheck.class,
      FunctionNameCheck.class,
      GenericExceptionCheck.class,
      GlobalKeywordAndArrayUsageCheck.class,
      GotoUseCheck.class,
      HashFunctionCheck.class,
      HardCodedCredentialsCheck.class,
      HardCodedIpAddressCheck.class,
      HardCodedUriCheck.class,
      HttpOnlyCheck.class,
      IdenticalOperandsInBinaryExpressionCheck.class,
      IfConditionAlwaysTrueOrFalseCheck.class,
      IgnoredReturnValueCheck.class,
      ImmediatelyReturnedVariableCheck.class,
      IncrementDecrementInSubExpressionCheck.class,
      InlineHTMLInFileCheck.class,
      InsecureHashCheck.class,
      InterfaceNameCheck.class,
      KeywordsAndConstantsNotLowerCaseCheck.class,
      LDAPAuthenticatedConnectionCheck.class,
      LeftCurlyBraceEndsLineCheck.class,
      LeftCurlyBraceStartsLineCheck.class,
      LineLengthCheck.class,
      LocalVariableAndParameterNameCheck.class,
      LocalVariableShadowsClassFieldCheck.class,
      LoggerConfigurationCheck.class,
      LogicalWordOperatorUsageCheck.class,
      LoopExecutingAtMostOnceCheck.class,
      LowerCaseColorCheck.class,
      MethodNameReturningBooleanCheck.class,
      MissingMethodVisibilityCheck.class,
      MissingNewLineAtEOFCheck.class,
      ModifiersOrderCheck.class,
      MoreThanOneClassInFileCheck.class,
      MultilineBlocksCurlyBracesCheck.class,
      NestedControlFlowDepthCheck.class,
      NestedFunctionDepthCheck.class,
      NestedTernaryOperatorsCheck.class,
      NonEmptyCaseWithoutBreakCheck.class,
      NonLFCharAsEOLCheck.class,
      NoPaddingRsaCheck.class,
      NoSonarCheck.class,
      NullDereferenceInConditionalCheck.class,
      OnePropertyDeclarationPerStatementCheck.class,
      OneStatementPerLineCheck.class,
      OpeningPHPTagCheck.class,
      OverridingMethodSimplyCallParentCheck.class,
      ParsingErrorCheck.class,
      PerlStyleCommentsUsageCheck.class,
      PermissionsControlCheck.class,
      PHPDeprecatedFunctionUsageCheck.class,
      PhpSapiNameFunctionUsageCheck.class,
      QueryUsageCheck.class,
      RandomGeneratorCheck.class,
      RedundantFinalCheck.class,
      RedundantJumpCheck.class,
      RedundantParenthesesCheck.class,
      ReferenceInFunctionCallCheck.class,
      RegexUsageCheck.class,
      RequireIncludeInstructionsUsageCheck.class,
      RequireInsteadOfRequireOnceCheck.class,
      ReturnOfBooleanExpressionCheck.class,
      RightCurlyBraceStartsLineCheck.class,
      SelfAssignmentCheck.class,
      SelfKeywordUsageCheck.class,
      SessionCookiePersistenceCheck.class,
      SessionFixationCheck.class,
      SignallingProcessCheck.class,
      SilencedErrorsCheck.class,
      SleepFunctionUsageCheck.class,
      SocketUsageCheck.class,
      SSLCertificatesVerificationDisabledCheck.class,
      StandardInputUsageCheck.class,
      StringLiteralDuplicatedCheck.class,
      SwitchCaseTooBigCheck.class,
      SwitchDefaultPositionCheck.class,
      SwitchWithoutDefaultCheck.class,
      TabCharacterCheck.class,
      ThisVariableUsageInStaticContextCheck.class,
      TodoTagPresenceCheck.class,
      TooManyCasesInSwitchCheck.class,
      TooManyFieldsInClassCheck.class,
      TooManyFunctionParametersCheck.class,
      TooManyLinesInClassCheck.class,
      TooManyLinesInFileCheck.class,
      TooManyLinesInFunctionCheck.class,
      TooManyMethodsInClassCheck.class,
      TooManyReturnCheck.class,
      TrailingCommentCheck.class,
      TrailingWhitespaceCheck.class,
      UnserializeCallCheck.class,
      UnusedExceptionCheck.class,
      UnusedFunctionParametersCheck.class,
      UnusedLocalVariableCheck.class,
      UnusedPrivateFieldCheck.class,
      UnusedPrivateMethodCheck.class,
      UselessExpressionStatementCheck.class,
      UselessIncrementCheck.class,
      UselessObjectCreationCheck.class,
      UseOfEmptyReturnValueCheck.class,
      UseOfUninitializedVariableCheck.class,
      VariableVariablesCheck.class,
      VarKeywordUsageCheck.class,
      WeakSSLProtocolCheck.class,
      WrongAssignmentOperatorCheck.class);
  }

  public static Iterable<Class> getPhpIniChecks() {
    return ImmutableList.<Class>of(
      AllowUrlCheck.class,
      CookiesSecureCheck.class,
      CgiForceRedirectCheck.class,
      EnableDlCheck.class,
      FileUploadsCheck.class,
      HttpOnlyCheck.class,
      OpenBasedirCheck.class,
      SessionCookiePersistenceCheck.class,
      SessionUseTransSidCheck.class);
  }

  public static Set<Class> getAllChecks() {
    return ImmutableSet.<Class>builder()
      .addAll(CheckList.getChecks())
      .addAll(CheckList.getPhpIniChecks())
      .build();
  }
}
