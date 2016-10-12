<?php

echo "aaa";    // OK - too short
echo "aaa";
echo "aaa";

echo "bbbbb";  // OK
echo "bbbbb";

echo "aaaaa";  // NOK [[effortToFix=3;secondary=+0,+2,+3]] {{Define a constant instead of duplicating this literal "aaaaa" 3 times.}}
//   ^^^^^^^
echo "aaaaa";
echo "aaaaa";

echo "$toto";  // OK
$toto = "new value";
echo "$toto";
echo "$toto";

$a["name1"];    // NOK [[effortToFix=4]] {{Define a constant instead of duplicating this literal "name1" 4 times.}}
$a["name1"];
$a["name1"];
$a["name1"];

