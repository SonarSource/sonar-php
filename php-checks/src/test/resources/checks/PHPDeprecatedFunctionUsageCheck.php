<?php

// Global namespace
namespace {

  call_user_method();          // Noncompliant {{Replace this "call_user_method()" call with a call to "call_user_func()".}}
//^^^^^^^^^^^^^^^^
  define_syslog_variables();   // Noncompliant {{Remove this call to deprecated "define_syslog_variables()".}}

  if (sql_regcase());          // Noncompliant

  setlocale('LC_ALL', "");     // Noncompliant
  setlocale("LC_ALL", "") ;    // Noncompliant {{Use the "LC_ALL" constant instead of a string literal.}}
//          ^^^^^^^^

  __autoload();                // Noncompliant
  create_function('', 'echo 42;'); // Noncompliant
  parse_str($str);             // Noncompliant {{Add a second argument to this call to "parse_str".}}
  parse_str($str, $array);     // OK
  gmp_random(4);               // Noncompliant
  each($foo);                  // Noncompliant
  assert();                    // OK
  assert($foo);                // OK
  assert("$foo");              // Noncompliant {{Change this call to "assert" to not pass a string argument.}}
  assert('foo()');             // Noncompliant

  \A\call_user_method();       // OK

  call_user_func();            // OK
  sql_regcase->func();         // OK
  setlocale(LC_ALL, "");       // OK
  setlocale("0", "") ;         // OK
  setlocale();                 // OK

}

namespace A {
  function call_user_method() {
  }

  function assert() { // Noncompliant {{Use the standard "assert" function instead of declaring a new assert function.}}
  //       ^^^^^^
  }

  class Foo {
    function assert() {
    }
  }

  call_user_method();             /* Noncompliant */ // FIXME (SONARPHP-552) False-Positive
  \call_user_method();            // Noncompliant
}

  define('BAR', 21, true);  // Noncompliant {{Define this constant as case sensitive.}}
//^^^^^^^^^^^^^^^^^^^^^^^
define('BAR', 21);        // OK
define('BAR', 21, false); // OK
define('BAR', 21, null);

$a = FILTER_FLAG_SCHEME_REQUIRED; // Noncompliant {{Do not use this deprecated "FILTER_FLAG_SCHEME_REQUIRED" constant.}}
//   ^^^^^^^^^^^^^^^^^^^^^^^^^^^
$a = FILTER_FLAG_HOST_REQUIRED; // Noncompliant
//   ^^^^^^^^^^^^^^^^^^^^^^^^^
$a = ANY_CONSTANT;

mbregex_encoding($test_enc);                // Noncompliant {{Replace this "mbregex_encoding()" call with a call to "mb_regex_encoding()".}}
mbereg_search_init($str, $look_for, $opt);  // Noncompliant {{Replace this "mbereg_search_init()" call with a call to "mb_ereg_search_init()".}}
while (mbereg_search_pos()) {               // Noncompliant {{Replace this "mbereg_search_pos()" call with a call to "mb_ereg_search_pos()".}}
    $regs = mbereg_search_getregs();        // Noncompliant {{Replace this "mbereg_search_getregs()" call with a call to "mb_ereg_search_getregs()".}}
    printf("%d\n", mbereg_search_getpos()); // Noncompliant {{Replace this "mbereg_search_getpos()" call with a call to "mb_ereg_search_getpos()".}}
}
mbereg('[0-9]', $text);                      // Noncompliant {{Replace this "mbereg()" call with a call to "mb_ereg()".}}
mberegi('äpfel', $text);                     // Noncompliant {{Replace this "mberegi()" call with a call to "mb_eregi()".}}
$data = mbereg_replace("/[0-9]/","",$data);  // Noncompliant {{Replace this "mbereg_replace()" call with a call to "mb_ereg_replace()".}}
$data = mberegi_replace("/[0-9]/","",$data); // Noncompliant {{Replace this "mberegi_replace()" call with a call to "mb_eregi_replace()".}}
$data = mbsplit("/\s/", "hello world");      // Noncompliant {{Replace this "mbsplit()" call with a call to "mb_split()".}}
$test = mbereg_match("a", "some apples");    // Noncompliant {{Replace this "mbereg_match()" call with a call to "mb_ereg_match()".}}
$r = mbereg_search();                        // Noncompliant {{Replace this "mbereg_search()" call with a call to "mb_ereg_search()".}}
$r = mbereg_search_regs();                   // Noncompliant {{Replace this "mbereg_search_regs()" call with a call to "mb_ereg_search_regs()".}}
mbereg_search_setpos($i);                    // Noncompliant {{Replace this "mbereg_search_setpos()" call with a call to "mb_ereg_search_setpos()".}}

