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
package org.sonar.php.checks;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.utils.PHPCheckTest;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.api.visitors.PhpFile;

class EmptyMethodCheckTest {

  private static final String PHP_OPENING_TAG = "<?php\n";
  private static final String ACCEPTED_EMPTY_FUNCTIONS = "function emptyFunction() {}\n";
  private static final String REPORTED_EMPTY_FUNCTIONS = "function applicationFunction() {} // NOK\n";

  @Test
  void test() throws Exception {
    CheckVerifier.verify(new EmptyMethodCheck(), "EmptyMethodCheck.php");
  }

  @Test
  void stubFilesAreIgnored() {
    PHPCheckTest.check(new EmptyMethodCheck(), new InMemoryPhpFile("EmptyMethodCheck.stub.php", PHP_OPENING_TAG + ACCEPTED_EMPTY_FUNCTIONS));
    PHPCheckTest.check(new EmptyMethodCheck(), new InMemoryPhpFile("stubs/EmptyMethodCheck.stub.php", PHP_OPENING_TAG + ACCEPTED_EMPTY_FUNCTIONS));
  }

  @Test
  void filesWithLeadingGenerationTagsAreIgnored() {
    for (String leadingComments : List.of(
      """
        /** @generate-class-entries */

        """,
      """
        /** @generate-function-entries */

        """,
      """
        /**
         * @generate-class-entries
         */

        """,
      """
        /** Copyright notice. */
        /** @generate-function-entries */

        """)) {
      PHPCheckTest.check(new EmptyMethodCheck(), new InMemoryPhpFile("src/EmptyMethodCheck.php", PHP_OPENING_TAG + leadingComments + ACCEPTED_EMPTY_FUNCTIONS));
    }
  }

  @Test
  void invalidOrMisplacedGenerationMarkersDoNotSuppressIssues() {
    for (String precedingCode : List.of(
      """
        $marker = '@generate-class-entries';
        """,
      """
        $marker = '@generate-function-entries';
        """,
      """
        /** This mentions @generate-class-entries without setting it. */
        $value = 1;
        """,
      // The marker must end after "entries"; the "-example" suffix makes this a different tag.
      """
        /** @generate-function-entries-example */
        $value = 1;
        """,
      // Generation markers are recognized only in leading docblocks, not line comments.
      """
        // @generate-class-entries
        $value = 1;
        """,
      """
        $value = 1;
        /** @generate-class-entries */
        $value = 2;
        """)) {
      PHPCheckTest.check(new EmptyMethodCheck(), new InMemoryPhpFile("src/EmptyMethodCheck.php", PHP_OPENING_TAG + precedingCode + REPORTED_EMPTY_FUNCTIONS));
    }
  }

  private record InMemoryPhpFile(String path, String contents) implements PhpFile {
    @Override
    public String filename() {
      return Path.of(path).getFileName().toString();
    }

    @Override
    public URI uri() {
      return URI.create("file:///project/" + path);
    }

    @Override
    public String key() {
      return path;
    }
  }
}
