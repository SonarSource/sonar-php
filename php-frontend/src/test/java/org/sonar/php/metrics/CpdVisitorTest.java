/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.metrics;

import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.php.FileTestUtils;
import org.sonar.php.cache.CacheContextImpl;
import org.sonar.php.cache.PhpReadCacheImpl;
import org.sonar.php.cache.PhpWriteCacheImpl;
import org.sonar.php.metrics.CpdVisitor.CpdToken;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.php.utils.ReadWriteInMemoryCache;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpInputFileContext;

import static org.assertj.core.api.Assertions.assertThat;

class CpdVisitorTest {

  public static final String EXAMPLE_CODE = "<?php $x = 1;\n$y = 'str' + $x;\n";
  private static final byte[] EXAMPLE_STRING_TABLE_BYTES = new byte[] {8, 5, 49, 46, 50, 46, 51, 2, 36, 120, 1, 61, 7, 36, 78, 85, 77, 66, 69, 82, 1, 59, 2, 36, 121, 6, 36, 67, 72,
    65, 82, 83, 1, 43, 3, 69, 78, 68};
  private static final byte[] EXAMPLE_DATA_BYTES = new byte[] {0, 10, 1, 6, 1, 8, 1, 1, 9, 1, 10, 2, 1, 11, 1, 12, 3, 1, 12, 1, 13, 4, 2, 0, 2, 2, 5, 2, 3, 2, 4, 2, 2, 5, 2, 10, 6,
    2, 11, 2, 12, 7, 2, 13, 2, 15, 1, 2, 15, 2, 16, 4, 3, 69, 78, 68};
  private final ActionParser<Tree> p = PHPParserBuilder.createParser();

  @TempDir
  public File tempFolder;

  @Test
  void test() {
    List<CpdToken> tokens = scan(EXAMPLE_CODE);

    assertThat(getImagesList(tokens)).containsExactly("$x", "=", "$NUMBER", ";", "$y", "=", "$CHARS", "+", "$x", ";");
  }

  @Test
  void testUse() {
    List<CpdToken> tokens = scan("<?php use a\\b;\n");

    assertThat(getImagesList(tokens)).containsExactly();
  }

  @Test
  void testExpandableString() {
    List<CpdToken> tokens = scan("<?php \"abc$x!abc\";");

    assertThat(getImagesList(tokens)).containsExactly("\"", "$CHARS", "$x", "$CHARS", "\"", ";");
  }

  @Test
  void testHeredocString() {
    List<CpdToken> tokens = scan("<?php <<<EOF\nabc$x!abc\nabc\nEOF;");

    assertThat(getImagesList(tokens)).containsExactly("<<<EOF\n", "$CHARS", "$x", "$CHARS", "\nEOF", ";");
  }

  @Test
  void shouldNotIncludeTags() {
    List<CpdToken> tokens = scan("<a/><?php $x; ?><b/>\n");

    assertThat(getImagesList(tokens)).containsExactly("$x", ";");
  }

  @Test
  void shouldStoreCpdInCache() {
    ReadWriteInMemoryCache writeCache = new ReadWriteInMemoryCache();
    CacheContext cacheContext = new CacheContextImpl(true,
      new PhpWriteCacheImpl(writeCache),
      new PhpReadCacheImpl(new ReadWriteInMemoryCache()),
      "1.2.3");

    scan(EXAMPLE_CODE, cacheContext);

    assertThat(writeCache.writeKeys().stream().map(s -> s.split(":")[0]))
      .containsOnly("php.cpd.data", "php.cpd.stringTable");
  }

  @Test
  void shouldNotStoreCpdWhenCacheDisabled() {
    ReadWriteInMemoryCache writeCache = new ReadWriteInMemoryCache();
    CacheContext cacheContext = new CacheContextImpl(false,
      new PhpWriteCacheImpl(writeCache),
      new PhpReadCacheImpl(new ReadWriteInMemoryCache()),
      "1.2.3");

    scan(EXAMPLE_CODE, cacheContext);

    assertThat(writeCache.writeKeys()).isEmpty();
  }

  @Test
  void shouldNotStoreCpdWhenCacheIsNull() {
    List<CpdToken> tokens = scan(EXAMPLE_CODE, null);

    assertThat(getImagesList(tokens)).containsExactly("$x", "=", "$NUMBER", ";", "$y", "=", "$CHARS", "+", "$x", ";");
  }

  @Test
  void shouldNotStoreCpdWhenWriteCacheIsNull() {
    CacheContext cacheContext = new CacheContextImpl(true,
      null,
      new PhpReadCacheImpl(new ReadWriteInMemoryCache()),
      "1.2.3");

    List<CpdToken> tokens = scan(EXAMPLE_CODE, cacheContext);

    assertThat(getImagesList(tokens)).containsExactly("$x", "=", "$NUMBER", ";", "$y", "=", "$CHARS", "+", "$x", ";");
  }

