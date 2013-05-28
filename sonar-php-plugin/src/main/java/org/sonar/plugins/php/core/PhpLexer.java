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
package org.sonar.plugins.php.core;

import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.impl.channel.BlackHoleChannel;
import com.sonar.sslr.impl.channel.BomCharacterChannel;

import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.commentRegexp;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.regexp;
import static org.sonar.plugins.php.core.PhpLexerType.OTHER;
import static org.sonar.plugins.php.core.PhpLexerType.STRING;

/**
 * Very basic PHP lexer to find comments
 */
public final class PhpLexer {

  private PhpLexer() {
  }

  public static Lexer create(PhpParserConfiguration conf) {
    return Lexer.builder()
        .withFailIfNoChannelToConsumeOneCharacter(true)

        .withCharset(conf.getCharset())

        .withChannel(new BlackHoleChannel("\\s++"))
        .withChannel(commentRegexp("(?s)(?:#|//)(?:[^?\\r\\n]|\\?(?![\\>r\\n]))*+|/\\*(?:(?!\\*/).)*+\\*/"))
        .withChannel(regexp(STRING, "(?s)'(?:[^'\\\\]|\\\\'|\\\\\\\\|\\\\[^'\\\\])*+'|\"(?:[^\"\\\\]|\\\\\"|\\\\\\\\|\\\\[^\"\\\\])*+\""))
        .withChannel(
            regexp(STRING, "(?s)<<<['\"]?+((?:[_]|\\p{L}\\p{M}*+)(?:[_0-9]|\\p{L}\\p{M}*+)*+)['\"]?+(?=[\r\n])(?:(?![\r\n]\\1;?(?:[\r\n]|(?!.))).)*+[\r\n]\\1"))

        /* UTF-8 BOM */
        .withChannel(new BomCharacterChannel())

        .withChannel(regexp(OTHER, "."))

        .build();
  }
}
