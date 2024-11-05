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
package com.sonar.it.php;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonarsource.sonarlint.core.StandaloneSonarLintEngineImpl;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFile;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneSonarLintEngine;
import org.sonarsource.sonarlint.core.commons.Language;

import static org.assertj.core.api.Assertions.assertThat;

class SonarLintTest {

  @TempDir
  public static File tempDirectory;

  private static StandaloneSonarLintEngine sonarlintEngine;

  private static Path baseDir;

  @BeforeAll
  static void prepare() {
    StandaloneGlobalConfiguration sonarLintConfig = StandaloneGlobalConfiguration.builder()
      .addPlugin(Tests.PHP_PLUGIN_LOCATION.getFile().toPath())
      .addEnabledLanguage(Language.PHP)
      .setSonarLintUserHome(tempDirectory.toPath())
      .setLogOutput((formattedMessage, level) -> {
        /* Don't pollute logs */
      })
      .build();
    sonarlintEngine = new StandaloneSonarLintEngineImpl(sonarLintConfig);
    baseDir = tempDirectory.toPath();
  }

  @AfterAll
  static void stop() {
    sonarlintEngine.stop();
  }

  @Test
  void shouldRaiseIssues() throws IOException {
    Path filePath = Tests.projectDirectoryFor("sonarlint").toPath().resolve("Math.php");
    filePath = Files.copy(filePath, baseDir.resolve("Math.php"));
    ClientInputFile inputFile = prepareInputFile(filePath, false);

    List<Issue> issues = new ArrayList<>();
    StandaloneAnalysisConfiguration configuration = StandaloneAnalysisConfiguration.builder()
      .setBaseDir(baseDir)
      .addInputFile(inputFile)
      .build();
    sonarlintEngine.analyze(configuration, issues::add, null, null);

    assertThat(issues).hasSize(3);
  }

  private ClientInputFile prepareInputFile(Path filePath, final boolean isTest) {
    return createInputFile(filePath, isTest);
  }

  private ClientInputFile createInputFile(final Path path, final boolean isTest) {
    return new ClientInputFile() {

      @Override
      public String getPath() {
        return path.toString();
      }

      @Override
      public boolean isTest() {
        return isTest;
      }

      @Override
      public Charset getCharset() {
        return StandardCharsets.UTF_8;
      }

      @Override
      public <G> G getClientObject() {
        return null;
      }

      @Override
      public String contents() throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
      }

      @Override
      public String relativePath() {
        return path.toString();
      }

      @Override
      public URI uri() {
        return path.toUri();
      }

      @Override
      public InputStream inputStream() throws IOException {
        return Files.newInputStream(path);
      }

    };
  }

}
