<?php

$arr = array(1, 2, 3);
foreach ($arr as &$value) { // Noncompliant {{Make sure that the referenced value variable is unset after the loop.}}
    $value = $value * 2;
}
$value = "x";

$arr2 = array(1, 2, 3);
foreach ($arr2 as &$value2) { // Compliant
    $value2 = $value2 * 2;
}
unset($value2);

$arr3 = array(1, 2, 3);
foreach ($arr3 as &$value3) { // Noncompliant
    $value3 = $value3 * 2;
}
echo $value3;

$arr4 = array(1, 2, 3);
foreach ($arr4 as &$value4) { // Noncompliant
    $value4 = $value4 * 2;
    unset($value4);
}
$value4 = "x";

$arr5 = array(1, 2, 3);
foreach ($arr5 as $value5) { // Compliant
    $value5 = $value5 * 2;
}

$arr6 = array(1, 2, 3);
foreach ($arr6 as &$value6) { // Compliant
    $value6 = $value6 * 2;
} unset($value6);

$arr7 = array(1, 2, 3);
foreach ($arr7 as &$value7) { // Noncompliant
    $value7 = $value7 * 2;
unset($value7); }
$value7 = "x";

$arr8 = array(1, 2, 3);
foreach ($arr8 as &$value8) { // Compliant - FN . Implementation does not use CFG.
    $value8 = $value8 * 2;
}
if (false) {
  unset($value8);
}

$arr9 = array(1, 2, 3);
foreach ($arr9 as &$value9) { // Compliant
    $value9 = $value9 * 2;
}

$arr10 = array(1, 2, 3);
foreach ($arr10 as $key => &$value10) { // Compliant
    $value10 = $value10 * 2;
}

foreach ($x as &$obj->prop) {
    var_dump($obj->prop);
}
