<?php

function testFunctions($inputs) {
    $replacement = 'myvalue';
    $input = $inputs[0];
    # PCRE functions
    preg_filter("/(a+)+/", $replacement, $inputs); // Noncompliant {{Make sure that using a regular expression is safe here.}}
    preg_grep("/(a+)+/", $inputs); // Noncompliant
    PREG_GREP("/(a+)+/", $inputs); // Noncompliant
    preg_match_all("/(a+)+/", $input); // Noncompliant
    preg_match("/(a+)+/", $input); // Noncompliant
    if (version_compare(PHP_VERSION, '7.0', '>=')) {
        preg_replace_callback_array( // FN
            [
                "/(a+)+/" => function ($match) {},
            ],
            $input
        );
    }
    preg_replace_callback("/(a+)+/", function ($matches) {return '';}, $input); // Noncompliant
    preg_replace("/(a+)+/", $replacement, $input); // Noncompliant
    preg_split("/(a+)+/", $input); // Noncompliant
    fnmatch ("/(a+)+/", $input); // Noncompliant
    // POSIX EXTENDED functions
    if (version_compare(PHP_VERSION, '7.0', '<')) {
        ereg_replace("/(a+)+/", $replacement, $input); // Noncompliant
        ereg("/(a+)+/", $input); // Noncompliant
        eregi_replace("/(a+)+/", $replacement, $input); // Noncompliant
        eregi("/(a+)+/", $input); // Noncompliant
        split("/(a+)+/", $input); // Noncompliant
        spliti("/(a+)+/", $input); // Noncompliant
    }
    // Multibyte POSIX EXTENDED
    mb_ereg_replace("/(a+)+/", $replacement, $input); // Noncompliant
    mb_ereg("/(a+)+/", $input); // Noncompliant
    mb_eregi_replace("/(a+)+/", $replacement, $input); // Noncompliant
    mb_eregi("/(a+)+/", $input); // Noncompliant
    mb_ereg_match("/(a+)+/", $input); // Noncompliant
    mb_ereg_replace_callback("/(a+)+/", '', $input); // Noncompliant
    mb_ereg_search_init($input); // OK
    mb_ereg_search_init("/(a+)+/"); // OK
    mb_ereg_search_init($input, "/(a+)+/"); // Noncompliant
    mb_ereg_search_pos("/(a+)+/"); // Noncompliant
    mb_ereg_search_pos(); // OK
    mb_ereg_search_regs("/(a+)+/"); // Noncompliant
    mb_ereg_search_regs(); // OK
    mb_ereg_search("/(a+)+/"); // Noncompliant
    mb_ereg_search(); // OK
}

function testPattern() {
  preg_match("/(a+)+/", "input"); // Noncompliant
  preg_match("/a*+/", "input"); // Noncompliant
  preg_match("/a*{/", "input"); // Noncompliant
  preg_match("/++/", "input"); // OK, too short
  preg_match("/(a+)/", "input"); // OK, not enough of special chars
  preg_match("/abc/", "input"); // OK, not enough of special chars
}
