/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 EchoSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.core;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * The Class PhpPackageTest.
 */
public class PhpPackageTest {

  /**
   * Should be considered default package.
   */
  @Test
  public void shouldBeConsideredDefaultPackage() {
    PhpPackage phpPackage = new PhpPackage("    ");
    assertTrue(phpPackage.isDefault());
    phpPackage = new PhpPackage("tree\\Monkey.php");
    assertTrue( !phpPackage.isDefault());
  }

  /**
   * Should match pattern.
   */
  @Test
  public void shouldMatchPattern() {
    PhpPackage phpPackage = new PhpPackage("    earth.tree");
    assertTrue(phpPackage.matchFilePattern("earth.tree.Monkey"));
  }

  /**
   * Should not match pattern.
   */
  @Test
  public void shouldNotMatchPattern() {
    PhpPackage phpPackage = new PhpPackage("earth.tree");
    assertTrue( !phpPackage.matchFilePattern("tree"));
  }
}
