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
package org.sonar.php;

import com.sonar.sslr.api.typed.ActionParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.symbols.SymbolQualifiedName;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassPropertyDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class FindFunctionWithReturnType {

  static final ActionParser<Tree> parser = PHPParserBuilder.createParser();

  static final Pattern RETURN_COMMENT = Pattern.compile("(@return|@var)\\s+([\\S]+)");
  static final List<String> TYPES_TO_FIND = Arrays.asList("\\Illuminate\\Support\\Facades\\Input");

  public static void main(String[] args) throws Exception {
    String dir = "/Users/pierre-loup/Developer/php/laravel/laravel-datatables-demo/public/vendor/laravel";
    Files.walk(Paths.get(dir))
      .filter(p -> p.toString().endsWith(".php"))
      .forEach(FindFunctionWithReturnType::analyze);
  }

  private static void analyze(Path path) {
    //System.out.println("Analyze: " + path);
    CompilationUnitTree cut = (CompilationUnitTree) parser.parse(path.toFile());
    SymbolTableImpl symbolTable = SymbolTableImpl.create(cut);
    Visitor visitor = new Visitor(symbolTable);
    cut.accept(visitor);
    visitor.found.forEach(s -> System.out.println("Found: " + s.qualifiedName()));
  }
}

class Visitor extends PHPVisitorCheck {


  private final SymbolTableImpl symbolTable;
  private SymbolQualifiedName namespace;

  List<Symbol> found = new ArrayList<>();

  Visitor(SymbolTableImpl symbolTable) {
    this.symbolTable = symbolTable;
  }

  @Override
  public void visitNamespaceStatement(NamespaceStatementTree tree) {
    namespace = SymbolQualifiedName.create(tree.namespaceName());
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    analyzeFunction(tree, symbolTable.getSymbol(tree.name()));
    super.visitFunctionDeclaration(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    analyzeFunction(tree, symbolTable.getSymbol(tree.name()));
    super.visitMethodDeclaration(tree);
  }

  @Override
  public  void visitClassPropertyDeclaration(ClassPropertyDeclarationTree tree) {
    analyzeClassProperty(tree);
    super.visitClassPropertyDeclaration(tree);
  }

  private void analyzeFunction(Tree tree,@Nullable Symbol symbol) {
    if (symbol == null) {
      return;
    }
    List<String> returnTypes = Collections.emptyList();
    List<SyntaxTrivia> trivias = ((PHPTree) tree).getFirstToken().trivias();
    if (!trivias.isEmpty()) {
      returnTypes = parseComment(trivias.get(0).text()).stream()
        .map(this::returnTypeName)
        .collect(Collectors.toList());
    }
    if (returnTypes.stream().anyMatch(FindFunctionWithReturnType.TYPES_TO_FIND::contains)) {
      found.add(symbol);
    }
  }

  private void analyzeClassProperty(ClassPropertyDeclarationTree tree) {
    List<String> returnTypes = Collections.emptyList();
    List<SyntaxTrivia> trivias = ((PHPTree) tree).getFirstToken().trivias();
    if (!trivias.isEmpty()) {
      returnTypes = parseComment(trivias.get(0).text()).stream()
              .map(this::returnTypeName)
              .collect(Collectors.toList());
    }
    if (returnTypes.stream().anyMatch(FindFunctionWithReturnType.TYPES_TO_FIND::contains) && !tree.hasModifiers("private")) {
      tree.declarations().forEach(this::addClassProperty);
    }
  }

  private List<String> parseComment(String text) {
    Matcher matcher = FindFunctionWithReturnType.RETURN_COMMENT.matcher(text);
    if (matcher.find()) {
      String[] returnTypes = matcher.group(2).split("\\|");
      return Arrays.asList(returnTypes);
    }
    return Collections.emptyList();
  }

  private String returnTypeName(String s) {
    if (s.equals("null")) {
      return s;
    }
    if (namespace == null) {
      return s;
    }
    return s.startsWith("\\") ? s : namespace.toString() + "\\" + s;
  }

  private void addClassProperty(VariableDeclarationTree tree) {
    Symbol symbol = symbolTable.getSymbol(tree.identifier());
    if (symbol == null) {
      return;
    }
    found.add(symbol);
  }
}
