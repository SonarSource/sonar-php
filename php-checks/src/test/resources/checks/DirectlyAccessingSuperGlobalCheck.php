<?php

$a = $_POST['name'];      // Noncompliant {{Do not access "$_POST" directly.}}
//   ^^^^^^

$b = $_MY_ARRAY['name'];  // OK
