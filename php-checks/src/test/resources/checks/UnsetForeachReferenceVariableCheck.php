<?php

$arr = array(1, 2, 3);
foreach ($arr as &$value) { // Noncompliant {{Make sure that the referenced value variable is unset after the loop.}}
    $value = $value * 2;
}

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
