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

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerBracketTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class CfgTestUtils {

  static void assertCfgStructure(Map<String, BlockMetadata> data, String dotNotation, boolean testPredecessors) {

    for (BlockMetadata metadata : data.values()) {
      if (metadata.isEnd()) {
        continue;
      }

      if (testPredecessors) {
        Set<CfgBlock> expectedPred = getCfgBlocks(metadata.getExpectedPredecessorIds(), data);
        Set<CfgBlock> actualPred = metadata.getBlock().predecessors();
        assertAreTheSame(expectedPred, actualPred,
            buildErrorMessage(metadata.getId(), dotNotation, "predecessors"));
      }

      Set<CfgBlock> expectedSucc = getCfgBlocks(metadata.getExpectedSuccessorIds(), data);
      Set<CfgBlock> actualSucc = metadata.getBlock().successors();
      assertAreTheSame(expectedSucc, actualSucc,
        buildErrorMessage(metadata.getId(), dotNotation, "successors"));
    }
  }

  static Map<String, BlockMetadata> buildMetadata(Set<CfgBlock> blocks) {
    Map<String, BlockMetadata> result = new HashMap<>();
    for (CfgBlock block : blocks) {
      if (block instanceof PhpCfgEndBlock) {
        result.put("END", new BlockMetadata.Builder("END", block).build());
        continue;
      }

      ArrayInitializerBracketTree bracketTree = getMetadataArray(block.elements().get(0));
      if (bracketTree == null) {
        throw new UnsupportedOperationException("CFG Block metadata must be the first statement in the block");
      }

      String id = null;
      String[] pred = {};
      String[] succ = {};
      for (ArrayPairTree arrayPair : bracketTree.arrayPairs()) {
        if (isNamespaceTreeWithValue(arrayPair.key(), "id")) {
          id = getValue(arrayPair.value());
        } else if (isNamespaceTreeWithValue(arrayPair.key(), "succ")) {
          succ = getStrings(arrayPair.value());
        } else if (isNamespaceTreeWithValue(arrayPair.key(), "pred")) {
          pred = getStrings(arrayPair.value());
        }
      }

      if (id != null) {
        result.put(id, new BlockMetadata.Builder(id, block).withSuccessorsIds(succ).withPredecessorIds(pred).build());
      } else {
        throw new UnsupportedOperationException("CFG Block metadata is not in expected format");
      }
    }

    return result;
  }

  private static ArrayInitializerBracketTree getMetadataArray(Tree firstElement) {
    if (!(firstElement instanceof ExpressionStatementTree)) {
      return null;
    }
    ExpressionStatementTree statement = (ExpressionStatementTree) firstElement;
    if (!(statement.expression() instanceof FunctionCallTree)) {
      return null;
    }
    FunctionCallTree function = (FunctionCallTree) statement.expression();
    if (function.arguments().size() != 1 ||
        !(function.arguments().get(0) instanceof ArrayInitializerBracketTree) ||
        !(function.callee() instanceof NamespaceNameTree)) {
      return null;
    }
    if (!((NamespaceNameTree)function.callee()).name().text().equalsIgnoreCase("block")) {
      return null;
    }
    return (ArrayInitializerBracketTree) function.arguments().get(0);
  }

  private static String buildErrorMessage(String id, String dotNotation, String hint) {
    StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
    stringJoiner.add("CFG structure is not the expected one for block " + id + " " + hint);
    stringJoiner.add("Use use a tool like http://www.webgraphviz.com/ to visualize the below graph in dot notation");
    stringJoiner.add("==========================================");
    stringJoiner.add(dotNotation);
    stringJoiner.add("==========================================");
    return stringJoiner.toString();
  }

  private static void assertAreTheSame(Set<CfgBlock> left, Set<CfgBlock> right, String errorMessage) {
    assertThat(Sets.symmetricDifference(left, right)).withFailMessage(errorMessage).isEmpty();
  }

  private static Set<CfgBlock> getCfgBlocks(Set<String> ids, Map<String, BlockMetadata> data) {
    return ids.stream().map(id -> data.get(id).getBlock()).collect(Collectors.toSet());
  }

  private static String[] getStrings(Tree tree) {
    List<String> result = new ArrayList<>();
    if (tree instanceof ArrayInitializerBracketTree) {
      ArrayInitializerBracketTree initializer = (ArrayInitializerBracketTree) tree;
      for (ArrayPairTree pair : initializer.arrayPairs()) {
        result.add(getValue(pair.value()));
      }
    }
    return result.toArray(new String[] {});
  }

  private static boolean isNamespaceTreeWithValue(@Nullable Tree tree, String s) {
    return tree != null &&
        tree.is(Tree.Kind.NAMESPACE_NAME) &&
        ((NamespaceNameTree)tree).fullName().equalsIgnoreCase(s);
  }

  private static String getValue(Tree tree) {
    if (tree.is(Tree.Kind.NUMERIC_LITERAL) || tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      return ((LiteralTree)tree).value();
    }
    if (tree.is(Tree.Kind.NAMESPACE_NAME)) {
      return ((NamespaceNameTree)tree).fullName();
    }
    throw new IllegalArgumentException("Cannot get literal value from tree");
  }
}
