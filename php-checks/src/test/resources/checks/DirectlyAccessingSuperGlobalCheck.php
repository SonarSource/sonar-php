<?php

$a = $_POST['name'];      // Noncompliant {{Do not access "$_POST" directly.}}
//   ^^^^^^

$b = $_MY_ARRAY['name'];  // OK

$c = $_SESSION['name']; // OK $_SESSION is a special case and can be accessed directly