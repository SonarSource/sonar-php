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
/**
 * 
 */
package org.sonar.plugins.php.core;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Akram Ben Aissi
 * 
 */
public abstract class PhpRuleRepository extends RuleRepository {

  private final Settings settings;

  /**
   * @param key
   * @param language
   */
  protected PhpRuleRepository(String key, String language, Settings settings) {
    super(key, language);
    this.settings = settings;
  }

  /**
   * @see org.sonar.api.rules.RuleRepository#createRules()
   */
  @Override
  public List<Rule> createRules() {
    List<Rule> rules = new ArrayList<Rule>();
    rules.addAll(getParser().parse(getRuleInputStream()));

    // Custom rules:
    // - old fashion: XML files in the file system
    for (File userExtensionXml : getFileSystem().getExtensions(getRepositoryKey(), "xml")) {
      rules.addAll(getParser().parse(userExtensionXml));
    }

    // - new fashion: through the Web interface
    String customRules = settings.getString(getCustomRulePropertyKey());
    if (StringUtils.isNotBlank(customRules)) {
      rules.addAll(getParser().parse(new StringReader(customRules)));
    }
    return rules;
  }

  protected abstract ServerFileSystem getFileSystem();

  protected abstract XMLRuleParser getParser();

  protected abstract String getRepositoryKey();

  protected abstract String getCustomRulePropertyKey();

  /**
   * @return an input stream pointing to a rules.xml file.
   */
  protected abstract InputStream getRuleInputStream();
}
