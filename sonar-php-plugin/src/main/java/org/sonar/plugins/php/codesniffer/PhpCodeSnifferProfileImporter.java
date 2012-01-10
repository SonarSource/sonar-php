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

import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Profile importer for PHPCS.
 */
public class PhpCodeSnifferProfileImporter extends AbstractPhpProfileImporter {

  private static final Logger LOG = LoggerFactory.getLogger(PhpCodeSnifferProfileImporter.class);
  private final RuleFinder ruleFinder;

  /**
   * Creates a {@link PhpCodeSnifferProfileImporter}
   * 
   * @param ruleFinder
   *          the rule finder
   */
  public PhpCodeSnifferProfileImporter(RuleFinder ruleFinder, PhpCodeSnifferPriorityMapper mapper) {
    super(PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY, PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_NAME, mapper);
    setSupportedLanguages(Php.KEY);
    this.ruleFinder = ruleFinder;
  }

  /**
   * @see org.sonar.api.profiles.ProfileImporter#importProfile(java.io.Reader, org.sonar.api.utils.ValidationMessages)
   */
  @Override
  public RulesProfile importProfile(Reader pmdConfigurationFile, ValidationMessages messages) {
    PmdRuleset ruleSet = parseRuleset(pmdConfigurationFile, messages);
    return createRuleProfile(ruleSet, messages);
  }

  private RulesProfile createRuleProfile(PmdRuleset pmdRuleset, ValidationMessages messages) {
    RulesProfile profile = RulesProfile.create(pmdRuleset.getName(), Php.KEY);
    Collection<Rule> allPhpCsRules = ruleFinder.findAll(RuleQuery.create().withRepositoryKey(PHPCS_REPOSITORY_KEY));
    for (PmdRule pmdRule : pmdRuleset.getPmdRules()) {
      String key = pmdRule.getRef();
      if (key == null) {
        messages.addWarningText("A rule without 'ref' attribute can't be imported. see '" + pmdRule.getClazz() + "'");
      } else {
        // Attention: rule is retrieved using the key field which different of what is done on PmdImporter that uses configKey.
        Rule rule = ruleFinder.find(RuleQuery.create().withRepositoryKey(PHPCS_REPOSITORY_KEY).withKey(key));
        if (rule != null) {
          addRuleToProfile(rule, profile, pmdRule, messages);
        } else {
          // let's try to find if we can find rules that belong to a sniff called "key"
          findPotentialRulesAndAddToProfile(key, pmdRule, allPhpCsRules, profile, messages);
        }
      }
    }
    return profile;
  }

  private void findPotentialRulesAndAddToProfile(String key, PmdRule pmdRule, Collection<Rule> allPhpCsRules, RulesProfile profile,
      ValidationMessages messages) {
    boolean found = false;
    for (Rule currentRule : allPhpCsRules) {
      if (currentRule.getKey().startsWith(key)) {
        addRuleToProfile(currentRule, profile, pmdRule, messages);
        found = true;
      }
    }
    if (!found) {
      StringBuilder message = new StringBuilder("Unable to import unknown PhpCodeSniffer rule '");
      message.append(key).append("' consider adding an extension in sonar extenions directory");
      messages.addWarningText(message.toString());
    }
  }

  private PmdRuleset parseRuleset(Reader pmdConfigurationFile, ValidationMessages messages) {
    try {
      SAXBuilder parser = new SAXBuilder();
      Document dom = parser.build(pmdConfigurationFile);
      Element ruleSetNode = dom.getRootElement();
      Namespace namespace = ruleSetNode.getNamespace();
      PmdRuleset pmdResultset = new PmdRuleset(ruleSetNode.getAttributeValue("name"));
      for (Element ruleNode : getChildren(ruleSetNode, "rule", namespace)) {
        Element severityNode = ruleNode.getChild("severity", namespace);
        String severity = "";
        if (severityNode != null) {
          severity = severityNode.getValue();
        }
        // we must check for "0" severity, which would mean that the sniff should be ignored
        if (!"0".equals(severity)) {
          PmdRule pmdRule = new PmdRule(ruleNode.getAttributeValue("ref"));
          pmdRule.setClazz(ruleNode.getAttributeValue("class"));
          pmdRule.setName(ruleNode.getAttributeValue("name"));
          pmdRule.setMessage(ruleNode.getAttributeValue("message"));
          pmdRule.setPriority(severity);
          parsePmdProperties(ruleNode, pmdRule, namespace);
          pmdResultset.addRule(pmdRule);
        }
      }
      return pmdResultset;
    } catch (JDOMException e) {
      return emptyRuleSetAndLogMessage(messages, e);
    } catch (IOException e) {
      return emptyRuleSetAndLogMessage(messages, e);
    }
  }

  private PmdRuleset emptyRuleSetAndLogMessage(ValidationMessages messages, Exception e) {
    String errorMessage = "The PhpCodeSniffer configuration file is not valid";
    messages.addErrorText(errorMessage + " : " + e.getMessage());
    LOG.error(errorMessage, e);
    return new PmdRuleset();
  }
}
