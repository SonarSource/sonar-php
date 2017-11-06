/*
 * SonarQube PHP Plugin
 * Copyright (C) 2014-2017 SonarSource SA
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
package org.sonar.php.it;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.wsclient.internal.HttpRequestFactory;
import org.sonar.wsclient.jsonsimple.JSONValue;

public class ProfileGenerator {

  private static final String LANGUAGE = "php";
  private static final String REPOSITORY = "php";

  static void generate(Orchestrator orchestrator, RulesConfiguration rulesConfiguration, Set<String> excluded) {

    StringBuilder sb = new StringBuilder()
      .append("<profile>")
      .append("<name>rules</name>")
      .append("<language>").append(LANGUAGE).append("</language>")
      .append("<rules>");

    String json = new HttpRequestFactory(orchestrator.getServer().getUrl())
      .get("/api/rules/search", ImmutableMap.<String, Object>of(
        "languages", LANGUAGE,
        "repositories", REPOSITORY,
        "ps", "1000"));
    @SuppressWarnings("unchecked")
    List<Map> jsonRules = (List<Map>) ((Map) JSONValue.parse(json)).get("rules");
    jsonRules.stream()
      .map(jsonRule -> (String) jsonRule.get("key"))
      .map(key -> key.split(":")[1])
      .filter(key -> !excluded.contains(key))
      .forEach(key -> {
        sb.append("<rule>")
          .append("<repositoryKey>").append(REPOSITORY).append("</repositoryKey>")
          .append("<key>").append(key).append("</key>")
          .append("<priority>INFO</priority>");
        if (rulesConfiguration.configurationbyRuleKey.containsKey(key)) {
          sb.append("<parameters>");
          rulesConfiguration.configurationbyRuleKey.get(key).entrySet()
            .forEach(parameter -> sb
              .append("<parameter>")
              .append("<key>").append(parameter.getKey()).append("</key>")
              .append("<value>").append(parameter.getValue()).append("</value>")
              .append("</parameter>"));
          sb.append("</parameters>");
        }
        sb.append("</rule>");
      });

    sb.append("</rules>")
      .append("</profile>");

    try {
      File file = File.createTempFile("profile", ".xml");
      Files.write(sb, file, StandardCharsets.UTF_8);
      orchestrator.getServer().restoreProfile(FileLocation.of(file));
      file.delete();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  static class RulesConfiguration {
    private final Map<String, Map<String, String>> configurationbyRuleKey;

    private RulesConfiguration() {
      configurationbyRuleKey = new HashMap<>();
    }

    public static RulesConfiguration create() {
      return new RulesConfiguration();
    }

    public RulesConfiguration addRule(String ruleKey, String parameter, String value) {
      configurationbyRuleKey.put(ruleKey, ImmutableMap.<String, String>builder().put(parameter, value).build());
      return this;
    }
  }
}
