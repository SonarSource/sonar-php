<?php

echo "aa()";    // OK - too short
echo "aa()";
echo "aa()";
echo "aa()";
echo "aa()";

echo "aaaa()"; // Noncompliant {{Define a constant instead of duplicating this literal "aaaa()" 5 times.}}
//   ^^^^^^^^
echo "aaaa()";
//   ^^^^^^^^< {{Duplication.}}
echo "aaaa()";
//   ^^^^^^^^< {{Duplication.}}
echo "aaaa()";
//   ^^^^^^^^< {{Duplication.}}
echo "aaaa()";
//   ^^^^^^^^< {{Duplication.}}

echo "12aaaaa";  // Noncompliant {{Define a constant instead of duplicating this literal "12aaaaa" 5 times.}}
//   ^^^^^^^^^
echo "12aaaaa";
echo "12aaaaa";
echo "12aaaaa";
echo "12aaaaa";

echo "$totototo";  // OK - Variable in string
$totototo = "new value";
echo "$totototo";
echo "$totototo";
echo "$totototo";
echo "$totototo";


$a["name1_name-2."];    // OK - Only alphanumeric, -, _ and .
$a["name1_name-2."];
$a["name1_name-2."];
$a["name1_name-2."];
$a["name1_name-2."];

$a["name1/name2"];    // Noncompliant {{Define a constant instead of duplicating this literal "name1/name2" 5 times.}}
$a["name1/name2"];
$a["name1/name2"];
$a["name1/name2"];
$a["name1/name2"];

$database = config('connections.read.database');    // OK - Only alphanumeric, -, _ and .
$database = config('connections.read.database');
$database = config('connections.read.database');
$database = config('connections.read.database');
$database = config('connections.read.database');

$score = $request->getParam('_score'); // OK - Only alphanumeric, -, _ and .
$score = $request->getParam('_score');
$score = $request->getParam('_score');

$output = '';
$output .= '<span class="payment">'.$total.'</span>';
$output .= '<span class="payment-summary">'.$total_payment.'</span>';
$output .= '<span class="badge normal">'.$inc_vat_text.'</span>';

$output .= '<div>'.$total.'</div>';
$output .= '<div>'.$total_payment.'</div>';
$output .= '<div>'.$inc_vat_text.'</div>';

$output = '<custom-tag_name:example.class>'.$var1.'</custom-tag_name:example.class>';
$output = '<custom-tag_name:example.class>'.$var2.'</custom-tag_name:example.class>';
$output = '<custom-tag_name:example.class>'.$var3.'</custom-tag_name:example.class>';

$output .= '<div class="beautiful">'.$total.'</div>'; // Noncompliant {{Define a constant instead of duplicating this literal "<div class="beautiful">" 3 times.}}
$output .= '<div class="beautiful">'.$total_payment.'</div>';
$output .= '<div class="beautiful">'.$inc_vat_text.'</div>';

$output .= '<span class="payment">'.$total.'</span' + '>'; // Noncompliant {{Define a constant instead of duplicating this literal "</span" 3 times.}}
$output .= '<span class="payment-summary">'.$total_payment.'</span' + '>';
$output .= '<span class="badge normal">'.$inc_vat_text.'</span' + '>';
