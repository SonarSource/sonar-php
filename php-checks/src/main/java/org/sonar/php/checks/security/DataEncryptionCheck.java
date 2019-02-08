/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.type.StaticFunctionCall;
import org.sonar.php.tree.impl.expression.MemberAccessTreeImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.type.StaticFunctionCall.staticFunctionCall;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

@Rule(key = "S4787")
public class DataEncryptionCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure that encrypting data is safe here.";

  private static final Set<String> SUSPICIOUS_GLOBAL_FUNCTIONS = CheckUtils.lowerCaseSet(
    // Builtin functions
    "mcrypt_ecb",
    "mcrypt_cfb",
    "mcrypt_cbc",
    "mcrypt_encrypt",
    "openssl_encrypt",
    "openssl_public_encrypt",
    "openssl_pkcs7_encrypt",
    "openssl_seal",
    "sodium_crypto_aead_aes256gcm_encrypt",
    "sodium_crypto_aead_chacha20poly1305_encrypt",
    "sodium_crypto_aead_chacha20poly1305_ietf_encrypt",
    "sodium_crypto_aead_xchacha20poly1305_ietf_encrypt",
    "sodium_crypto_box_seal",
    "sodium_crypto_box",
    "sodium_crypto_secretbox",
    "sodium_crypto_stream_xor",

    // Drupal, Laravel or any other extension with an 'encrypt' helper
    "encrypt");

  private static final Set<String> ENCRYPTION_MEMBER = CheckUtils.lowerCaseSet("encryption");

  private static final Set<String> SUSPICIOUS_ENCRYPTION_FUNCTIONS = CheckUtils.lowerCaseSet(
    "create_key",
    "initialize",
    "encrypt");

  private static final Set<String> SUSPICIOUS_MEMBER_FUNCTIONS = CheckUtils.lowerCaseSet(
    // Craft and Yii frameworks
    "encryptByKey",
    "encryptByPassword");

  private static final List<StaticFunctionCall> SUSPICIOUS_STATIC_FUNCTIONS = Arrays.asList(
    // CakePHP
    staticFunctionCall("Cake\\Utility\\Security::encrypt"),
    staticFunctionCall("Cake\\Utility\\Security::engine"),

    // Laravel
    staticFunctionCall("Illuminate\\Support\\Facades\\Crypt::encrypt"),
    staticFunctionCall("Illuminate\\Support\\Facades\\Crypt::encryptString"),

    // Defuse PHP-Encryption
    staticFunctionCall("Defuse\\Crypto\\Crypto::encrypt"),
    staticFunctionCall("Defuse\\Crypto\\Crypto::encryptWithPassword"),
    staticFunctionCall("Defuse\\Crypto\\File::encryptFile"),
    staticFunctionCall("Defuse\\Crypto\\File::encryptFileWithPassword"),
    staticFunctionCall("Defuse\\Crypto\\File::encryptResource"),
    staticFunctionCall("Defuse\\Crypto\\File::encryptResourceWithPassword"),

    // Sodium Compat polyfill library
    staticFunctionCall("ParagonIE_Sodium_Compat::crypto_aead_chacha20poly1305_ietf_encrypt"),
    staticFunctionCall("ParagonIE_Sodium_Compat::crypto_aead_xchacha20poly1305_ietf_encrypt"),
    staticFunctionCall("ParagonIE_Sodium_Compat::crypto_aead_chacha20poly1305_encrypt"),
    staticFunctionCall("ParagonIE_Sodium_Compat::crypto_aead_aes256gcm_encrypt"),
    staticFunctionCall("ParagonIE_Sodium_Compat::crypto_box"),
    staticFunctionCall("ParagonIE_Sodium_Compat::crypto_secretbox"),
    staticFunctionCall("ParagonIE_Sodium_Compat::crypto_box_seal"),
    staticFunctionCall("ParagonIE_Sodium_Compat::crypto_secretbox_xchacha20poly1305"),

    // Zend
    staticFunctionCall("Zend\\Crypt\\PublicKey\\Rsa::factory"),
    staticFunctionCall("Zend\\Crypt\\BlockCipher::factory"));

  private static final List<QualifiedName> SUSPICIOUS_CLASS_INSTANTIATIONS = Arrays.asList(
    // Joomla
    qualifiedName("Joomla\\Crypt\\Cipher_Sodium"),
    qualifiedName("Joomla\\Crypt\\Cipher_Simple"),
    qualifiedName("Joomla\\Crypt\\Cipher_Rijndael256"),
    qualifiedName("Joomla\\Crypt\\Cipher_Crypto"),
    qualifiedName("Joomla\\Crypt\\Cipher_Blowfish"),
    qualifiedName("Joomla\\Crypt\\Cipher_3DES"),

    // PhpSecLib
    qualifiedName("phpseclib\\Crypt\\RSA"),
    qualifiedName("phpseclib\\Crypt\\AES"),
    qualifiedName("phpseclib\\Crypt\\Rijndael"),
    qualifiedName("phpseclib\\Crypt\\Twofish"),
    qualifiedName("phpseclib\\Crypt\\Blowfish"),
    qualifiedName("phpseclib\\Crypt\\RC4"),
    qualifiedName("phpseclib\\Crypt\\RC2"),
    qualifiedName("phpseclib\\Crypt\\TripleDES"),
    qualifiedName("phpseclib\\Crypt\\DES"),

    // Zend
    qualifiedName("Zend\\Crypt\\PublicKey\\DiffieHellman"),
    qualifiedName("Zend\\Crypt\\PublicKey\\Rsa"),
    qualifiedName("Zend\\Crypt\\FileCipher"),
    qualifiedName("Zend\\Crypt\\Hybrid"),
    qualifiedName("Zend\\Crypt\\BlockCipher"));

  private static final QualifiedName JOOMLA_CIPHER_INTERFACE = qualifiedName("Joomla\\Crypt\\CipherInterface");
  private static final QualifiedName CODE_IGNITER_CONTROLLER_CLASS = qualifiedName("CI_Controller");

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);
    checkSuspiciousClassDeclaration(tree);
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    super.visitAnonymousClass(tree);
    checkSuspiciousClassDeclaration(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();
    if (isSuspiciousGlobalFunction(callee) || isSuspiciousMemberFunction(callee) || isSuspiciousClassInstantiation(callee)) {
      context().newIssue(this, tree, MESSAGE);
    }

    super.visitFunctionCall(tree);
  }

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    if (isSuspiciousClassInstantiation(tree.expression())) {
      context().newIssue(this, tree, MESSAGE);
    }

    super.visitNewExpression(tree);
  }

  private void checkSuspiciousClassDeclaration(ClassTree tree) {
    NamespaceNameTree superClass = tree.superClass();
    if (superClass != null) {
      QualifiedName fullyQualifiedSuperclassName = getFullyQualifiedName(superClass);
      if (fullyQualifiedSuperclassName.equals(CODE_IGNITER_CONTROLLER_CLASS)) {
        checkCodeIgniterControllerMethods(tree);
      }
    }

    tree.superInterfaces().stream()
      .filter(superInterface -> JOOMLA_CIPHER_INTERFACE.equals(getFullyQualifiedName(superInterface)))
      .forEach(superInterface -> context().newIssue(this, superInterface, MESSAGE));
  }

  private void checkCodeIgniterControllerMethods(ClassTree classTree) {
    for (ClassMemberTree member : classTree.members()) {
      if (member.is(Tree.Kind.METHOD_DECLARATION)) {
        MethodDeclarationTree method = (MethodDeclarationTree) member;
        CodeIgniterMethodCallChecker codeIgniterMethodCallChecker = new CodeIgniterMethodCallChecker();
        method.body().accept(codeIgniterMethodCallChecker);
        codeIgniterMethodCallChecker.suspiciousFunctionCalls.forEach(tree -> context().newIssue(this, tree, MESSAGE));
      }
    }
  }

  private boolean isSuspiciousMemberFunction(ExpressionTree callee) {
    if (callee.is(Tree.Kind.CLASS_MEMBER_ACCESS, Tree.Kind.OBJECT_MEMBER_ACCESS)) {
      MemberAccessTreeImpl memberAccess = (MemberAccessTreeImpl) callee;
      if (isStaticFunction(memberAccess)) {
        QualifiedName className = getFullyQualifiedName((NamespaceNameTree) memberAccess.object());
        String memberName = ((NameIdentifierTree) memberAccess.member()).text();
        return SUSPICIOUS_STATIC_FUNCTIONS.stream().anyMatch(staticFunctionCall -> staticFunctionCall.matches(className, memberName));
      } else if (memberAccess.member().is(Tree.Kind.NAME_IDENTIFIER)) {
        return SUSPICIOUS_MEMBER_FUNCTIONS.contains(((NameIdentifierTree) memberAccess.member()).text().toLowerCase(Locale.ROOT));
      }
    }
    return false;
  }

  private boolean isSuspiciousClassInstantiation(ExpressionTree callee) {
    if (callee.is(Tree.Kind.NAMESPACE_NAME)) {
      NamespaceNameTree classNameTree = (NamespaceNameTree) callee;
      QualifiedName className = getFullyQualifiedName(classNameTree);
      return SUSPICIOUS_CLASS_INSTANTIATIONS.stream().anyMatch(className::equals);
    }
    return false;
  }

  private static boolean isSuspiciousGlobalFunction(ExpressionTree callee) {
    return callee.is(Tree.Kind.NAMESPACE_NAME) &&
      SUSPICIOUS_GLOBAL_FUNCTIONS.contains(((NamespaceNameTree) callee).qualifiedName().toLowerCase(Locale.ROOT));
  }

  private static boolean isStaticFunction(MemberAccessTreeImpl memberAccess) {
    return memberAccess.isStatic() && memberAccess.object().is(Tree.Kind.NAMESPACE_NAME) && memberAccess.member().is(Tree.Kind.NAME_IDENTIFIER);
  }

  private static class CodeIgniterMethodCallChecker extends PHPVisitorCheck {
    private List<Tree> suspiciousFunctionCalls = new ArrayList<>();

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      ExpressionTree callee = tree.callee();

      if (isSuspiciousEncryptionFunction(callee)) {
        suspiciousFunctionCalls.add(tree);
      }

      super.visitFunctionCall(tree);
    }

    private static boolean isSuspiciousEncryptionFunction(Tree tree) {
      return isMemberAccess(tree, SUSPICIOUS_ENCRYPTION_FUNCTIONS) && isMemberAccess(((MemberAccessTree) tree).object(), ENCRYPTION_MEMBER);
    }

    private static boolean isMemberAccess(Tree tree, Set<String> expectedNames) {
      if (tree.is(Tree.Kind.OBJECT_MEMBER_ACCESS)) {
        Tree member = ((MemberAccessTree) tree).member();
        if (member.is(Tree.Kind.NAME_IDENTIFIER)) {
          return expectedNames.contains(((NameIdentifierTree) member).text().toLowerCase(Locale.ROOT));
        }
      }
      return false;
    }
  }

}
