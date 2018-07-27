<?php

function x() {
  return self::$s['a'];
}

$f1 = function() use($a, &$b) { return $a + $b; };

function f1() {
  $v01 = $v02; // Noncompliant {{Review the data-flow - use of uninitialized value.}}
  //     ^^^^

  if ($v03 == $v04) {} // Noncompliant 2
  if ($v06 && $v07) {} // Noncompliant 2
  if ($v08 || $v09) {} // Noncompliant 2
  if ($v10 <  $v11) {} // Noncompliant 2
  if ($v12 <= $v13) {} // Noncompliant 2
  if ($v14 >  $v15) {} // Noncompliant 2
  if ($v16 >= $v17) {} // Noncompliant 2
  if ($v18 != $v19) {} // Noncompliant 2
  if ($v20) {} // Noncompliant
  if ($v21 + $v22) {} // Noncompliant 2
  if ($v23 - $v24) {} // Noncompliant 2
  if ($v25 * $v26) {} // Noncompliant 2
  if ($v27 / $v28) {} // Noncompliant 2
  if (!$v29) {} // Noncompliant
  if ($v30 % $v31) {} // Noncompliant 2
  if ($v32 ** $v33) {} // Noncompliant 2
  if ($v34 === $v35) {} // Noncompliant 2
  if ($v36 !== $v37) {} // Noncompliant 2

  while ($v50) {} // Noncompliant
  for ($v51 = 0; $v52; $v53++) {} // Noncompliant 2

  if (++$v54) {} // Noncompliant
  if ($v55--) {} // Noncompliant
  if (--$v56) {} // Noncompliant

  if ($v57 <> $v58) {} // Noncompliant 2
  if ($v59 and $v60) {} // Noncompliant 2
  if ($v61 or $v62) {} // Noncompliant 2
  if ($v63 xor $v64) {} // Noncompliant 2

  if ($v65 | $v66) {} // Noncompliant 2
  if ($v67 & $v68) {} // Noncompliant 2
  if ($v69 ^ $v70) {} // Noncompliant 2
  if (~$v71) {} // Noncompliant
  if ($v72 << $v73) {} // Noncompliant 2
  if ($v74 >> $v75) {} // Noncompliant 2
  if ($v76 . $v77) {} // Noncompliant 2

  for($i=0; $i<2; $i += $v78) { // Noncompliant
  //                    ^^^^
  }

  for($i=0; $i<2; $i += $v79) {
    $v79 = 1;
  }

  $v80->m(); // Noncompliant
  $a = new A();
  $a->v81();
  $a->$v81(); // Noncompliant
  $$v82->m(); // Noncompliant
  ${$v83}->m(); // Noncompliant

  $v84[0] = 2; // valid, create the array and set one value
  $x = $v85[0]; // Noncompliant
  $y = $x[$v86]; // Noncompliant

  return $v99; // Noncompliant
  //     ^^^^
}

function f2() {
  $a = 2;
  return $a;
}

function f3($a, &$b)  {
  return $a + $b;
}

function f4() {
  global $a;
  return $a;
}

function f5() {
  static $a = 3, $b;
  if ($a) {}
  return $b; // Noncompliant
}

function f6() {
  $a; // Noncompliant
  return $a;
}

function f7() {
  unknown_function();
  return $a; // Noncompliant
}

function f8() {
  if ($a) {} // false-negative, need cfg
  parse_str("a=3&b=4");
  return $b;
}

function f9() {
  extract(array("a" => "3", "b" => "4"));
  return $a + $b;
}

function f10() {
  eval('$a = 2;');
  return $a;
}

function f11() {
  preg_replace('...');
  return $a;
}

function f12() {
  include 'other-file.php';
  return $a;
}

function f13() {
  include_once 'other-file.php';
  return $a;
}


function f14() {
  require 'other-file.php';
  return $a;
}

function f15() {
  require_once 'other-file.php';
  return $a;
}

function f16() {
  unknown_function($a);
}

function f17() {
  $b = hex2bin($a); // Noncompliant
  //           ^^
}

function f18() {
  $unknown1($a, $b); // Noncompliant
//^^^^^^^^^
  ($unknown2)($a, $b); // Noncompliant
// ^^^^^^^^^
}

function f19() {
  return $unknown1[2]; // Noncompliant
}

function f20() {
  foreach ($values as $key => $val) { // Noncompliant
  //       ^^^^^^^
    return $key . $val;
  }
}

function f21() {
  $a = <<<EOS
    A: $a, B: $b
EOS; 
  // Noncompliant@-2
  //          ^^@-2

  return "A: $a, B: $c"; // Noncompliant
  //                ^^
}

function f22() {
  $a = "2";
  $b = "2";
  return array(
      $a => $b,
      $a => $c, // Noncompliant
    //      ^^
      $d => $a, // Noncompliant
    //^^
      $a => &$e);
}

function f23() {
  list($extractA, $extractB) = array(1, 2);
  [$extractC, ,$extractD] = array(4, 5, 6);
  return $extractA + $extractB + $extractC + $extractD;
}

function f24() {
  unset($a);
  if (isset($b)) {
  }
}

function f25() {
  try {
    throw new MyException();
  } catch (MyException | MyOtherException $e) {
    return $e . $x; // Noncompliant
    //          ^^
  }
}

function predefined_variables() {
  return $_COOKIE . $_ENV . $_FILES . $_GET . $_POST . $_REQUEST . $_SERVER . $_SESSION . $GLOBALS .
  $HTTP_RAW_POST_DATA . $http_response_header . $php_errormsg . $unknown_name; // Noncompliant
//                                                              ^^^^^^^^^^^^^
}

$f1 = function()              { return $a + $b; }; // Noncompliant 2
$f1 = function() use($a, &$b) { return $a + $b; };
$f1 = function($a, &$b, boolean $c) { return $a + $b + $c; };

class A {
  public static $static_v1;
  public $v = 3;

  public function __construct($pvalue1, $pvalue2 = 3) {
    $this->v = $pvalue1;
    $this->v = $pvalue2;
    $this->v = $pvalue3; // Noncompliant
    self::$static_v1++;
    A::$static_v1++;
    self::$static_v1['a'] + A::$static_v1['a'] + A::$static_v1['a']['a'];
    $static_v1++; // Noncompliant
  }

  function f1()        { $a = $this; }
  static function f2() { $a = $this; } // it's not a FN, this case is handeled by S2014

  function f3()        { $a = $this->v; }

  function f4()        {
    $type1 = $this;
    $loader = function (Options $options) use ($type1, &$type2, $type3) { // Noncompliant
    //                                                          ^^^^^^
      return $type1 + $options + $type4; // Noncompliant
      //                         ^^^^^^
    };
    $loader = function (boolean $loaderParam) {
      return $loaderParam;
    };
    return $loaderParam; // Noncompliant
  }
}

function parentFunc() {
  $a = 2;
  function childFunc() {
    $b = 2;
    if ($b) {}
    if ($a) {} // Noncompliant
  }
  if ($b) {} // Noncompliant
  if ($a) {}
}

// SONARPHP-756

function alternativeForeach() {
    $values = array(1, 2, 3);
    foreach ($values as $key => $val) : // OK, $key and $value initialized by foreach
      return $key . $val;
    endforeach;

    foreach ($values as $value) :
          return $key . $val;
    endforeach;

    $array = [
        [1, 2],
        [3, 4],
    ];

    foreach ($array as list($a, $b)) :
        echo "$a\n";
    endforeach;
}