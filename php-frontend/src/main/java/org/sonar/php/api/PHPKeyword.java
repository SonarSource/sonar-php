/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
  FN("fn"),
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
  MATCH("match"),
  NAMESPACE("namespace"),
  NEW("new"),
  OR("or"),
  PRINT("print"),
  PRIVATE("private"),
  PROTECTED("protected"),
  PUBLIC("public"),
  READONLY("readonly"),
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
