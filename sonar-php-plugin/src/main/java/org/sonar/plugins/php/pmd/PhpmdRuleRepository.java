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
package org.sonar.plugins.php.pmd;

import java.io.InputStream;

import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.core.PhpRuleRepository;

public final class PhpmdRuleRepository extends PhpRuleRepository {

  public static final String PHPMD_REPOSITORY_KEY = "phppmd_rules";
  public static final String PHPMD_REPOSITORY_NAME = "PHPMD";
  // for user extensions
  private ServerFileSystem fileSystem;
  private XMLRuleParser parser;

  /**
   * @param fileSystem
   * @param parser
   */
  public PhpmdRuleRepository(ServerFileSystem fileSystem, XMLRuleParser parser) {
    super(PHPMD_REPOSITORY_KEY, Php.KEY);
    setName(PHPMD_REPOSITORY_NAME);
    this.fileSystem = fileSystem;
    this.parser = parser;
  }

  /**
   * @see org.sonar.plugins.php.core.PmdStyleRuleRepository#getRuleInputStream()
   */
  protected InputStream getRuleInputStream() {
    return getClass().getResourceAsStream("/org/sonar/plugins/php/pmd/rules.xml");
  }

  /**
   * @return the fileSystem
   */
  public ServerFileSystem getFileSystem() {
    return fileSystem;
  }

  /**
   * @return the parser
   */
  public XMLRuleParser getParser() {
    return parser;
  }

  /**
   * @see org.sonar.plugins.php.core.PmdStyleRuleRepository#getRepositoryKey()
   */
  @Override
  protected String getRepositoryKey() {
    return PHPMD_REPOSITORY_KEY;
  }

}
