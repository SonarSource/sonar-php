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
package org.sonar.php.api;

import org.sonar.sslr.grammar.GrammarRuleKey;

public enum PHPKeyword implements GrammarRuleKey {

  HALT_COMPILER("__halt_compiler"),
  ABSTRACT("abstract"),
  AND("and"),
  ARRAY("array"),
  AS("as"),
  BREAK("break"),
  CALLABLE("callable"),
  CASE("case"),
  CATCH("catch"),
  CLASS("class"),
  CLONE("clone"),
  CONST("const"),
  CONTINUE("continue"),
  DECLARE("declare"),
  DEFAULT("default"),
  DIE("die"),
  DO("do"),
  ECHO("echo"),
  ELSEIF("elseif"),
  ELSE("else"),
  EMPTY("empty"),
  ENDDECLARE("enddeclare"),
  ENDFOREACH("endforeach"),
  ENDFOR("endfor"),
  ENDIF("endif"),
  ENDSWITCH("endswitch"),
  ENDWHILE("endwhile"),
  EVAL("eval"),
  EXIT("exit"),
  EXTENDS("extends"),
  FINALLY("finally"),
  FINAL("final"),
  FOREACH("foreach"),
  FOR("for"),
  FUNCTION("function"),
  GLOBAL("global"),
  GOTO("goto"),
  IF("if"),
  IMPLEMENTS("implements"),
  INCLUDE_ONCE("include_once"),
  INCLUDE("include"),
  INSTANCEOF("instanceof"),
  INSTEADOF("insteadof"),
  INTERFACE("interface"),
  ISSET("isset"),
  LIST("list"),
  NAMESPACE("namespace"),
  NEW("new"),
  OR("or"),
  PRINT("print"),
  PRIVATE("private"),
  PROTECTED("protected"),
  PUBLIC("public"),
  REQUIRE_ONCE("require_once"),
  REQUIRE("require"),
  RETURN("return"),
  STATIC("static"),
  SWITCH("switch"),
  THROW("throw"),
  TRAIT("trait"),
  TRY("try"),
  UNSET("unset"),
  USE("use"),
  VAR("var"),
  WHILE("while"),
  XOR("xor"),
  YIELD("yield");

  private final String value;

  PHPKeyword(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static String[] getKeywordValues() {
    PHPKeyword[] keywordsEnum = PHPKeyword.values();
    String[] keywords = new String[keywordsEnum.length];
    for (int i = 0; i < keywords.length; i++) {
      keywords[i] = keywordsEnum[i].getValue();
    }
    return keywords;
  }

}
