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
package org.sonar.plugins.php.core;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.api.config.Settings;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.test.TestUtils;

import java.io.InputStream;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class PhpRuleRepositoryTest {

  @Mock
  private ServerFileSystem fileSystem;

  private PhpRuleRepository fakeRuleRepository;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldCreateRulesWithExtensions() throws Exception {
    when(fileSystem.getExtensions("fake", "xml")).thenReturn(
        Lists.newArrayList(TestUtils.getResource("/org/sonar/plugins/php/core/PhpRuleRepositoryTest/extended-ruleset.xml")));
    fakeRuleRepository = new FakeRuleRepositoryWithCustomRulesAsExtension(fileSystem);
    List<Rule> rules = fakeRuleRepository.createRules();
    assertThat(rules.size()).isEqualTo(4);
  }

  @Test
  public void shouldCreateRulesWithProperty() throws Exception {
    Settings settings = new Settings();
    settings.setProperty("custom.rules", FileUtils.readFileToString(TestUtils.getResource("/org/sonar/plugins/php/core/PhpRuleRepositoryTest/extended-ruleset.xml")));
    fakeRuleRepository = new FakeRuleRepositoryWithCustomRulesAsProperty(fileSystem, settings);
    List<Rule> rules = fakeRuleRepository.createRules();
    assertThat(rules.size()).isEqualTo(4);
  }

  class FakeRuleRepositoryWithCustomRulesAsExtension extends PhpRuleRepository {

    private ServerFileSystem fileSystem;
    private XMLRuleParser xmlRuleParser;

    public FakeRuleRepositoryWithCustomRulesAsExtension(ServerFileSystem fileSystem) {
      super("fake", "php", new Settings());
      setName("Fake repo");
      this.fileSystem = fileSystem;
      xmlRuleParser = new XMLRuleParser();
    }

    protected InputStream getRuleInputStream() {
      return getClass().getResourceAsStream("/org/sonar/plugins/php/core/PhpRuleRepositoryTest/default-ruleset.xml");
    }

    public ServerFileSystem getFileSystem() {
      return fileSystem;
    }

    public XMLRuleParser getParser() {
      return xmlRuleParser;
    }

    @Override
    protected String getRepositoryKey() {
      return "fake";
    }

    @Override
    protected String getCustomRulePropertyKey() {
      return "";
    }
  }

  class FakeRuleRepositoryWithCustomRulesAsProperty extends PhpRuleRepository {

    private ServerFileSystem fileSystem;
    private XMLRuleParser xmlRuleParser;

    public FakeRuleRepositoryWithCustomRulesAsProperty(ServerFileSystem fileSystem, Settings settings) {
      super("fake", "php", settings);
      setName("Fake repo");
      this.fileSystem = fileSystem;
      xmlRuleParser = new XMLRuleParser();
    }

    protected InputStream getRuleInputStream() {
      return getClass().getResourceAsStream("/org/sonar/plugins/php/core/PhpRuleRepositoryTest/default-ruleset.xml");
    }

    public ServerFileSystem getFileSystem() {
      return fileSystem;
    }

    public XMLRuleParser getParser() {
      return xmlRuleParser;
    }

    @Override
    protected String getRepositoryKey() {
      return "fake";
    }

    @Override
    protected String getCustomRulePropertyKey() {
      return "custom.rules";
    }
  }

}
