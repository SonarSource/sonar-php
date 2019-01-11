<?php

$id = $_GET['id'];

$query = "SELECT * FROM myTable WHERE id = " + $id;
$result = mysql_query($query); // Noncompliant {{Make sure that executing SQL queries is safe here.}}
       // ^^^^^^^^^^^^^^^^^^^

foo($query);

function foo($name) {
  echo "Hello $name!";
}
