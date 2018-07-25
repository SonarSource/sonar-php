/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.checks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class CheckListTest {

  /**
   * Enforces that each check declared in list.
   */
  @Test
  public void count() {
    int count = 0;
    List<File> files = new ArrayList<>();
    for (String folder : new String[] { "src/main/java/org/sonar/php/checks/", "src/main/java/org/sonar/php/checks/security" }) {
      files.addAll(FileUtils.listFiles(new File(folder), new String[]{"java"}, false));
    }
    for (File file : files) {
      if (file.getName().endsWith("Check.java")) {
        count++;
      }
    }
    assertThat(CheckList.getChecks()).hasSize(count);
  }

  /**
   * Enforces that each check has test
   */
  @Test
  public void test() {
    Set<Class> checks = CheckList.getAllChecks();

    for (Class<?> cls : checks) {
      if (cls != ParsingErrorCheck.class) {
        String testName = '/' + cls.getName().replace('.', '/') + "Test.class";
        assertThat(getClass().getResource(testName))
          .overridingErrorMessage("No test for " + cls.getSimpleName())
          .isNotNull();
      }
    }
  }

}
