/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.sonar.plugins.php.api.visitors.CheckContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class CheckBundleTest {

  private static final CheckContext CONTEXT = mock(CheckContext.class);
  private static final CheckBundlePart BUNDLE_PART = spy(CheckBundlePart.class);

  @Test
  public void test_bundle_part_analysis() {
    CheckBundle bundle = new TestCheckBundle();
    bundle.init();
    bundle.analyze(CONTEXT);
    verify(BUNDLE_PART).init();
    verify(BUNDLE_PART).analyze(any(CheckContext.class));
  }

  static class TestCheckBundle extends CheckBundle {

    @Override
    protected List<CheckBundlePart> checks() {
      return Collections.singletonList(BUNDLE_PART);
    }
  }
}
