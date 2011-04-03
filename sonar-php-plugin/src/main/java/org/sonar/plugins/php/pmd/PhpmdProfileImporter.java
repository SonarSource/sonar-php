/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Sonar PHP Plugin
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

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.sonar.plugins.php.pmd.PhpmdRuleRepository.PHPMD_REPOSITORY_KEY;

import java.io.IOException;
import java.io.Reader;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.PhpProfileImporter;
import org.sonar.plugins.php.pmd.xml.PmdProperty;
import org.sonar.plugins.php.pmd.xml.PmdRule;
import org.sonar.plugins.php.pmd.xml.PmdRuleset;

/**
 *
 *
 */
public class PhpmdProfileImporter extends PhpProfileImporter {

  public static final String XPATH_CLASS = "net.sourceforge.pmd.rules.XPathRule";
  public static final String XPATH_EXPRESSION_PARAM = "xpath";
  public static final String XPATH_MESSAGE_PARAM = "message";

  private final RuleFinder ruleFinder;

  /**
   * @param ruleFinder
   */
  public PhpmdProfileImporter(RuleFinder ruleFinder) {
    super(PhpmdRuleRepository.PHPMD_REPOSITORY_KEY, PhpmdRuleRepository.PHPMD_REPOSITORY_NAME);
    setSupportedLanguages(Php.KEY);
    this.ruleFinder = ruleFinder;
  }

  /**
   * @see org.sonar.api.profiles.ProfileImporter#importProfile(java.io.Reader, org.sonar.api.utils.ValidationMessages)
   */
  @Override
  public RulesProfile importProfile(Reader pmdConfigurationFile, ValidationMessages messages) {
    PmdRuleset pmdRuleset = parsePmdRuleset(pmdConfigurationFile, messages);
    return createRuleProfile(pmdRuleset, messages);
  }

  /**
   * @param pmdRuleset
   * @param messages
   * @return
   */
  protected RulesProfile createRuleProfile(PmdRuleset pmdRuleset, ValidationMessages messages) {
    RulesProfile profile = RulesProfile.create();
    for (PmdRule pmdRule : pmdRuleset.getPmdRules()) {
      if (XPATH_CLASS.equals(pmdRule.getClazz())) {
        StringBuilder message = new StringBuilder("PMD XPath rule '").append(pmdRule.getName());
        message.append("' can't be imported automatically. The rule must be created manually through the Sonar web interface.");
        messages.addWarningText(message.toString());
      }
      String configKey = pmdRule.getRef();
      if (configKey == null) {
        StringBuilder message = new StringBuilder("Rule '").append(pmdRule.getClazz());
        message.append("' does not have a 'ref' attribute and can't be imported");
        messages.addWarningText(message.toString());
      } else {
        Rule rule = ruleFinder.find(RuleQuery.create().withRepositoryKey(PHPMD_REPOSITORY_KEY).withConfigKey(configKey));
        if (rule == null) {
          StringBuilder message = new StringBuilder("Unable to import unknown PMD rule '");
          message.append(configKey).append("' consider adding an extension in sonar extenions directory");
          messages.addWarningText(message.toString());
        } else {
          createRule(messages, profile, pmdRule, configKey, rule);
        }
      }
    }
    return profile;
  }

  /**
   * @param messages
   * @param profile
   * @param pmdRule
   * @param configKey
   * @param rule
   */
  private void createRule(ValidationMessages messages, RulesProfile profile, PmdRule pmdRule, String configKey, Rule rule) {
    PmdRulePriorityMapper mapper = new PmdRulePriorityMapper();
    ActiveRule activeRule = profile.activateRule(rule, mapper.from(pmdRule.getPriority()));
    if (pmdRule.getProperties() != null) {
      for (PmdProperty prop : pmdRule.getProperties()) {
        String name = prop.getName();
        if (rule.getParam(name) != null) {
          String value = prop.getValue();
          String ruleValue = prop.isCdataValue() && isBlank(value) ? prop.getCdataValue() : value;
          activeRule.setParameter(name, ruleValue);
        } else {
          StringBuilder message = new StringBuilder("The property '").append(name);
          message.append("' is not supported in the pmd rule: ").append(configKey);
          messages.addWarningText(message.toString());
        }
      }
    }
  }

  /**
   * @param pmdConfigurationFile
   * @param messages
   * @return
   */
  protected PmdRuleset parsePmdRuleset(Reader pmdConfigurationFile, ValidationMessages messages) {
    try {
      SAXBuilder parser = new SAXBuilder();
      Document dom = parser.build(pmdConfigurationFile);
      Element ruleSetNode = dom.getRootElement();
      Namespace namespace = ruleSetNode.getNamespace();
      PmdRuleset pmdResultset = new PmdRuleset();
      for (Element ruleNode : getChildren(ruleSetNode, "rule", namespace)) {
        PmdRule pmdRule = new PmdRule(ruleNode.getAttributeValue("ref"));
        pmdRule.setClazz(ruleNode.getAttributeValue("class"));
        pmdRule.setName(ruleNode.getAttributeValue("name"));
        pmdRule.setMessage(ruleNode.getAttributeValue("message"));
        parsePmdPriority(ruleNode, pmdRule, namespace);
        parsePmdProperties(ruleNode, pmdRule, namespace);
        pmdResultset.addRule(pmdRule);
      }
      return pmdResultset;
    } catch (IOException e) {
      return emptyRuleSetAndLogMessages(messages, e);
    } catch (JDOMException e) {
      return emptyRuleSetAndLogMessages(messages, e);
    }
  }
}
