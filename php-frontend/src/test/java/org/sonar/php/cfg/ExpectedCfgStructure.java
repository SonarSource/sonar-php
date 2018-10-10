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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerBracketTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;

class ExpectedCfgStructure {

  // The string value is the CfgBlock test id
  private final BiMap<CfgBlock, String> testIds;

  // The key is CfgBlock test id
  private final Map<String, BlockExpectation> expectations;

  private ExpectedCfgStructure() {
    testIds = HashBiMap.create();
    expectations = new HashMap<>();
  }

  static ExpectedCfgStructure parse(Set<CfgBlock> blocks) {
    return Parser.parse(blocks);
  }

  int size() {
    return expectations.size();
  }

  boolean hasNonEmptyPredecessors() {
    return expectations.values().stream()
      .anyMatch(expectation -> !expectation.expectedPredecessorIds.isEmpty());
  }

  boolean hasNonEmptyElementNumbers() {
    return expectations.values().stream()
      .anyMatch(expectation -> expectation.expectedNumberOfElements != -1);
  }

  String testId(CfgBlock block) {
    return testIds.get(block);
  }

  CfgBlock cfgBlock(String testId) {
    return testIds.inverse().get(testId);
  }

  List<String> expectedSucc(CfgBlock block) {
    return getExpectation(block).expectedSuccessorIds;
  }

  List<String> expectedPred(CfgBlock block) {
    return getExpectation(block).expectedPredecessorIds;
  }

  int expectedNumberOfElements(CfgBlock block) {
    return getExpectation(block).expectedNumberOfElements;
  }

  private BlockExpectation getExpectation(CfgBlock block) {
    return expectations.get(testId(block));
  }

  private BlockExpectation createExpectation(CfgBlock cfgBlock, String testId) {
    testIds.put(cfgBlock, testId);
    BlockExpectation expectation = new BlockExpectation();
    expectations.put(testId, expectation);
    return expectation;
  }

  private class BlockExpectation {
    private final List<String> expectedSuccessorIds = new ArrayList<>();
    private final List<String> expectedPredecessorIds = new ArrayList<>();
    private int expectedNumberOfElements = -1;

    BlockExpectation withSuccessorsIds(String... ids) {
      Collections.addAll(expectedSuccessorIds, ids);
      return this;
    }

    BlockExpectation withPredecessorIds(String... ids) {
      Collections.addAll(expectedPredecessorIds, ids);
      return this;
    }

    BlockExpectation withElementNumber(int elementNumber) {
      expectedNumberOfElements = elementNumber;
      return this;
    }

  }

  /**
   * The expected structure for each basic block is encoded in a function call
   * See {@link ControlFlowGraphTest} for details
   */
  private static class Parser {

    static ExpectedCfgStructure parse(Set<CfgBlock> blocks) {
      ExpectedCfgStructure result = new ExpectedCfgStructure();

      for (CfgBlock block : blocks) {
        if (block instanceof PhpCfgEndBlock) {
          result.createExpectation(block, "END");
          continue;
        }

        FunctionCallTree blockFunction = getBlockFunctionCall(block.elements().get(0));
        if (blockFunction == null) {
          throw new UnsupportedOperationException("CFG Block metadata must be the first statement in the block");
        }

        String id = getValue(blockFunction.callee());
        String[] pred = {};
        String[] succ = {};
        int elem = -1;
        for (ExpressionTree argument : blockFunction.arguments()) {
          if (!argument.is(Tree.Kind.ASSIGNMENT)) {
            throw new UnsupportedOperationException("The arguments of must be assignments");
          }
          AssignmentExpressionTree assignment = (AssignmentExpressionTree) argument;
          Tree name = assignment.variable();
          if (isNamespaceTreeWithValue(name, "succ")) {
            succ = getStrings(assignment.value());
          } else if (isNamespaceTreeWithValue(name, "pred")) {
            pred = getStrings(assignment.value());
          } else if (isNamespaceTreeWithValue(name, "elem")) {
            elem = Integer.parseInt(getValue(assignment.value()));
          }
        }

        if (id != null) {
          result.createExpectation(block, id)
            .withSuccessorsIds(succ)
            .withPredecessorIds(pred)
            .withElementNumber(elem);
        } else {
          throw new UnsupportedOperationException("CFG Block metadata is not in expected format");
        }
      }

      return result;
    }

    private static FunctionCallTree getBlockFunctionCall(Tree firstElement) {
      ExpressionTree expressionTree;
      if (firstElement instanceof ExpressionStatementTree) {
        expressionTree = ((ExpressionStatementTree) firstElement).expression();
      } else if (firstElement instanceof ExpressionTree) {
        expressionTree = (ExpressionTree) firstElement;
      } else {
        return null;
      }

      if (!(expressionTree instanceof FunctionCallTree)) {
        return null;
      }
      FunctionCallTree function = (FunctionCallTree) expressionTree;
      if (function.arguments().isEmpty() ||
        !(function.callee() instanceof NamespaceNameTree)) {
        return null;
      }
      return function;

    }

    private static String[] getStrings(Tree tree) {
      List<String> result = new ArrayList<>();
      if (tree instanceof ArrayInitializerBracketTree) {
        ArrayInitializerBracketTree initializer = (ArrayInitializerBracketTree) tree;
        for (ArrayPairTree pair : initializer.arrayPairs()) {
          result.add(getValue(pair.value()));
        }
      }
      return result.toArray(new String[]{});
    }

    private static boolean isNamespaceTreeWithValue(@Nullable Tree tree, String s) {
      return tree != null &&
        tree.is(Tree.Kind.NAMESPACE_NAME) &&
        ((NamespaceNameTree) tree).fullName().equalsIgnoreCase(s);
    }

    private static String getValue(Tree tree) {
      if (tree.is(Tree.Kind.NUMERIC_LITERAL) || tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
        return ((LiteralTree) tree).value();
      }
      if (tree.is(Tree.Kind.NAMESPACE_NAME)) {
        return ((NamespaceNameTree) tree).fullName();
      }
      throw new IllegalArgumentException("Cannot get literal value from tree");
    }
  }
}
