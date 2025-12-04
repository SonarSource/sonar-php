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
package org.sonar.php.regex;

import java.util.List;
import org.sonar.plugins.php.api.visitors.IssueLocation;
import org.sonar.plugins.php.api.visitors.LocationInFile;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonarsource.analyzer.commons.regex.RegexIssueLocation;
import org.sonarsource.analyzer.commons.regex.ast.IndexRange;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;

public interface PhpRegexCheck extends PHPCheck {

  class PhpRegexIssueLocation extends IssueLocation {

    public PhpRegexIssueLocation(RegexSyntaxElement tree, String message) {
      super(((PhpAnalyzerRegexSource) tree.getSource()).locationInFileFor(tree.getRange()), message);
    }

    public PhpRegexIssueLocation(RegexIssueLocation location) {
      super(locationInFileFromRegexSyntaxElements(location.syntaxElements()), location.message());
    }

    private static LocationInFile locationInFileFromRegexSyntaxElements(List<RegexSyntaxElement> trees) {
      PhpAnalyzerRegexSource source = (PhpAnalyzerRegexSource) trees.get(0).getSource();
      IndexRange current = null;
      for (RegexSyntaxElement tree : trees) {
        if (current == null) {
          current = tree.getRange();
        } else if (tree.getRange().getBeginningOffset() == current.getEndingOffset()) {
          current = new IndexRange(current.getBeginningOffset(), tree.getRange().getEndingOffset());
        }
        // We do not combine RegexSyntaxElement which are not located side by side
      }
      return source.locationInFileFor(current);
    }

  }
}
