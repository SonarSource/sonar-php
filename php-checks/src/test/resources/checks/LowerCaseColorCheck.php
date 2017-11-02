<?php

$white = '#fffffF';    // Noncompliant {{Replace "#fffffF" with "#FFFFFF".}}
//       ^^^^^^^^^
$dkgray = '#006400';   // OK
$aqua = '#00ffff';     // Noncompliant
$aqua = '#0ff';        // Noncompliant {{Replace "#0ff" with "#0FF".}}

$white = '#FFFFFF';    // OK
$aqua = '#00FFFF';     // OK

$tag = '#lipsum';      // OK
