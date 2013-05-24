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
package org.sonar.plugins.php.codesniffer;

import org.sonar.api.config.Settings;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.plugins.php.api.PhpConstants;
import org.sonar.plugins.php.core.PhpRuleRepository;

import java.io.InputStream;

import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_CUSTOM_RULES_PROP_KEY;

/**
 * @author Akram Ben Aissi
 * 
 */
public final class PhpCodeSnifferRuleRepository extends PhpRuleRepository {

  public static final String PHPCS_REPOSITORY_KEY = "php_codesniffer_rules";
  public static final String PHPCS_REPOSITORY_NAME = "PHP CodeSniffer";
  // for user extensions
  private ServerFileSystem fileSystem;
  private XMLRuleParser parser;

  public PhpCodeSnifferRuleRepository(ServerFileSystem fileSystem, XMLRuleParser parser, Settings settings) {
    super(PHPCS_REPOSITORY_KEY, PhpConstants.LANGUAGE_KEY, settings);
    setName(PHPCS_REPOSITORY_NAME);
    this.fileSystem = fileSystem;
    this.parser = parser;
  }

  /**
   * @see org.sonar.plugins.php.core.PmdStyleRuleRepository#getRuleInputStream()
   */
  protected InputStream getRuleInputStream() {
    return getClass().getResourceAsStream("/org/sonar/plugins/php/codesniffer/rules.xml");
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
    return PHPCS_REPOSITORY_KEY;
  }

  @Override
  protected String getCustomRulePropertyKey() {
    return PHPCS_CUSTOM_RULES_PROP_KEY;
  }

}
