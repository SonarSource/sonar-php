/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
package org.sonar.php.checks.security;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Rule(key = "S5332")
public class ClearTextProtocolsCheck extends PHPVisitorCheck {
  private static final List<String> UNSAFE_PROTOCOLS = Arrays.asList("http://", "ftp://", "telnet://");
  private static final Map<String, String> ALTERNATIVE_PROTOCOLS = new HashMap<>();
  static {
    ALTERNATIVE_PROTOCOLS.put("http", "https");
    ALTERNATIVE_PROTOCOLS.put("ftp", "sftp, scp or ftps");
    ALTERNATIVE_PROTOCOLS.put("telnet", "ssh");
  }
  private static final String LOOPBACK_IPV4 = "^127(?:\\.[0-9]+){0,2}\\.[0-9]+$";
  private static final String LOOPBACK_IPV6 = "^(?:0*:){0,7}?:?0*1$";
  private static final Pattern LOOPBACK_IP = Pattern.compile(LOOPBACK_IPV4 + "|" + LOOPBACK_IPV6);

  private static final String MESSAGE_PROTOCOL = "Using %s protocol is insecure. Use %s instead";
  private static final String MESSAGE_FTP = "Using ftp_connect() can be insecure. use ftp_ssl_connect() instead";

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (!tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      return;
    }

    String value = CheckUtils.trimQuotes(tree).trim().toLowerCase(Locale.ROOT);

    if (!value.matches("\\S+")) {
      // If the trimmed value contains a whitespace or is empty, it is probably not a URL
      return;
    }

    if (startsWithUnsafeProtocol(value) && !isLoopbackUrl(value)) {
      String usedProtocol = ALTERNATIVE_PROTOCOLS.keySet().stream().filter(value::startsWith)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("URL should start with unsafe protocol at this point"));
      context().newIssue(this, tree, String.format(MESSAGE_PROTOCOL, usedProtocol, ALTERNATIVE_PROTOCOLS.get(usedProtocol)));
    }
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if("ftp_connect".equalsIgnoreCase(CheckUtils.getFunctionName(tree))) {
      context().newIssue(this, tree.callee(), MESSAGE_FTP);
    }

    super.visitFunctionCall(tree);
  }

  private static boolean startsWithUnsafeProtocol(String value) {
    return UNSAFE_PROTOCOLS.stream().anyMatch(value::startsWith);
  }

  private static boolean isLoopbackUrl(String value) {
    URI uri;

    try {
      uri = new URI(value);
    } catch (URISyntaxException e) {
      return false;
    }

    String host = uri.getHost();
    if (host == null) {
      // Fallback for some IPV6 cases
      host = uri.getAuthority();
    }

    return "localhost".equals(host) || LOOPBACK_IP.matcher(host).matches();
  }
}
