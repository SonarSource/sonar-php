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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This contains metadata extracted from a {@link ControlFlowGraph}
 */
class BlockMetadata {
  private final CfgBlock block;
  private final String id;
  private final Set<String> successorIds;
  private final Set<String> predecessorIds;

  private BlockMetadata(String id, CfgBlock block) {
    this.id = id;
    this.block = block;
    this.successorIds = new HashSet<>();
    this.predecessorIds = new HashSet<>();
  }

  String getId() {
    return id;
  }

  CfgBlock getBlock() {
    return block;
  }

  Set<String> getExpectedSuccessorIds() {
    return successorIds;
  }

  Set<String> getExpectedPredecessorIds() {
    return predecessorIds;
  }

  boolean isEnd() {
    return id.equalsIgnoreCase("end");
  }

  static class Builder {
    BlockMetadata instance;

    Builder(String id, CfgBlock block) {
      instance = new BlockMetadata(id, block);
    }

    Builder withSuccessorsIds(String... ids) {
      Collections.addAll(instance.successorIds, ids);
      return this;
    }

    Builder withPredecessorIds(String... ids) {
      Collections.addAll(instance.predecessorIds, ids);
      return this;
    }

    BlockMetadata build() {
      return instance;
    }
  }
}
