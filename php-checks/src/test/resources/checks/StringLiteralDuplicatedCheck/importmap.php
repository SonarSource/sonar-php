<?php

echo "aaaa()"; // Noncompliant {{Define a constant instead of duplicating this literal "aaaa()" 5 times.}}
//   ^^^^^^^^
echo "aaaa()"; // OK

echo "aaaa()";
//   ^^^^^^^^< {{Duplication.}}
echo "aaaa()";
//   ^^^^^^^^< {{Duplication.}}
echo "aaaa()";
//   ^^^^^^^^< {{Duplication.}}


return [
    'imports' => [
        'jquery' => 'https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js',
        'vue' => 'https://cdn.jsdelivr.net/npm/vue@2.6.14/dist/vue.min.js',
    ],
];
