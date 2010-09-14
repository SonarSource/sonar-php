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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.Configuration;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
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
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.PhpPlugin;
import org.sonar.plugins.php.core.resources.PhpFile;

/**
 * The Class PhpPmdViolationStaxParserTest.
 */
public class PhpPmdViolationStaxParserTest {

  /**
   * Parses the.
   * 
   * @param context
   *          the context
   * @param xmlPath
   *          the xml path
   * 
   * @throws URISyntaxException
   *           the URI syntax exception
   * @throws XMLStreamException
   *           the XML stream exception
   */
  private void parse(SensorContext context, String xmlPath) throws URISyntaxException, XMLStreamException {
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
    PhpPmdViolationsXmlParser parser = new PhpPmdViolationsXmlParser(project, context, manager, profile);

    File xmlFile = new File(getClass().getResource(xmlPath).toURI());
    parser.parse(xmlFile);
  }

  /**
   * Should save violations on classes.
   * 
   * @throws URISyntaxException
   *           the URI syntax exception
   * @throws XMLStreamException
   *           the XML stream exception
   */
  @Test
  public void shouldSaveViolationsOnClasses() throws URISyntaxException, XMLStreamException {
    SensorContext context = mock(SensorContext.class);
	Configuration configuration = mock(Configuration.class);
	Php php = new Php(configuration);
	when(configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY)).thenReturn(null);
    parse(context, "/org/sonar/plugins/php/pmd/php-pmd-result.xml");

    verify(context, times(30)).saveViolation(argThat(new IsViolationOnPhpClass()));
    verify(context, times(4)).saveViolation(argThat(new IsViolationOnPhpClass(new PhpFile("earth/animal/MonkeyWithComments.php"))));

    Violation wanted = new Violation(null, new PhpFile("earth/animal/MonkeyWithComments.php")).setMessage(
        "Avoid unused local variables such as 'toto'.").setLineId(22);
    verify(context, times(1)).saveViolation(argThat(new IsViolation(wanted)));
  }

  /**
   * Default package should be set onclass without package.
   * 
   * @throws URISyntaxException
   *           the URI syntax exception
   * @throws XMLStreamException
   *           the XML stream exception
   */
  @Test
  public void defaultPackageShouldBeSetOnclassWithoutPackage() throws URISyntaxException, XMLStreamException {
    SensorContext context = mock(SensorContext.class);
    parse(context, "/org/sonar/plugins/php/pmd/php-pmd-class-without-package.xml");
    verify(context, times(3)).saveViolation(argThat(new IsViolationOnPhpClass(new PhpFile("ClassOnDefaultPackage.php"))));
  }

  /**
   * Unknown xml entity.
   * 
   * @throws URISyntaxException
   *           the URI syntax exception
   * @throws XMLStreamException
   *           the XML stream exception
   */
  @Test
  public void unknownXMLEntity() throws URISyntaxException, XMLStreamException {
    SensorContext context = mock(SensorContext.class);
    parse(context, "/org/sonar/plugins/php/pmd/php-pmd-result-with-unknown-entity.xml");
    verify(context, times(2)).saveViolation(argThat(new IsViolationOnPhpClass(new PhpFile("animal/Monkey.php"))));
  }

  /**
   * ISO control chars xml file.
   * 
   * @throws URISyntaxException
   *           the URI syntax exception
   * @throws XMLStreamException
   *           the XML stream exception
   */
  @Test
  public void ISOControlCharsXMLFile() throws URISyntaxException, XMLStreamException {
    SensorContext context = mock(SensorContext.class);
    parse(context, "/org/sonar/plugins/php/pmd/php-pmd-result-with-control-char.xml");
    verify(context, times(1)).saveViolation(argThat(new IsViolationOnPhpClass(new PhpFile("animal/Monkey.php"))));
  }

  /**
   * The Class IsViolationOnPhpClass.
   */
  private class IsViolationOnPhpClass extends BaseMatcher<Violation> {

    /** The php class. */
    private PhpFile phpClass;

    /**
     * Instantiates a new checks if is violation on php class.
     * 
     * @param javaClass
     *          the java class
     */
    private IsViolationOnPhpClass(PhpFile javaClass) {
      this.phpClass = javaClass;
    }

    /**
     * Instantiates a new checks if is violation on php class.
     */
    private IsViolationOnPhpClass() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hamcrest.Matcher#matches(java.lang.Object)
     */
    public boolean matches(Object o) {
      Violation v = (Violation) o;
      boolean ok = (v.getResource() != null) && (v.getResource() instanceof PhpFile);
      if (ok && phpClass != null) {
        ok = phpClass.equals(v.getResource());
      }
      return ok;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hamcrest.SelfDescribing#describeTo(org.hamcrest.Description)
     */
    public void describeTo(Description description) {

    }
  }
}
