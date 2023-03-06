/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class DurationStatistics {

  private static final Logger LOG = Loggers.get(DurationStatistics.class);

  private static final String PROPERTY_KEY = "sonar.php.duration.statistics";

  private final Map<String, AtomicLong> stats = new HashMap<>();

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
    stats.computeIfAbsent(id, key -> new AtomicLong(0)).addAndGet(elapsedTime);
  }

  public void log() {
    if (recordStat) {
      StringBuilder out = new StringBuilder();
      DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
      symbols.setGroupingSeparator('\'');
      NumberFormat format = new DecimalFormat("#,###", symbols);
      out.append("Duration Statistics");
      stats.entrySet().stream()
        .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
        .forEach(e -> out.append(", ")
          .append(e.getKey())
          .append(" ")
          .append(format.format(e.getValue().get() / 1_000_000L))
          .append(" ms"));
      LOG.info(out.toString());
    }
  }
}
