<?php

echo "aaa";    // OK - too short
echo "aaa";
echo "aaa";

echo "bbbbb";  // OK
echo "bbbbb";

echo "aaaaa";  // NOK {{Define a constant instead of duplicating this literal "aaaaa" 3 times.}}
echo "aaaaa";
echo "aaaaa";

echo "$toto";  // NOK {{Define a constant instead of duplicating this literal "$toto" 3 times.}}
echo "$toto";
echo "$toto";

$a["name1"];    // NOK {{Define a constant instead of duplicating this literal "name1" 4 times.}}
$a["name1"];
$a["name1"];
$a["name1"];

