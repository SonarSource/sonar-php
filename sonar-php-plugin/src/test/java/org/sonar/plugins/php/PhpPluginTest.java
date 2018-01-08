/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
/**
 *
 */
package org.sonar.plugins.php;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;

public class PhpPluginTest {

  private static final String DEPRECATION_NOTICE = "DEPRECATED: use " + PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY + ". ";
  private PhpPlugin plugin;

  @Before
  public void setUp() throws Exception {
    this.plugin = new PhpPlugin();
  }

  @Test
  public void test() {
    Plugin.Context context = qubeContext(Version.create(6, 7));
    plugin.define(context);

    assertThat(context.getExtensions()).hasSize(13);
  }

  @Test
  public void test_sonarlint() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarLint(Version.create(6, 7));
    Plugin.Context context = new Plugin.Context(runtime);
    plugin.define(context);

    assertThat(context.getExtensions()).hasSize(9);
  }

  @Test
  public void should_contain_REPORT_PATHS_from_6_2() throws Exception {
    Plugin.Context context6_7 = qubeContext(Version.create(6, 7));
    plugin.define(context6_7);

    assertThat(extensionKeysOf(context6_7)).contains(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATHS_KEY);
    assertThat(extensionKeysOf(context6_7)).contains(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY);
  }

  @Test
  public void should_add_deprecation_warning_to_legacy_coverage_report_path_keys_from_6_2() throws Exception {
    Plugin.Context context6_7 = qubeContext(Version.create(6, 7));
    plugin.define(context6_7);

    assertThat(property(context6_7, PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY).description()).startsWith(DEPRECATION_NOTICE);
    assertThat(property(context6_7, PhpPlugin.PHPUNIT_IT_COVERAGE_REPORT_PATH_KEY).description()).startsWith(DEPRECATION_NOTICE);
    assertThat(property(context6_7, PhpPlugin.PHPUNIT_OVERALL_COVERAGE_REPORT_PATH_KEY).description()).startsWith(DEPRECATION_NOTICE);
  }

  private PropertyDefinition property(Plugin.Context context, String propertyKey) {
    final List<Object> extensions = context.getExtensions();
    final Optional<PropertyDefinition> maybeProperty = extensions.stream().filter(obj -> obj instanceof PropertyDefinition).map(obj -> (PropertyDefinition) obj)
      .filter(prop -> prop.key().equals(propertyKey)).findFirst();
    if (maybeProperty.isPresent()) {
      return maybeProperty.get();
    } else {
      throw new IllegalArgumentException(propertyKey + " property not found in " + context);
    }
  }

  private static Plugin.Context qubeContext(Version version) {
    final SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(version, SonarQubeSide.SCANNER);
    return new Plugin.Context(runtime);
  }

  private static Set<String> extensionKeysOf(Plugin.Context context) {
    final List<Object> extensions = context.getExtensions();
    return extensions.stream().filter(obj -> obj instanceof PropertyDefinition).map(obj -> ((PropertyDefinition) obj).key()).collect(Collectors.toSet());
  }

}
