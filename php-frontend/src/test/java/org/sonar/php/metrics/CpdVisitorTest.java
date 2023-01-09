/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.php.metrics;

import com.sonar.sslr.api.typed.ActionParser;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.php.FileTestUtils;
import org.sonar.php.cache.CacheContextImpl;
import org.sonar.php.cache.PhpReadCacheImpl;
import org.sonar.php.cache.PhpWriteCacheImpl;
import org.sonar.php.compat.PhpFileImpl;
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
import static org.assertj.core.api.Assertions.catchThrowable;

public class CpdVisitorTest {

  public static final String EXAMPLE_CODE = "<?php $x = 1;\n$y = 'str' + $x;\n";
  private static final byte[] EXAMPLE_STRING_TABLE_BYTES = new byte [] {8, 5, 49, 46, 50, 46, 51, 2, 36, 120, 1, 61, 7, 36, 78, 85, 77, 66, 69, 82, 1, 59, 2, 36, 121, 6, 36, 67, 72, 65, 82, 83, 1, 43, 3, 69, 78, 68};
  private static final byte[] EXAMPLE_DATA_BYTES = new byte[]{0, 10, 1, 6, 1, 8, 1, 1, 9, 1, 10, 2, 1, 11, 1, 12, 3, 1, 12, 1, 13, 4, 2, 0, 2, 2, 5, 2, 3, 2, 4, 2, 2, 5, 2, 10, 6, 2, 11, 2, 12, 7, 2, 13, 2, 15, 1, 2, 15, 2, 16, 4, 3, 69, 78, 68};
  private static final String FILE_HASH = ":17896552431696592101494790980459960136579126941178730472258048353809908396724";
  private final ActionParser<Tree> p = PHPParserBuilder.createParser();

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void test() throws Exception {
    List<CpdToken> tokens = scan(EXAMPLE_CODE);

    assertThat(getImagesList(tokens)).containsExactly("$x", "=", "$NUMBER", ";", "$y", "=", "$CHARS", "+", "$x", ";");
  }

  @Test
  public void test_use() throws Exception {
    List<CpdToken> tokens = scan("<?php use a\\b;\n");

    assertThat(getImagesList(tokens)).containsExactly();
  }

  @Test
  public void test_expandable_string() throws Exception {
    List<CpdToken> tokens = scan("<?php \"abc$x!abc\";");

    assertThat(getImagesList(tokens)).containsExactly("\"", "$CHARS", "$x", "$CHARS", "\"", ";");
  }

  @Test
  public void test_heredoc_string() throws Exception {
    List<CpdToken> tokens = scan("<?php <<<EOF\nabc$x!abc\nabc\nEOF;");

    assertThat(getImagesList(tokens)).containsExactly("<<<EOF\n", "$CHARS", "$x", "$CHARS", "\nEOF", ";");
  }

  @Test
  public void should_not_include_tags() throws Exception {
    List<CpdToken> tokens = scan("<a/><?php $x; ?><b/>\n");

    assertThat(getImagesList(tokens)).containsExactly("$x", ";");
  }

