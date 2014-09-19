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
package org.sonar.php.lexer;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.impl.LexerException;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.php.PHPConfiguration;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

@Ignore
public class StringVariableNestedTest {

  private Lexer lexer = PHPLexer.create(new PHPConfiguration(Charsets.UTF_8));

  @Test
  public void test_simple_variable() {
    // Not explicitly delimited
    assertThat(lex("$foo[0]").size()).isEqualTo(6); // "$ foo [ 0 ] "
    lex("$foo[0]"); // "$ foo [ 0 ] "
    lex("$foo[identifier]");
    lex("$foo[$variable]");

    lex("$foo->prop");
    lex("$foo->prop1->prop2");

    lex("$foo->prop1[index]");

    // Explicitly delimited
    lex("${foo}");
    lex("${${foo}}");
    lex("${foo[0]}");
    assertThat(lex("${var[0]}->bar").size()).isEqualTo(6); // "${ var [ 0 ] }->bar"
  }

  @Test
  public void test_complex_variable() {
    lex("{$var}");
    lex("{${$var}}");
    lex("{$var[/* ... */ 42 - 2*21]}");
    lex("{${method()}}");
    lex("{$method()}");
    lex("{${'test'}}");
    lex("{$foo['}']}");
    lex("{${$foo}}");
  }

  @Test
  public void test_string_literal() {
    assertThat(lex("/regexp $/").size()).isEqualTo(1);
    assertThat(lex("non regexp $").size()).isEqualTo(1); // PHP is permissive
    assertThat(lex("str \\$foo").size()).isEqualTo(1);
    lex("{'str'}");

  }

  @Test(expected=LexerException.class)
  public void nok() {
    lex("$var[0/*...*/]");
    lex("$var[0.1]");
    lex("$var[-1]");
    lex("$var[\"foo\"]");
    lex("{$'str'}");
    lex("{$0}");

    lex("$var[test()]");
    lex("$var[{$test}]");
    lex("$var[$$test]");
  }

  public List<Token> lex(String expression) {
    return lexer.lex("<?php \"" + expression + "\"");
  }

}
