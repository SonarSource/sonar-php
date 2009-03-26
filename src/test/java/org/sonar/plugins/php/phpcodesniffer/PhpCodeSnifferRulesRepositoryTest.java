/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
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

package org.sonar.plugins.php.phpcodesniffer;

import org.apache.commons.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.OrderingComparisons.greaterThan;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.sonar.commons.rules.*;
import org.sonar.plugins.php.Php;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhpCodeSnifferRulesRepositoryTest {

  @Test
  public void shouldRulesInInitialReferentialBeDefinedWithTheDefaultSonarXmlFormat() {
    PhpCodeSnifferRulesRepository repository = new PhpCodeSnifferRulesRepository();
    List<Rule> rules = repository.getInitialReferential();
    assertThat(rules.size(), greaterThan(0));
    for (Rule rule : rules) {
      assertNotNull(rule.getKey());
      assertNotNull(rule.getDescription());
      assertNotNull(rule.getConfigKey());
      assertNotNull(rule.getName());
    }
  }

  @Test
  public void shouldReturnAnEmptyConfigurationIfNoActiveRules() throws IOException {
    PhpCodeSnifferRulesRepository rulesRepository = new PhpCodeSnifferRulesRepository();
    String result = rulesRepository.getConfigurationFromActiveRules("TEST", Collections.<ActiveRule>emptyList());
    assertThatAConfigurationIsEqualTo(result, "test-profile-empty");
  }

  @Test
  public void shouldReturnAListOfStringFromAListOfActiveRules() {
    PhpCodeSnifferRulesRepository rulesRepository = new PhpCodeSnifferRulesRepository() {
      protected String getSniffFromConfigKey(String configKey) {
        return configKey;
      }
    };

    List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
    activeRules.add(new ActiveRule(new RulesProfile(), new Rule("aRuleName", "aRuleKey", "aConfigKey", new RulesCategory("aCat"), "apluginName", ""),
      RuleFailureLevel.ERROR));
    activeRules.add(new ActiveRule(new RulesProfile(), new Rule("aRuleName", "aRuleKey", "anOtherConfigKey", new RulesCategory("aCat"), "apluginName", ""),
      RuleFailureLevel.ERROR));
    String result = rulesRepository.getPhpRules(activeRules);

    assertThat(result, is("'aConfigKey','anOtherConfigKey'"));
  }

  @Test
  public void shouldGetRulesConfigurationFromSomeActiveRules() throws IOException {
    PhpCodeSnifferRulesRepository rulesRepository = new PhpCodeSnifferRulesRepository();

    List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
    activeRules.add(new ActiveRule(new RulesProfile(), new Rule("aRuleName", "aRuleKey", "Generic/Sniffs/WhiteSpace/DisallowTabIndentSniff.php", new RulesCategory("aCat"), "apluginName", ""),
      RuleFailureLevel.ERROR));
    activeRules.add(new ActiveRule(new RulesProfile(), new Rule("aRuleName", "aRuleKey", "Zend/Sniffs/Files/ClosingTagSniff.php", new RulesCategory("aCat"), "apluginName", ""),
      RuleFailureLevel.ERROR));
    activeRules.add(new ActiveRule(new RulesProfile(), new Rule("aRuleName", "aRuleKey", "PEAR/Sniffs/Commenting/FunctionCommentSniff.php", new RulesCategory("aCat"), "apluginName", ""),
      RuleFailureLevel.ERROR));

    String result = rulesRepository.getConfigurationFromActiveRules("TEST", activeRules);

    assertThatAConfigurationIsEqualTo(result, "test-profile-with-some-rules");
  }

  @Test
  public void shouldReturnOneProvidedProfile() {
    PhpCodeSnifferRulesRepository rulesRepository = new PhpCodeSnifferRulesRepository();
    assertThat(rulesRepository.getProvidedProfiles().size(), is(1));
    RulesProfile firstRulesProfile = rulesRepository.getProvidedProfiles().get(0);
    assertThat(firstRulesProfile.getLanguage(), is(Php.KEY));
    assertThat(firstRulesProfile.getActiveRules().size(), greaterThan(0));
  }


  private void assertThatAConfigurationIsEqualTo(String configurationFound, String configurationExpected) {
    try {
      InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/phpcodesniffer/" + configurationExpected + ".php");
      String configuration = IOUtils.toString(input, "UTF-8");
      assertThat(configurationFound, equalToIgnoringWhiteSpace(configuration));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
