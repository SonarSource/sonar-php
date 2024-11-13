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

import com.sonar.it.php.utils.MockSonarLintRpcClientDelegate;
import com.sonar.it.php.utils.SonarLintUtils;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonarsource.sonarlint.core.rpc.client.ClientJsonRpcLauncher;
import org.sonarsource.sonarlint.core.rpc.client.SonarLintRpcClientDelegate;
import org.sonarsource.sonarlint.core.rpc.impl.BackendJsonRpcLauncher;
import org.sonarsource.sonarlint.core.rpc.protocol.SonarLintRpcServer;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.analysis.AnalyzeFilesParams;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.analysis.AnalyzeFilesResponse;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.file.DidUpdateFileSystemParams;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.HttpConfigurationDto;
import org.sonarsource.sonarlint.core.rpc.protocol.backend.initialize.InitializeParams;
import org.sonarsource.sonarlint.core.rpc.protocol.common.ClientFileDto;
import org.sonarsource.sonarlint.core.serverconnection.FileUtils;

import static com.sonar.it.php.utils.SonarLintUtils.ENABLED_LANGUAGES;
import static com.sonar.it.php.utils.SonarLintUtils.IT_CLIENT_INFO;
import static com.sonar.it.php.utils.SonarLintUtils.IT_TELEMETRY_ATTRIBUTES;
import static com.sonar.it.php.utils.SonarLintUtils.toMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Inspiration: <a href="https://github.com/SonarSource/sonarlint-core/blob/master/its/tests/src/test/java/its/StandaloneTests.java">
 * sonarlint-core StandaloneTests.java</a>
 */
public class SonarLintTest {
  public static final Path BASE_DIR = Paths.get("projects/sonarlint");
  private static final String CONFIG_SCOPE_ID = "my-ide-project-name";

  @TempDir
  private static Path sonarUserHome;

  private static SonarLintRpcServer backend;
  private static SonarLintRpcClientDelegate client;

  @BeforeAll
  static void prepare() throws IOException, ExecutionException, InterruptedException {
    startBackend();
  }

  @AfterEach
  void reset() {
    ((MockSonarLintRpcClientDelegate) client).getRaisedIssues().clear();
  }

  @AfterAll
  static void cleanup() throws InterruptedException, ExecutionException, TimeoutException {
    backend.shutdown().get(1_000, TimeUnit.MILLISECONDS);
    FileUtils.deleteRecursively(sonarUserHome);
  }

  private static SonarLintRpcClientDelegate newMockSonarLintClient() {
    return new MockSonarLintRpcClientDelegate();
  }

  @ParameterizedTest
  @MethodSource
  void shouldRaiseIssue(Path inputFile) {
    var analyzeResponse = analyzeFile(CONFIG_SCOPE_ID, inputFile);

    assertThat(analyzeResponse.getFailedAnalysisFiles()).isEmpty();
    // it could happen that the notification is not yet received while the analysis request is finished.
    Awaitility.await().atMost(Duration.ofMillis(200)).untilAsserted(
      () -> assertThat(((MockSonarLintRpcClientDelegate) client).getRaisedIssues(CONFIG_SCOPE_ID)).hasSize(3));
  }

  static List<Path> shouldRaiseIssue() {
    return provideTestFiles();
  }

  private static List<Path> provideTestFiles() {
    List<Path> testFiles;

    try (Stream<Path> pathStream = Files.list(BASE_DIR)) {
      testFiles = pathStream
        .map(Path::getFileName)
        .map(BASE_DIR::resolve)
        .toList();
    } catch (IOException e) {
      throw new AssertionError("Can not load test files from " + BASE_DIR, e);
    }

    if (testFiles.isEmpty()) {
      throw new AssertionError("There are no test files provided");
    }

    return testFiles;
  }

  private static AnalyzeFilesResponse analyzeFile(String configScopeId, Path filePath, String... properties) {
    var fileUri = filePath.toUri();
    backend.getFileService().didUpdateFileSystem(
      new DidUpdateFileSystemParams(List.of(), List.of(
        new ClientFileDto(fileUri, filePath, configScopeId, false, null, filePath.toAbsolutePath(), null, null, true))));

    return backend.getAnalysisService().analyzeFiles(
      new AnalyzeFilesParams(configScopeId, UUID.randomUUID(), List.of(fileUri), toMap(properties), System.currentTimeMillis()))
      .join();
  }

  static void startBackend() throws IOException, ExecutionException, InterruptedException {
    var clientToServerOutputStream = new PipedOutputStream();
    var clientToServerInputStream = new PipedInputStream(clientToServerOutputStream);

    var serverToClientOutputStream = new PipedOutputStream();
    var serverToClientInputStream = new PipedInputStream(serverToClientOutputStream);
    client = newMockSonarLintClient();
    new BackendJsonRpcLauncher(clientToServerInputStream, serverToClientOutputStream);
    var clientLauncher = new ClientJsonRpcLauncher(serverToClientInputStream, clientToServerOutputStream, client);
    backend = clientLauncher.getServerProxy();

    var featureFlags = SonarLintUtils.featureFlagsForStandaloneMode();
    backend.initialize(
      new InitializeParams(
        IT_CLIENT_INFO,
        IT_TELEMETRY_ATTRIBUTES,
        HttpConfigurationDto.defaultConfig(),
        null,
        featureFlags,
        sonarUserHome.resolve("storage"),
        sonarUserHome.resolve("work"),
        Set.of(Tests.PHP_PLUGIN_LOCATION.getFile().toPath()),
        emptyMap(),
        ENABLED_LANGUAGES,
        emptySet(),
        emptySet(),
        emptyList(),
        emptyList(),
        sonarUserHome.toString(),
        Map.of(),
        false,
        null,
        false,
        null))
      .get();
  }
}
