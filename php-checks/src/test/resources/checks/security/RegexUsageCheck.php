<?php

function test($pattern, $inputs) {
    $replacement = 'myvalue';
    $input = $inputs[0];
    # PCRE functions
    preg_filter($pattern, $replacement, $inputs); // Noncompliant {{Make sure that using a regular expression is safe here.}}
    preg_grep($pattern, $inputs); // Noncompliant
    PREG_GREP($pattern, $inputs); // Noncompliant
    preg_match_all($pattern, $input); // Noncompliant
    preg_match($pattern, $input); // Noncompliant
    if (version_compare(PHP_VERSION, '7.0', '>=')) {
        preg_replace_callback_array( // Noncompliant
            [
                $pattern => function ($match) {},
            ],
            $input
        );
    }
    preg_replace_callback($pattern, function ($matches) {return '';}, $input); // Noncompliant
    preg_replace($pattern, $replacement, $input); // Noncompliant
    preg_split($pattern, $input); // Noncompliant
    fnmatch ($pattern, $input); // Noncompliant
    // POSIX EXTENDED functions
    if (version_compare(PHP_VERSION, '7.0', '<')) {
        ereg_replace($pattern, $replacement, $input); // Noncompliant
        ereg($pattern, $input); // Noncompliant
        eregi_replace($pattern, $replacement, $input); // Noncompliant
        eregi($pattern, $input); // Noncompliant
        split($pattern, $input); // Noncompliant
        spliti($pattern, $input); // Noncompliant
    }
    // Multibyte POSIX EXTENDED
    mb_ereg_replace($pattern, $replacement, $input); // Noncompliant
    mb_ereg($pattern, $input); // Noncompliant
    mb_eregi_replace($pattern, $replacement, $input); // Noncompliant
    mb_eregi($pattern, $input); // Noncompliant
    mb_ereg_match($pattern, $input); // Noncompliant
    mb_ereg_replace_callback($pattern, '', $input); // Noncompliant
    mb_ereg_search_init($input); // Noncompliant
    mb_ereg_search_pos($pattern); // Noncompliant
    mb_ereg_search_pos(); // OK
    mb_ereg_search_regs($pattern); // Noncompliant
    mb_ereg_search_regs(); // OK
    mb_ereg_search($pattern); // Noncompliant
    mb_ereg_search(); // OK
}
