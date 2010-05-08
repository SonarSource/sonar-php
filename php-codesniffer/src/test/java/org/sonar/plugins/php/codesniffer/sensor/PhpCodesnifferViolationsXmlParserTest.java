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

package org.sonar.plugins.php.codesniffer.sensor;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.DefaultProjectFileSystem;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RulesManager;
import org.sonar.api.rules.Violation;
import org.sonar.api.test.IsViolation;
import org.sonar.plugins.php.core.resources.PhpFile;

/**
 * The Class PhpCodesnifferViolationsXmlParserTest.
 */
public class PhpCodesnifferViolationsXmlParserTest {

  /**
   * Parses the.
   * 
   * @param context
   *          the context
   * @param xmlPath
   *          the xml path
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws URISyntaxException
   *           the URI syntax exception
   * @throws XMLStreamException
   *           the XML stream exception
   */
  private void parse(SensorContext context, String xmlPath) throws IOException, URISyntaxException, XMLStreamException {
    DefaultProjectFileSystem fileSystem = mock(DefaultProjectFileSystem.class);
    when(fileSystem.getSourceDirs()).thenReturn(Arrays.asList(new File("/test/src/main/")));

    Project project = mock(Project.class);
    when(project.getFileSystem()).thenReturn(fileSystem);

    RulesManager manager = mock(RulesManager.class);
    when(manager.getPluginRule(anyString(), anyString())).thenAnswer(new Answer<Rule>() {

      public Rule answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        return new Rule((String) args[1], (String) args[1], null, (String) args[0], "");
      }
    });
    RulesProfile profile = mock(RulesProfile.class);
    when(profile.getActiveRule(anyString(), anyString())).thenReturn(new ActiveRule(null, null, RulePriority.MINOR));
    PhpCheckStyleViolationsXmlParser parser = new PhpCheckStyleViolationsXmlParser(project, context, manager);

    File xmlFile = new File(getClass().getResource(xmlPath).toURI());
    parser.parse(xmlFile);
  }

  /**
   * Should find all class violations.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws URISyntaxException
   *           the URI syntax exception
   * @throws XMLStreamException
   *           the XML stream exception
   */
  @Test
  public void shouldFindAllClassViolations() throws IOException, URISyntaxException, XMLStreamException {
    SensorContext context = mock(SensorContext.class);
    parse(context, "/org/sonar/plugins/php/codesniffer/PhpCodesnifferViolationsXmlParserTest/codesniffer-full-result.xml");
    verify(context, times(22)).saveViolation((Violation) anyObject());
  }

  /**
   * Should not parse html files.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws URISyntaxException
   *           the URI syntax exception
   * @throws XMLStreamException
   *           the XML stream exception
   */
  @Test
  public void shouldNotParseHtmlFiles() throws IOException, URISyntaxException, XMLStreamException {
    SensorContext context = mock(SensorContext.class);
    parse(context, "/org/sonar/plugins/php/codesniffer/PhpCodesnifferViolationsXmlParserTest/codesniffer-full-result.xml");

    class IsNotHtmlPackage extends ArgumentMatcher<Violation> {

      @Override
      public boolean matches(Object violation) {
        return !((Violation) violation).getResource().getName().equals("package")
            && !((Violation) violation).getResource().getName().equals("package.html");
      }
    }

    verify(context, times(22)).saveViolation(argThat(new IsNotHtmlPackage()));
  }

  /**
   * Should parse violation.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws URISyntaxException
   *           the URI syntax exception
   * @throws XMLStreamException
   *           the XML stream exception
   */
  @Test
  public void shouldParseViolation() throws IOException, URISyntaxException, XMLStreamException {
    SensorContext context = mock(SensorContext.class);
    parse(context, "/org/sonar/plugins/php/codesniffer/PhpCodesnifferViolationsXmlParserTest/codesniffer-simple-result.xml");

    Violation wanted = new Violation(new Rule("PHP CODESNIFFER", "GN/Commenting/ClassCommentSniff/MISSING_CLASS_COMMENT"), new PhpFile(
        "org/sonar/mvn/BaseSonarMojo.php"));
    wanted.setMessage("Commentaire de classe manquant.");
    verify(context).saveViolation(argThat(new IsViolation(wanted)));
  }
}
