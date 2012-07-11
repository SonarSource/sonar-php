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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

  private FakeRuleRepository fakeRuleRepository;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(fileSystem.getExtensions("fake", "xml")).thenReturn(
        Lists.newArrayList(TestUtils.getResource("/org/sonar/plugins/php/core/PhpRuleRepositoryTest/extended-ruleset.xml")));

    fakeRuleRepository = new FakeRuleRepository(fileSystem);
  }

  @Test
  public void shouldCreateRulesWithExtensions() throws Exception {
    List<Rule> rules = fakeRuleRepository.createRules();
    assertThat(rules.size()).isEqualTo(4);
  }

  class FakeRuleRepository extends PhpRuleRepository {

    private ServerFileSystem fileSystem;
    private XMLRuleParser xmlRuleParser;

    public FakeRuleRepository(ServerFileSystem fileSystem) {
      super("fake", "php");
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
  }

}
