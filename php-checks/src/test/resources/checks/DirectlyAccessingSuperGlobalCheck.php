<?php

$a = $_POST['name'];      // NOK {{Do not access "$_POST" directly.}}
//   ^^^^^^

$b = $_MY_ARRAY['name'];  // OK
