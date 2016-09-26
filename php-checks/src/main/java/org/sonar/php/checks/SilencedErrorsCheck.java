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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;

@Rule(
  key = SilencedErrorsCheck.KEY,
  name = "Errors should not be silenced",
  priority = Priority.MAJOR,
  tags = {Tags.PITFALL})
@SqaleConstantRemediation("30min")
public class SilencedErrorsCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S2002";
  private static final String MESSAGE = "Remove the '@' symbol from this function call to un-silence errors.";

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(Kind.ERROR_CONTROL);
  }

  @Override
  public void visitNode(Tree tree) {
    context().newIssue(this, MESSAGE).tree(tree);
  }

}
