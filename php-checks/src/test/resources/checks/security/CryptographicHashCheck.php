<?php

$str = "foo";

hash($str);
foo($str);
sha1_file($str);

  md5($str);     // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}
//^^^
md5($str, true); // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}
sha1($str);       // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}
SHA1($str, false); // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}
