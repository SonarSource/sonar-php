<?php

function executeMysqlQuery($query, $database)
{
    mysql_query("select * from A where " . $query); // Noncompliant {{Make sure that formatting this SQL query is safe here.}}
//  ^^^^^^^^^^^

    mysql_query($query); // OK
    mysql_db_query($database, $query); // OK
    mysql_unbuffered_query($query); // OK

    mysql_query("select * from db"); // OK
    mysql_query('select * from db'); // OK
    mysql_query("select * from $db"); // Noncompliant {{Make sure that formatting this SQL query is safe here.}}
    mysql_query('select * from $db'); // OK

    mysql_db_query($database, "select * from db".$query); // Noncompliant
    mysql_unbuffered_query("select * from db".$query); // Noncompliant
}

function executeQuery($query) {
    mssql_query("select * from db".$query); // Noncompliant
    MSSQL_query("SELECT * FROM mytable"); // OK
}

function executeMysqliQuery($query) {
    // Object oriented style
    $mysqli = new mysqli("localhost", "myUser", "myPassword", "myDatabase");
    $mysqli->query("select * from db".$query); // Noncompliant
    $mysqli->real_query("select * from db".$query); // Noncompliant
    $mysqli->multi_query("select * from db".$query); // Noncompliant
    $mysqli->send_query("select * from db".$query); // Noncompliant

    // Procedural style
    $link = mysqli_connect("localhost", "myUser", "myPassword", "myDatabase");
    mysqli_query($link, "select * from db".$query); // Noncompliant
    mysqli_real_query($link, "select * from db".$query); // Noncompliant
    mysqli_multi_query($link, "select * from db".$query); // Noncompliant
    mysqli_send_query($link, "select * from db".$query); // Noncompliant

    $mysqli->query("SELECT * from $mytable"); // Noncompliant
    $mysqli->real_query("SELECT * from $mytable"); // Noncompliant
}

function executePDOQuery($dsn, $user, $password, $query, $type, $obj) {
    $conn = new PDO($dsn, $user, $password);
    $conn->query("select * from db".$query); // Noncompliant
    $conn->query('SELECT * from test'); // OK
    $conn->query("select * from db".$query, PDO::FETCH_COLUMN, 42); // Noncompliant
    $conn->query("select * from db".$query, PDO::FETCH_CLASS, 'MyClass'); // Noncompliant
    $conn->query("select * from db".$query, PDO::FETCH_INTO, $obj); // Noncompliant
    $conn->exec("select * from db".$query); // Noncompliant

    $hardcoded_statement = 'SELECT * from test';
    $conn->prepare($hardcoded_statement); // OK
    $conn->prepare($unknown_variable); // OK

    $conn->prepare('SELECT * from test'); // OK
    $conn->prepare("SELECT * from $tableNameInterpolation"); // Noncompliant
}

function executePostgresQuery($conn, $query, $tableName) {
    pg_query("select * from db".$query); // Noncompliant
    PG_Query($conn, "select * from db".$query); // Noncompliant
    pg_send_query ($conn, "select * from db".$query); // Noncompliant

    pg_send_query($conn, 'INSERT INTO test (ID) VALUES (1)'); // OK
    pg_query('INSERT INTO test (ID) VALUES (1)'); // OK
    pg_query($conn, 'INSERT INTO test (ID) VALUES (1)'); // OK

    pg_send_query ($conn, "INSERT INTO test (ID) VALUES ($query)"); // Noncompliant
    pg_query("INSERT INTO test (ID) VALUES ($query)"); // Noncompliant
    pg_query($conn, "INSERT INTO test (ID) VALUES ($query)"); // Noncompliant

    $cond = array('id'=>('1'.$query));
    $data = array('id'=>'2');
    pg_update($conn, $tableName, $data, $cond); // Ok, "pg_update" is ignored by this rule
}

// coverage
abc('INSERT INTO test (ID) VALUES ($query)');
a->abc('INSERT INTO test (ID) VALUES ($query)');
mysql_query();
$conn = new PDO($dsn, $user, $password);
$conn->query();
$conn->prepare();
pg_query();
