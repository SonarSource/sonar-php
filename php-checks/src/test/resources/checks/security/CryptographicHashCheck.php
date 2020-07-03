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

hash_init("md2"); // Noncompliant
//        ^^^^^
hash_init("md4"); // Noncompliant
hash_init("md5"); // Noncompliant
hash_init("sha1"); // Noncompliant
hash_init("sha224"); // Noncompliant
hash_init("ripemd128"); // Noncompliant
hash_init("ripemd160"); // Noncompliant
hash_init("haval160,3"); // Noncompliant
hash_init("haval192,3"); // Noncompliant
hash_init("haval224,3"); // Noncompliant


hash("md2"); // Noncompliant
hash("md4"); // Noncompliant
hash("md5"); // Noncompliant
hash("sha1"); // Noncompliant
hash("sha224"); // Noncompliant
hash("ripemd128"); // Noncompliant
hash("ripemd160"); // Noncompliant
hash("haval160,3"); // Noncompliant
hash("haval192,3"); // Noncompliant
hash("haval224,3"); // Noncompliant

hash_pbkdf2("md2", $password, $salt, $iterations); // Noncompliant
hash_pbkdf2("md4", $password, $salt, $iterations); // Noncompliant
hash_pbkdf2("md5", $password, $salt, $iterations); // Noncompliant
hash_pbkdf2("sha1", $password, $salt, $iterations); // Noncompliant
hash_pbkdf2("sha224", $password, $salt, $iterations); // Noncompliant
hash_pbkdf2("ripemd128", $password, $salt, $iterations); // Noncompliant
hash_pbkdf2("ripemd160", $password, $salt, $iterations); // Noncompliant
hash_pbkdf2("haval160,3", $password, $salt, $iterations); // Noncompliant
hash_pbkdf2("haval192,3", $password, $salt, $iterations); // Noncompliant
hash_pbkdf2("haval224,3", $password, $salt, $iterations); // Noncompliant

mhash(MHASH_MD2, $data); // Noncompliant
mhash(MHASH_MD4, $data); // Noncompliant
mhash(MHASH_MD5, $data); // Noncompliant
mhash(MHASH_RIPEMD128, $data); // Noncompliant
mhash(MHASH_SHA1, $data); // Noncompliant
mhash(MHASH_SHA192, $data); // Noncompliant
mhash(MHASH_SHA224, $data); // Noncompliant
mhash(MHASH_HAVAL128, $data); // Noncompliant
mhash(MHASH_HAVAL160, $data); // Noncompliant
mhash(MHASH_HAVAL192, $data); // Noncompliant
mhash(MHASH_HAVAL224, $data); // Noncompliant
