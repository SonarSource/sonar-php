/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
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

package org.sonar.plugins.php.codesniffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.rules.ConfigurationExportable;
import org.sonar.api.rules.ConfigurationImportable;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleParam;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RulesRepository;
import org.sonar.api.rules.StandardRulesXmlParser;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.checkstyle.CheckstyleRulePriorityMapper;
import org.sonar.plugins.checkstyle.xml.Module;
import org.sonar.plugins.checkstyle.xml.Property;
import org.sonar.plugins.php.core.Php;

/**
 * The Class PhpCheckstyleRulesRepository.
 */
public final class PhpCodesnifferRulesRepository implements RulesRepository<Php>, ConfigurationExportable, ConfigurationImportable {

  /** The ruleset file name */
  private static final String RULESET_FILE_NAME = "rules.xml";

  /** The priority mapper. */
  private CheckstyleRulePriorityMapper priorityMapper = new CheckstyleRulePriorityMapper();

  /**
   * Returns an instance of PHP language.
   * 
   * @see org.sonar.api.rules.RulesRepository#getLanguage()
   * @return a PHP instance
   */
  public Php getLanguage() {
    return Php.INSTANCE;
  }

  /**
   * Returns the list of PHP_CodeSniffer rules declared by the ruleset
   * 
   * @see org.sonar.api.rules.RulesRepository#getInitialReferential()
   * @return the list of PHP_CodeSniffer rules declared by the ruleset
   */
  public List<Rule> getInitialReferential() {
    // If ruleset can't be found throws an exception
    InputStream input = getClass().getResourceAsStream(RULESET_FILE_NAME);
    if (input == null) {
      throw new SonarException("Resource not found : " + RULESET_FILE_NAME);
    }
    // else
    try {
      return new StandardRulesXmlParser().parse(input);
    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  /**
   * Gets a list of rules from the given String (representing a Ruleset)
   * 
   * @see org.sonar.api.rules.RulesRepository#parseReferential(java.lang.String)
   * @return a list of rules from the given String (representing a Ruleset)
   */
  public List<Rule> parseReferential(String fileContent) {
    return new StandardRulesXmlParser().parse(fileContent);
  }

  /**
   * Gets the provided profiles.
   * 
   * @return the provided rules profiles
   * @see org.sonar.api.rules.RulesRepository#getProvidedProfiles()
   */
  public List<RulesProfile> getProvidedProfiles() {
    List<RulesProfile> profiles = new ArrayList<RulesProfile>();
    profiles.add(buildProfile("Default Php Profile", "profile-php.xml"));
    return profiles;
  }

  /**
   * Builds the profile from an xml file.
   * 
   * @param name
   *          the name
   * @param path
   *          the path
   * @return the rules profile
   */
  public RulesProfile buildProfile(String name, String path) {
    InputStream input = null;
    try {
      input = getClass().getResourceAsStream(path);
      RulesProfile profile = new RulesProfile(name, Php.KEY);
      List<ActiveRule> activeRules = importConfiguration(IOUtils.toString(input, "UTF-8"), getInitialReferential());
      profile.setActiveRules(activeRules);
      return profile;
    } catch (IOException e) {
      throw new SonarException("Configuration file not found for the profile : " + name, e);
    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  /**
   * Export the configuration to an xml String.
   * 
   * @param activeProfile
   *          the active profile
   * @return the configuration to an xml String
   * @see org.sonar.api.rules.ConfigurationExportable#exportConfiguration(org.sonar.api.profiles.RulesProfile)
   */
  public String exportConfiguration(RulesProfile activeProfile) {
    Module module = toXStream(activeProfile.getActiveRulesByPlugin(PhpCodesnifferPlugin.KEY));
    module.getOrCreateChild("TreeWalker" + Module.MODULE_SEPARATOR + "FileContentsHolder");
    module.getOrCreateChild("SuppressionCommentFilter");
    return addXmlHeader(module.toXml());
  }

  /**
   * Import configuration.
   * 
   * @param xml
   *          the xml
   * @param rules
   *          the rules
   * @return the list< active rule>
   * @see org.sonar.api.rules.ConfigurationImportable#importConfiguration(java.lang.String, java.util.List)
   */
  public List<ActiveRule> importConfiguration(String xml, List<Rule> rules) {
    List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
    Module module = Module.fromXml(xml);
    buildActiveRulesFromXStream(module, "", activeRules, rules);
    return activeRules;
  }

  /**
   * To x stream.
   * 
   * @param activeRules
   *          the active rules
   * 
   * @return the module
   */
  protected Module toXStream(List<ActiveRule> activeRules) {
    Module root = new Module("");

    for (ActiveRule activeRule : activeRules) {
      if (activeRule.getRule().getPluginName().equals(PhpCodesnifferPlugin.KEY)) {
        String configKey = activeRule.getRule().getConfigKey();
        Module module = root.getOrCreateChild(configKey);
        List<Property> properties = new ArrayList<Property>();
        properties.add(new Property("severity", priorityMapper.to(activeRule.getPriority())));
        for (ActiveRuleParam activeRuleParam : activeRule.getActiveRuleParams()) {
          if (activeRuleParam.getValue() != null && activeRuleParam.getValue().length() != 0) {
            properties.add(new Property(activeRuleParam.getRuleParam().getKey(), activeRuleParam.getValue()));
          }
        }
        module.setProperties(properties);
      }
    }
    if (root.getChildren().isEmpty()) {
      return new Module("Checker", null);
    }
    return root.getChildren().get(0);
  }

  /**
   * Adds the xml header.
   * 
   * @param xml
   *          the xml
   * 
   * @return the string
   */
  protected String addXmlHeader(String xml) {
    StringBuilder sb = new StringBuilder().append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sb.append("<!DOCTYPE module PUBLIC \"-//Puppy Crawl//DTD Check Configuration ");
    sb.append("1.2//EN\" \"http://www.puppycrawl.com/dtds/configuration_1_2.dtd\"><!-- generated by Sonar -->\n");
    sb.append(xml);
    return sb.toString();
  }

  /**
   * Builds the active rules from x stream.
   * 
   * @param module
   *          the module
   * @param modulePath
   *          the module path
   * @param activeRules
   *          the active rules
   * @param rules
   *          the rules
   */
  protected void buildActiveRulesFromXStream(Module module, String modulePath, List<ActiveRule> activeRules, List<Rule> rules) {
    if (module.getChildren() == null || module.getChildren().isEmpty()) {
      for (Rule rule : rules) {
        if (rule.getConfigKey().equals(modulePath)) {
          RulePriority rulePriority = getRulePriority(module);
          ActiveRule activeRule = new ActiveRule(null, rule, rulePriority);
          activeRule.setActiveRuleParams(getActiveRuleParams(module, rule, activeRule));
          activeRules.add(activeRule);
          return;

        }
      }
    } else {
      String baseModulePath = modulePath.length() == 0 ? module.getName() + Module.MODULE_SEPARATOR : modulePath + Module.MODULE_SEPARATOR;
      for (Module child : module.getChildren()) {
        buildActiveRulesFromXStream(child, baseModulePath + child.getName(), activeRules, rules);
      }
    }
  }

  /**
   * Gets the rule priority.
   * 
   * @param module
   *          the module
   * 
   * @return the rule priority
   */
  private RulePriority getRulePriority(Module module) {
    if (module.getProperties() != null) {
      for (Property property : module.getProperties()) {
        if ("severity".equals(property.getName())) {
          return priorityMapper.from(property.getValue());
        }
      }
    }
    return null;
  }

  /**
   * Gets the active rule params.
   * 
   * @param module
   *          the module
   * @param rule
   *          the rule
   * @param activeRule
   *          the active rule
   * 
   * @return the active rule params
   */
  private List<ActiveRuleParam> getActiveRuleParams(Module module, Rule rule, ActiveRule activeRule) {
    List<ActiveRuleParam> activeRuleParams = new ArrayList<ActiveRuleParam>();
    if (module.getProperties() != null) {
      for (Property property : module.getProperties()) {
        if (rule.getParams() != null) {
          for (RuleParam ruleParam : rule.getParams()) {
            if (ruleParam.getKey().equals(property.getName())) {
              activeRuleParams.add(new ActiveRuleParam(activeRule, ruleParam, property.getValue()));
            }
          }
        }
      }
    }
    return activeRuleParams;
  }
}