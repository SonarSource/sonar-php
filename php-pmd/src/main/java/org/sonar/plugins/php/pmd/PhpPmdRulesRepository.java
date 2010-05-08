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

package org.sonar.plugins.php.pmd;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.rules.ConfigurationExportable;
import org.sonar.api.rules.ConfigurationImportable;
import org.sonar.api.rules.RuleParam;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RulesCategory;
import org.sonar.api.rules.RulesRepository;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.pmd.sensor.PhpPmdSensor;
import org.sonar.plugins.php.pmd.xml.Properties;
import org.sonar.plugins.php.pmd.xml.Property;
import org.sonar.plugins.php.pmd.xml.Rule;
import org.sonar.plugins.php.pmd.xml.Ruleset;
import org.sonar.plugins.pmd.PmdRulePriorityMapper;

import com.thoughtworks.xstream.XStream;

/**
 * The Class PhpPmdRulesRepository handles ruleset and profile import and export
 */
public final class PhpPmdRulesRepository implements RulesRepository<Php>, ConfigurationExportable, ConfigurationImportable {

  private static final String PHPMD_RULESET_NAME = "Sonar PHPMD rules";

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpPmdSensor.class);

  /** The rule priority mapper. */
  private PmdRulePriorityMapper mapper = new PmdRulePriorityMapper();

  /**
   * Initial references.
   */
  private List<org.sonar.api.rules.Rule> initialRefs;

  /**
   * Instantiates a new php pmd rules repository.
   * 
   * @param language
   *          the language
   */
  public PhpPmdRulesRepository() {
    super();
  }

  /**
   * Adds headers to exported xml profile.
   * 
   * @param xmlModules
   *          the xml modules
   * 
   * @return the string
   */
  protected String addHeaderToXml(String xmlModules) {
    String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    return header + xmlModules;
  }

  /**
   * Builds a list of rules that are present in both profile and initial ruleset
   * 
   * @param ruleset
   *          the profile ruleset
   * @param activeRules
   *          the constructed list
   * @param rules
   *          the rules from initial ruleset
   */
  protected void buildActiveRulesFromRuleset(Ruleset ruleset, List<ActiveRule> activeRules, List<org.sonar.api.rules.Rule> rules) {
    List<Rule> allRules = ruleset.getRules();
    if (allRules != null && !allRules.isEmpty()) {
      // For each rules in the profile
      for (Rule rule : allRules) {
        String name = rule.getName();
        for (org.sonar.api.rules.Rule dbRule : rules) {
          // If rule is referenced by initial ruleset
          if (dbRule.getName().equals(name)) {
            // Activate rule for profile
            RulePriority rulePriority = mapper.from(rule.getPriority());
            ActiveRule activeRule = new ActiveRule(null, dbRule, rulePriority);
            activeRule.setActiveRuleParams(getActiveRuleParams(rule, dbRule, activeRule));
            activeRules.add(activeRule);
            break;
          }
        }
      }
    }
  }

  /**
   * Builds ruleset depending on the given rules.
   * 
   * @param activeRules
   *          the active rules
   * 
   * @return the ruleset
   */
  protected Ruleset buildRuleset(List<ActiveRule> activeRules) {
    Ruleset ruleset = new Ruleset(PHPMD_RULESET_NAME);
    for (ActiveRule activeRule : activeRules) {
      if (activeRule.getRule().getPluginName().equals(PhpPmdPlugin.KEY)) {
        String configKey = activeRule.getRuleKey();
        Rule rule = new Rule(configKey, mapper.to(activeRule.getPriority()));
        rule.setClassName(activeRule.getConfigKey());
        Properties properties = null;
        if (activeRule.getActiveRuleParams() != null && !activeRule.getActiveRuleParams().isEmpty()) {
          properties = new Properties();
          for (ActiveRuleParam activeRuleParam : activeRule.getActiveRuleParams()) {
            properties.add(new Property(activeRuleParam.getRuleParam().getKey(), activeRuleParam.getValue()));
          }
        }
        rule.setProperties(properties);
        ruleset.getRules().add(rule);
      }
    }
    return ruleset;
  }

  /**
   * Builds the ruleset from xml.
   * 
   * @param configuration
   *          the configuration
   * 
   * @return the ruleset
   */
  protected Ruleset buildRulesetFromXml(String configuration) {
    if (LOG.isInfoEnabled() && !LOG.isDebugEnabled()) {
      LOG.info("Loading configuration.");
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Loading following configuration :");
      LOG.debug(configuration);
    }
    InputStream inputStream = null;
    try {
      XStream xstream = new XStream();
      xstream.aliasSystemAttribute("classType", "class");
      xstream.processAnnotations(Ruleset.class);
      xstream.processAnnotations(Rule.class);
      xstream.processAnnotations(Property.class);
      inputStream = IOUtils.toInputStream(configuration, "UTF-8");
      return (Ruleset) xstream.fromXML(inputStream);

    } catch (IOException e) {
      String errorMessage = "Can't transform given configuration into inputStream with UTF-8 encoding.";
      LOG.error(errorMessage);
      throw new SonarException(errorMessage, e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }

  /**
   * Builds the profile from an xml file.
   * 
   * @param name
   *          the name
   * @param path
   *          the path
   * 
   * @return the rules profile
   */
  public final RulesProfile buildProfile(String name, String path) {
    InputStream input = null;
    try {
      // Gets the input stream
      input = getClass().getResourceAsStream(path);
      RulesProfile profile = new RulesProfile(name, Php.KEY);
      List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
      buildActiveRulesFromRuleset(buildRulesetFromXml(IOUtils.toString(input, "UTF-8")), activeRules, getInitialReferential());
      profile.setActiveRules(activeRules);
      return profile;
    } catch (IOException e) {
      throw new SonarException("Configuration file not found for the profile : " + name, e);

    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  /**
   * Builds an xml string from the ruleset
   * 
   * @param tree
   *          the tree
   * 
   * @return the string
   */
  protected String buildXmlFromRuleset(Ruleset tree) {
    XStream xstream = new XStream();
    xstream.processAnnotations(Ruleset.class);
    xstream.processAnnotations(Rule.class);
    xstream.processAnnotations(Property.class);
    return xstream.toXML(tree);
  }

  /**
   * Copy the rules obtained from XML tree to sonar rules instances
   * 
   * @param ruleset
   *          the ruleset
   * @param rules
   *          the rules
   * @param buildModuleTreeFromXml
   *          the build module tree from xml
   */
  private void copyToSonarRules(List<org.sonar.api.rules.Rule> rules, Ruleset buildModuleTreeFromXml) {
    for (Rule phpmdRule : buildModuleTreeFromXml.getRules()) {
      org.sonar.api.rules.Rule rule = new org.sonar.api.rules.Rule();
      rule.setConfigKey(phpmdRule.getClassName());
      rule.setName(phpmdRule.getName());
      rule.setDescription(phpmdRule.getDescription());
      rule.setKey(phpmdRule.getName());
      rule.setPluginName(PhpPmdPlugin.KEY);
      rule.setPriority(mapper.from(phpmdRule.getPriority()));
      if (phpmdRule.getProperties() != null) {
        List<RuleParam> params = new ArrayList<RuleParam>();
        for (Property property : phpmdRule.getProperties().getProperties()) {
          RuleParam param = new RuleParam(rule, property.getName(), property.getDescription(), "i");
          params.add(param);
        }
        rule.setParams(params);
      }
      rule.setRulesCategory(new RulesCategory("Maintainability"));
      rules.add(rule);
    }
  }

  /**
   * Export configuration.
   * 
   * @param activeProfile
   *          the active profile
   * 
   * @return the string
   * 
   * @see org.sonar.api.rules.ConfigurationExportable#exportConfiguration(org.sonar.api.profiles.RulesProfile)
   */
  public String exportConfiguration(RulesProfile activeProfile) {
    if (LOG.isInfoEnabled()) {
      LOG.info("Exporting " + activeProfile.getName() + ".");
    }
    Ruleset tree = buildRuleset(activeProfile.getActiveRulesByPlugin(PhpPmdPlugin.KEY));
    String xmlModules = buildXmlFromRuleset(tree);
    return addHeaderToXml(xmlModules);
  }

  /**
   * Gets the active rule params.
   * 
   * @param rule
   *          the rule
   * @param dbRule
   *          the db rule
   * @param activeRule
   *          the active rule
   * 
   * @return the active rule params
   */
  private List<ActiveRuleParam> getActiveRuleParams(Rule rule, org.sonar.api.rules.Rule dbRule, ActiveRule activeRule) {
    List<ActiveRuleParam> activeRuleParams = new ArrayList<ActiveRuleParam>();
    if (rule.getProperties() != null && rule.getProperties().getProperties() != null) {
      for (Property property : rule.getProperties().getProperties()) {
        if (dbRule.getParams() != null) {
          for (RuleParam ruleParam : dbRule.getParams()) {
            if (ruleParam.getKey().equals(property.getName())) {
              activeRuleParams.add(new ActiveRuleParam(activeRule, ruleParam, property.getValue()));
            }
          }
        }
      }
    }
    return activeRuleParams;
  }

  /**
   * Gets the initial referential.
   * 
   * @return the initial referential
   * 
   * @see org.sonar.api.rules.RulesRepository#getInitialReferential()
   */
  public List<org.sonar.api.rules.Rule> getInitialReferential() {
    if (initialRefs != null) {
      return initialRefs;
    }
    InputStream codeSizeRulesetInput = null;
    try {
      List<org.sonar.api.rules.Rule> rules = new ArrayList<org.sonar.api.rules.Rule>();
      loadRuleset("codesize.xml", rules);
      loadRuleset("unusedcode.xml", rules);
      return rules;
    } catch (IOException e) {
      String errorMessage = "Can't transform given configuration into inputStream with UTF-8 encoding.";
      LOG.error(errorMessage);
      throw new SonarException(errorMessage, e);
    } finally {
      if (codeSizeRulesetInput != null) {
        IOUtils.closeQuietly(codeSizeRulesetInput);
      }
    }
  }

  public void loadRuleset(String ruleset, List<org.sonar.api.rules.Rule> rules) throws IOException {
    InputStream is = getClass().getResourceAsStream(ruleset);
    try {
      if (is == null) {
        throw new SonarException("Resource not found : " + ruleset);
      }
      Ruleset buildModuleTreeFromXml = buildRulesetFromXml(IOUtils.toString(is, "UTF-8"));
      copyToSonarRules(rules, buildModuleTreeFromXml);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * Gets the language.
   * 
   * @return the language
   * 
   * @see org.sonar.api.rules.RulesRepository#getLanguage()
   */
  public Php getLanguage() {
    return Php.INSTANCE;
  }

  /**
   * Gets the provided profiles.
   * 
   * @return the provided profiles
   * 
   * @see org.sonar.api.rules.RulesRepository#getProvidedProfiles()
   */
  public List<RulesProfile> getProvidedProfiles() {
    List<RulesProfile> profiles = new ArrayList<RulesProfile>();
    profiles.add(buildProfile("Default Php Profile", "profile-php.xml"));
    return profiles;
  }

  /**
   * Import configuration.
   * 
   * @param configuration
   *          the configuration
   * @param rules
   *          the rules
   * 
   * @return the list< active rule>
   * 
   * @see org.sonar.api.rules.ConfigurationImportable#importConfiguration(java.lang.String, java.util.List)
   */
  public List<ActiveRule> importConfiguration(String configuration, List<org.sonar.api.rules.Rule> rules) {
    List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
    Ruleset moduleTree = buildRulesetFromXml(configuration);
    buildActiveRulesFromRuleset(moduleTree, activeRules, rules);
    return activeRules;
  }

  /**
   * Parses the referential.
   * 
   * @param fileContent
   *          the file content
   * 
   * @return the list<org.sonar.api.rules. rule>
   * 
   * @see org.sonar.api.rules.RulesRepository#parseReferential(java.lang.String)
   */
  public List<org.sonar.api.rules.Rule> parseReferential(String fileContent) {
    List<org.sonar.api.rules.Rule> rules = new ArrayList<org.sonar.api.rules.Rule>();
    Ruleset buildModuleTreeFromXml = buildRulesetFromXml(fileContent);
    copyToSonarRules(rules, buildModuleTreeFromXml);
    return rules;
  }

}