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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.TestFileExcludedCheck;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.skipParenthesis;
import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

@Rule(key = "S1313")
public class HardCodedIpAddressCheck extends PHPVisitorCheck implements TestFileExcludedCheck {

  private static final String LOOPBACK_IPV4 = "^127(?:\\.\\d+){0,2}\\.\\d+$";
  private static final String LOOPBACK_IPV6 = "^(?:0*:){0,7}?:?0*1$";
  private static final String LOOPBACK_IPV4_MAPPED_TO_IPV6 = "^(::(?i)ffff(:0{1,4})?):127$";
  private static final Pattern LOOPBACK_IP = Pattern.compile(LOOPBACK_IPV4 + "|" + LOOPBACK_IPV6 + "|" + LOOPBACK_IPV4_MAPPED_TO_IPV6);

  private static final String PROTOCOL = "((\\w+:)?\\/\\/)?";
  public static final String IP_V4 = "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[1-9])(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}(?![\\w\\.])";

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
    ")(?![\\w:])";
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

  private final Set<LiteralTree> candidates = new HashSet<>();

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    candidates.clear();
    super.visitCompilationUnit(tree);
    if (!candidates.isEmpty()) {
      new VersionComparisonFilter(candidates).analyze(context());
    }
    if (!candidates.isEmpty()) {
      new ClassNameSuffixFilter(candidates).analyze(context());
    }
    for (LiteralTree candidate : candidates) {
      context().newIssue(this, candidate, MESSAGE);
    }
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      String literalValue = trimQuotes(tree.value());

      if (!couldBeIpAddress(literalValue)) {
        return;
      }

      Matcher matcher = IP_PATTERN.matcher(literalValue);
      if (matcher.lookingAt()) {
        String ip = matcher.group("ip");
        if (isSensitive(ip)) {
          candidates.add(tree);
        }
      }
    }
    super.visitLiteral(tree);
  }

  private static boolean couldBeIpAddress(String value) {
    // An IPv4 cannot start with a dot thus the ">". A v6 one can start with ":".
    return value.indexOf('.') > 0 || value.indexOf(':') >= 0;
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

  private static boolean isBuiltinFunction(FunctionCallTree call, QualifiedName expectedName) {
    if (!(call.callee() instanceof NamespaceNameTree)) {
      return false;
    }
    var symbol = Symbols.get(call);
    // Builtins have no declaration in the function index; known symbols represent user-defined functions.
    boolean isBuiltin = symbol.isUnknownSymbol();
    return isBuiltin && expectedName.equals(symbol.qualifiedName());
  }

  // Removes string literals passed directly to builtin version_compare.
  private static class VersionComparisonFilter extends PHPVisitorCheck {
    private static final QualifiedName VERSION_COMPARE = QualifiedName.qualifiedName("version_compare");

    private final Set<LiteralTree> candidates;

    private VersionComparisonFilter(Set<LiteralTree> candidates) {
      this.candidates = candidates;
    }

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      if (isBuiltinFunction(tree, VERSION_COMPARE)) {
        for (CallArgumentTree argument : tree.callArguments()) {
          if (skipParenthesis(argument.value()) instanceof LiteralTree literal) {
            candidates.remove(literal);
          }
        }
      }
      super.visitFunctionCall(tree);
    }

  }

  // Removes string literals appended directly to builtin get_class results.
  private static class ClassNameSuffixFilter extends PHPVisitorCheck {
    private static final QualifiedName GET_CLASS = QualifiedName.qualifiedName("get_class");

    private final Set<LiteralTree> candidates;

    private ClassNameSuffixFilter(Set<LiteralTree> candidates) {
      this.candidates = candidates;
    }

    @Override
    public void visitBinaryExpression(BinaryExpressionTree tree) {
      if (tree.is(Tree.Kind.CONCATENATION)
        && skipParenthesis(tree.leftOperand()) instanceof FunctionCallTree call
        && isBuiltinFunction(call, GET_CLASS)
        && skipParenthesis(tree.rightOperand()) instanceof LiteralTree suffix) {
        candidates.remove(suffix);
      }
      super.visitBinaryExpression(tree);
    }

  }

}
