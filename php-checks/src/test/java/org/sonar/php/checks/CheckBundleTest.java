/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
package org.sonar.php.checks;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.api.visitors.CheckContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class CheckBundleTest {

  private static final CheckContext CONTEXT = mock(CheckContext.class);
  private static final CheckBundlePart BUNDLE_PART = spy(CheckBundlePart.class);

  @Test
  void testBundlePartAnalysis() {
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
