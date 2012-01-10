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

import static org.sonar.plugins.php.pmd.PhpmdRuleRepository.PHPMD_REPOSITORY_KEY;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.core.AbstractPhpProfileImporter;
import org.sonar.plugins.php.pmd.xml.PmdRule;
import org.sonar.plugins.php.pmd.xml.PmdRuleset;

/**
 * The profile importer for PHPMD
 */
public class PhpmdProfileImporter extends AbstractPhpProfileImporter {

  public static final String XPATH_CLASS = "net.sourceforge.pmd.rules.XPathRule";
  public static final String XPATH_EXPRESSION_PARAM = "xpath";
  public static final String XPATH_MESSAGE_PARAM = "message";

  private final RuleFinder ruleFinder;

  /**
   * Creates a new {@link PhpmdProfileImporter}
   * 
   * @param ruleFinder
   *          the Rule finder
   */
  public PhpmdProfileImporter(RuleFinder ruleFinder, PmdRulePriorityMapper mapper) {
    super(PhpmdRuleRepository.PHPMD_REPOSITORY_KEY, PhpmdRuleRepository.PHPMD_REPOSITORY_NAME, mapper);
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

  private RulesProfile createRuleProfile(PmdRuleset pmdRuleset, ValidationMessages messages) {
    RulesProfile profile = RulesProfile.create();
    Collection<Rule> allPhpmdRules = ruleFinder.findAll(RuleQuery.create().withRepositoryKey(PHPMD_REPOSITORY_KEY));
    for (PmdRule pmdRule : pmdRuleset.getPmdRules()) {
      if (XPATH_CLASS.equals(pmdRule.getClazz())) {
        StringBuilder message = new StringBuilder("PMD XPath rule '").append(pmdRule.getName());
        message.append("' can't be imported automatically. The rule must be created manually through the Sonar web interface.");
        messages.addWarningText(message.toString());
      }
      String configKey = pmdRule.getRef();
      if (configKey == null) {
        messages.addWarningText("A rule without 'ref' attribute can't be imported. see '" + pmdRule.getClazz() + "'");
      } else {
        Rule rule = ruleFinder.find(RuleQuery.create().withRepositoryKey(PHPMD_REPOSITORY_KEY).withConfigKey(configKey));
        if (rule != null) {
          addRuleToProfile(rule, profile, pmdRule, messages);
        } else {
          // let's try to find if we can find rules that belong to a sniff called "key"
          findPotentialRulesAndAddToProfile(configKey, pmdRule, allPhpmdRules, profile, messages);
        }
      }
    }
    return profile;
  }

  private void findPotentialRulesAndAddToProfile(String configKey, PmdRule pmdRule, Collection<Rule> allPhpmdRules, RulesProfile profile,
      ValidationMessages messages) {
    boolean found = false;
    for (Rule currentRule : allPhpmdRules) {
      if (currentRule.getConfigKey().startsWith(configKey)) {
        addRuleToProfile(currentRule, profile, pmdRule, messages);
        found = true;
      }
    }
    if (!found) {
      StringBuilder message = new StringBuilder("Unable to import unknown PHPMD rule '");
      message.append(configKey).append("' consider adding an extension in sonar extenions directory");
      messages.addWarningText(message.toString());
    }
  }

  /**
   * Parse the given PHPMD config file.
   * 
   * @param pmdConfigurationFile
   *          the config file
   * @param messages
   *          the error messages if any
   * @return the corresponding set of Pmd rules
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
        Element priorityNode = ruleNode.getChild("priority", namespace);
        if (priorityNode != null) {
          pmdRule.setPriority(priorityNode.getValue());
        }
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
