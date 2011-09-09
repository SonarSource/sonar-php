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

/**
 * 
 */
package org.sonar.plugins.php.core;

import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.ProfileImporter;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.pmd.PhpmdProfileImporter;
import org.sonar.plugins.php.pmd.xml.PmdProperty;
import org.sonar.plugins.php.pmd.xml.PmdRule;
import org.sonar.plugins.php.pmd.xml.PmdRuleset;

/**
 * @author Akram Ben Aissi
 * 
 */
public abstract class PhpProfileImporter extends ProfileImporter {

  /**
   * @param key
   * @param name
   */
  protected PhpProfileImporter(String key, String name) {
    super(key, name);
    setSupportedLanguages(Php.KEY);
  }

  private static final Logger LOG = LoggerFactory.getLogger(PhpmdProfileImporter.class);

  /**
   * @param messages
   * @param e
   * @return
   */
  protected final PmdRuleset emptyRuleSetAndLogMessages(ValidationMessages messages, Exception e) {
    String errorMessage = "The PMD configuration file is not valid";
    messages.addErrorText(errorMessage + " : " + e.getMessage());
    LOG.error(errorMessage, e);
    return new PmdRuleset();
  }

  @SuppressWarnings("unchecked")
  protected final List<Element> getChildren(Element parent, String childName, Namespace namespace) {
    if (namespace == null) {
      return parent.getChildren(childName);
    } else {
      return parent.getChildren(childName, namespace);
    }
  }

  protected final void parsePmdProperties(Element ruleNode, PmdRule pmdRule, Namespace namespace) {
    for (Element eltProperties : getChildren(ruleNode, "properties", namespace)) {
      for (Element eltProperty : getChildren(eltProperties, "property", namespace)) {
        pmdRule.addProperty(new PmdProperty(eltProperty.getAttributeValue("name"), eltProperty.getAttributeValue("value")));
      }
    }
  }

  protected final void parsePmdPriority(Element ruleNode, PmdRule pmdRule, Namespace namespace) {
    for (Element priorityNode : getChildren(ruleNode, "priority", namespace)) {
      pmdRule.setPriority(priorityNode.getValue());
    }
  }

}
