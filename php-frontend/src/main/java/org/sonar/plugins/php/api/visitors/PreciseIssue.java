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
package org.sonar.plugins.php.api.visitors;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.Tree;

public class PreciseIssue implements PhpIssue {

  private final PHPCheck check;
  private Double cost;
  private final IssueLocation primaryLocation;
  private final List<IssueLocation> secondaryLocations;

  public PreciseIssue(PHPCheck check, IssueLocation primaryLocation) {
    this.check = check;
    this.primaryLocation = primaryLocation;
    this.cost = null;
    this.secondaryLocations = new ArrayList<>();
  }

  public IssueLocation primaryLocation() {
    return primaryLocation;
  }

  public List<IssueLocation> secondaryLocations() {
    return secondaryLocations;
  }

  public PreciseIssue secondary(Tree tree, @Nullable String message) {
    this.secondaryLocations.add(new IssueLocation(tree, message));
    return this;
  }

  public PreciseIssue secondary(Tree startTree, Tree endTree, @Nullable String message) {
    this.secondaryLocations.add(new IssueLocation(startTree, endTree, message));
    return this;
  }

  @Override
  public PHPCheck check() {
    return check;
  }

  @Nullable
  @Override
  public Double cost() {
    return cost;
  }

  @Override
  public PreciseIssue cost(double cost) {
    this.cost = cost;
    return this;
  }
}
