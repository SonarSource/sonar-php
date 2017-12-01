/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.php.phpunit.xml;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XmlUtils {

  private XmlUtils() {
    // utility
  }

  public static DocumentBuilder documentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setExpandEntityReferences(true);
    factory.setCoalescing(true);
    factory.setNamespaceAware(false);
    factory.setValidating(false);
    return factory.newDocumentBuilder();
  }

  public static List<Element> elements(Node parent, String elementName) {
    List<Element> result = new ArrayList<>();
    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(elementName)) {
        result.add((Element) child);
      }
    }
    return result;
  }

  @Nullable
  public static String elementText(Node parent, String elementName) {
    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(elementName)) {
        return child.getTextContent();
      }
    }
    return null;
  }

}
