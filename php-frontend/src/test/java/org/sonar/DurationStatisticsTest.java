/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;

class DurationStatisticsTest {

  private final SensorContextTester sensorContext = SensorContextTester.create(Paths.get("."));

  @RegisterExtension
  public final LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void statisticsDisabled() {
    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    fillStatistics(statistics);
    statistics.log();
    assertThat(logTester.logs(Level.INFO)).isEmpty();
  }

  @Test
  void statisticsActivated() {
    sensorContext.settings().setProperty("sonar.php.duration.statistics", "true");
    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    fillStatistics(statistics);
    statistics.log();
    assertThat(logTester.logs(Level.INFO)).hasSize(1);
    assertThat(logTester.logs(Level.INFO).get(0)).startsWith("Duration Statistics, ");
  }

  @Test
  void statisticsFormat() {
    sensorContext.settings().setProperty("sonar.php.duration.statistics", "true");
    DurationStatistics statistics = new DurationStatistics(sensorContext.config());
    statistics.addRecord("A", 12_000_000L);
    statistics.addRecord("B", 15_000_000_000L);
    statistics.log();
    assertThat(logTester.logs(Level.INFO)).hasSize(1);
    assertThat(logTester.logs(Level.INFO).get(0)).isEqualTo("Duration Statistics, B 15'000 ms, A 12 ms");
  }

  private void fillStatistics(DurationStatistics statistics) {
    StringBuilder txt = new StringBuilder();
    statistics.time("A", () -> txt.append("1")).append(2);
    assertThat((CharSequence) txt).hasToString("12");
  }
}
