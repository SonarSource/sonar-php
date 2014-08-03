/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.metrics;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.graph.DirectedGraph;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class DependenciesVisitor extends SquidAstVisitor<Grammar> {

  private static final Logger LOG = LoggerFactory.getLogger(DependenciesVisitor.class);

  private final DirectedGraph<SourceCode, SourceCodeEdge> graph;

  private String namespace;
  private HashMap<String, String> uses = new HashMap<String, String>();

  private SourceClass currentClass;

  public DependenciesVisitor(DirectedGraph<SourceCode, SourceCodeEdge> graph) {
    super();
    this.graph = graph;
  }

  private static String getAstNodeValue(AstNode astNode) {
    StringBuilder sb = new StringBuilder();
    for (Token token : astNode.getTokens()) {
      if (token.getType() == PHPPunctuator.SEMICOLON) {
        break;
      }
      if (token.getType() == PHPKeyword.NAMESPACE) {
        continue;
      }
      sb.append(token.getOriginalValue());
    }
    return sb.toString();
  }

  @Override
  public void visitFile(AstNode astNode) {
    namespace = "";
    currentClass = null;
    uses = new HashMap<String, String>();
    SourceCode sourceFile = getContext().peekSourceCode();
    LOG.debug("Searching dependencies in "+sourceFile.getKey());
    if (!SourceFile.class.isInstance(sourceFile)) {
      throw new RuntimeException();
    }
    SourceProject sourceProject = peekSourceProject();
    SourcePackage sourcePackage = findSourcePackage(getPackageKey(astNode));
    getContext().popSourceCode();
    sourceProject.getChildren().remove(sourceFile);
    getContext().addSourceCode(sourcePackage);
    getContext().addSourceCode(sourceFile);

  }

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.NAMESPACE_STATEMENT,

      PHPGrammar.USE_STATEMENT,

      PHPGrammar.INTERFACE_DECLARATION,
      PHPGrammar.CLASS_DECLARATION,
      PHPGrammar.METHOD_DECLARATION,
      PHPGrammar.FUNCTION_DECLARATION,

      PHPGrammar.CLASS_MEMBER_ACCESS,
      PHPGrammar.NEW_EXPR
    );
  }

  @Override
  public void visitNode(@Nullable AstNode astNode) {
    try {
      if (astNode == null) {
        return;
      }
      if (astNode.is(PHPGrammar.NAMESPACE_STATEMENT)) {
        parseNamespace(astNode);
        return;
      }
      if (astNode.is(PHPGrammar.USE_STATEMENT)) {
        parseUse(astNode);
        return;
      }
      if (astNode.is(PHPGrammar.INTERFACE_DECLARATION) || astNode.is(PHPGrammar.CLASS_DECLARATION)) {
        parseClass(astNode);
        return;
      }
      if (astNode.is(PHPGrammar.METHOD_DECLARATION) || astNode.is(PHPGrammar.FUNCTION_DECLARATION)) {
        parseMethodParamList(astNode);
        return;
      }
      if (astNode.is(PHPGrammar.CLASS_MEMBER_ACCESS)) {
        parseClassMemberAccess(astNode);
        return;
      }
      if (astNode.is(PHPGrammar.NEW_EXPR)) {
        parseNewExpression(astNode);
        return;
      }
      LOG.warn("unsupported node: " + astNode.getType());
    } catch (Exception e) {
      LOG.warn("Exception in dependency visitor", e);
    }
  }

  @Override
  public void leaveNode(@Nullable AstNode astNode) {
    if (astNode == null) {
      return;
    }
    if (astNode.is(PHPGrammar.CLASS_DECLARATION)) {
      currentClass = null;
      LOG.debug("Current class: null");
    }
  }

  private String getPackageKey(AstNode astNode) {
    AstNode nsSt = getNamespaceNode(astNode);
    if (nsSt != null) {
      AstNode packageNameNode = nsSt.getFirstChild(PHPGrammar.NAMESPACE_NAME);
      return getAstNodeValue(packageNameNode).replace('.', '/');
    } else {
      // unnamed package
      return "";
    }
  }

  private AstNode getNamespaceNode(AstNode astNode) {
    try {
      return astNode.getFirstChild(PHPGrammar.TOP_STATEMENT_LIST).getFirstChild().getFirstChild(PHPGrammar.NAMESPACE_STATEMENT);
    } catch (NullPointerException e) {
      return null;
    }
  }

  private void parseNamespace(AstNode expr) {
    StringBuilder builder = new StringBuilder();

    for (Token token : expr.getTokens()) {
      if (token.getType() == PHPPunctuator.SEMICOLON) {
        break;
      }
      if (token.getType() == PHPKeyword.NAMESPACE) {
        continue;
      }
      builder.append(token.getOriginalValue());
    }

    namespace = builder.toString();
    LOG.debug("Namespace is: "+namespace);
  }

  private void parseUse(AstNode expr) {
    StringBuilder ns = new StringBuilder();
    StringBuilder builder = ns;
    String alias = "";

    for (Token token : expr.getTokens()) {
      if (token.getType() == PHPPunctuator.SEMICOLON) {
        break;
      }
      if (token.getType() == PHPKeyword.USE) {
        continue;
      }
      if (token.getType() == PHPKeyword.AS) {
        builder = null;
        continue;
      }
      alias = token.getOriginalValue();
      if (builder != null) {
        builder.append(alias);
      }
    }

    uses.put(alias, ns.toString());
  }

  private void parseClass(AstNode astNode) {
    setClassName(astNode);
    addExtends(astNode);
    addImplements(astNode);
  }

  private void parseMethodParamList(AstNode astNode) {
    AstNode paramList = astNode.getFirstChild(PHPGrammar.PARAMETER_LIST);
    if (paramList == null) {
      return;
    }
    for (AstNode param : paramList.getChildren()) {
      parseMethodParam(param);
    }
  }

  private void parseMethodParam(AstNode param) {
    AstNode typeNode = param.getFirstChild(PHPGrammar.OPTIONAL_CLASS_TYPE);
    if (typeNode == null) {
      return;
    }
    AstNode classNode = typeNode.getFirstChild(PHPGrammar.FULLY_QUALIFIED_CLASS_NAME);
    if (classNode == null) {
      return;
    }
    String alias = getClassName(classNode);
    String realName = getRealName(alias);
    addLink(realName, SourceCodeEdgeUsage.USES);
  }

  private void parseClassMemberAccess(AstNode astNode) {
    AstNode className = astNode.getParent().getFirstChild(PHPGrammar.CLASS_NAME);
    if (className == null) {
      /**
       * object access for static method
       * "$className::staticMethod()"
       */
      return;
    }
    AstNode classNode = className.getFirstChild(PHPGrammar.FULLY_QUALIFIED_CLASS_NAME);
    if (classNode == null) {
      /**
       * parent, static, self
       */
      return;
    }
    addDependency(classNode);
  }

  private void parseNewExpression(AstNode astNode) {
    AstNode className = astNode.getFirstChild(PHPGrammar.VARIABLE).getFirstChild(PHPGrammar.CLASS_NAME);
    if (className == null) {
      /**
       * dynamic class name
       * "new $className()"
       */
      return;
    }
    addDependency(className);
  }

  private void setClassName(AstNode expr) {
    for (Token token : expr.getTokens()) {
      if (token.getType() == GenericTokenType.IDENTIFIER) {
        String alias = token.getOriginalValue();
        String className = getRealName(alias);
        currentClass = findSourceClass(className);
        LOG.debug("Current class: "+currentClass);
        fixClassParent();
        return;
      }
    }
  }

  private void fixClassParent() {
    SourceFile sourceFile = peekSourceFile();
    SourceCode parent = currentClass.getParent();
    if (!sourceFile.equals(parent)) {
      parent.getChildren().remove(currentClass);
      sourceFile.addChild(currentClass);
      fixParentLinks(currentClass);
    }
  }

  private void fixParentLinks(SourceClass sourceCode) {
    if (graph == null) {
      return;
    }
    for (SourceCodeEdge edge : graph.getOutgoingEdges(sourceCode)) {
      SourceClass to = (SourceClass) edge.getTo();
      SourceCodeEdge fileEdge = createEdgeBetweenParents(SourceFile.class, sourceCode, to, edge);
      createEdgeBetweenParents(SourcePackage.class, sourceCode, to, fileEdge);
    }
    for (SourceCodeEdge edge : graph.getIncomingEdges(sourceCode)) {
      SourceClass from = (SourceClass) edge.getFrom();
      SourceCodeEdge fileEdge = createEdgeBetweenParents(SourceFile.class, from, sourceCode, edge);
      createEdgeBetweenParents(SourcePackage.class, from, sourceCode, fileEdge);
    }
  }

  private void addExtends(AstNode expr) {
    StringBuilder builder = new StringBuilder();
    boolean isExtends = false;

    for (Token token : expr.getTokens()) {
      if (token.getType() == PHPPunctuator.LCURLYBRACE) {
        break;
      }
      if (token.getType() == PHPKeyword.EXTENDS) {
        isExtends = true;
      }
      if (token.getType() == PHPKeyword.IMPLEMENTS) {
        isExtends = false;
      }
      if (!isExtends) {
        continue;
      }
      if (token.getType() == GenericTokenType.IDENTIFIER || token.getType() == PHPPunctuator.NS_SEPARATOR) {
        builder.append(token.getOriginalValue());
      }
    }

    String alias = builder.toString();
    if (alias.length() > 0) {
      String realName = getRealName(alias);
      addLink(realName, SourceCodeEdgeUsage.EXTENDS);
    }
  }

  private void addImplements(AstNode expr) {
    StringBuilder builder = new StringBuilder();
    boolean isImplements = false;

    for (Token token : expr.getTokens()) {
      if (token.getType() == PHPKeyword.IMPLEMENTS) {
        isImplements = true;
      }
      if (!isImplements) {
        continue;
      }
      if (token.getType() == GenericTokenType.IDENTIFIER || token.getType() == PHPPunctuator.NS_SEPARATOR) {
        builder.append(token.getOriginalValue());
      }
      if (token.getType() == PHPPunctuator.COMMA || token.getType() == PHPPunctuator.LCURLYBRACE) {
        String alias = builder.toString();
        String realName = getRealName(alias);
        addLink(realName, SourceCodeEdgeUsage.IMPLEMENTS);
        builder = new StringBuilder();
      }
      if (token.getType() == PHPPunctuator.LCURLYBRACE) {
        break;
      }
    }
  }

  private void addDependency(AstNode expr) {
    String alias = getClassName(expr);
    String realName = getRealName(alias);
    addLink(realName, SourceCodeEdgeUsage.USES);
  }

  private void addLink(String realName, SourceCodeEdgeUsage link) {
    LOG.debug("Create link to "+realName+" ("+link.toString()+")");
    if (currentClass != null) {
      SourceClass toClass = findSourceClass(realName);
      link(currentClass, toClass, link);
    } else {
      /**
       * not in class
       * ex. in ns. function
       */
      SourcePackage from = findSourcePackage(namespace);
      SourcePackage to = findSourcePackage(getNamespace(realName));
      if (canWeLinkNodes(from, to) && graph.getEdge(from, to) == null) {
        SourceCodeEdge edge = new SourceCodeEdge(from, to, link);
        graph.addEdge(edge);
      }
    }
  }

  private void link(SourceCode from, SourceCode to, SourceCodeEdgeUsage link) {
    if (canWeLinkNodes(from, to) && graph.getEdge(from, to) == null) {
      SourceCodeEdge edge = new SourceCodeEdge(from, to, link);
      graph.addEdge(edge);
      SourceCodeEdge fileEdge = createEdgeBetweenParents(SourceFile.class, from, to, edge);
      createEdgeBetweenParents(SourcePackage.class, from, to, fileEdge);
    }
  }

  private boolean canWeLinkNodes(SourceCode from, SourceCode to) {
    return from != null && to != null && !from.equals(to);
  }

  private String getClassName(AstNode expr) {
    StringBuilder builder = new StringBuilder();

    for (Token token : expr.getTokens()) {
      builder.append(token.getOriginalValue());
    }

    return builder.toString();
  }

  private String getRealName(String name) {
    String separator = PHPPunctuator.NS_SEPARATOR.getValue();
    if (name.startsWith(separator)) {
      return name.substring(1);
    }
    for (Map.Entry<String, String> use : uses.entrySet()) {
      String alias = use.getKey();
      if (name.equals(alias)) {
        return use.getValue();
      }
      String prefix = alias + separator;
      if (name.startsWith(prefix)) {
        return name.replaceFirst(alias, use.getValue().replace("\\", "\\\\"));
      }
    }

    return (namespace.equals("") ? "" : namespace + separator) + name;
  }

  private String getNamespace(String realName) {
    String ns = realName.replaceAll("\\\\[^\\\\]+$", "");
    if (ns.equals(realName)) {
      return "";
    }
    return ns;
  }

  private SourcePackage findSourcePackage(String packageKey) {
    SourceProject sourceProject = peekSourceProject();
    if (sourceProject.hasChildren()) {
      for (SourceCode sourcePackage : sourceProject.getChildren()) {
        if (sourcePackage.getKey().equals(packageKey)) {
          return (SourcePackage) sourcePackage;
        }
      }
    }
    SourcePackage sourcePackage = new SourcePackage(packageKey);
    sourceProject.addChild(sourcePackage);
    return sourcePackage;
  }

  private SourceClass findSourceClass(String className) {
    String ns = getNamespace(className);
    SourcePackage sourcePackage = findSourcePackage(ns);
    SourceClass sourceClass = findSourceClass(sourcePackage, className);
    if (sourceClass != null) {
      return sourceClass;
    }
    sourceClass = new SourceClass(className);
    sourcePackage.addChild(sourceClass);
    return sourceClass;
  }

  private SourceClass findSourceClass(SourceCode code, String className) {
    if (SourceClass.class.isInstance(code)) {
      if (!code.getKey().equals(className)) {
        return null;
      }
      return (SourceClass) code;
    }
    if (!code.hasChildren()) {
      return null;
    }
    for (SourceCode child : code.getChildren()) {
      SourceClass sourceClass = findSourceClass(child, className);
      if (sourceClass != null) {
        return sourceClass;
      }
    }
    return null;
  }

  private SourceCodeEdge createEdgeBetweenParents(Class<? extends SourceCode> type, SourceCode from, SourceCode to,
                                                  SourceCodeEdge rootEdge) {
    SourceCode fromParent = from.getParent(type);
    SourceCode toParent = to.getParent(type);
    SourceCodeEdge parentEdge = null;
    if (canWeLinkNodes(fromParent, toParent) && rootEdge != null) {
      if (graph.getEdge(fromParent, toParent) == null) {
        parentEdge = new SourceCodeEdge(fromParent, toParent, SourceCodeEdgeUsage.USES);
        parentEdge.addRootEdge(rootEdge);
        graph.addEdge(parentEdge);
      } else {
        parentEdge = graph.getEdge(fromParent, toParent);
        parentEdge.addRootEdge(rootEdge);
      }
    }
    return parentEdge;
  }

  protected final SourceProject peekSourceProject() {
    SourceCode sourceCode = getContext().peekSourceCode();
    if (sourceCode.isType(SourceProject.class)) {
      return (SourceProject) getContext().peekSourceCode();
    }
    return sourceCode.getParent(SourceProject.class);
  }

  protected final SourceFile peekSourceFile() {
    SourceCode sourceCode = getContext().peekSourceCode();
    if (sourceCode.isType(SourceFile.class)) {
      return (SourceFile) getContext().peekSourceCode();
    }
    return sourceCode.getParent(SourceFile.class);
  }
}
