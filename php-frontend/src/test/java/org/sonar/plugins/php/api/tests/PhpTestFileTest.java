/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.plugins.php.api.tests;

import java.io.File;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PhpTestFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhpTestFileTest {

  @Test
  void loadFile() {
    File physicalFile = new File("src/test/resources/tests/testfile.php");
    PhpTestFile file = new PhpTestFile(physicalFile);
    assertThat(file.contents()).isEqualTo("<?php echo \"Hello\";\n");
    assertThat(file.filename()).isEqualTo("testfile.php");
    String expectedPath = Paths.get("src", "test", "resources", "tests", "testfile.php").toString();
    assertThat(file).hasToString(expectedPath);
    assertThat(file.uri()).isEqualTo(physicalFile.toURI());
    assertThat(file.key()).isEqualTo("testFileModule:testfile.php");
  }

  @Test
  void loadInvalidShowFilename() {
    File file = new File("invalid.php");
    assertThatThrownBy(() -> new PhpTestFile(file))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("invalid.php");
  }

}
