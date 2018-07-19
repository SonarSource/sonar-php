<?php

$str = "foo";

hash($str);
foo($str);
sha1_file($str);

  md5($str);     // Noncompliant {{Use a stronger hashing algorithm than MD5.}}
//^^^
md5($str, true); // Noncompliant {{Use a stronger hashing algorithm than MD5.}}
sha1($str);       // Noncompliant {{Use a stronger hashing algorithm than SHA1.}}
sha1($str, false); // Noncompliant {{Use a stronger hashing algorithm than SHA1.}}
