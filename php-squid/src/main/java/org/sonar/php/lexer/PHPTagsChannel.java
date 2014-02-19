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

import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.impl.channel.BlackHoleChannel;
import org.sonar.channel.Channel;
import org.sonar.channel.CodeReader;

public class PHPTagsChannel extends Channel<Lexer> {

  private static final String OPENING = "<\\?(php|=|)";
  private static final String CLOSING = "\\?>";

  private static final String START = "(?:(?!" + OPENING + ")[\\s\\S])*+(" + OPENING + ")?+";
  private static final String END = CLOSING + START;

  private final Channel<Lexer> start = new BlackHoleChannel(START);
  private final Channel<Lexer> end = new BlackHoleChannel(END);

  @Override
  public boolean consume(CodeReader code, Lexer lexer) {
    if ((code.getLinePosition() == 1) && (code.getColumnPosition() == 0)) {
      return start.consume(code, lexer);
    } else {
      return end.consume(code, lexer);
    }
  }

}
