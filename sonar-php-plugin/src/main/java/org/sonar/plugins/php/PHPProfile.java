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
package org.sonar.plugins.php;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.php.checks.CheckList;
import org.sonar.plugins.php.api.Php;

/**
 * Sonar way profile.
 * <p>
 * We currently also define two other profiles, see {@link DrupalProfile} and {@link PSR2Profile}.
 *
 */
public final class PHPProfile extends ProfileDefinition {

  private final RuleFinder ruleFinder;

  public PHPProfile(RuleFinder ruleFinder) {
    this.ruleFinder = ruleFinder;
  }

  @Override
  public RulesProfile createProfile(ValidationMessages validation) {
    RulesProfile profile = RulesProfile.create(CheckList.SONAR_WAY_PROFILE, Php.KEY);

    loadFromCommonRepository(profile);

    loadActiveKeysFromJsonProfile(profile);

    return profile;
  }

  private void loadFromCommonRepository(RulesProfile profile) {
    Rule duplicatedBlocksRule = ruleFinder.findByKey("common-" + Php.KEY, "DuplicatedBlocks");

    // in SonarLint duplicatedBlocksRule == null
    if (duplicatedBlocksRule != null) {
      profile.activateRule(duplicatedBlocksRule, null);
    }
  }

  private void loadActiveKeysFromJsonProfile(RulesProfile profile) {
    for (String ruleKey : activatedRuleKeys()) {
      Rule rule = ruleFinder.findByKey(CheckList.REPOSITORY_KEY, ruleKey);
      profile.activateRule(rule, null);
    }
  }

  public static Set<String> activatedRuleKeys() {
    String location = "/org/sonar/l10n/php/rules/php/Sonar_way_profile.json";
    InputStream stream = PHPProfile.class.getResourceAsStream(location);
    try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      Gson gson = new Gson();
      return gson.fromJson(reader, Profile.class).ruleKeys;
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read: " + location, e);
    }
  }

  private static class Profile {
    Set<String> ruleKeys;
  }

}
