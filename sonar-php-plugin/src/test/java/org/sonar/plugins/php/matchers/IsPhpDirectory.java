/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 Akram Ben Aissi
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

package org.sonar.plugins.php.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.resources.PhpPackage;

/**
 * The Class IsPhpDirectory.
 */
public class IsPhpDirectory extends BaseMatcher<Resource<PhpPackage>> {

  /** The key. */
  private String key = null;

  /**
   * Instantiates a new checks if is php directory.
   */
  public IsPhpDirectory() {
  }

  /**
   * Instantiates a new checks if is php directory.
   * 
   * @param key
   *          the key
   */
  public IsPhpDirectory(String key) {
    this.key = key;
  }

  /*
   * @see org.hamcrest.SelfDescribing#describeTo(org.hamcrest.Description)
   */
  /*
   * (non-Javadoc)
   * 
   * @see org.hamcrest.SelfDescribing#describeTo(org.hamcrest.Description)
   */
  public void describeTo(Description arg0) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hamcrest.Matcher#matches(java.lang.Object)
   */
  public boolean matches(Object o) {
    Resource resource = (Resource) o;
    boolean result = ResourceUtils.isDirectory(resource) && Php.INSTANCE.equals(resource.getLanguage());
    if (result && key != null) {
      result = key.equals(resource.getKey());
    }
    return result;
  }
}
