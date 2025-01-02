/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;

public class DurationStatistics {

  private static final Logger LOG = LoggerFactory.getLogger(DurationStatistics.class);

  private static final String PROPERTY_KEY = "sonar.php.duration.statistics";

  private final Map<String, AtomicLong> statistics = new HashMap<>();

  private final boolean recordStat;

  public DurationStatistics(Configuration config) {
    recordStat = config.getBoolean(PROPERTY_KEY).orElse(false);
  }

  public <T> T time(String id, Supplier<T> supplier) {
    if (recordStat) {
      long startTime = System.nanoTime();
      T result = supplier.get();
      addRecord(id, System.nanoTime() - startTime);
      return result;
    } else {
      return supplier.get();
    }
  }

  void addRecord(String id, long elapsedTime) {
    statistics.computeIfAbsent(id, key -> new AtomicLong(0)).addAndGet(elapsedTime);
  }

  public void log() {
    if (recordStat) {
      StringBuilder out = new StringBuilder();
      DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
      symbols.setGroupingSeparator('\'');
      NumberFormat format = new DecimalFormat("#,###", symbols);
      out.append("Duration Statistics");
      statistics.entrySet().stream()
        .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
        .forEach(e -> out.append(", ")
          .append(e.getKey())
          .append(" ")
          .append(format.format(e.getValue().get() / 1_000_000L))
          .append(" ms"));
      String message = out.toString();
      LOG.info(message);
    }
  }
}
