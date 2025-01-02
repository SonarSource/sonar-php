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
package org.sonar.plugins.php.reports.psalm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.sonar.plugins.php.reports.JsonReportReader;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PsalmJsonReportReader extends JsonReportReader {

  private final Consumer<Issue> consumer;

  private PsalmJsonReportReader(Consumer<Issue> consumer) {
    this.consumer = consumer;
  }

  static void read(InputStream in, Consumer<Issue> consumer) throws IOException, ParseException {
    new PsalmJsonReportReader(consumer).read(in);
  }

  private void read(InputStream in) throws IOException, ParseException {
    JSONObject rootObject = (JSONObject) jsonParser.parse(new InputStreamReader(in, UTF_8));
    JSONArray issues = (JSONArray) rootObject.get("issues");
    if (issues != null) {
      ((Stream<JSONObject>) issues.stream()).forEach(this::onIssue);
    }
  }

  private void onIssue(JSONObject i) {
    JSONObject primaryLocation = (JSONObject) i.get("primaryLocation");
    if (primaryLocation != null) {
      Issue issue = new Issue();
      issue.ruleId = (String) i.get("ruleId");
      issue.filePath = (String) primaryLocation.get("filePath");
      issue.message = (String) primaryLocation.get("message");

      JSONObject textRange = (JSONObject) primaryLocation.get("textRange");
      if (textRange != null) {
        issue.startLine = toInteger(textRange.get("startLine"));
        issue.startColumn = toInteger(textRange.get("startColumn"));
        issue.endLine = toInteger(textRange.get("endLine"));
        issue.endColumn = toInteger(textRange.get("endColumn"));
      }

      issue.type = (String) i.get("type");
      issue.severity = (String) i.get("severity");

      consumer.accept(issue);
    }
  }
}