mb_regex_encoding($test_enc);
mb_ereg_search_init($str, $look_for, $opt);
while (mb_ereg_search_pos()) {
    $regs = mb_ereg_search_getregs();
    printf("%d\n", mb_ereg_search_getpos());
}
mb_ereg('[0-9]', $text);
mb_eregi('äpfel', $text);
$data = mb_ereg_replace("/[0-9]/","",$data);
$data = mb_eregi_replace("/[0-9]/","",$data);
$data = mb_split("/\s/", "hello world");
$test = mb_ereg_match("a", "some apples");
$r = mb_ereg_search();
$r = mb_ereg_search_regs();
mb_ereg_search_setpos($i);

$buffer = fgetss($handle, 4096);  // Noncompliant {{Remove this call to deprecated "fgetss()".}}
$buffer = gzgetss($handle, 4096); // Noncompliant {{Remove this call to deprecated "gzgetss()".}}

stream_filter_append($fp); // coverage
stream_filter_append($fp, $filterName);
stream_filter_append($fp, 'string.tolower');
stream_filter_append($fp, 'string.strip_tags', STREAM_FILTER_WRITE, "<strong><em><span>"); // Noncompliant {{Remove this deprecated "string.strip_tags" filter usage.}}
//                        ^^^^^^^^^^^^^^^^^^^

function f() {
  $file = new SplFileObject("sample.php");
  $obj = new Unknown();
  while (!$file->eof()) {
    echo $file->fgetss();  // Noncompliant {{Remove this call to deprecated "fgetss()".}}
//       ^^^^^^^^^^^^^^^
    echo $obj->fgetss();
  }
}

image2wbmp($image);  // Noncompliant {{Replace this "image2wbmp()" call with a call to "imagewbmp()".}}
imagewbmp($image);

$char = Normalizer::normalize($char_A_ring, Normalizer::NONE);  // Noncompliant {{Do not use this deprecated "Normalizer::NONE" constant.}}
//                                          ^^^^^^^^^^^^^^^^
$x = Other::Normalizer::NONE;
$x = Normalizer::$NONE;

$a = stristr($email, 'e');
$a = stristr($email, 42);  // Noncompliant {{Convert this integer needle into a string.}}
//                   ^^
$a = strrchr($PATH, ":");
$a = strrchr($PATH, -1);  // Noncompliant {{Convert this integer needle into a string.}}

$user = strstr($email, '@', true);
$user = strstr($email, 0x41, true);  // Noncompliant {{Convert this integer needle into a string.}}

$pos = strripos($haystack, $needle);
$pos = strripos($haystack, 127);  // Noncompliant {{Convert this integer needle into a string.}}

$pos1 = stripos($mystring1, null);
$pos1 = stripos($mystring1, 1);  // Noncompliant {{Convert this integer needle into a string.}}

$pos = strrpos($mystring, "b");
$pos = strrpos($mystring, 49);  // Noncompliant {{Convert this integer needle into a string.}}

$pos = strpos($mystring, f());
$pos = strpos($mystring, 32);  // Noncompliant {{Convert this integer needle into a string.}}

$result = strchr($stringA); // coverage
$result = strchr($stringA,$toFind);
$result = strchr($stringA, 0); // Noncompliant {{Convert this integer needle into a string.}}
