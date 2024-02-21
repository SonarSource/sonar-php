/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.parser.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class FunctionDeclarationTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.FUNCTION_DECLARATION)
      .matches("function f() {}")
      .matches("function &f() {}")
      .matches("function f() : bool {}")
      .matches("function f() : ?bool {}")
      .matches("function f() : object {}")
      .matches("#[A1(8)] function f() {}")
      .matches("function f($prop) {}")
      .matches("function f($prop = null) {}")
      .matches("function f($prop = new Foo()) {}")
      .matches("function f(A&B $prop): A&B {}")
      .matches("function f(A|B $prop): A|B {}")
      // readonly is a keyword, but it can be used as a function name
      .matches("function readonly() {}")
      .matches("function READONLY() {}")
      .matches("function f(var\\foo $prop) {}")
      .notMatches("function f(var $prop) {}")
      .notMatches("function ABSTRACT() {}")
      .notMatches("function __HALT_COMPILER() {}")
      .notMatches("function abstract() {}")
      .notMatches("function __halt_compiler() {}");
  }
}
