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
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ArrayInitializerTree;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import javax.annotation.Nullable;
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
  private static final String MESSAGE_FTP = "Using ftp_connect() is insecure. Use ftp_ssl_connect() instead";
  private static final String MESSAGE_LARAVEL = "Mail transport without encryption is insecure. Specify an encryption";

  /**
   * To avoid unnecessary steps, we check for clear-text laravel SMTP configuration only when we are in a file "config/mail.php"
   * as the configuration is usually done in there.
   */
  private boolean inLaravelConfigFile;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    inLaravelConfigFile = context().getPhpFile().uri().getPath().endsWith("config/mail.php");
    super.visitCompilationUnit(tree);
  }

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

  @Override
  public void visitReturnStatement(ReturnStatementTree tree) {
    if (inLaravelConfigFile && tree.expression() != null && tree.expression().is(Tree.Kind.ARRAY_INITIALIZER_BRACKET, Tree.Kind.ARRAY_INITIALIZER_FUNCTION)) {
      checkLaravelMailConfig((ArrayInitializerTree)tree.expression());
    }
    super.visitReturnStatement(tree);
  }

  private void checkLaravelMailConfig(ArrayInitializerTree tree) {
    ArrayInitializerTree mailerConfigs = tree.arrayPairs().stream()
      .filter(p -> p.key() != null)
      .filter(p -> p.key().is(Tree.Kind.REGULAR_STRING_LITERAL))
      .filter(p -> "mailers".equals(CheckUtils.trimQuotes((LiteralTree) p.key())))
      .filter(p -> p.value().is(Tree.Kind.ARRAY_INITIALIZER_BRACKET, Tree.Kind.ARRAY_INITIALIZER_FUNCTION))
      .map(p -> ((ArrayInitializerTree)p.value()))
      .findFirst().orElse(null);

    if (mailerConfigs == null) {
      return;
    }

    for (ArrayPairTree pairTree : mailerConfigs.arrayPairs()) {
      if (pairTree.key() == null || !pairTree.value().is(Tree.Kind.ARRAY_INITIALIZER_BRACKET, Tree.Kind.ARRAY_INITIALIZER_FUNCTION)) {
        continue;
      }

      LaravelMailConfig config = LaravelMailConfig.of((ArrayInitializerTree) pairTree.value());
      if (config.isSmtp() && config.isClearText()) {
        if (config.encryption == null) {
          context().newIssue(this, pairTree.key(), MESSAGE_LARAVEL);
        } else {
          context().newIssue(this, config.encryption, MESSAGE_LARAVEL);
        }
      }
    }
  }

  private static class LaravelMailConfig {
    private final ExpressionTree transport;
    private final ExpressionTree host;
    private final ExpressionTree encryption;

    public LaravelMailConfig(@Nullable ExpressionTree transport, @Nullable ExpressionTree host, @Nullable ExpressionTree encryption) {
      this.transport = transport;
      this.host = host;
      this.encryption = encryption;
    }

    private static LaravelMailConfig of(ArrayInitializerTree tree) {
      ExpressionTree transport = null;
      ExpressionTree host = null;
      ExpressionTree encryption = null;

      for (ArrayPairTree pairTree : tree.arrayPairs()) {
        if (pairTree.key() == null || !pairTree.key().is(Tree.Kind.REGULAR_STRING_LITERAL)) {
          continue;
        }

        String key = CheckUtils.trimQuotes((LiteralTree) pairTree.key());

        switch (key) {
          case "transport":
            transport = pairTree.value();
            break;
          case "host":
            host = pairTree.value();
            break;
          case "encryption":
            encryption = pairTree.value();
            break;
          default:
        }
      }

      return new LaravelMailConfig(transport, host, encryption);
    }

    private boolean isClearText() {
      return hasInsecureEncryption() && hasInsecureHost();
    }

    private boolean hasInsecureHost() {
      if (host == null) {
        return false;
      }

      ExpressionTree hostValueTree = CheckUtils.assignedValue(host);
      if (!hostValueTree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
        // We don't know in this case
        return false;
      }

      String hostValue = CheckUtils.trimQuotes((LiteralTree) hostValueTree).toLowerCase(Locale.ROOT);
      return !(hostValue.startsWith("ssl://") || hostValue.startsWith("tls://")) && !LOOPBACK_IP.matcher(hostValue).matches();
    }

    private boolean hasInsecureEncryption() {
      if (encryption == null) {
        return true;
      }

      ExpressionTree encryptionValueTree = CheckUtils.assignedValue(encryption);
      if (encryptionValueTree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
        String encryptionValue = CheckUtils.trimQuotes((LiteralTree) encryptionValueTree).toLowerCase(Locale.ROOT);
        return !("ssl".equals(encryptionValue) || "tls".equals(encryptionValue));
      }

      return encryptionValueTree.is(Tree.Kind.NULL_LITERAL);
    }

    private boolean isSmtp() {
      return transport != null && transport.is(Tree.Kind.REGULAR_STRING_LITERAL) && "smtp".equals(CheckUtils.trimQuotes((LiteralTree) transport));
    }
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
