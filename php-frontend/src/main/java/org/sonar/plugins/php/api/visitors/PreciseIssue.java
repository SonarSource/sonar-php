/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.php.api.visitors;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.symbols.UnknownLocationInFile;
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
    return secondary(new IssueLocation(tree, message));
  }

  public PreciseIssue secondary(Tree startTree, Tree endTree, @Nullable String message) {
    return secondary(new IssueLocation(startTree, endTree, message));
  }

  public PreciseIssue secondary(LocationInFile locationInFile, @Nullable String message) {
    if (locationInFile == UnknownLocationInFile.UNKNOWN_LOCATION) {
      return this;
    }
    return secondary(new IssueLocation(locationInFile, message));
  }

  public PreciseIssue secondary(IssueLocation issueLocation) {
    this.secondaryLocations.add(issueLocation);
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
