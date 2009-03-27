/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
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

package org.sonar.plugins.php.phpcodesniffer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.commons.Language;
import org.sonar.commons.rules.ActiveRule;
import org.sonar.commons.rules.Rule;
import org.sonar.commons.rules.RulesProfile;
import org.sonar.plugins.api.rules.RulesRepository;
import org.sonar.plugins.api.rules.StandardProfileXmlParser;
import org.sonar.plugins.api.rules.StandardRulesXmlParser;
import org.sonar.plugins.php.Php;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PhpCodeSnifferRulesRepository implements RulesRepository {

  public Language getLanguage() {
    return new Php();
  }

  public List<Rule> getInitialReferential() {
    InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/phpcodesniffer/rules.xml");
    try {
      return new StandardRulesXmlParser().parse(input);
    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  public List<Rule> parseReferential(String fileContent) {
    return new StandardRulesXmlParser().parse(fileContent);
  }

  public List<RulesProfile> getProvidedProfiles() {
    return Arrays.asList(
      loadProvidedProfile("/org/sonar/plugins/php/phpcodesniffer/profile-default-php.xml"));
  }

  protected RulesProfile loadProvidedProfile(String filename) {
    try {
      InputStream profile = getClass().getResourceAsStream(filename);
      StandardProfileXmlParser standardProfileXmlParser = new StandardProfileXmlParser(getInitialReferential());
      return standardProfileXmlParser.importConfiguration(IOUtils.toString(profile));

    } catch (IOException e) {
      throw new RuntimeException("Configuration file not found for the file : " + filename, e);
    }
  }

  public String exportConfiguration(RulesProfile activeProfile, String cleanProfileName) {
    try {
      return getConfigurationFromActiveRules(cleanProfileName, activeProfile.getActiveRulesByPlugin(Php.KEY));
    } catch (IOException e) {
      throw new PhpCodeSnifferExecutionException(e);
    }
  }

  protected String getConfigurationFromActiveRules(String profileName, List<ActiveRule> activeRules) throws IOException {
      String configuration = getTemplateConfiguration();
      configuration = configuration.replace("$(PROFILE)", profileName);
      configuration = configuration.replace("$(RULES)", getPhpRules(activeRules));
      return configuration;
  }

  protected String getTemplateConfiguration() throws IOException {
      InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/phpcodesniffer/skeleton-profile.php");
      return IOUtils.toString(input, "UTF-8");
  }

  protected String getPhpRules(List<ActiveRule> activeRules) {
    Collection listOfConfigKey = CollectionUtils.collect(activeRules, new Transformer(){
      public Object transform(Object o) {
        ActiveRule activeRule = (ActiveRule) o;
        return activeRule.getRule().getConfigKey();
      }
    });
    if (!listOfConfigKey.isEmpty()){
      return "'"+ StringUtils.join(listOfConfigKey, "','") +"'";
    }
    return "";
  }

}
