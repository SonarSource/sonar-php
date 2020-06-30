<?php

$str = "foo";

hash($str);
foo($str);
sha1_file($str);

  md5($str);     // Noncompliant {{Make sure that hashing data is safe here.}}
//^^^
md5($str, true); // Noncompliant {{Make sure that hashing data is safe here.}}
sha1($str);       // Noncompliant {{Make sure that hashing data is safe here.}}
SHA1($str, false); // Noncompliant {{Make sure that hashing data is safe here.}}
