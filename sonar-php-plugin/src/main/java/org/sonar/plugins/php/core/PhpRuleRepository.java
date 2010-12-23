/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi or Jerome Tama or Frederic Leroy
 * mailto: akram.benaissi@free.fr or jerome.tama@codehaus.org
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

/**
 * 
 */
package org.sonar.plugins.php.core;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;

/**
 * @author Akram Ben Aissi
 * 
 */
public abstract class PhpRuleRepository extends RuleRepository {

  /**
   * @param key
   * @param language
   */
  protected PhpRuleRepository(String key, String language) {
    super(key, language);
  }

  /**
   * @see org.sonar.api.rules.RuleRepository#createRules()
   */
  @Override
  public List<Rule> createRules() {
    List<Rule> rules = new ArrayList<Rule>();
    rules.addAll(getParser().parse(getRuleInputStream()));
    for (File userExtensionXml : getFileSystem().getExtensions(getRepositoryKey(), "xml")) {
      rules.addAll(getParser().parse(userExtensionXml));
    }
    return rules;
  }

  protected abstract ServerFileSystem getFileSystem();

  protected abstract XMLRuleParser getParser();

  protected abstract String getRepositoryKey();

  /**
   * @return an input stream pointing to a rules.xml file.
   */
  protected abstract InputStream getRuleInputStream();
}
