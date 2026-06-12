<?php

$str = "foo";

hash($str);
foo($str);
sha1_file($str);
hash(algo: $str); 

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
hash_init(1, algo: "haval224,3"); // Noncompliant
hash_init("sha512"); // Compliant
hash_init($unknown); // Compliant
hash_init(algo: $unknown); // Compliant

hash("md2", $data); // Noncompliant
hash("md4", $data); // Noncompliant
hash("md5", $data); // Noncompliant
hash("sha1", $data); // Noncompliant
hash("sha224", $data); // Noncompliant
hash("ripemd128", $data); // Noncompliant
hash("ripemd160", $data); // Noncompliant
hash("haval160,3", $data); // Noncompliant
hash("haval192,3", $data); // Noncompliant
hash("haval224,3", $data); // Noncompliant
hash($data, $value, "foo", algo: "md2"); // Noncompliant
hash("sha512", $data); // Compliant
hash($unknown, $data); // Compliant
hash(algo: "sha512"); 

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
hash_pbkdf2($password, $salt, $iterations, algo: "haval224,3"); // Noncompliant
hash_pbkdf2("sha512", $password, $salt, $iterations); // Compliant
hash_pbkdf2($unknown, $password, $salt, $iterations); // Compliant
hash_pbkdf2($password, $salt, $iterations, algo: "sha512"); // Compliant

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
mhash(MHASH_HAVAL224, $data); // Noncompliant
mhash($data, hash: MHASH_HAVAL224); // Noncompliant
mhash(MHASH_SHA512, $data); // Compliant
mhash("xyz", $data); // Compliant
mhash(algo: "xyz", $data); // Compliant

// Compliant: truncated output signals a non-cryptographic use (cache key, ETag, short identifier, etc.)
substr(md5($data), 0, 8);
substr(sha1($url), 0, 4);
substr(hash('sha1', $email), 0, 5);
substr(hash('sha1', $seed), -6);
mb_substr(md5($data), 0, 8);
mb_substr(sha1($data), 0, 4);
SUBSTR(md5($data), 0, 8);

// Hash used as length argument rather than string to truncate - still noncompliant
substr($str, 0, md5($str));  // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}
//              ^^^
substr($str, 0, sha1($str)); // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}
substr($str, 0, hash('sha1', $str)); // Noncompliant
//                   ^^^^^^

// Wrapped in something other than substr/mb_substr - still noncompliant
strtolower(md5($data)); // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}
//         ^^^
strtolower(sha1($data)); // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}

// Dynamic/variable callee - still noncompliant, no NPE on null function name
$fn(md5($data)); // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}

// Compliant: hash used as a WordPress cache/transient key - not a security primitive
wp_cache_get(md5($sql), 'group');
wp_cache_set(sha1($sql), $value, 'group');
wp_cache_add(md5($sql), $value);
wp_cache_delete(md5($sql), 'group');
wp_cache_replace(md5($sql), $value);
wp_cache_incr(md5($sql));
wp_cache_decr(sha1($sql));
get_transient(md5($sql));
set_transient(sha1($query), $result, HOUR_IN_SECONDS);
set_transient('tx_' . sha1($query), $result, HOUR_IN_SECONDS);
set_transient('prefix_' . md5($query) . '_suffix', $result, HOUR_IN_SECONDS);
wp_cache_get('key_' . hash('sha1', $sql), 'group');
delete_transient(md5($sql));
wp_cache_get(hash('sha1', $sql), 'group');

// Named argument places hash in the value slot, not the key slot - still noncompliant
wp_cache_set(value: md5($value), key: $key); // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}
//                  ^^^

// Hash not in the key position (not first argument) - still noncompliant
wp_cache_set($key, md5($value)); // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}
//                 ^^^
wp_cache_set($key, sha1($value)); // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}
wp_cache_set($key, hash('sha1', $value)); // Noncompliant
//                      ^^^^^^

// Hash passed to a non-cache function or dynamic callee - still noncompliant, no NPE on null function name
some_function(md5($data)); // Noncompliant {{Make sure this weak hash algorithm is not used in a sensitive context here.}}
//            ^^^