  @Test
  public void should_store_cpd_in_cache() throws IOException {
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
  public void should_not_store_cpd_when_cache_disabled() throws IOException {
    ReadWriteInMemoryCache writeCache = new ReadWriteInMemoryCache();
    CacheContext cacheContext = new CacheContextImpl(false,
      new PhpWriteCacheImpl(writeCache),
      new PhpReadCacheImpl(new ReadWriteInMemoryCache()),
      "1.2.3");

    scan(EXAMPLE_CODE, cacheContext);

    assertThat(writeCache.writeKeys()).isEmpty();
  }

  @Test
  public void should_not_store_cpd_when_cache_is_null() throws IOException {
    List<CpdToken> tokens = scan(EXAMPLE_CODE, null);

    assertThat(getImagesList(tokens)).containsExactly("$x", "=", "$NUMBER", ";", "$y", "=", "$CHARS", "+", "$x", ";");
  }

  @Test
  public void should_not_store_cpd_when_write_cache_is_null() throws IOException {
    CacheContext cacheContext = new CacheContextImpl(true,
      null,
      new PhpReadCacheImpl(new ReadWriteInMemoryCache()),
      "1.2.3");

    List<CpdToken> tokens = scan(EXAMPLE_CODE, cacheContext);

    assertThat(getImagesList(tokens)).containsExactly("$x", "=", "$NUMBER", ";", "$y", "=", "$CHARS", "+", "$x", ";");
  }

  @Test
  public void should_not_store_cpd_in_cache_when_no_file_content() throws IOException {
    ReadWriteInMemoryCache writeCache = new ReadWriteInMemoryCache();
    CacheContext cacheContext = new CacheContextImpl(true,
      new PhpWriteCacheImpl(writeCache),
      new PhpReadCacheImpl(new ReadWriteInMemoryCache()),
      "1.2.3");
    InputFile file = new TestInputFileBuilder("projectKey", "do_not_exist.php")
      .build();
    PhpFile testFile = PhpFileImpl.create(file);
    CpdVisitor cpdVisitor = new CpdVisitor();
    String contents = FileTestUtils.getFile(tempFolder.newFile(), EXAMPLE_CODE).contents();
    CompilationUnitTree tree = (CompilationUnitTree) p.parse(contents);

    Throwable throwable = catchThrowable(() -> cpdVisitor.computeCpdTokens(testFile, tree, SymbolTableImpl.create(tree), cacheContext));

    assertThat(throwable).isInstanceOf(RuntimeException.class);
    assertThat(writeCache.writeKeys()).isEmpty();
  }

  @Test
  public void should_restore_from_cache() throws IOException {
    CpdVisitor cpdVisitor = new CpdVisitor();
    PhpFile testFile = FileTestUtils.getFile(tempFolder.newFile(), EXAMPLE_CODE);
    ReadWriteInMemoryCache readCache = new ReadWriteInMemoryCache();
    readCache.write("php.cpd.stringTable:" + testFile.key() + FILE_HASH, EXAMPLE_STRING_TABLE_BYTES);
    readCache.write("php.cpd.data:" + testFile.key() + FILE_HASH, EXAMPLE_DATA_BYTES);
    CacheContext cacheContext = new CacheContextImpl(true,
      new PhpWriteCacheImpl(new ReadWriteInMemoryCache()),
      new PhpReadCacheImpl(readCache),
      "1.2.3");
    PhpInputFileContext fileContext = new PhpInputFileContext(testFile, tempFolder.getRoot(), cacheContext);

    boolean actual = cpdVisitor.scanWithoutParsing(fileContext);

    assertThat(actual).isTrue();
    assertThat(readCache.readKeys().stream().map(s -> s.split(":")[0]))
      .containsOnly("php.cpd.data", "php.cpd.stringTable");
  }

  @Test
  public void should_not_restore_from_cache_when_cache_is_null() throws IOException {
    CpdVisitor cpdVisitor = new CpdVisitor();
    PhpFile testFile = FileTestUtils.getFile(tempFolder.newFile(), EXAMPLE_CODE);
    PhpInputFileContext fileContext = new PhpInputFileContext(testFile, tempFolder.getRoot(), null);

    boolean actual = cpdVisitor.scanWithoutParsing(fileContext);

    assertThat(actual).isFalse();
  }

  @Test
  public void should_not_restore_from_cache_when_read_cache_is_null() throws IOException {
    CpdVisitor cpdVisitor = new CpdVisitor();
    PhpFile testFile = FileTestUtils.getFile(tempFolder.newFile(), EXAMPLE_CODE);
    CacheContext cacheContext = new CacheContextImpl(true,
      new PhpWriteCacheImpl(new ReadWriteInMemoryCache()),
      null,
      "1.2.3");
    PhpInputFileContext fileContext = new PhpInputFileContext(testFile, tempFolder.getRoot(), cacheContext);

    boolean actual = cpdVisitor.scanWithoutParsing(fileContext);

    assertThat(actual).isFalse();
  }

  @Test
  public void should_not_restore_from_cache_when_data_bytes_are_null() throws IOException {
    CpdVisitor cpdVisitor = new CpdVisitor();
    PhpFile testFile = FileTestUtils.getFile(tempFolder.newFile(), EXAMPLE_CODE);
    ReadWriteInMemoryCache readCache = new ReadWriteInMemoryCache();
    readCache.write("php.cpd.stringTable:" + testFile.key(), EXAMPLE_STRING_TABLE_BYTES);
    CacheContext cacheContext = new CacheContextImpl(true,
      new PhpWriteCacheImpl(new ReadWriteInMemoryCache()),
      new PhpReadCacheImpl(readCache),
      "1.2.3");
    PhpInputFileContext fileContext = new PhpInputFileContext(testFile, tempFolder.getRoot(), cacheContext);

    boolean actual = cpdVisitor.scanWithoutParsing(fileContext);

    assertThat(actual).isFalse();
  }

  @Test
  public void should_not_restore_from_cache_when_string_table_are_null() throws IOException {
    CpdVisitor cpdVisitor = new CpdVisitor();
    PhpFile testFile = FileTestUtils.getFile(tempFolder.newFile(), EXAMPLE_CODE);
    ReadWriteInMemoryCache readCache = new ReadWriteInMemoryCache();
    readCache.write("php.cpd.data:" + testFile.key(), EXAMPLE_DATA_BYTES);
    CacheContext cacheContext = new CacheContextImpl(true,
      new PhpWriteCacheImpl(new ReadWriteInMemoryCache()),
      new PhpReadCacheImpl(readCache),
      "1.2.3");
    PhpInputFileContext fileContext = new PhpInputFileContext(testFile, tempFolder.getRoot(), cacheContext);

    boolean actual = cpdVisitor.scanWithoutParsing(fileContext);

    assertThat(actual).isFalse();
  }

  @Test
  public void should_restore_from_cache_when_no_content() {
    CpdVisitor cpdVisitor = new CpdVisitor();
    InputFile file = new TestInputFileBuilder("projectKey", "do_not_exist.php")
      .build();
    PhpFile testFile = PhpFileImpl.create(file);
    ReadWriteInMemoryCache readCache = new ReadWriteInMemoryCache();
    CacheContext cacheContext = new CacheContextImpl(true,
      new PhpWriteCacheImpl(new ReadWriteInMemoryCache()),
      new PhpReadCacheImpl(readCache),
      "1.2.3");
    PhpInputFileContext fileContext = new PhpInputFileContext(testFile, tempFolder.getRoot(), cacheContext);

    Throwable throwable = catchThrowable(() -> cpdVisitor.scanWithoutParsing(fileContext));

    assertThat(throwable).isInstanceOf(RuntimeException.class);
    assertThat(readCache.readKeys()).isEmpty();
  }

  private List<CpdToken> scan(String source) throws IOException {
    PhpFile testFile = FileTestUtils.getFile(tempFolder.newFile(), source);
    CpdVisitor cpdVisitor = new CpdVisitor();
    CompilationUnitTree tree = (CompilationUnitTree)p.parse(testFile.contents());
    return cpdVisitor.computeCpdTokens(testFile, tree, SymbolTableImpl.create(tree), null);
  }

  private List<CpdToken> scan(String source, @Nullable CacheContext cacheContext) throws IOException {
    PhpFile testFile = FileTestUtils.getFile(tempFolder.newFile(), source);
    CpdVisitor cpdVisitor = new CpdVisitor();
    CompilationUnitTree tree = (CompilationUnitTree)p.parse(testFile.contents());
    return cpdVisitor.computeCpdTokens(testFile, tree, SymbolTableImpl.create(tree), cacheContext);
  }

  private static List<String> getImagesList(List<CpdToken> tokens) {
    return tokens.stream().map(CpdToken::text).collect(Collectors.toList());
  }
}
