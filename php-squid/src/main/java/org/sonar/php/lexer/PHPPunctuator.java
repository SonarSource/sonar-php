/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.TokenType;

public enum PHPPunctuator implements TokenType {

  ANDEQUAL("&="),
  CONCATEQUAL(".="),
  DIVEQUAL("/="),
  EQU("="),
  EQUAL("=="),
  NOTEQUAL("!="),
  NOTEQUALBIS("<>"),
  EQUAL2("==="),
  NOTEQUAL2("!=="),
  LT("<"),
  GT(">"),
  LE("<="),
  GE(">="),
  STAR_EQU("*="),
  MOD_EQU("%="),
  MINUS_EQU("-="),
  OR_EQU("|="),
  PLUS_EQU("+="),
  SL_EQU("<<="),
  SR_EQU(">>="),
  SL("<<"),
  SR(">>"),
  XOR_EQU("^="),

  PLUS("+"),
  MINUS("-"),

  XOR("^"),
  STAR("*"),
  MOD("%"),
  DIV("/"),
  INC("++"),
  DEC("--"),
  ANDAND("&&"),
  AND("&"),
  OROR("||"),
  OR("|"),

  ARROW("->"),
  DOUBLEARROW("=>"),

  DOLAR_LCURLY("${"),
  LCURLYBRACE("{"),
  RCURLYBRACE("}"),
  LPARENTHESIS("("),
  RPARENTHESIS(")"),
  LBRACKET("["),
  RBRACKET("]"),
  DOT("."),
  SEMICOLON(";"),
  COMMA(","),

  AT("@"),
  BANG("!"),
  QUERY("?"),
  COLON(":"),
  DOUBLECOLON("::"),
  NS_SEPARATOR("\\");

  private final String value;

  private PHPPunctuator(String value) {
    this.value = value;
  }
  @Override
  public boolean hasToBeSkippedFromAst(AstNode astNode) {
    return false;
  }

  @Override
  public String getName() {
    return name();
  }


  @Override
  public String getValue() {
    return value;
  }

  public static String[] getPunctuatorValues() {
    PHPKeyword[] keywordsEnum = PHPKeyword.values();
    String[] keywords = new String[keywordsEnum.length];
    for (int i = 0; i < keywords.length; i++) {
      keywords[i] = keywordsEnum[i].getValue();
    }
    return keywords;
  }
}
