/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 Akram Ben Aissi
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.XmlParserException;

/**
 * @author Akram Ben Aissi
 * 
 */
public class PhpmdViolationsXmlParser {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpmdViolationsXmlParser.class);

  private static final String FILE_NODE_NAME = "file";
  private static final String FILE_NAME_ATTRIBUTE_NAME = "name";

  private static final String RULE_NAME_ATTRIBUTE_NAME = "rule";
  private static final String RULESET_ATTRIBUTE_NAME = "ruleset";
  private static final String BEGIN_LINE_NUMBER_ATTRIBUTE_NAME = "beginline";
  private static final String END_LINE_NUMBER_ATTRIBUTE_NAME = "endline";
  private static final String PRIORITY_ATTRIBUTE_NAME = "priority";

  private static final String RULE_KEY_RULESET_SEPARATOR = "/";

  private final File reportFile;
  private final String reportPath;

  /**
   * Instantiates a new pmd violations xml parser.
   * 
   * @param reportFile
   */
  public PhpmdViolationsXmlParser(File reportFile) {
    this.reportFile = reportFile;
    LOG.debug("Report file for Phpms is " + reportFile);
    reportPath = reportFile.getAbsolutePath();
    if ( !reportFile.exists()) {
      throw new SonarException("The XML report can't be found at '" + reportPath + "'");
    }
  }

  /**
   * @return
   */
  public List<PhpmdViolation> getViolations() {
    LOG.debug("Getting violations form report file");
    List<PhpmdViolation> violations = new ArrayList<PhpmdViolation>();
    try {
      SMInputFactory inputFactory = new SMInputFactory(XMLInputFactory.newInstance());
      // <pmd>
      SMInputCursor rootNodeCursor = inputFactory.rootElementCursor(reportFile).advance();
      // <file>
      SMInputCursor fileNodeCursor = rootNodeCursor.childElementCursor(FILE_NODE_NAME).advance();
      while (fileNodeCursor.asEvent() != null) {
        String fileName = fileNodeCursor.getAttrValue(FILE_NAME_ATTRIBUTE_NAME);
        // <violation>
        SMInputCursor violationNodeCursor = fileNodeCursor.childElementCursor().advance();
        while (violationNodeCursor.asEvent() != null) {
          PhpmdViolation violation = new PhpmdViolation();
          violation.setFileName(fileName);
          violation.setSourcePath(fileName);
          violation.setBeginLine(Integer.parseInt(violationNodeCursor.getAttrValue(BEGIN_LINE_NUMBER_ATTRIBUTE_NAME)));
          violation.setEndLine(Integer.parseInt(violationNodeCursor.getAttrValue(END_LINE_NUMBER_ATTRIBUTE_NAME)));
          String ruleName = violationNodeCursor.getAttrValue(RULE_NAME_ATTRIBUTE_NAME);
          violation.setRuleName(ruleName);
          String ruleSet = violationNodeCursor.getAttrValue(RULESET_ATTRIBUTE_NAME);
          StringBuilder ruleKey = new StringBuilder(ruleSet).append(RULE_KEY_RULESET_SEPARATOR).append(ruleName);
          violation.setRuleKey(ruleKey.toString());
          violation.setPriority(violationNodeCursor.getAttrValue(PRIORITY_ATTRIBUTE_NAME));
          violation.setLongMessage(violationNodeCursor.getElemStringValue());
          violations.add(violation);
          violationNodeCursor.advance();
        }
        fileNodeCursor.advance();
      }
      rootNodeCursor.getStreamReader().closeCompletely();
    } catch (XMLStreamException e) {
      throw new XmlParserException("Unable to parse the  XML Report '" + reportPath + "'", e);
    }
    return violations;
  }
}
