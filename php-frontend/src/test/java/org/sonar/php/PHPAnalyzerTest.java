/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.php.utils.DummyCheck;
import org.sonar.plugins.php.api.visitors.Issue;
import org.sonar.plugins.php.api.visitors.PHPCheck;

import java.io.File;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class PHPAnalyzerTest {
  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Test
  public void test_analyze() throws Exception {
    PHPAnalyzer analyzer = new PHPAnalyzer(Charsets.UTF_8, ImmutableList.<PHPCheck>of(new DummyCheck()));
    File file =  tmpFolder.newFile();
    FileUtils.write(file, "<?php $a = 1;");

    analyzer.nextFile(file);
    List<Issue> issues = analyzer.analyze();
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).line()).isEqualTo(1);
    assertThat(issues.get(0).ruleKey()).isEqualTo(DummyCheck.KEY);
    assertThat(issues.get(0).message()).isEqualTo(DummyCheck.MESSAGE);
  }

}
