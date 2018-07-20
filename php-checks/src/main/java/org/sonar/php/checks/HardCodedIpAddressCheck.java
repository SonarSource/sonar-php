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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

@Rule(key = "S1313")
public class HardCodedIpAddressCheck extends PHPVisitorCheck {

  private static final Pattern IP = Pattern.compile("([^\\d.]*\\/)?((?<ip>(?:\\d{1,3}\\.){3}\\d{1,3})(:\\d{1,5})?(?!\\d|\\.))(\\/.*)?");
  private static final String MESSAGE = "Make sure using this hardcoded IP address is safe here.";

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      String literalValue = trimQuotes(tree.value());
      Matcher matcher = IP.matcher(literalValue);
      if (matcher.matches()) {
        String ip = matcher.group("ip");
        if (Arrays.stream(ip.split("\\.")).mapToInt(Integer::parseInt).allMatch(i -> i < 255)
          && !isLocalhost(ip)) {
          context().newIssue(this, tree, MESSAGE);
        }
      }
    }
    super.visitLiteral(tree);
  }

  private static boolean isLocalhost(String literalValue) {
    return "127.0.0.1".equals(literalValue);
  }

}
