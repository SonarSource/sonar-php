/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
      TooManyFunctionParametersCheck.class
    );
  }
}
