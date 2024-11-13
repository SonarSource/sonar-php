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
package com.sonar.it.php.utils;

import com.google.common.base.Preconditions;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.ClientConstantInfoDto;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.FeatureFlagsDto;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.TelemetryClientConstantAttributesDto;
import org.sonarsource.sonarlint.core.rpc.protocol.common.Language;

import static java.util.Collections.emptyMap;

public class SonarLintUtils {
  public static final Set<Language> ENABLED_LANGUAGES = EnumSet.of(Language.PHP);
  public static final ClientConstantInfoDto IT_CLIENT_INFO = new ClientConstantInfoDto("clientName", "integrationTests");
  public static final TelemetryClientConstantAttributesDto IT_TELEMETRY_ATTRIBUTES = new TelemetryClientConstantAttributesDto(
    "SonarLint ITs", "SonarLint ITs", "1.2.3", "4.5.6", emptyMap());

  public static FeatureFlagsDto featureFlagsForStandaloneMode() {
    return new FeatureFlagsDto(
      true,
      true,
      true,
      false,
      true,
      true,
      false,
      true,
      false,
      false);
  }

  public static Map<String, String> toMap(String[] keyValues) {
    Preconditions.checkArgument(keyValues.length % 2 == 0, "Must be an even number of key/values");
    Map<String, String> map = new HashMap<>();
    var index = 0;
    while (index < keyValues.length) {
      var key = keyValues[index++];
      var value = keyValues[index++];
      map.put(key, value);
    }
    return map;
  }
}
