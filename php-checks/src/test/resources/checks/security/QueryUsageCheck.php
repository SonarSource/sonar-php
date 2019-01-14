<?php

function executeMysqlQuery($query, $database)
{
    mysql_query($query); // Noncompliant {{Make sure that executing SQL queries is safe here.}}
//  ^^^^^^^^^^^
    mysql_db_query($database, $query); // Noncompliant
    mysql_unbuffered_query($query); // Noncompliant

    mysql_query("select * from db"); // OK
    mysql_query('select * from db'); // OK
    mysql_query("select * from $db"); // Noncompliant
    mysql_query('select * from $db'); // OK

    mysql_db_query($database, "select * from db".$query); // Noncompliant
    mysql_unbuffered_query("select * from db".$query); // Noncompliant
}

function executeQuery($query) {
    mssql_query($query); // Noncompliant
    mssql_query("SELECT * FROM mytable"); // OK
}

function executeMysqliQuery($statement, $column) {
    // Object oriented style
    $mysqli = new mysqli("localhost", "myUser", "myPassword", "myDatabase");
    $mysqli->query($statement); // Noncompliant
    $mysqli->real_query($statement); // Noncompliant
    $mysqli->multi_query($statement); // Noncompliant
    $mysqli->send_query($statement); // Noncompliant

    // Procedural style
    $link = mysqli_connect("localhost", "myUser", "myPassword", "myDatabase");
    mysqli_query($link, $statement); // Noncompliant
    mysqli_real_query($link, $statement); // Noncompliant
    mysqli_multi_query($link, $statement); // Noncompliant
    mysqli_send_query($link, $statement); // Noncompliant

    $mysqli->query("SELECT * from $mytable"); // Noncompliant
    $mysqli->real_query("SELECT * from $mytable"); // Noncompliant
}

function executePDOQuery($dsn, $user, $password, $statement, $type, $obj) {
    $conn = new PDO($dsn, $user, $password);
    $conn->query($statement); // Noncompliant
    $conn->query($statement, PDO::FETCH_COLUMN, 42); // Noncompliant
    $conn->query($statement, PDO::FETCH_CLASS, 'MyClass'); // Noncompliant
    $conn->query($statement, PDO::FETCH_INTO, $obj); // Noncompliant
    $conn->exec($statement); // Noncompliant

    $hardcoded_statement = 'SELECT * from test';
    $conn->prepare($hardcoded_statement); // OK
    $conn->prepare($unknown_variable); // OK

    $conn->prepare('SELECT * from test'); // OK
    $conn->prepare("SELECT * from $tableNameInterpolation"); // Noncompliant
}

function executePostgresQuery($conn, $query, $tableName) {
    pg_query($query); // Noncompliant
    pg_query($conn, $query); // Noncompliant
    pg_send_query ($conn, $query); // Noncompliant


    pg_send_query($conn, 'INSERT INTO test (ID) VALUES (1)'); // OK
    pg_query('INSERT INTO test (ID) VALUES (1)'); // OK
    pg_query($conn, 'INSERT INTO test (ID) VALUES (1)'); // OK

    pg_send_query ($conn, "INSERT INTO test (ID) VALUES ($query)"); // Noncompliant
    pg_query("INSERT INTO test (ID) VALUES ($query)"); // Noncompliant
    pg_query($conn, "INSERT INTO test (ID) VALUES ($query)"); // Noncompliant

    $cond = array('id'=>'1');
    $data = array('id'=>'2');
    pg_update($conn, $tableName, $data, $cond); // Noncompliant
}
