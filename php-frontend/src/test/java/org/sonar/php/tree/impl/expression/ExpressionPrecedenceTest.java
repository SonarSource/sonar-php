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
package org.sonar.php.tree.impl.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpressionPrecedenceTest extends PHPTreeModelTest {

  @Test
  public void operator_precedence_and_associativity() throws Exception {
    assertPrecedence("2 ** 3 ** 4", "2 ** (3 ** 4)");
    assertPrecedence("2 ** 3 * 4", "(2 ** 3) * 4");
    assertPrecedence("2 * 3 ** 4", "2 * (3 ** 4)");
    assertPrecedence("2 * 3 * 4", "(2 * 3) * 4");
    assertPrecedence("2 * 3 / 4", "(2 * 3) / 4");
    assertPrecedence("2 / 3 * 4", "(2 / 3) * 4");
    assertPrecedence("2 / 3 % 4", "(2 / 3) % 4");
    assertPrecedence("2 % 3 / 4", "(2 % 3) / 4");
    assertPrecedence("2 * 3 + 4", "(2 * 3) + 4");
    assertPrecedence("2 + 3 * 4", "2 + (3 * 4)");
    assertPrecedence("2 + 3 + 4", "(2 + 3) + 4");
    assertPrecedence("2 + 3 - 4", "(2 + 3) - 4");
    assertPrecedence("2 - 3 + 4", "(2 - 3) + 4");
    assertPrecedence("2 - 3 . 4", "(2 - 3) . 4");
    assertPrecedence("2 . 3 - 4", "(2 . 3) - 4");
    assertPrecedence("2 + 3 << 4", "(2 + 3) << 4");
    assertPrecedence("2 << 3 + 4", "2 << (3 + 4)");
    assertPrecedence("2 << 3 << 4", "(2 << 3) << 4");
    assertPrecedence("2 << 3 >> 4", "(2 << 3) >> 4");
    assertPrecedence("2 >> 3 << 4", "(2 >> 3) << 4");
    assertPrecedence("2 << 3 < 4", "(2 << 3) < 4");
    assertPrecedence("2 < 3 << 4", "2 < (3 << 4)");
    assertPrecedence("2 < 3 < 4", "(2 < 3) < 4");
    assertPrecedence("2 < 3 <= 4", "(2 < 3) <= 4");
    assertPrecedence("2 <= 3 < 4", "(2 <= 3) < 4");
    assertPrecedence("2 <= 3 > 4", "(2 <= 3) > 4");
    assertPrecedence("2 > 3 <= 4", "(2 > 3) <= 4");
    assertPrecedence("2 > 3 >= 4", "(2 > 3) >= 4");
    assertPrecedence("2 >= 3 > 4", "(2 >= 3) > 4");
    assertPrecedence("2 < 3 == 4", "(2 < 3) == 4");
    assertPrecedence("2 == 3 < 4", "2 == (3 < 4)");
    assertPrecedence("2 == 3 == 4", "(2 == 3) == 4");
    assertPrecedence("2 == 3 != 4", "(2 == 3) != 4");
    assertPrecedence("2 != 3 == 4", "(2 != 3) == 4");
    assertPrecedence("2 != 3 === 4", "(2 != 3) === 4");
    assertPrecedence("2 === 3 != 4", "(2 === 3) != 4");
    assertPrecedence("2 === 3 !== 4", "(2 === 3) !== 4");
    assertPrecedence("2 !== 3 === 4", "(2 !== 3) === 4");
    assertPrecedence("2 !== 3 <> 4", "(2 !== 3) <> 4");
    assertPrecedence("2 <> 3 !== 4", "(2 <> 3) !== 4");
    assertPrecedence("2 <> 3 <=> 4", "(2 <> 3) <=> 4");
    assertPrecedence("2 <=> 3 <> 4", "(2 <=> 3) <> 4");
    assertPrecedence("2 == 3 & 4", "(2 == 3) & 4");
    assertPrecedence("2 & 3 == 4", "2 & (3 == 4)");
    assertPrecedence("2 & 3 & 4", "(2 & 3) & 4");
    assertPrecedence("2 & 3 ^ 4", "(2 & 3) ^ 4");
    assertPrecedence("2 ^ 3 & 4", "2 ^ (3 & 4)");
    assertPrecedence("2 ^ 3 ^ 4", "(2 ^ 3) ^ 4");
    assertPrecedence("2 ^ 3 | 4", "(2 ^ 3) | 4");
    assertPrecedence("2 | 3 ^ 4", "2 | (3 ^ 4)");
    assertPrecedence("2 | 3 | 4", "(2 | 3) | 4");
    assertPrecedence("2 | 3 && 4", "(2 | 3) && 4");
    assertPrecedence("2 && 3 | 4", "2 && (3 | 4)");
    assertPrecedence("2 && 3 && 4", "(2 && 3) && 4");
    assertPrecedence("2 && 3 || 4", "(2 && 3) || 4");
    assertPrecedence("2 || 3 && 4", "2 || (3 && 4)");
    assertPrecedence("2 || 3 || 4", "(2 || 3) || 4");
    assertPrecedence("2 || 3 ?? 4", "(2 || 3) ?? 4");
    assertPrecedence("2 ?? 3 || 4", "2 ?? (3 || 4)");
    assertPrecedence("2 ?? 3 ?? 4", "2 ?? (3 ?? 4)");
    assertPrecedence("2 and $a = 4", "2 and ($a = 4)");
    assertPrecedence("2 and 3 and 4", "(2 and 3) and 4");
    assertPrecedence("2 and 3 xor 4", "(2 and 3) xor 4");
    assertPrecedence("2 xor 3 and 4", "2 xor (3 and 4)");
    assertPrecedence("2 xor 3 xor 4", "(2 xor 3) xor 4");
    assertPrecedence("2 xor 3 or 4", "(2 xor 3) or 4");
    assertPrecedence("2 or 3 xor 4", "2 or (3 xor 4)");
    assertPrecedence("2 or 3 or 4", "(2 or 3) or 4");
    assertPrecedence("$a ?? $b = 4", "$a ?? ($b = 4)");
    assertPrecedence("!!$a", "! (! $a)");
    assertPrecedence("++ $a --", "++ ($a --)");
    assertPrecedence("++ -- $a", "++ (-- $a)");
    assertPrecedence("(int) $a ++", "( int ) ($a ++)");
    assertPrecedence("! $a instanceof B", "! ($a instanceof B)");
    assertPrecedence("$a = true ? 0 : true ? 1 : 2", "$a = ((true ? 0 : true) ? 1 : 2)");
    assertPrecedence("$x &&  $y ? $a : $b", "($x && $y) ? $a : $b");
    assertPrecedence("$x and $y ? $a : $b", "$x and ($y ? $a : $b)");
    assertPrecedence("- 3 ** 2", "- (3 ** 2)");
    assertPrecedence("(int) $a ** 2", "( int ) ($a ** 2)");
  }

  @Test
  public void assignment() throws Exception {
    assertPrecedence("$a = 3 ?? 4", "$a = (3 ?? 4)");
    assertPrecedence("$a = $b = 4", "$a = ($b = 4)");
    assertPrecedence("$a = $b += 4", "$a = ($b += 4)");
    assertPrecedence("$a += $b = 4", "$a += ($b = 4)");
    assertPrecedence("$a += $b -= 4", "$a += ($b -= 4)");
    assertPrecedence("$a -= $b += 4", "$a -= ($b += 4)");
    assertPrecedence("$a -= $b *= 4", "$a -= ($b *= 4)");
    assertPrecedence("$a *= $b -= 4", "$a *= ($b -= 4)");
    assertPrecedence("$a *= $b **= 4", "$a *= ($b **= 4)");
    assertPrecedence("$a **= $b *= 4", "$a **= ($b *= 4)");
    assertPrecedence("$a **= $b /= 4", "$a **= ($b /= 4)");
    assertPrecedence("$a /= $b **= 4", "$a /= ($b **= 4)");
    assertPrecedence("$a /= $b .= 4", "$a /= ($b .= 4)");
    assertPrecedence("$a .= $b /= 4", "$a .= ($b /= 4)");
    assertPrecedence("$a .= $b %= 4", "$a .= ($b %= 4)");
    assertPrecedence("$a %= $b .= 4", "$a %= ($b .= 4)");
    assertPrecedence("$a %= $b &= 4", "$a %= ($b &= 4)");
    assertPrecedence("$a &= $b %= 4", "$a &= ($b %= 4)");
    assertPrecedence("$a &= $b |= 4", "$a &= ($b |= 4)");
    assertPrecedence("$a |= $b &= 4", "$a |= ($b &= 4)");
    assertPrecedence("$a |= $b ^= 4", "$a |= ($b ^= 4)");
    assertPrecedence("$a ^= $b |= 4", "$a ^= ($b |= 4)");
    assertPrecedence("$a ^= $b <<= 4", "$a ^= ($b <<= 4)");
    assertPrecedence("$a <<= $b ^= 4", "$a <<= ($b ^= 4)");
    assertPrecedence("$a <<= $b >>= 4", "$a <<= ($b >>= 4)");
    assertPrecedence("$a >>= $b <<= 4", "$a >>= ($b <<= 4)");
    assertPrecedence("$a = 3 and 4", "($a = 3) and 4");
    assertPrecedence("4 and $a = 3", "4 and ($a = 3)");
    assertPrecedence("4 && $a = 3", "4 && ($a = 3)");
    assertPrecedence("$a = 3 && 4", "$a = (3 && 4)");
    assertPrecedence("$x and $a = $a ? 1 : 2", "$x and ($a = ($a ? 1 : 2))");
  }

  private void assertPrecedence(String code, String expected) throws Exception {
    ExpressionTree expression = parse(code, PHPLexicalGrammar.EXPRESSION);
    String actual = dumpWithParentheses(expression).stream().collect(Collectors.joining(" "));
    assertThat(actual).isEqualTo(expected);
  }

  private static List<String> dumpWithParentheses(@Nullable Tree tree) {
    if (tree == null) {
      return Collections.emptyList();
    } else if (tree instanceof SyntaxToken) {
      return Collections.singletonList(((SyntaxToken) tree).text());
    } else {
      List<String> children = new ArrayList<>();
      Iterator<Tree> iterator = ((PHPTree) tree).childrenIterator();
      while (iterator.hasNext()) {
        List<String> child = dumpWithParentheses(iterator.next());
        if (child.size() == 1) {
          children.add(child.get(0));
        } else if (child.size() > 1) {
          children.add("(" + child.stream().collect(Collectors.joining(" ")) + ")");
        }
      }
      return children;
    }
  }

}
