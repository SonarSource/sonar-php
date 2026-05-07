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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.slf4j.event.Level;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.IssueResolution;
import org.sonar.api.batch.sensor.issue.NewIssueResolution;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.api.utils.Version;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SonarResolveHandlerTest {

  @RegisterExtension
  public final LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private static final File BASE_DIR = new File("src/test/resources/sonar-resolve").getAbsoluteFile();

  private SensorContext context;
  private NewIssueResolution resolution;

  @BeforeEach
  void setUp() {
    resolution = mock(NewIssueResolution.class);
    when(resolution.on(any())).thenReturn(resolution);
    when(resolution.at(any())).thenReturn(resolution);
    when(resolution.status(any())).thenReturn(resolution);
    when(resolution.forRules(any())).thenReturn(resolution);
    when(resolution.comment(any())).thenReturn(resolution);

    context = mock(SensorContext.class);
    when(context.newIssueResolution()).thenReturn(resolution);
  }

  @Test
  void singleLineSlash() throws IOException {
    // Fixture has two directives on lines 2 and 5 — resolution is anchored to the directive line itself
    handle("single_line_slash.php");

    ArgumentCaptor<TextRange> rangeCaptor = ArgumentCaptor.forClass(TextRange.class);
    verify(context, times(2)).newIssueResolution();
    verify(resolution, times(2)).status(IssueResolution.Status.DEFAULT);
    verify(resolution, times(2)).comment("accepted reason");
    verify(resolution).forRules(Set.of(RuleKey.of("php", "S100")));
    verify(resolution).forRules(Set.of(RuleKey.of("phpsecurity", "S5131")));
    verify(resolution, times(2)).at(rangeCaptor.capture());
    verify(resolution, times(2)).save();

    List<TextRange> ranges = rangeCaptor.getAllValues();
    assertThat(ranges.get(0).start().line()).isEqualTo(2);
    assertThat(ranges.get(1).start().line()).isEqualTo(5);
  }

  @Test
  void singleLineFalsePositive() throws IOException {
    handle("single_line_false_positive.php");

    ArgumentCaptor<TextRange> rangeCaptor = ArgumentCaptor.forClass(TextRange.class);
    verify(context).newIssueResolution();
    verify(resolution).status(IssueResolution.Status.FALSE_POSITIVE);
    verify(resolution).forRules(Set.of(RuleKey.of("php", "S100")));
    verify(resolution).comment("false positive reason");
    verify(resolution).at(rangeCaptor.capture());
    verify(resolution).save();

    assertThat(rangeCaptor.getValue().start().line()).isEqualTo(2);
  }

  @Test
  void singleLineHash() throws IOException {
    handle("single_line_hash.php");

    ArgumentCaptor<TextRange> rangeCaptor = ArgumentCaptor.forClass(TextRange.class);
    verify(context).newIssueResolution();
    verify(resolution).status(IssueResolution.Status.DEFAULT);
    verify(resolution).forRules(Set.of(RuleKey.of("php", "S100")));
    verify(resolution).comment("hash reason");
    verify(resolution).at(rangeCaptor.capture());
    verify(resolution).save();

    assertThat(rangeCaptor.getValue().start().line()).isEqualTo(2);
  }

  @Test
  void blockComment() throws IOException {
    handle("block_comment.php");

    ArgumentCaptor<TextRange> rangeCaptor = ArgumentCaptor.forClass(TextRange.class);
    verify(context).newIssueResolution();
    verify(resolution).status(IssueResolution.Status.DEFAULT);
    verify(resolution).forRules(Set.of(RuleKey.of("php", "S100")));
    verify(resolution).comment("block reason");
    verify(resolution).at(rangeCaptor.capture());
    verify(resolution).save();

    assertThat(rangeCaptor.getValue().start().line()).isEqualTo(2);
  }

  @Test
  void continuationLines() throws IOException {
    // directive starts on line 2; resolution is anchored to line 2
    handle("continuation.php");

    ArgumentCaptor<TextRange> rangeCaptor = ArgumentCaptor.forClass(TextRange.class);
    verify(context).newIssueResolution();
    verify(resolution).status(IssueResolution.Status.DEFAULT);
    verify(resolution).forRules(Set.of(RuleKey.of("php", "S100")));
    verify(resolution).comment("multi\nline reason");
    verify(resolution).at(rangeCaptor.capture());
    verify(resolution).save();

    assertThat(rangeCaptor.getValue().start().line()).isEqualTo(2);
  }

  @Test
  void multipleDirectives() throws IOException {
    handle("multiple_directives.php");

    verify(context, times(2)).newIssueResolution();
    verify(resolution).status(IssueResolution.Status.DEFAULT);
    verify(resolution).forRules(Set.of(RuleKey.of("php", "S100")));
    verify(resolution).comment("first reason");
    verify(resolution).status(IssueResolution.Status.FALSE_POSITIVE);
    verify(resolution).forRules(Set.of(RuleKey.of("php", "S200")));
    verify(resolution).comment("second reason");
    verify(resolution, times(2)).save();
  }

  @Test
  void invalidDirective() throws IOException {
    handle("invalid.php");

    verify(context, never()).newIssueResolution();
    assertThat(logTester.logs(Level.WARN))
      .anyMatch(msg -> msg.contains("sonar-resolve") && msg.contains("missing justification"));
  }

  @Test
  void noDirective() throws IOException {
    handle("no_directive.php");

    verify(context, never()).newIssueResolution();
  }

  @Test
  void skippedWhenApiUnsupported() throws IOException {
    InputFile inputFile = createInputFile("single_line_slash.php");
    CompilationUnitTree tree = parseFile("single_line_slash.php");

    SonarResolveHandler.handle(context, inputFile, tree, false);

    verify(context, never()).newIssueResolution();
    assertThat(logTester.logs(Level.WARN))
      .anyMatch(msg -> msg.contains("sonar-resolve skipped: unsupported API"));
  }

  @Test
  void apiVersionCheck() {
    SonarRuntime supported = SonarRuntimeImpl.forSonarQube(Version.create(13, 5), SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
    SensorContext ctx135 = mock(SensorContext.class);
    when(ctx135.runtime()).thenReturn(supported);
    assertThat(SonarResolveHandler.apiSupportsIssueResolution(ctx135)).isTrue();

    SonarRuntime unsupported = SonarRuntimeImpl.forSonarQube(Version.create(13, 4), SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
    SensorContext ctx134 = mock(SensorContext.class);
    when(ctx134.runtime()).thenReturn(unsupported);
    assertThat(SonarResolveHandler.apiSupportsIssueResolution(ctx134)).isFalse();
  }

  private void handle(String fileName) throws IOException {
    InputFile inputFile = createInputFile(fileName);
    CompilationUnitTree tree = parseFile(fileName);
    SonarResolveHandler.handle(context, inputFile, tree, true);
  }

  private InputFile createInputFile(String fileName) throws IOException {
    File file = new File(BASE_DIR, fileName);
    String source = Files.readString(file.toPath());
    return TestInputFileBuilder.create("moduleKey", fileName)
      .setModuleBaseDir(BASE_DIR.toPath())
      .setLanguage(Php.KEY)
      .initMetadata(source)
      .build();
  }

  private static CompilationUnitTree parseFile(String fileName) {
    return (CompilationUnitTree) PHPParserBuilder.createParser().parse(new File(BASE_DIR, fileName));
  }
}
