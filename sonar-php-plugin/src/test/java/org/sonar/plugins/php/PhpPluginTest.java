/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;

class PhpPluginTest {

  private PhpPlugin plugin;

  @BeforeEach
  public void setUp() {
    this.plugin = new PhpPlugin();
  }

  @Test
  void testNumberOfExtensionsInSonarQubeContext() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(9, 9), SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);
    Plugin.Context context = new Plugin.Context(runtime);
    plugin.define(context);

    assertThat(context.getExtensions()).hasSize(19);
  }

  @Test
  void testNumberOfExtensionsInSonarLintContext() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarLint(Version.create(6, 7));
    Plugin.Context context = new Plugin.Context(runtime);
    plugin.define(context);

    assertThat(context.getExtensions()).hasSize(10);
  }
}
