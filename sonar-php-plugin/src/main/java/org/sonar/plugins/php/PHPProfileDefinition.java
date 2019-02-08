/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.plugins.php;

import com.google.common.annotations.VisibleForTesting;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.api.SonarRuntime;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.php.checks.CheckList;
import org.sonar.plugins.php.api.Php;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

import static org.sonar.plugins.php.PHPRulesDefinition.RESOURCE_BASE_PATH;

/**
 * Sonar way profile.
 * <p>
 * We currently also define two other profiles, see {@link DrupalProfileDefinition} and {@link PSR2ProfileDefinition}.
 *
 */
public final class PHPProfileDefinition implements BuiltInQualityProfilesDefinition {

  private static final Logger LOG = Loggers.get(PHPProfileDefinition.class);

  public static final String SONAR_WAY_PROFILE = "Sonar way";
  public static final String SONAR_WAY_PATH = "org/sonar/l10n/php/rules/php/Sonar_way_profile.json";

  private final SonarRuntime sonarRuntime;

  public PHPProfileDefinition(SonarRuntime sonarRuntime) {
    this.sonarRuntime = sonarRuntime;
  }

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile sonarWay = context.createBuiltInQualityProfile(SONAR_WAY_PROFILE, Php.KEY);
    sonarWay.activateRule("common-" + Php.KEY, "DuplicatedBlocks");
    BuiltInQualityProfileJsonLoader.load(sonarWay, CheckList.REPOSITORY_KEY, SONAR_WAY_PATH, RESOURCE_BASE_PATH, sonarRuntime);
    getSecurityRuleKeys().forEach(key -> sonarWay.activateRule(key.repository(), key.rule()));
    sonarWay.done();
  }

  @VisibleForTesting
  static Set<RuleKey> getSecurityRuleKeys() {
    try {

      Class<?> phpRulesClass = Class.forName("com.sonar.plugins.security.api.PhpRules");
      Method getRuleKeysMethod = phpRulesClass.getMethod("getRuleKeys");
      Set<String> ruleKeys = (Set<String>) getRuleKeysMethod.invoke(null);
      Method getRepositoryKeyMethod = phpRulesClass.getMethod("getRepositoryKey");
      String repositoryKey = (String) getRepositoryKeyMethod.invoke(null);
      return ruleKeys.stream().map(k -> RuleKey.of(repositoryKey, k)).collect(Collectors.toSet());

    } catch (ClassNotFoundException e) {
      LOG.debug("com.sonar.plugins.security.api.PhpRules is not found, " + securityRuleMessage(e));
    } catch (NoSuchMethodException e) {
      LOG.debug("Method not found on com.sonar.plugins.security.api.PhpRules, " + securityRuleMessage(e));
    } catch (IllegalAccessException e) {
      LOG.debug("[IllegalAccessException] " + securityRuleMessage(e));
    } catch (InvocationTargetException e) {
      LOG.debug("[InvocationTargetException] " + securityRuleMessage(e));
    }

    return new HashSet<>();
  }

  private static String securityRuleMessage(Exception e) {
    return "no security rules added to Sonar way PHP profile: " + e.getMessage();
  }
}