  @Test
  void shouldRestoreFromCache() {
    CpdVisitor cpdVisitor = new CpdVisitor();
    PhpFile testFile = FileTestUtils.getFile(new File(tempFolder, "file"), EXAMPLE_CODE);
    ReadWriteInMemoryCache readCache = new ReadWriteInMemoryCache();
    readCache.write("php.cpd.stringTable:" + testFile.key(), EXAMPLE_STRING_TABLE_BYTES);
    readCache.write("php.cpd.data:" + testFile.key(), EXAMPLE_DATA_BYTES);
    CacheContext cacheContext = new CacheContextImpl(true,
      new PhpWriteCacheImpl(new ReadWriteInMemoryCache()),
      new PhpReadCacheImpl(readCache),
      "1.2.3");
    PhpInputFileContext fileContext = new PhpInputFileContext(testFile, tempFolder, cacheContext);

    boolean actual = cpdVisitor.scanWithoutParsing(fileContext);

    assertThat(actual).isTrue();
    assertThat(readCache.readKeys().stream().map(s -> s.split(":")[0]))
      .containsOnly("php.cpd.data", "php.cpd.stringTable");
  }

  @Test
  void shouldNotRestoreFromCacheWhenCacheIsNull() {
    CpdVisitor cpdVisitor = new CpdVisitor();
    PhpFile testFile = FileTestUtils.getFile(new File(tempFolder, "file"), EXAMPLE_CODE);
    PhpInputFileContext fileContext = new PhpInputFileContext(testFile, tempFolder, null);

    boolean actual = cpdVisitor.scanWithoutParsing(fileContext);

    assertThat(actual).isFalse();
  }

  @Test
  void shouldNotRestoreFromCacheWhenReadCacheIsNull() {
    CpdVisitor cpdVisitor = new CpdVisitor();
    PhpFile testFile = FileTestUtils.getFile(new File(tempFolder, "file"), EXAMPLE_CODE);
    CacheContext cacheContext = new CacheContextImpl(true,
      new PhpWriteCacheImpl(new ReadWriteInMemoryCache()),
      null,
      "1.2.3");
    PhpInputFileContext fileContext = new PhpInputFileContext(testFile, tempFolder, cacheContext);

    boolean actual = cpdVisitor.scanWithoutParsing(fileContext);

    assertThat(actual).isFalse();
  }

  @Test
  void shouldNotRestoreFromCacheWhenDataBytesAreNull() {
    CpdVisitor cpdVisitor = new CpdVisitor();
    PhpFile testFile = FileTestUtils.getFile(new File(tempFolder, "file"), EXAMPLE_CODE);
    ReadWriteInMemoryCache readCache = new ReadWriteInMemoryCache();
    readCache.write("php.cpd.stringTable:" + testFile.key(), EXAMPLE_STRING_TABLE_BYTES);
    CacheContext cacheContext = new CacheContextImpl(true,
      new PhpWriteCacheImpl(new ReadWriteInMemoryCache()),
      new PhpReadCacheImpl(readCache),
      "1.2.3");
    PhpInputFileContext fileContext = new PhpInputFileContext(testFile, tempFolder, cacheContext);

    boolean actual = cpdVisitor.scanWithoutParsing(fileContext);

    assertThat(actual).isFalse();
  }

  @Test
  void shouldNotRestoreFromCacheWhenStringTableAreNull() {
    CpdVisitor cpdVisitor = new CpdVisitor();
    PhpFile testFile = FileTestUtils.getFile(new File(tempFolder, "file"), EXAMPLE_CODE);
    ReadWriteInMemoryCache readCache = new ReadWriteInMemoryCache();
    readCache.write("php.cpd.data:" + testFile.key(), EXAMPLE_DATA_BYTES);
    CacheContext cacheContext = new CacheContextImpl(true,
      new PhpWriteCacheImpl(new ReadWriteInMemoryCache()),
      new PhpReadCacheImpl(readCache),
      "1.2.3");
    PhpInputFileContext fileContext = new PhpInputFileContext(testFile, tempFolder, cacheContext);

    boolean actual = cpdVisitor.scanWithoutParsing(fileContext);

    assertThat(actual).isFalse();
  }

  @Test
  void shouldNotComputeCptTokensForAttributes() {
    String source = "<?php\n" +
      "    #[Route(\n" +
      "        path: '/v1/infocontroller/{id}',\n" +
      "        name: 'info',\n" +
      "        requirements: [\"id\"=>\"\\d+\"],\n" +
      "        methods: ['GET']\n" +
      "    )]\n" +
      "    function foo(){}";

    List<CpdToken> tokens = scan(source);

    assertThat(getImagesList(tokens)).containsExactly("function", "foo", "(", ")", "{", "}");
  }

  private List<CpdToken> scan(String source) {
    PhpFile testFile = FileTestUtils.getFile(new File(tempFolder, "file"), source);
    CpdVisitor cpdVisitor = new CpdVisitor();
    CompilationUnitTree tree = (CompilationUnitTree) p.parse(testFile.contents());
    return cpdVisitor.computeCpdTokens(testFile, tree, SymbolTableImpl.create(tree), null);
  }

  private List<CpdToken> scan(String source, @Nullable CacheContext cacheContext) {
    PhpFile testFile = FileTestUtils.getFile(new File(tempFolder, "file"), source);
    CpdVisitor cpdVisitor = new CpdVisitor();
    CompilationUnitTree tree = (CompilationUnitTree) p.parse(testFile.contents());
    return cpdVisitor.computeCpdTokens(testFile, tree, SymbolTableImpl.create(tree), cacheContext);
  }

  private static List<String> getImagesList(List<CpdToken> tokens) {
    return tokens.stream().map(CpdToken::text).collect(Collectors.toList());
  }
}
