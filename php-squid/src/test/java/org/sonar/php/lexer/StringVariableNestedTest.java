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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.php.parser.RuleTest;

@Ignore
public class StringVariableNestedTest extends RuleTest {

  @Before
  public void setUp() throws Exception {
    // TODO: set root rule;
  }

  @Test
  public void test_simple_variable() {
    // Not explicitly delimited
    matches("$foo[0]");// "$ foo [ 0 ] "
    matches("$foo[0]"); // "$ foo [ 0 ] "
    matches("$foo[identifier]");
    matches("$foo[$variable]");

    matches("$foo->prop");
    matches("$foo->prop1->prop2");

    matches("$foo->prop1[index]");

    // Explicitly delimited
    matches("${foo}");
    matches("${${foo}}");
    matches("${foo[0]}");
    matches("${var[0]}->bar"); // "${ var [ 0 ] }->bar"
  }

  @Test
  public void test_compmatches_variable() {
    matches("{$var}");
    matches("{${$var}}");
    matches("{$var[/* ... */ 42 - 2*21]}");
    matches("{${method()}}");
    matches("{$method()}");
    matches("{${'test'}}");
    matches("{$foo['}']}");
    matches("{${$foo}}");
  }

  @Test
  public void test_string_literal() {
    matches("/regexp $/");
    matches("non regexp $"); // PHP is permissive
    matches("str \\$foo");
    matches("{'str'}");
  }

  public void nok() {
    notMatches("$var[0/*...*/]");
    notMatches("$var[0.1]");
    notMatches("$var[-1]");
    notMatches("$var[\"foo\"]");
    notMatches("{$'str'}");
    notMatches("{$0}");

    notMatches("$var[test()]");
    notMatches("$var[{$test}]");
    notMatches("$var[$$test]");
  }

}
