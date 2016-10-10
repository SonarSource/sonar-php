/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
package org.sonar.php;

import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.RecognitionException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.php.utils.DummyCheck;
import org.sonar.plugins.php.api.visitors.Issue;
import org.sonar.plugins.php.api.visitors.PHPCheck;

import static com.google.common.base.Charsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class PHPAnalyzerTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Test(expected = RecognitionException.class)
  public void parsing_failure_should_raise_an_exception() throws IOException {
    PHPCheck check = new DummyCheck();
    PHPAnalyzer analyzer = new PHPAnalyzer(UTF_8, ImmutableList.of(check));
    File file = tmpFolder.newFile();
    FileUtils.write(file, "<?php if(condition): ?>", UTF_8);

    analyzer.nextFile(file);
  }

  public void test_analyze() throws Exception {
    PHPCheck check = new DummyCheck();
    PHPAnalyzer analyzer = new PHPAnalyzer(UTF_8, ImmutableList.of(check));
    File file = tmpFolder.newFile();
    FileUtils.write(file, "<?php $a = 1;", UTF_8);

    analyzer.nextFile(file);
    List<Issue> issues = analyzer.analyze();
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).line()).isEqualTo(1);
    assertThat(issues.get(0).check()).isEqualTo(check);
    assertThat(issues.get(0).message()).isEqualTo(DummyCheck.MESSAGE);
  }

}
