/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.php.duplications;

import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;
import org.apache.commons.io.IOUtils;
import org.sonar.api.batch.AbstractCpdMapping;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;
import org.sonar.plugins.php.api.Php;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

/**
 * Temporary PHP CPD engine mapping class, used until we can migrate to Sonar CPD Engine.
 */
public class PhpCPDMapping extends AbstractCpdMapping {

  private Php php;
  private FileSystem fileSystem;

  /**
   * Creates a {@link PhpCPDMapping} object
   *
   * @param php
   * @param project
   */
  public PhpCPDMapping(Php php, Project project, FileSystem fileSystem) {
    this.php = php;
    this.fileSystem = fileSystem;
  }

  /**
   * Returns the language
   *
   * @return the language
   */
  @Override
  public Language getLanguage() {
    return php;
  }

  /**
   * Returns the tokenizer
   *
   * @return the tokenizer
   */
  @Override
  public Tokenizer getTokenizer() {
    return new PHPCPDTokenizer();
  }

  class PHPCPDTokenizer implements Tokenizer {

    private TokenChunker tokenChunker;
    private static final String USE_KEYWORD = "use";
    private static final String SEMI_COLON = ";";

    /**
     * Creates a {@link {@link PHPCPDTokenizer}
     */
    public PHPCPDTokenizer() {
      this.tokenChunker = PhpTokenProducer.build();
    }

    /**
     * Cuts the given source into a list of tokens.
     */
    @Override
    public final void tokenize(SourceCode source, Tokens cpdTokens) {
      String fileName = source.getFileName();

      Reader reader = null;
      try {
        reader = new InputStreamReader(new FileInputStream(fileName), fileSystem.encoding());
        TokenQueue queue = tokenChunker.chunk(reader);

        Iterator<Token> iterator = queue.iterator();
        // we currently use this hack to remove "use" directives
        boolean useDirective = false;
        while (iterator.hasNext()) {
          Token token = (Token) iterator.next();
          if (token.getValue().equalsIgnoreCase(USE_KEYWORD)) {
            useDirective = true;
          } else if (useDirective) {
            // We do nothing as we want to ignore "use" directives
            if (token.getValue().equalsIgnoreCase(SEMI_COLON)) {
              useDirective = false;
            }
          } else {
            cpdTokens.add(new TokenEntry(token.getValue(), fileName, token.getLine()));
          }
        }
      } catch (FileNotFoundException e) {
        throw new SonarException(e);
      } finally {
        IOUtils.closeQuietly(reader);
      }

      cpdTokens.add(TokenEntry.getEOF());
    }
  }

}
