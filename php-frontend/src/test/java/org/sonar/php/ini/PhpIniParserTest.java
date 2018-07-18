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
package org.sonar.php.ini;

import com.sonar.sslr.api.RecognitionException;
import java.io.File;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.php.FileTestUtils;
import org.sonar.php.ini.tree.Directive;
import org.sonar.php.ini.tree.PhpIniFile;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;

public class PhpIniParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void no_directive() throws Exception {
    assertThat(parse("").directives()).isEmpty();
    assertThat(parse("   \n  ").directives()).isEmpty();
  }

  @Test
  public void section() throws Exception {
    assertThat(parse("[section1]").directives()).isEmpty();
    assertThat(parse("  [section1] ").directives()).isEmpty();
    checkSingleDirective("[section1]\n name1=value1\n [section2]", "name1", "value1");
  }

  @Test
  public void simple_directive() throws Exception {
    checkSingleDirective("name1=value1", "name1", "value1");
  }

  @Test
  public void empty_value() throws Exception {
    checkSingleDirective("name1=", "name1", "");
  }

  @Test
  public void two_directives() throws Exception {
    List<Directive> directives = parse("name1=value1\nname2=value2").directives();
    assertThat(directives).hasSize(2);
    checkDirective(directives.get(0), "name1", "value1");
    checkDirective(directives.get(1), "name2", "value2");
  }

  @Test
  public void full_line_comment() throws Exception {
    checkSingleDirective(";comment1\nname1=value1\n;comment2", "name1", "value1");
  }

  @Test
  public void end_of_line_comment() throws Exception {
    checkSingleDirective("name1=value1;comment1", "name1", "value1");
  }

  @Test
  public void whitespaces() throws Exception {
    checkSingleDirective(" \t  name1 \t = \t   value1  ", "name1", "value1");
  }

  @Test
  public void string_value() throws Exception {
    checkSingleDirective("name1=\"value1\"", "name1", "\"value1\"");
  }

  @Test
  public void string_value_containing_special_char() throws Exception {
    checkSingleDirective("name1=\"value1;value2\"", "name1", "\"value1;value2\"");
    checkSingleDirective("name1=\"value1=value2\"", "name1", "\"value1=value2\"");
  }

  @Test
  public void string_value_containing_escaped_quotes() throws Exception {
    checkSingleDirective("name1=\"aa\\\"bb;\\\"cc\"", "name1", "\"aa\\\"bb;\\\"cc\"");
  }

  @Test
  public void numeric_value() throws Exception {
    checkSingleDirective("name1=42", "name1", "42");
    checkSingleDirective("name1=42.", "name1", "42.");
    checkSingleDirective("name1=4.2", "name1", "4.2");
    checkSingleDirective("name1=.42", "name1", ".42");
  }

  @Test
  public void expressions() throws Exception {
    checkSingleDirective("name1=E_ALL & ~E_DEPRECATED & ~E_STRICT", "name1", "E_ALL & ~E_DEPRECATED & ~E_STRICT");
  }

  @Test
  public void no_equal_sign() {
    assertThat(parse("xxx").directives()).isEmpty();
  }

  @Test
  public void empty_name() throws Exception {
    thrown.expect(RecognitionException.class);
    parse("=value1");
  }

  @Test
  public void blank_name() throws Exception {
    thrown.expect(RecognitionException.class);
    parse(" =value1");
  }

  @Test
  public void more_than_one_equal_sign() throws Exception {
    thrown.expect(RecognitionException.class);
    parse("name1=value1=1");
  }

  @Test
  public void tokens() throws Exception {
    Directive directive = parse("\n  name1=value1").directives().get(0);
    checkToken(directive.name(), "name1", 2, 3, 2, 8);
    checkToken(directive.equalSign(), "=", 2, 8, 2, 9);
    checkToken(directive.value(), "value1", 2, 9, 2, 15);
  }

  @Test
  public void parse_file() throws Exception {
    PhpFile file = FileTestUtils.getFile(new File("src/test/resources/phpini/php.ini"));
    PhpIniFile phpIni = new PhpIniParser().parse(file);
    assertThat(phpIni.directives()).hasSize(1);
    assertThat(phpIni.directives().get(0).name().text()).isEqualTo("max_execution_time");
  }

  @Test
  public void unknown_file() throws Exception {
    PhpIniParser parser = new PhpIniParser();
    String fileName = "dir" + File.separator + "xxx.ini";
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage(fileName);
    PhpFile file = FileTestUtils.getFile(new File(fileName));
    parser.parse(file);
  }

  @Test
  public void directive_by_name() {
    PhpIniFile phpIniFile = parse("\n  name1=value1 \n name2=value2 \n name1=value3");
    assertThat(phpIniFile.directivesForName("name1"))
      .extracting(Directive::value)
      .extracting(SyntaxToken::text)
      .containsOnly("value1", "value3");
    assertThat(phpIniFile.directivesForName("x")).isEmpty();
  }

  private void checkToken(SyntaxToken token, String text, int startLine, int startColumn, int endLine, int endColumn) {
    assertThat(token.text()).isEqualTo(text);
    assertThat(token.line()).isEqualTo(startLine);
    assertThat(token.column()).isEqualTo(startColumn);
    assertThat(token.endLine()).isEqualTo(endLine);
    assertThat(token.endColumn()).isEqualTo(endColumn);
  }

  private void checkSingleDirective(String toParse, String expectedName, String expectedValue) {
    PhpIniFile file = parse(toParse);
    assertThat(file.directives()).hasSize(1);
    Directive directive = file.directives().get(0);
    checkDirective(directive, expectedName, expectedValue);
  }

  private PhpIniFile parse(String toParse) {
    return new PhpIniParser().parse(toParse);
  }

  private void checkDirective(Directive directive, String expectedName, String expectedValue) {
    assertThat(directive.name().text()).isEqualTo(expectedName);
    assertThat(directive.value().text()).isEqualTo(expectedValue);
  }

}
