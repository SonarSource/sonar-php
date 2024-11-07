/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

@Rule(key = "S1313")
public class HardCodedIpAddressCheck extends PHPVisitorCheck {

  private static final String LOOPBACK_IPV4 = "^127(?:\\.\\d+){0,2}\\.\\d+$";
  private static final String LOOPBACK_IPV6 = "^(?:0*:){0,7}?:?0*1$";
  private static final String LOOPBACK_IPV4_MAPPED_TO_IPV6 = "^(::(?i)ffff(:0{1,4})?):127$";
  private static final Pattern LOOPBACK_IP = Pattern.compile(LOOPBACK_IPV4 + "|" + LOOPBACK_IPV6 + "|" + LOOPBACK_IPV4_MAPPED_TO_IPV6);

  private static final String PROTOCOL = "((\\w+:)?\\/\\/)?";
  public static final String IP_V4 = "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[1-9])(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}(?!\\d)";

  // @spotless:off
  public static final String IP_V6 = "\\[?(" +
    "([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|" +         // 1:2:3:4:5:6:7:8
    "([0-9a-fA-F]{1,4}:){1,7}:|"+                         // 1::                              1:2:3:4:5:6:7::
    "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +        // 1::8             1:2:3:4:5:6::8  1:2:3:4:5:6::8
    "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|"+  // 1::7:8           1:2:3:4:5::7:8  1:2:3:4:5::8
    "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|"+  // 1::6:7:8         1:2:3:4::6:7:8  1:2:3:4::8
    "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|"+  // 1::5:6:7:8       1:2:3::5:6:7:8  1:2:3::8
    "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|"+  // 1::4:5:6:7:8     1:2::4:5:6:7:8  1:2::8
    "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|"+       // 1::3:4:5:6:7:8   1::3:4:5:6:7:8  1::8
    ":((:[0-9a-fA-F]{1,4}){1,7})|"+                     // ::2:3:4:5:6:7:8  ::2:3:4:5:6:7:8 ::8
    "fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|"+     // fe80::7:8%eth0   fe80::7:8%1     (link-local IPv6 addresses with zone index)
    "::(ffff(:0{1,4}){0,1}:){0,1}" +
    "((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}" +
    "(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|" +          //::255.255.255.255   ::ffff:255.255.255.255  ::ffff:0:255.255.255.255  (IPv4-mapped IPv6 addresses and IPv4-translated addresses)
    "([0-9a-fA-F]{1,4}:){1,4}:" +
    "((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}" +
    "(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])"+           // 2001:db8:3:4::192.0.2.33  64:ff9b::192.0.2.33 (IPv4-Embedded IPv6 Address)
    ")(?![\\d\\w:])";
  // @spotless:on

  private static final Pattern IP_PATTERN = Pattern.compile(String.format("%s(?<ip>((%s)|(%s)))", PROTOCOL, IP_V4, IP_V6));

  private static final List<String> RESERVED_IP_PREFIXES = List.of(
    "192.0.2.",
    "198.51.100.",
    "203.0.113.",
    "2001:0db8:",
    "2001:db8:");

  private static final String MESSAGE = "Make sure using this hardcoded IP address is safe here.";
  private static final String BROADCAST_IPV4 = "255.255.255.255";

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      String literalValue = trimQuotes(tree.value());
      Matcher matcher = IP_PATTERN.matcher(literalValue);
      if (matcher.find() && matcher.start() == 0) {
        String ip = matcher.group("ip");
        if (isSensitive(ip)) {
          context().newIssue(this, tree, MESSAGE);
        }
      }
    }
    super.visitLiteral(tree);
  }

  private static boolean isSensitive(String ip) {
    return !isLoopback(ip) && !isBroadcast(ip) && !isReservedIP(ip);
  }

  private static boolean isLoopback(String ip) {
    ip = ip.replace("[", "");
    return LOOPBACK_IP.matcher(ip).find();
  }

  private static boolean isBroadcast(String ip) {
    return BROADCAST_IPV4.equals(ip);
  }

  private static boolean isReservedIP(String ip) {
    return RESERVED_IP_PREFIXES.stream().anyMatch(ip::startsWith);
  }
}
