<?php

echo "aaa";    // OK - too short
echo "aaa";
echo "aaa";

echo "bbbbb";  // OK
echo "bbbbb";

echo "aaaaa";  // NOK
echo "aaaaa";
echo "aaaaa";

echo "$toto";  // NOK
echo "$toto";
echo "$toto";

$a["name1"];    // NOK
$a["name1"];
$a["name1"];
$a["name1"];

