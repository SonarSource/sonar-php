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

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.ProfileImporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.pmd.xml.PmdProperty;
import org.sonar.plugins.php.pmd.xml.PmdRule;
import org.sonar.plugins.php.pmd.xml.PmdRuleset;

/**
 * Abstract class for profile importers.
 * 
 */
public abstract class AbstractPhpProfileImporter extends ProfileImporter {

  private PriorityMapper priorityMapper;

  /**
   * Creates an {@link AbstractPhpProfileImporter}
   * 
   * @param key
   * @param name
   * @param mapper
   */
  protected AbstractPhpProfileImporter(String key, String name, PriorityMapper mapper) {
    super(key, name);
    setSupportedLanguages(Php.KEY);
    this.priorityMapper = mapper;
  }

  private static final Logger LOG = LoggerFactory.getLogger(AbstractPhpProfileImporter.class);

  /**
   * Returns an empty rule set and log an error message.
   * 
   * @param messages
   *          the validation messages
   * @param e
   *          the exception that occurred and that must be logged
   * @return the empty rule set
   */
  protected final PmdRuleset emptyRuleSetAndLogMessages(ValidationMessages messages, Exception e) {
    String errorMessage = "The PMD configuration file is not valid";
    messages.addErrorText(errorMessage + " : " + e.getMessage());
    LOG.error(errorMessage, e);
    return new PmdRuleset();
  }

  /**
   * Returns all the children of the given parent with the given name and namespace.
   * 
   * @param parent
   *          the parent
   * @param childName
   *          the name of the children to look for.
   * @param namespace
   *          the namespace
   * @return the list of children that were found
   */
  @SuppressWarnings("unchecked")
  protected final List<Element> getChildren(Element parent, String childName, Namespace namespace) {
    if (namespace == null) {
      return parent.getChildren(childName);
    } else {
      return parent.getChildren(childName, namespace);
    }
  }

  /**
   * PArses the property and sets them to the Pmd rule
   * 
   * @param ruleNode
   *          the XML node
   * @param pmdRule
   *          the pmd rule
   * @param namespace
   *          the current namespace
   */
  protected final void parsePmdProperties(Element ruleNode, PmdRule pmdRule, Namespace namespace) {
    for (Element eltProperties : getChildren(ruleNode, "properties", namespace)) {
      for (Element eltProperty : getChildren(eltProperties, "property", namespace)) {
        pmdRule.addProperty(new PmdProperty(eltProperty.getAttributeValue("name"), eltProperty.getAttributeValue("value")));
      }
    }
  }

  /**
   * Activates the given rule to the given profile. The rule is enhanced with information found in the pmdRule object.
   * 
   * @param rule
   *          the rule to activate
   * @param profile
   *          the profile that will contain the rule
   * @param pmdRule
   *          the pmd rule that will be used to set properties
   * @param messages
   *          the error messages if any
   */
  protected void addRuleToProfile(Rule rule, RulesProfile profile, PmdRule pmdRule, ValidationMessages messages) {
    ActiveRule activeRule = profile.activateRule(rule, priorityMapper.from(pmdRule.getPriority()));
    if (pmdRule.getProperties() != null) {
      completeRuleWithProperties(activeRule, rule, pmdRule, messages);
    }
  }

  private void completeRuleWithProperties(ActiveRule activeRule, Rule rule, PmdRule pmdRule, ValidationMessages messages) {
    for (PmdProperty prop : pmdRule.getProperties()) {
      String name = prop.getName();
      if (rule.getParam(name) != null) {
        String value = prop.getValue();
        String ruleValue = prop.isCdataValue() && isBlank(value) ? prop.getCdataValue() : value;
        activeRule.setParameter(name, ruleValue);
      } else {
        StringBuilder message = new StringBuilder("The property '").append(name);
        message.append("' is not supported in the rule: ").append(rule.getKey());
        messages.addWarningText(message.toString());
      }
    }
  }

}
