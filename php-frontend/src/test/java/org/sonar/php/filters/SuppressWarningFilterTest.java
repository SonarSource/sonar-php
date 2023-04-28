/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.filters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class SuppressWarningFilterTest extends ParsingTestUtils {

  private PhpFile prepareFile(String path) throws URISyntaxException {
    PhpFile file = spy(PhpFile.class);
    when(file.uri()).thenReturn(new URI(path));
    return file;
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "#[SuppressWarnings(\"php:S1234\")]",
    "// @SuppressWarnings(\"php:S1234\")",
    "# @SuppressWarnings(\"php:S1234\")",
    "/* @SuppressWarnings(\"php:S1234\") */",
    "/* Test comment @SuppressWarnings  (  \"php:S1234\"  )   */",
  })
  void filterOutIssueNextLine(String suppressWarning) throws URISyntaxException {
    PhpFile file = prepareFile("myFile.php");
    String code = asCode("<?php",
      suppressWarning,
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.analyze(file, parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isFalse();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S4657", 3)).isTrue();
    assertThat(suppressWarningFilter.accept("notMyFile.php", "php:S1234", 3)).isTrue();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 8)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "// @SuppressWarnings(\"php:S1234\")",
    "# @SuppressWarnings(\"php:S1234\")",
    "/* @SuppressWarnings(\"php:S1234\") */",
  })
  void filterOutIssueCommentOnSameLineButApplyToNextLine(String suppressWarning) throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "",          "function foo(){} " + suppressWarning,
      "php:S1234", "function foo(){} "
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "#[SuppressWarnings(\"php:S1234\", \"php:S4567\")]",
    "// @SuppressWarnings(\"php:S1234\", \"php:S4567\")",
    "# @SuppressWarnings(\"php:S1234\", \"php:S4567\")",
    "/* @SuppressWarnings(\"php:S1234\", \"php:S4567\") */",
    "/* Test comment @SuppressWarnings  (  \"php:S1234\", \"php:S4567\"  )   */",
    "/*@SuppressWarnings(\"php:S1234\",\"php:S4567\")*/",
  })
  void filterOutIssueMultipleRule(String suppressWarning) throws URISyntaxException {
    PhpFile file = prepareFile("myFile.php");
    String code = asCode("<?php",
      suppressWarning,
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.analyze(file, parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isFalse();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S4567", 3)).isFalse();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S8888", 3)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "// @SuppressWarnings(\"php:S1234\")",
    "# @SuppressWarnings(\"php:S1234\")",
    "/* @SuppressWarnings(\"php:S1234\") */",
  })
  void filterOutIssueCommentSeparatedByEmptyLine(String suppressWarning) throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "",          suppressWarning,
      "",          "",
      "php:S1234", "function foo(){} "
    );
  }

  @Test
  void filterOutIssueAttributeSeparatedByEmptyLine() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "php:S1234", "#[SuppressWarnings(\"php:S1234\")]",
      "php:S1234", "",
      "php:S1234", "function foo(){} "
    );
  }

  @ParameterizedTest
  @CsvSource({
    "function foo() {                  , } ",
    "class Foo {                       , } ",
    "trait Foo {                       , } ",
    "interface Foo {                   , } ",
    "enum Foo {                        , } ",
    "$foo = function () {              , };",
    "$foo = function ($x) use ($y) {   , };",
    "$foo = fn($x) => $x + $y          ,  ;",
    "$foo = new class {                , };",
  })
  void filterOutOnFullScopeUsingComment(String scopeDeclaration, String scopeEnd) throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "",          "// @SuppressWarnings(\"php:S1234\")",
      "php:S1234", scopeDeclaration,
      "php:S1234", "  // in the scope",
      "php:S1234", scopeEnd,
      "",          "$x = 3; // out of the scope"
    );
  }

  @ParameterizedTest
  @CsvSource({
    "function foo() {                  , }",
    "class Foo {                       , }",
    "trait Foo {                       , }",
    "interface Foo {                   , }",
    "enum Foo {                        , }",
  })
  void filterOutOnFullScopeUsingAttribute(String scopeDeclaration, String scopeEnd) throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "php:S1234", "#[SuppressWarnings(\"php:S1234\")]",
      "php:S1234", scopeDeclaration,
      "php:S1234", "  // in the scope",
      "php:S1234", scopeEnd,
      "",          "$x = 3; // out of the scope"
    );
  }

  @Test
  void filterOutCommentOnMethod() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "",          "class Foo {",
      "",          "  public $name;",
      "",          "  // @SuppressWarnings(\"php:S1234\")",
      "php:S1234", "  function foo() {",
      "php:S1234", "    return $name;",
      "php:S1234", "  }",
      "",          "}",
      "",          "$x = 3; // out of the scope"
    );
  }

  @Test
  void filterOutAttributeOnMethod() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "",          "class Foo {",
      "",          "  public $name;",
      "php:S1234", "  #[SuppressWarnings(\"php:S1234\")]",
      "php:S1234", "  function foo() {",
      "php:S1234", "    return $name;",
      "php:S1234", "  }",
      "",          "}",
      "",          "$x = 3; // out of the scope"
    );
  }

  @Test
  void filterOutAttributeOnClassVariableDeclaration() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "",          "class Foo {",
      "php:S1234", "  #[SuppressWarnings(\"php:S1234\")]",
      "php:S1234", "  ",
      "php:S1234", "  public $name;",
      "",          "}",
      "",          "$x = 3; // out of the scope"
    );
  }

  @Test
  void filterOutAttributeOnClassConstantDeclaration() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "",          "class Foo {",
      "php:S1234", "  #[SuppressWarnings(\"php:S1234\")]",
      "php:S1234", "  const CONSTANT = 'constant value';",
      "",          "}",
      "",          "$x = 3; // out of the scope"
    );
  }

  @Test
  void filterOutAttributeOnParameter() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "",          "function foo (",
      "php:S1234", "  #[SuppressWarnings(\"php:S1234\")]",
      "php:S1234", "  $x,",
      "",          "  $y",
      "",          ") {",
      "",          "  return $x; // out of the scope",
      "",          "}"
    );
  }

  @Test
  void filterOutAttributeOnAnonymousClass() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "",          "$x = new",
      "php:S1234", "#[SuppressWarnings(\"php:S1234\")]",
      "php:S1234", "class {",
      "php:S1234", "  public $y;",
      "php:S1234", "};",
      "",          "$x = 3; // out of the scope"
    );
  }

  @Test
  void filterOutAttributeOnFunctionExpression() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "",          "$x =",
      "php:S1234", "#[SuppressWarnings(\"php:S1234\")]",
      "php:S1234", "function ($x) {",
      "php:S1234", "  return $x + 2;",
      "php:S1234", "};",
      "",          "$y = 3; // out of the scope"
    );
  }

  @Test
  void filterOutAttributeOnArrowFunctionExpression() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "",          "$x =",
      "php:S1234", "#[SuppressWarnings(\"php:S1234\")]",
      "php:S1234", "fn($x, $y) => ",
      "php:S1234", "  $x + $y;",
      "",          "$z = 3; // out of the scope"
    );
  }

  @Test
  void filterOutAttributeOnEnumCase() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "",          "enum Color {",
      "",          "  case Red;",
      "php:S1234", "  #[SuppressWarnings(\"php:S1234\")]",
      "php:S1234", "  case Green;",
      "",          "  case Blue;",
      "",          "}",
      "",          "$x = 3; // out of the scope"
    );
  }

  @Test
  void noFilterOutIssue() throws URISyntaxException {
    PhpFile file = prepareFile("myFile.php");
    String code = asCode("<?php",
      "",
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.analyze(file, parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isTrue();
  }

  @Test
  void testReset() throws URISyntaxException {
    PhpFile file = prepareFile("myFile.php");
    String code = asCode("<?php",
      "#[SuppressWarnings(\"php:S1234\")]",
      "function foo(){}");
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    suppressWarningFilter.analyze(file, parseSource(code));
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isFalse();
    suppressWarningFilter.reset();
    assertThat(suppressWarningFilter.accept("myFile.php", "php:S1234", 3)).isTrue();
  }

  @Test
  void filterOutMultipleIssueOnDifferentScope() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",          "<?php",
      "php:S1234", "#[SuppressWarnings(\"php:S1234\")]",
      "php:S1234", "function foo(){}",
      "",          "// @SuppressWarnings(\"php:S4567\")",
      "php:S4567", "function bar(){}");
  }

  @Test
  void filterOutMultipleIssueInSingleComment() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",                    "<?php",
      "",                    "/* @SuppressWarnings(\"php:S1234\")",
      "",                    "   @SuppressWarnings(\"php:S4567\") */",
      "php:S1234,php:S4567", "function foo(){}",
      "",                    "$x = 3;");
  }

  @Test
  void filterOutMultipleIssueInSingleSuppressWarningsInstruction() throws URISyntaxException {
    assertScopeOfSuppressWarningInstruction(
      "",                    "<?php",
      "",                    "// @SuppressWarnings(\"php:S1234\", \"php:S4567\")",
      "php:S1234,php:S4567", "function foo(){}",
      "",                    "$x = 3;");
  }

  @Test
  void filterOutOnMultipleFile() throws URISyntaxException {
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();

    PhpFile file1 = prepareFile("myFile1.php");
    String code1 = asCode("<?php",
      "#[SuppressWarnings(\"php:S1234\")]",
      "function foo(){}");
    suppressWarningFilter.analyze(file1, parseSource(code1));

    PhpFile file2 = prepareFile("myFile2.php");
    String code2 = asCode("<?php",
      "#[SuppressWarnings(\"php:S4567\")]",
      "function foo(){}");
    suppressWarningFilter.analyze(file2, parseSource(code2));

    assertThat(suppressWarningFilter.accept("myFile1.php", "php:S1234", 3)).isFalse();
    assertThat(suppressWarningFilter.accept("myFile1.php", "php:S4567", 3)).isTrue();
    assertThat(suppressWarningFilter.accept("myFile2.php", "php:S1234", 3)).isTrue();
    assertThat(suppressWarningFilter.accept("myFile2.php", "php:S4567", 3)).isFalse();
  }

  /**
   * Tool to check in a script which lines are impacted by a SuppressWarnings instruction.
   * Each parameter should go by pair:
   * - the list of expected suppresssed rules separated by colon
   * - the line of code
   * Example :
   * <pre>
   * "",              "<?php"
   * "php:S1,php:S2", "// SuppressWarnings("php:S1", "php:S2")
   * "php:S1,php:S2", "$x = 3;"
   * </>
   */
  private void assertScopeOfSuppressWarningInstruction(String ...params) throws URISyntaxException {
    assertThat(params.length % 2).as("Expecting even number of arguments").isZero();

    String filename = "myFile.php";
    PhpFile file = prepareFile(filename);
    SuppressWarningFilter suppressWarningFilter = new SuppressWarningFilter();
    StringBuilder fullScript = new StringBuilder();
    List<String[]> filteredRulesPerLine = new ArrayList<>();
    Set<String> allRules = new HashSet<>();

    for (int i = 0; i < params.length; i += 2) {
      String[] filteredRules = params[i].isEmpty() ? new String[0] : params[i].split(",");
      String lineCode = params[i+1];

      filteredRulesPerLine.add(filteredRules);
      fullScript.append(lineCode).append(System.lineSeparator());
      allRules.addAll(Arrays.asList(filteredRules));
    }

    suppressWarningFilter.analyze(file, parseSource(fullScript.toString()));
    for (int line = 1; line <= filteredRulesPerLine.size(); line++) {
      // check that expected filtered rules are indeed present
      String[] filteredRules = filteredRulesPerLine.get(line - 1);
      for (String filteredRule : filteredRules) {
        assertThat(suppressWarningFilter.accept(filename, filteredRule, line))
          .as("Line %s is not suppressing warning for rule %s while it should", line, filteredRule).isFalse();
      }
      // check that no other rules are filtered out
      Set<String> otherRules = new HashSet<>(allRules);
      Arrays.asList(filteredRules).forEach(otherRules::remove);
      for (String otherRule : otherRules) {
        assertThat(suppressWarningFilter.accept(filename, otherRule, line))
          .as("Line %s is suppressing warning for rule %s while it shouldn't", line, otherRule).isTrue();
      }
    }
  }
}
