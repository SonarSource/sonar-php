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
package org.sonar.php.parser.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class MethodDeclarationTest {

  @Test
  void test() throws Exception {
    assertThat(PHPLexicalGrammar.METHOD_DECLARATION)
      .matches("function f ();")
      .matches("function f () {}")
      .matches("function &f () {}")
      .matches("private function f () {}")
      .matches("protected abstract function f () {}")
      .matches("public static function f () {}")
      .matches("final function f () {}")
      .matches("function f () : bool {}")
      .matches("function f () : ?bool {}")
      .matches("function if() {}")
      .matches("function match() {}")
      .matches("#[A1(4)] public function f() {}")
      .matches("public function f($prop) {}")
      .matches("public function f($prop = null) {}")
      .matches("public function f($prop = new Foo()) {}")
      .matches("public function f($p { get; }) {}");
  }

  @Test
  void construct() {
    assertThat(PHPLexicalGrammar.METHOD_DECLARATION)
      .matches("public function __construct(private $prop) {}")
      .matches("public function __construct(public $prop) {}")
      .matches("public function __construct(protected $prop) {}")
      .matches("public function __construct(protected readonly $prop) {}")
      .matches("public function __construct(protected readonly string $prop) {}")
      .matches("public function __construct(readonly protected $prop) {}")
      .matches("public function __construct(readonly public protected(set) $prop) {}")
      .matches("public function __construct(protected(set) $prop) {}")
      .matches("public function __construct(readonly protected string $prop) {}")
      .matches("public function __construct(readonly $prop) {}")
      .matches("public function __construct(public $p { get; }) {}")
      .matches("public function __construct(protected final bool $a = true) {}")
      .matches("public function __construct(final protected bool $b = true) {}")
      .matches("public function __construct(final $i) {}")
      .notMatches("public function __construct(var $prop) {}");
  }

  @Test
  void optionalSemicolon() {
    assertThat(PHPLexicalGrammar.METHOD_DECLARATION)
      .matches("function fun() ?>")
      .notMatches("function fun() ?> <?php {}");
  }

}
