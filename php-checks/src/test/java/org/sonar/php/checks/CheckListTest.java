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
package org.sonar.php.checks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckListTest {

  /**
   * Enforces that each check declared in list.
   */
  @Test
  void count() {
    int count = 0;
    List<File> files = new ArrayList<>();
    String[] checkFolders = {
      "src/main/java/org/sonar/php/checks/",
      "src/main/java/org/sonar/php/checks/phpini",
      "src/main/java/org/sonar/php/checks/phpunit",
      "src/main/java/org/sonar/php/checks/security",
      "src/main/java/org/sonar/php/checks/wordpress",
      "src/main/java/org/sonar/php/checks/regex"
    };
    for (String folder : checkFolders) {
      files.addAll(Arrays.asList(new File(folder).listFiles((f, name) -> name.endsWith("java"))));
    }
    for (File file : files) {
      String fileName = file.getName();
      if (fileName.endsWith("Check.java") && !fileName.startsWith("Abstract")) {
        count++;
      }
    }
    assertThat(CheckList.getAllChecks()).hasSize(count);
  }

  /**
   * Enforces that each check has test
   */
  @Test
  void test() {
    List<Class<?>> checks = CheckList.getAllChecks();

    for (Class<?> cls : checks) {
      if (cls != ParsingErrorCheck.class) {
        String testName = '/' + cls.getName().replace('.', '/') + "Test.class";
        assertThat(getClass().getResource(testName))
          .overridingErrorMessage("No test for " + cls.getSimpleName())
          .isNotNull();
      }
    }
  }

  @Test
  void checksShouldNotContainDuplicates() {
    List<Class<?>> allChecks = CheckList.getAllChecks();
    HashSet<Class<?>> allChecksWithoutDuplicates = new HashSet<>(allChecks);
    assertThat(allChecks).hasSameSizeAs(allChecksWithoutDuplicates);
  }
}
