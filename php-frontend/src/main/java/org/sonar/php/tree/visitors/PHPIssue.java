/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.visitors;

import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.visitors.Issue;

import javax.annotation.Nullable;

/**
 * This class is used to represent issue created by checks before feeding them to SonarQube.
 */
public class PHPIssue implements Issue {

  private final String ruleKey;
  private final String message;
  private int line;
  @Nullable
  private Double cost;


  public PHPIssue(String ruleKey, String message) {
    this.ruleKey = ruleKey;
    this.message = message;
    this.line = 0;
    this.cost = null;
  }

  @Override
  public String ruleKey() {
    return ruleKey;
  }

  @Override
  public int line() {
    return line;
  }

  @Override
  @Nullable
  public Double cost() {
    return cost;
  }

  @Override
  public String message() {
    return message;
  }

  @Override
  public PHPIssue line(int line) {
    this.line = line;
    return this;
  }

  @Override
  public PHPIssue tree(Tree tree) {
    this.line = ((PHPTree) tree).getLine();
    return this;
  }

  @Override
  public PHPIssue cost(double cost) {
    this.cost = cost;
    return this;
  }

}
