/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 Akram Ben Aissi
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.cpd;

import java.io.File;
import java.util.List;

import net.sourceforge.pmd.cpd.Tokenizer;

import org.sonar.api.batch.AbstractCpdMapping;
import org.sonar.api.resources.Language;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.resources.PhpFile;

/**
 * The Class PhpCpdMapping.
 */
public class PhpCpdMapping extends AbstractCpdMapping {

  /** The php. */
  private Php php;

  /**
   * Instantiates a new php cpd mapping.
   * 
   * @param php
   *          the php
   */
  public PhpCpdMapping(Php php) {
    this.php = php;
  }

  /**
   * Creates the resource.
   * 
   * @param file
   *          the file
   * @param sourceDirs
   *          the source dirs
   * @return the php file
   * @see org.sonar.api.batch.AbstractCpdMapping#createResource(java.io.File, java.util.List)
   */
  @Override
  public PhpFile createResource(File file, List<File> sourceDirs) {
    return PhpFile.fromIOFile(file, sourceDirs, false);
  }

  /**
   * Gets the language.
   * 
   * @return the language
   * @see org.sonar.api.batch.CpdMapping#getLanguage()
   */
  public Language getLanguage() {
    return php;
  }

  /**
   * Gets the tokenizer.
   * 
   * @return the tokenizer
   * @see org.sonar.api.batch.CpdMapping#getTokenizer()
   */
  public Tokenizer getTokenizer() {
    return new org.sonar.plugins.php.cpd.PHPTokenizer();
  }

}
