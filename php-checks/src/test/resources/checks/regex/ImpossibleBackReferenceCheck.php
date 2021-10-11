<?php

  function noncompliant($input) {
    preg_match("//\\1 (.)/", $input); // Noncompliant {{Fix this backreference, so that it refers to a group that can be matched before it.}}
    //            ^^^ ^^^<
    preg_match("/\k<name>(?<name>.)/", $input);// Noncompliant {{Fix this backreference, so that it refers to a group that can be matched before it.}}

    preg_match("/(.)|\\1/", $input); // Noncompliant
    preg_match("/\\1/", $input); // Noncompliant {{Fix this backreference - it refers to a capturing group that doesn't exist.}}
    preg_match("/\\2(.)/", $input); // Noncompliant {{Fix this backreference - it refers to a capturing group that doesn't exist.}}
    preg_match("/(.)\\2(.)/", $input); // Noncompliant {{Fix this backreference, so that it refers to a group that can be matched before it.}}
    preg_match("/(?<x>.)\\k<y>(?<y>.)/", $input); // Noncompliant {{Fix this backreference, so that it refers to a group that can be matched before it.}}
    preg_match("/(?<anothername>)\\k<name>/", $input); // Noncompliant {{Fix this backreference - it refers to a capturing group that doesn't exist.}}
    preg_match("/\\k<name>(?<name>.)/", $input); // Noncompliant
    preg_match("/(?<name>.)|\\k<name>/", $input); // Noncompliant
    preg_match("/(?:\\1(.))*/", $input); // Noncompliant
    preg_match("/\\1|(.)/", $input); // Noncompliant
    preg_match("/(.)\\2(.)\\1/", $input); // Noncompliant
    preg_match("/(?:\\1\\2|x(.))*/", $input); // Noncompliant
    preg_match("/(.)(?:\\1\\2\\3|x(.))*/", $input); // Noncompliant
    preg_match("/(\\1)*/", $input); // Noncompliant
    preg_match("/(?:\\1|x(.))?/", $input); // Noncompliant
    preg_match("/(?:\\1|x(.)){1,1}/", $input); // Noncompliant
  }

  function compliant($input) {
    preg_match("/(.)\\1/", $input);
    preg_match("/(?:(.)\\1)*/", $input);
    preg_match("/(.)\\1(.)\\2/", $input);
    preg_match("/(?:x(.)|\\1)*/", $input);
    preg_match("/(?<name>)\\k<name>/", $input);
    preg_match("/(?<name>)\\1/", $input);
    preg_match("/(?:\\1|x(.))*/", $input);
    preg_match("/(?:\\1|x(.))+/", $input);
    preg_match("/(?:\\1|x(.)){0,2}/", $input);
    preg_match("/(?:\\1|x(.)){1,2}/", $input);
    preg_match("/(?:\\1\\2|(x)(.))*/", $input);
    preg_match("/(.)(?:\\1\\2|x(.))*/", $input);
    preg_match("/(1)\\11/", $input); // Compliant, backreference is \1 because group 11 does not exist
    preg_match("/(1)(2)(3)(4)(5)(6)(7)(8)(9)(a)\\11(b)/", $input); // Compliant, backreference is \1 because 11 does not exist at this point in the regex
    preg_match("/(1)(2)(3)(4)(5)(6)(7)(8)(9)(a)(b)\\11/", $input); // Compliant, backreference is \11 because group 11 exists at this point in the regex
  }
