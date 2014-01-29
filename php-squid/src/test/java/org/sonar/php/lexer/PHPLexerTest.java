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

import com.google.common.base.Charsets;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class PHPLexerTest {

  @Test
  public void testLexPHPSourceCode() throws FileNotFoundException {
    Lexer lexer = PHPLexer.create(Charsets.UTF_8);
    List<Token> tokens = lexer.lex(FileUtils.toFile(getClass().getResource("/lexer/melting-pot-for-lexing.php")));

    assertThat(tokens.size()).isEqualTo(126);

    Token mail = tokens.get(5);
    assertThat(mail.getOriginalValue()).isEqualTo("Mail");
    assertThat(mail.getType()).isEqualTo(GenericTokenType.IDENTIFIER);

    Token charsetVar = tokens.get(10);
    assertThat(charsetVar.getOriginalValue()).isEqualTo("$_charset");
    assertThat(charsetVar.getType()).isEqualTo(PHPTokenType.VAR_IDENTIFIER);

    Token exponent = tokens.get(27);
    assertThat(exponent.getOriginalValue()).isEqualTo("7E-10");
    assertThat(exponent.getType()).isEqualTo(GenericTokenType.LITERAL);

  }
}
