<?php


if (count($a) > 0) { // Noncompliant
//  ^^^^^^^^^^^^^
  echo $a[0];
}

if (sizeof($a) > 0) { // Noncompliant
//  ^^^^^^^^^^^^^^
  echo $a[0];
}

if (count($z_3) != 0) { // Noncompliant
  echo $z_3[0];
}

if (count($z_9) >= 1) { // Noncompliant
  echo $z_9[0];
}

if (0 != count($z_10)) { // Noncompliant
  echo $z_10[0];
}

if (1 <= count($z_11)) { // Noncompliant
  echo $z_11[0];
}

if (count($z_12) >= 1) { // Noncompliant
  echo $z_12[0];
}

if (count($_POST) > 0) { // Noncompliant
  echo $a;
}

if (count($a_2) >= 0) { // Compliant
  echo $a_2;
}

if (0 < count($a_3)) { // Noncompliant
  echo $a_3[0];
}

if (count($a_4) >= 2) { // Compliant
  echo $a_4[1];
}

$b = getData();
echo $b[0];
if (count($b) > 0) { // Noncompliant
  echo "foo";
}

if (count($c) > 0) { // Compliant - We are not sure that $c is an array
  echo "foo";
}

if (foo($c_1) > 0) { // Compliant
  echo $c_1[0];
}

if (foo($c_2)) {
  echo $c_2[0];
}

function f(array $d) {
  if (count($d) > 0) { // Noncompliant
    echo "foo";
  }
}

function f_2(string $d) {
  if (count($d) > 0) { // Compliant
    echo "foo";
  }
}

function f_3(SomeObject $d) {
  if (count($d) > 0) { // Compliant
    echo "foo";
  }
}

function f_4($d) {
  if (count($d) > 0) { // Compliant
    echo "foo";
  }
}

function union_type(array|Foo $d) {
  if (count($d) > 0) { // Compliant
    echo "foo";
  }
}

if (count($x)) { // Compliant - FN
  echo $x[0];
}

if (count($y) > "abc") {
  echo $y[0];
}

if (count(z) + 1) { // Compliant
  echo $z[0];
}

if (count()) { // Compliant
  echo $foo[0];
}

if (count(getData()) > 0) { // Compliant
  echo "foo";
}

if (count($z_2, COUNT_RECURSIVE) > 0) { // Noncompliant
  echo $z_2[0];
}

if (count($z_4) === "abc") { // Compliant - Doesn't make sense but for test coverage
  echo $z_4[0];
}

if (count($z_5) === $someVar) { // Compliant
  echo $z_5[0];
}

if (count($z_6) === 2) { // Compliant
  echo $z_6[0];
}


if (count($z_8) >= "abc") { // Compliant
  echo $z_8[0];
}

function named_arguments() {
  count(array_or_countable: $_POST) > 0; // Noncompliant
  count(mode: 1, array_or_countable: $_POST) > 0; // Noncompliant
  count(mode: 1, array_or_countable: $unknown_var) > 0;
  sizeof(mode: 1, array_or_countable: $_POST) > 0; // Noncompliant
  sizeof(mode: 1, array_or_countable: $unknown_var) > 0;
}

$example = (!empty($exampleName) && count($exampleName) > 0) ? $exampleName[0] : '';
$example2 = (empty($exampleName2) || count($exampleName2) == 0) ? '' : $exampleName2[0];
if (!empty($exampleName3) && count($exampleName3) > 0) {
  echo $exampleName3[0];
}
if (empty($exampleName4) or count($exampleName4) == 0) {
  echo "empty";
}

if (!empty($differentVar) && count($exampleName5) > 0) { // Noncompliant
  echo $exampleName5[0];
}

