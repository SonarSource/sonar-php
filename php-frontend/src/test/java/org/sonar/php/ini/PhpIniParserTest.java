/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.ini;

import com.sonar.sslr.api.RecognitionException;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.FileTestUtils;
import org.sonar.php.ini.tree.Directive;
import org.sonar.php.ini.tree.PhpIniFile;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhpIniParserTest {

  @Test
  void noDirective() {
    assertThat(parse("").directives()).isEmpty();
    assertThat(parse("   \n  ").directives()).isEmpty();
  }

  @Test
  void section() {
    assertThat(parse("[section1]").directives()).isEmpty();
    assertThat(parse("  [section1] ").directives()).isEmpty();
    checkSingleDirective("[section1]\n name1=value1\n [section2]", "name1", "value1");
  }

  @Test
  void simpleDirective() {
    checkSingleDirective("name1=value1", "name1", "value1");
  }

  @Test
  void emptyValue() {
    checkSingleDirective("name1=", "name1", "");
  }

  @Test
  void twoDirectives() {
    List<Directive> directives = parse("name1=value1\nname2=value2").directives();
    assertThat(directives).hasSize(2);
    checkDirective(directives.get(0), "name1", "value1");
    checkDirective(directives.get(1), "name2", "value2");
  }

  @Test
  void fullLineComment() {
    checkSingleDirective(";comment1\nname1=value1\n;comment2", "name1", "value1");
  }

  @Test
  void endOfLineComment() {
    checkSingleDirective("name1=value1;comment1", "name1", "value1");
  }

  @Test
  void whitespaces() {
    checkSingleDirective(" \t  name1 \t = \t   value1  ", "name1", "value1");
  }

  @Test
  void stringValue() {
    checkSingleDirective("name1=\"value1\"", "name1", "\"value1\"");
  }

  @Test
  void stringValueContainingSpecialChar() {
    checkSingleDirective("name1=\"value1;value2\"", "name1", "\"value1;value2\"");
    checkSingleDirective("name1=\"value1=value2\"", "name1", "\"value1=value2\"");
  }

  @Test
  void stringValueContainingEscapedQuotes() {
    checkSingleDirective("name1=\"aa\\\"bb;\\\"cc\"", "name1", "\"aa\\\"bb;\\\"cc\"");
  }

  @Test
  void numericValue() {
    checkSingleDirective("name1=42", "name1", "42");
    checkSingleDirective("name1=42.", "name1", "42.");
    checkSingleDirective("name1=4.2", "name1", "4.2");
    checkSingleDirective("name1=.42", "name1", ".42");
  }

  @Test
  void expressions() {
    checkSingleDirective("name1=E_ALL & ~E_DEPRECATED & ~E_STRICT", "name1", "E_ALL & ~E_DEPRECATED & ~E_STRICT");
  }

  @Test
  void noEqualSign() {
    assertThat(parse("xxx").directives()).isEmpty();
  }

  @Test
  void emptyName() {
    assertThatExceptionOfType(RecognitionException.class).isThrownBy(() -> parse("=value1"));
  }

  @Test
  void blankName() {
    assertThatExceptionOfType(RecognitionException.class).isThrownBy(() -> parse(" =value1"));
  }

  @Test
  void moreThanOneEqualSign() {
    assertThatExceptionOfType(RecognitionException.class).isThrownBy(() -> parse("name1=value1=1"));
  }

  @Test
  void tokens() {
    Directive directive = parse("\n  name1=value1").directives().get(0);
    checkToken(directive.name(), "name1", 2, 3, 2, 8);
    checkToken(directive.equalSign(), "=", 2, 8, 2, 9);
    checkToken(directive.value(), "value1", 2, 9, 2, 15);
  }

  @Test
  void parseFile() {
    PhpFile file = FileTestUtils.getFile(new File("src/test/resources/phpini/php.ini"));
    PhpIniFile phpIni = new PhpIniParser().parse(file);
    assertThat(phpIni.directives()).hasSize(1);
    assertThat(phpIni.directives().get(0).name().text()).isEqualTo("max_execution_time");
  }

  @Test
  void unknownFile() {
    String fileName = "dir" + File.separator + "xxx.ini";
    File file = new File(fileName);

    assertThatThrownBy(() -> FileTestUtils.getFile(file))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining(fileName);
  }

  @Test
  void directiveByName() {
    PhpIniFile phpIniFile = parse("\n  name1=value1 \n name2=value2 \n name1=value3");
    assertThat(phpIniFile.directivesForName("name1"))
      .extracting(Directive::value)
      .extracting(SyntaxToken::text)
      .containsOnly("value1", "value3");
    assertThat(phpIniFile.directivesForName("x")).isEmpty();
  }

  private static void checkToken(SyntaxToken token, String text, int startLine, int startColumn, int endLine, int endColumn) {
    assertThat(token.text()).isEqualTo(text);
    assertThat(token.line()).isEqualTo(startLine);
    assertThat(token.column()).isEqualTo(startColumn);
    assertThat(token.endLine()).isEqualTo(endLine);
    assertThat(token.endColumn()).isEqualTo(endColumn);
  }

  private static void checkSingleDirective(String toParse, String expectedName, String expectedValue) {
    PhpIniFile file = parse(toParse);
    assertThat(file.directives()).hasSize(1);
    Directive directive = file.directives().get(0);
    checkDirective(directive, expectedName, expectedValue);
  }

  private static PhpIniFile parse(String toParse) {
    return new PhpIniParser().parse(toParse);
  }

  private static void checkDirective(Directive directive, String expectedName, String expectedValue) {
    assertThat(directive.name().text()).isEqualTo(expectedName);
    assertThat(directive.value().text()).isEqualTo(expectedValue);
  }

}
