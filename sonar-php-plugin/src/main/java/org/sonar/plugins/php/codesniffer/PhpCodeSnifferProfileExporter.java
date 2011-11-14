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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.pmd.xml.PmdProperty;
import org.sonar.plugins.php.pmd.xml.PmdRule;
import org.sonar.plugins.php.pmd.xml.PmdRuleset;

/**
 * @author Akram Ben Aissi
 */
public class PhpCodeSnifferProfileExporter extends ProfileExporter {

  public static final String XPATH_CLASS = "net.sourceforge.pmd.rules.XPathRule";
  public static final String XPATH_EXPRESSION_PARAM = "xpath";
  public static final String XPATH_MESSAGE_PARAM = "message";

  private PhpCodeSnifferPriorityMapper mapper;

  /**
   * Instantiate the profile exporter. Exports are xml, mime type is set to fit.
   */
  public PhpCodeSnifferProfileExporter(PhpCodeSnifferPriorityMapper mapper) {
    super(PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY, PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_NAME);
    setSupportedLanguages(Php.KEY);
    setMimeType("application/xml");
    this.mapper = mapper;
  }

  /**
   * Perform export: Materialize the current active rule set for the profile. The convert it to XML.
   * 
   * @see org.sonar.api.profiles.ProfileExporter#exportProfile(org.sonar.api.profiles.RulesProfile, java.io.Writer)
   */
  @Override
  public void exportProfile(RulesProfile profile, Writer writer) {
    try {
      List<ActiveRule> activeRulesByRepository = profile.getActiveRulesByRepository(PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY);
      PmdRuleset ruleset = createRuleset(activeRulesByRepository, profile.getName());
      String xmlModules = exportRulesetToXml(ruleset);
      writer.append(xmlModules);
      writer.flush();
    } catch (IOException e) {
      throw new SonarException("Fail to export the profile " + profile, e);
    }
  }

  /**
   * Materialize the current active rule set for the profile
   * 
   * @param activeRules
   * @param profileName
   * @return
   */
  protected PmdRuleset createRuleset(List<ActiveRule> activeRules, String profileName) {
    PmdRuleset ruleset = new PmdRuleset(profileName);
    for (ActiveRule activeRule : activeRules) {
      if (activeRule.getRule().getRepositoryKey().equals(PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY)) {
        String configKey = activeRule.getRule().getKey();
        PmdRule rule = new PmdRule(configKey, mapper.to(activeRule.getSeverity()));
        List<PmdProperty> properties = null;
        List<ActiveRuleParam> activeRuleParams = activeRule.getActiveRuleParams();
        if (activeRuleParams != null && !activeRuleParams.isEmpty()) {
          properties = new ArrayList<PmdProperty>();
          for (ActiveRuleParam activeRuleParam : activeRuleParams) {
            properties.add(new PmdProperty(activeRuleParam.getRuleParam().getKey(), activeRuleParam.getValue()));
          }
        }
        rule.setProperties(properties);
        ruleset.addRule(rule);
        processXPathRule(activeRule.getRuleKey(), rule);
      }
    }
    return ruleset;
  }

  /**
   * @param sonarRuleKey
   * @param rule
   */
  protected void processXPathRule(String sonarRuleKey, PmdRule rule) {
    if (XPATH_CLASS.equals(rule.getRef())) {
      rule.setRef(null);
      rule.setMessage(rule.getProperty(XPATH_MESSAGE_PARAM).getValue());
      rule.removeProperty(XPATH_MESSAGE_PARAM);
      PmdProperty xpathExp = rule.getProperty(XPATH_EXPRESSION_PARAM);
      xpathExp.setCdataValue(xpathExp.getValue());
      rule.setClazz(XPATH_CLASS);
      rule.setName(sonarRuleKey);
    }
  }

  /**
   * @param pmdRuleset
   * @return
   */
  protected String exportRulesetToXml(PmdRuleset pmdRuleset) {
    Element rulesetNode = new Element("ruleset");
    addAttribute(rulesetNode, "name", pmdRuleset.getName());
    addChild(rulesetNode, "description", pmdRuleset.getDescription());
    for (PmdRule pmdRule : pmdRuleset.getPmdRules()) {
      // Generate the main <rule> element
      Element ruleNode = new Element("rule");
      addAttribute(ruleNode, "ref", pmdRule.getRef());
      addAttribute(ruleNode, "class", pmdRule.getClazz());
      addAttribute(ruleNode, "message", pmdRule.getMessage());
      addAttribute(ruleNode, "name", pmdRule.getName());
      addChild(ruleNode, "severity", pmdRule.getPriority());
      rulesetNode.addContent(ruleNode);
      // If there are params, generate another <rule> element
      // IMPORTANT NOTE: this is a hack because PHPCS does not currently accept parameters for rules.
      // The params need to be set on the sniff itself after the declaration of the rule...
      // SEE: https://jira.codehaus.org/browse/SONARPLUGINS-1508
      if (pmdRule.hasProperties()) {
        // => "ruleNode" is a new element !!
        ruleNode = new Element("rule");
        // => but this element must refer to the sniff, not to the rule
        addAttribute(ruleNode, "ref", StringUtils.substringBeforeLast(pmdRule.getRef(), "."));
        // and then add the properties "normally"
        Element propertiesNode = new Element("properties");
        ruleNode.addContent(propertiesNode);
        for (PmdProperty property : pmdRule.getProperties()) {
          Element propertyNode = new Element("property");
          propertyNode.setAttribute("name", property.getName());
          if (property.isCdataValue()) {
            Element valueNode = new Element("value");
            valueNode.addContent(new CDATA(property.getCdataValue()));
            propertyNode.addContent(valueNode);
          } else {
            propertyNode.setAttribute("value", property.getValue());
          }
          propertiesNode.addContent(propertyNode);
        }
        // and we add this extra <rule> element
        rulesetNode.addContent(ruleNode);
      }
    }
    XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
    StringWriter xml = new StringWriter();
    try {
      serializer.output(new Document(rulesetNode), xml);
    } catch (IOException e) {
      throw new SonarException("A exception occured while generating the PMD configuration file.", e);
    }
    return xml.toString();
  }

  /**
   * @param elt
   * @param name
   * @param text
   */
  private void addChild(Element elt, String name, String text) {
    if (text != null) {
      elt.addContent(new Element(name).setText(text));
    }
  }

  /**
   * @param elt
   * @param name
   * @param value
   */
  private void addAttribute(Element elt, String name, String value) {
    if (value != null) {
      elt.setAttribute(name, value);
    }
  }
}
