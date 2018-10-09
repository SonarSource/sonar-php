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
package org.sonar.php.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains the expected structure of a {@link CfgBlock}.
 */
class ExpectedBlockStructure {
  private final CfgBlock actualBlock;
  private final String testId;
  // for if branches, we care about the order in which the ids are declared in the test cases
  private final List<String> expectedSuccessorIds;
  private final Set<String> expectedPredecessorIds;

  private ExpectedBlockStructure(String id, CfgBlock actualBlock) {
    this.testId = id;
    this.actualBlock = actualBlock;
    this.expectedSuccessorIds = new ArrayList<>();
    this.expectedPredecessorIds = new HashSet<>();
  }

  String testId() {
    return testId;
  }

  CfgBlock actualBlock() {
    return actualBlock;
  }

  List<String> expectedSuccIds() {
    return expectedSuccessorIds;
  }

  Set<String> expectedPredIds() {
    return expectedPredecessorIds;
  }

  boolean isEnd() {
    return testId.equalsIgnoreCase("end");
  }

  static class Builder {
    ExpectedBlockStructure instance;

    Builder(String id, CfgBlock block) {
      instance = new ExpectedBlockStructure(id, block);
    }

    Builder withSuccessorsIds(String... ids) {
      Collections.addAll(instance.expectedSuccessorIds, ids);
      return this;
    }

    Builder withPredecessorIds(String... ids) {
      Collections.addAll(instance.expectedPredecessorIds, ids);
      return this;
    }

    ExpectedBlockStructure build() {
      return instance;
    }
  }
}
