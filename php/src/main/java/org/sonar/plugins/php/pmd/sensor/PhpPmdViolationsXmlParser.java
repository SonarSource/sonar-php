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

package org.sonar.plugins.php.pmd.sensor;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractViolationsStaxParser;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.RulesManager;
import org.sonar.plugins.php.core.resources.PhpFile;
import org.sonar.plugins.php.core.resources.PhpPackage;

/**
 * The PmdViolationsXmlParser reads the pmd result files and creates violations depending on the repository.
 */
class PhpPmdViolationsXmlParser extends AbstractViolationsStaxParser {

  /** The Constant FILE_NAME_ATTRIBUTE_NAME. */
  private static final String FILE_NAME_ATTRIBUTE_NAME = "name";

  /** The Constant RULE_NAME_ATTRIBUTE_NAME. */
  private static final String RULE_NAME_ATTRIBUTE_NAME = "rule";

  /** The Constant LINE_NUMBER_ATTRIBUTE_NAME. */
  private static final String LINE_NUMBER_ATTRIBUTE_NAME = "beginline";

  /** The Constant VIOLATION_NODE_NAME. */
  private static final String VIOLATION_NODE_NAME = "violation";

  /** The Constant FILE_NODE_NAME. */
  private static final String FILE_NODE_NAME = "file";

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpPmdViolationsXmlParser.class);

  /** The analyzed project. */
  private Project project;
  /** The plugin KEY. */
  public static final String KEY = "Phpmd";

  /**
   * Instantiates a new pmd violations xml parser.
   * 
   * @param project
   *          the project
   * @param context
   *          the context
   * @param rulesManager
   *          the rules manager
   * @param profile
   *          the profile
   */
  public PhpPmdViolationsXmlParser(Project project, SensorContext context, RulesManager rulesManager, RulesProfile profile) {
    super(context, rulesManager, profile);
    this.project = project;
  }

  /**
   * Cursor for resources.
   * 
   * @param rootCursor
   *          the root cursor
   * 
   * @return the SM input cursor
   * 
   * @throws XMLStreamException
   *           the XML stream exception
   * 
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#cursorForResources(org.codehaus.staxmate.in.SMInputCursor)
   */
  @Override
  protected SMInputCursor cursorForResources(SMInputCursor rootCursor) throws XMLStreamException {
    SMInputCursor descendantElementCursor = rootCursor.descendantElementCursor(FILE_NODE_NAME);
    LOG.debug("Cursor for resources: " + descendantElementCursor);
    return descendantElementCursor;
  }

  /**
   * Cursor for violations.
   * 
   * @param resourcesCursor
   *          the resources cursor
   * 
   * @return the SM input cursor
   * 
   * @throws XMLStreamException
   *           the XML stream exception
   * 
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#cursorForViolations(org.codehaus.staxmate.in.SMInputCursor)
   */
  @Override
  protected SMInputCursor cursorForViolations(SMInputCursor resourcesCursor) throws XMLStreamException {
    SMInputCursor descendantElementCursor = resourcesCursor.descendantElementCursor(VIOLATION_NODE_NAME);
    LOG.debug("Cursor for violations: " + descendantElementCursor);
    return descendantElementCursor;
  }

  /**
   * Key for plugin.
   * 
   * @return the string
   * 
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#keyForPlugin()
   */
  @Override
  protected String keyForPlugin() {
    return KEY;
  }

  /**
   * Returns the line number of the given violation.
   * 
   * @param violationCursor
   *          the violation cursor
   * 
   * @return the string
   * 
   * @throws XMLStreamException
   *           the XML stream exception
   * 
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#lineNumberForViolation(org.codehaus.staxmate.in.SMInputCursor)
   */
  @Override
  protected String lineNumberForViolation(SMInputCursor violationCursor) throws XMLStreamException {
    return violationCursor.getAttrValue(LINE_NUMBER_ATTRIBUTE_NAME);
  }

  /**
   * Returns the message for the given violation.
   * 
   * @param violationCursor
   *          the violation cursor
   * 
   * @return the string
   * 
   * @throws XMLStreamException
   *           the XML stream exception
   * 
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#messageFor(org.codehaus.staxmate.in.SMInputCursor)
   */
  @Override
  protected String messageFor(SMInputCursor violationCursor) throws XMLStreamException {
    return StringUtils.trim(violationCursor.collectDescendantText());
  }

  /**
   * Rule key.
   * 
   * @param violationCursor
   *          the violation cursor
   * 
   * @return the string
   * 
   * @throws XMLStreamException
   *           the XML stream exception
   * 
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#ruleKey(org.codehaus .staxmate.in.SMInputCursor)
   */
  @Override
  protected String ruleKey(SMInputCursor violationCursor) throws XMLStreamException {
    String ruleName = violationCursor.getAttrValue(RULE_NAME_ATTRIBUTE_NAME);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Found " + ruleName + " violation");
    }
    return ruleName;
  }

  /**
   * Returns the php file corresponding to the given violation.
   * 
   * @param resourcesCursor
   *          the resources cursor
   * 
   * @return the resource
   * 
   * @throws XMLStreamException
   *           the XML stream exception
   * 
   * @see org.sonar.api.batch.AbstractViolationsStaxParser#toResource(org.codehaus.staxmate.in.SMInputCursor)
   */
  @Override
  protected Resource<PhpPackage> toResource(SMInputCursor resourcesCursor) throws XMLStreamException {
    String fileName = resourcesCursor.getAttrValue(FILE_NAME_ATTRIBUTE_NAME);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Loading " + fileName + " to be associated with rule violation");
    }
    return PhpFile.fromIOFile(new File(fileName), project.getFileSystem().getSourceDirs(), false);
  }
}
