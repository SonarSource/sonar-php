/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.metrics;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.php.cache.CpdDeserializationInput;
import org.sonar.php.cache.CpdDeserializer;
import org.sonar.php.cache.CpdSerializationInput;
import org.sonar.php.cache.CpdSerializer;
import org.sonar.php.cache.SerializationResult;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.cache.PhpReadCache;
import org.sonar.plugins.php.api.cache.PhpWriteCache;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.InlineHTMLTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpInputFileContext;

public class CpdVisitor extends PHPVisitorCheck {

  public static final String CACHE_DATA_PREFIX = "php.cpd.data:";
  public static final String CACHE_STRING_TABLE_PREFIX = "php.cpd.stringTable:";
  private List<CpdToken> cpdTokens = new ArrayList<>();

  private static final String NORMALIZED_NUMERIC_LITERAL = "$NUMBER";
  private static final String NORMALIZED_CHARACTER_LITERAL = "$CHARS";

  @Override
  public void visitScript(ScriptTree tree) {
    // skip opening tag
    this.scan(tree.statements());
  }

  @Override
  public void visitInlineHTML(InlineHTMLTree tree) {
    // skip
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Kind.NUMERIC_LITERAL)) {
      addToken(tree.token(), NORMALIZED_NUMERIC_LITERAL);

    } else if (tree.is(Kind.REGULAR_STRING_LITERAL, Kind.NOWDOC_LITERAL)) {
      addToken(tree.token(), NORMALIZED_CHARACTER_LITERAL);

    } else {
      super.visitLiteral(tree);
    }
  }

  @Override
  public void visitExpandableStringCharacters(ExpandableStringCharactersTree tree) {
    addToken(tree.token(), NORMALIZED_CHARACTER_LITERAL);
  }

  @Override
  public void visitToken(SyntaxToken token) {
    if (((InternalSyntaxToken) token).isEOF()) {
      return;
    }

    addToken(token, token.text());
  }

  private void addToken(SyntaxToken token, String text) {
    cpdTokens.add(CpdToken.create(token, text));
  }

  @Override
  public void visitUseStatement(UseStatementTree tree) {
    // do not enter (in order to avoid use statement tokens be considered in duplication detection)
  }

  @Override
  public void visitAttributeGroup(AttributeGroupTree tree) {
    // do not enter (in order to avoid attribute groups tokens be considered in duplication detection)
  }

  public List<CpdToken> computeCpdTokens(PhpFile file,
    CompilationUnitTree tree,
    SymbolTable symbolTable,
    @Nullable CacheContext cacheContext) {

    super.analyze(file, tree, symbolTable);
    storeCpdTokensInCache(file, cacheContext);
    return cpdTokens;
  }

  public List<CpdToken> getCpdTokens() {
    return cpdTokens;
  }

  @Override
  public boolean scanWithoutParsing(PhpInputFileContext phpInputFileContext) {
    List<CpdToken> restoredTokens = restoreCpdTokensFromCache(phpInputFileContext);
    if (restoredTokens != null) {
      cpdTokens = restoredTokens;
      return true;
    }
    return false;
  }

  private void storeCpdTokensInCache(PhpFile file, @Nullable CacheContext cacheContext) {
    if (cacheContext != null && cacheContext.isCacheEnabled()) {
      PhpWriteCache writeCache = cacheContext.getWriteCache();
      if (writeCache != null) {
        CpdSerializationInput input = new CpdSerializationInput(cpdTokens, cacheContext.pluginVersion());
        SerializationResult serializationResult = CpdSerializer.toBinary(input);

        String dataKey = CACHE_DATA_PREFIX + file.key();
        writeCache.writeBytes(dataKey, serializationResult.data());

        String stringTableKey = CACHE_STRING_TABLE_PREFIX + file.key();
        writeCache.writeBytes(stringTableKey, serializationResult.stringTable());
      }
    }
  }

  @CheckForNull
  private static List<CpdToken> restoreCpdTokensFromCache(PhpInputFileContext phpInputFileContext) {
    CacheContext cacheContext = phpInputFileContext.cacheContext();
    if (cacheContext != null) {
      PhpReadCache readCache = cacheContext.getReadCache();
      if (readCache != null) {
        byte[] dataBytes = readCache.readBytes(CACHE_DATA_PREFIX + phpInputFileContext.phpFile().key());
        byte[] stringTableBytes = readCache.readBytes(CACHE_STRING_TABLE_PREFIX + phpInputFileContext.phpFile().key());
        if (dataBytes != null && stringTableBytes != null) {
          CpdDeserializationInput input = new CpdDeserializationInput(dataBytes, stringTableBytes, cacheContext.pluginVersion());
          return CpdDeserializer.fromBinary(input);
        }
      }
    }
    return null;
  }

  public static class CpdToken {
    private final int line;
    private final int column;
    private final int endLine;
    private final int endColumn;
    private final String text;

    public CpdToken(int line, int column, int endLine, int endColumn, String text) {
      this.line = line;
      this.column = column;
      this.endLine = endLine;
      this.endColumn = endColumn;
      this.text = text;
    }

    public static CpdToken create(SyntaxToken token, String text) {
      return new CpdToken(token.line(),
        token.column(),
        token.endLine(),
        token.endColumn(),
        text);
    }

    public int line() {
      return line;
    }

    public int column() {
      return column;
    }

    public int endLine() {
      return endLine;
    }

    public int endColumn() {
      return endColumn;
    }

    public String text() {
      return text;
    }
  }
}
