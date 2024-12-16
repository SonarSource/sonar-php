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

$output .= '<div class="beautiful">'.$total.'</div>';
$output .= '<div class="beautiful">'.$total_payment.'</div>';
$output .= '<div class="beautiful">'.$inc_vat_text.'</div>';

$output .= '<span class="payment">'.$total.'</span' + '>';
$output .= '<span class="payment-summary">'.$total_payment.'</span' + '>';
$output .= '<span class="badge normal">'.$inc_vat_text.'</span' + '>';

// HTML tag with attribute
$output .= '<div some-attribute-1="value1" some-attribute-2=\'value2\' some-attribute-3=value3 some-attribute-4>'.$total.'</div>';
$output .= '<div some-attribute-1="value1" some-attribute-2=\'value2\' some-attribute-3=value3 some-attribute-4>'.$total.'</div>';
$output .= '<div some-attribute-1="value1" some-attribute-2=\'value2\' some-attribute-3=value3 some-attribute-4>'.$total.'</div>';

// partial HTML tag start
$output .= '<div some-attr='.$value.'>';
$output .= '<div some-attr='.$value.'>';
$output .= '<div some-attr='.$value.'>';

// partial HTML tag end
$output .= '<div><'.$tag_name.' with="value">';
$output .= '<div><'.$tag_name.' with="value">';
$output .= '<div><'.$tag_name.' with="value">';

// multiple HTML tags
$output .= '<div><p><span>'.$tag_name.'</div></p></span>';
$output .= '<div><p><span>'.$tag_name.'</div></p></span>';
$output .= '<div><p><span>'.$tag_name.'</div></p></span>';

// multiple HTML tags with text content outside of them
$output .= '<div> some <p> content <span> here '.$tag_name.'</div> and </p> there </span>';
$output .= '<div> some <p> content <span> here '.$tag_name.'</div> and </p> there </span>';
$output .= '<div> some <p> content <span> here '.$tag_name.'</div> and </p> there </span>';

// multiple HTML tags with parital end or start tag
$output .= ' left-from-previous="string"> here '.$tag_name.'</div><div';
$output .= ' left-from-previous="string"> here '.$tag_name.'</div><div';
$output .= ' left-from-previous="string"> here '.$tag_name.'</div><div';

// end and start tag in the same string literal
$output .= 'end-of-tag="val"><div start-of-tag="val" '.$additionl_attribute;
$output .= 'end-of-tag="val"><div start-of-tag="val" '.$additionl_attribute;
$output .= 'end-of-tag="val"><div start-of-tag="val" '.$additionl_attribute;
