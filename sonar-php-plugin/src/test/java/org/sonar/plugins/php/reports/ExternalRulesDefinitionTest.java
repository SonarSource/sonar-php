/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.php.reports;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.Version;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ExternalRulesDefinitionTest {

  private static final SonarRuntime SONAR_RUNTIME_10_6 = SonarRuntimeImpl.forSonarQube(Version.create(10, 6), SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
  private static final SonarRuntime SONAR_RUNTIME_9_9 = SonarRuntimeImpl.forSonarQube(Version.create(9, 9), SonarQubeSide.SERVER, SonarEdition.COMMUNITY);

  static Stream<SonarRuntime> externalRepositoryShouldBeInitializedWithSonarRuntime() {
    return Stream.of(SONAR_RUNTIME_10_6, SONAR_RUNTIME_9_9, null);
  }

  @MethodSource
  @ParameterizedTest
  void externalRepositoryShouldBeInitializedWithSonarRuntime(@Nullable SonarRuntime sonarRuntime) {
    RulesDefinition.Context context = new RulesDefinition.Context();
    RulesDefinition rulesDefinition = rulesDefinition(sonarRuntime);
    rulesDefinition.define(context);

    assertExternalRuleLoader(context, sonarRuntime);
  }

  @Test
  void externalRepositoryShouldBeInitializedWithoutSonarRuntime() {
    RulesDefinition.Context context = new RulesDefinition.Context();
    ExternalRuleLoader externalRuleLoader = ruleLoader();
    externalRuleLoader.createExternalRuleRepository(context);

    assertExternalRuleLoader(context, null);
  }

  protected void assertExternalRuleLoader(RulesDefinition.Context context, @Nullable SonarRuntime sonarRuntime) {
    assertThat(context.repositories()).hasSize(1);
    RulesDefinition.Repository repository = context.repository("external_" + reportKey());
    assertThat(repository).isNotNull();
    assertThat(repository.name()).isEqualTo(reportName());
    assertThat(repository.language()).isEqualTo("php");
    assertThat(repository.isExternal()).isTrue();
    assertThat(repository.rules()).hasSize(numberOfRules());

    boolean isCCTSupported = sonarRuntime != null
      && sonarRuntime.getApiVersion().isGreaterThanOrEqual(ExternalRuleLoader.API_VERSION_SUPPORTING_CLEAN_CODE_IMPACTS_AND_ATTRIBUTES);

    assertThat(ruleLoader().isCleanCodeImpactsAndAttributesSupported()).isEqualTo(isCCTSupported);

    customRuleAssertion(repository);
  }

  protected abstract void customRuleAssertion(RulesDefinition.Repository repository);

  protected abstract ExternalRuleLoader ruleLoader();

  protected abstract RulesDefinition rulesDefinition(@Nullable SonarRuntime sonarRuntime);

  protected abstract int numberOfRules();

  protected abstract String reportName();

  protected abstract String reportKey();
}
