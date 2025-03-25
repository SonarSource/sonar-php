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
package org.sonar.php.checks.regex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.CapturingGroupTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexBaseVisitor;

@Rule(key = "S6328")
public class GroupReplacementCheck extends AbstractRegexCheck {

  private static final String MESSAGE = "Referencing non-existing group%s: %s.";
  // Not allowing numbers starting by 0, other than zero itself.
  private static final String NUMBER_PATTERN = "0(?!\\d)|[1-9]\\d*+";
  private static final Pattern REFERENCE_PATTERN = Pattern.compile("\\$(" + NUMBER_PATTERN + ")|\\$\\{(" + NUMBER_PATTERN + ")}|\\\\(" + NUMBER_PATTERN + ")");

  @Override
  protected Set<String> lookedUpFunctionNames() {
    return Set.of("preg_replace");
  }

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    if (regexParseResult.hasSyntaxErrors()) {
      return;
    }
    GroupFinder groupFinder = new GroupFinder();
    groupFinder.visit(regexParseResult);
    checkReplacement(regexFunctionCall, groupFinder.groups);
  }

  private void checkReplacement(FunctionCallTree tree, Set<CapturingGroupTree> groups) {
    CheckUtils.resolvedArgumentLiteral(tree, "replacement", 1).ifPresent(
      replacement -> {
        List<Integer> references = collectReferences(replacement.value());
        references.removeIf(reference -> groups.stream().anyMatch(group -> group.getGroupNumber() == reference));
        if (!references.isEmpty()) {
          List<String> stringReferences = references.stream().map(String::valueOf).toList();
          newIssue(replacement, String.format(MESSAGE, references.size() == 1 ? "" : "s", String.join(", ", stringReferences)));
        }
      });
  }

  private static List<Integer> collectReferences(String replacement) {
    Matcher match = REFERENCE_PATTERN.matcher(replacement);
    List<Integer> references = new ArrayList<>();
    while (match.find()) {
      // extract reference number out of one of the possible 3 groups of the regex
      for (int i = 1; i <= 3; i++) {
        Optional.ofNullable(match.group(i)).map(Integer::valueOf).filter(ref -> ref != 0).ifPresent(references::add);
      }
    }
    return references;
  }

  static class GroupFinder extends RegexBaseVisitor {

    private final Set<CapturingGroupTree> groups = new HashSet<>();

    @Override
    public void visitCapturingGroup(CapturingGroupTree group) {
      groups.add(group);
      super.visitCapturingGroup(group);
    }
  }
}
