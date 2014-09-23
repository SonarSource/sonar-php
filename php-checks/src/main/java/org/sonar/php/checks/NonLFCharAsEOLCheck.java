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
package org.sonar.php.checks;

import com.google.common.io.Files;
import com.sonar.sslr.api.AstNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.CharsetAwareVisitor;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;

@Rule(
  key = "S1779",
  priority = Priority.MINOR)
public class NonLFCharAsEOLCheck extends SquidCheck<LexerlessGrammar> implements CharsetAwareVisitor {

  private static final Logger LOG = LoggerFactory.getLogger(NonLFCharAsEOLCheck.class);
  private Charset charset;

  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void visitFile(AstNode astNode) {
    try {
      BufferedReader reader = Files.newReader(getContext().getFile(), charset);

      int c;
      while ((c = reader.read()) != -1) {

        if (c == '\r' || c == '\u2028' || c == '\u2029') {
          getContext().createFileViolation(this, "Replace all non line feed end of line characters in this file \"{0}\" by LF.",
            getContext().getFile().getName());
          break;
        }
      }
    } catch (IOException e) {
      LOG.error("Unable to process check S1779 on file: {}", getContext().getFile().getName(), e);
    }
  }
}
