<?php
$servername = "localhost";
$username = "AppLogin";

// MySQL
// http://php.net/manual/en/mysqli.construct.php
function mysql() {
    $empty = "";
    $maybeEmpty = "";
    if (condition()) {
      $maybeEmpty = foo();
    }
    $secretPassword = "abcdefg";
    $obj->pwd = '';

    $conn = new mysqli($servername, $username); // OK, defaut value is ini_get("mysqli.default_pw")
    $conn = new mysqli($servername, $username, ''); // Noncompliant
//                                             ^^
    $conn = new mysqli($servername, $username, ""); // Noncompliant
    $conn = new mysqli($servername, $username, $secretPassword);
    $conn = new mysqli($servername, $username, $unknown);
    $conn = new mysqli($servername, $username, $secretPassword . 'somethingElse');
    $conn = new mysqli($servername, $username, $empty); // Noncompliant
//                                             ^^^^^^
    $conn = new mysqli($servername, $username, $maybeEmpty);
    $conn = new MyClass($servername, $username, '');

    $conn = mysqli_connect($servername, $username);
    $conn = mysqli_connect($servername, $username, ''); // Noncompliant
    $conn = mysqli_connect($servername, $username, $pwd);
}

// PDO
// http://php.net/manual/en/pdo.construct.php
function pdo() {
    $conn = new PDO("mysql:host=$servername;dbname=myDB", $username);
    $conn = new PDO("mysql:host=$servername;dbname=myDB", $username, ''); // Noncompliant
    $conn = new PDO("mysql:host=$servername;dbname=myDB", $username, $pwd);
    $conn = new PDO("mysql:host=$servername;dbname=myDB", $username, 'secret');
}

// Oracle
// http://php.net/manual/en/function.oci-connect.php
function oracle() {
    $conn = oci_connect($username);
    $conn = oci_connect($username, '', $servername); // Noncompliant
    $conn = oci_connect($username, $pwd, $servername);
    $conn = oci_connect($username, 'secret', $servername);
}

// MS SQL Server
function sqlServer() {
    $sqlsrvName = "serverName\sqlexpress";

    $conn = sqlsrv_connect($sqlsrvName);
    $conn = sqlsrv_connect($sqlsrvName, array("Database"=>"myDB", "UID"=>$username));
    $conn = sqlsrv_connect($sqlsrvName, array("Database"=>"myDB", "UID"=>$username, '', $pwd=>''));
    $conn = sqlsrv_connect($sqlsrvName, array("Database"=>"myDB", "UID"=>$username, "PWD"=>'')); // Noncompliant
    $conn = sqlsrv_connect($sqlsrvName, array("Database"=>"myDB", "UID"=>$username, "PWD"=>'secret'));
    $conn = sqlsrv_connect($sqlsrvName, ["Database"=>"myDB", "UID"=>$username, "PWD"=>'']); // Noncompliant
    $conn = sqlsrv_connect($sqlsrvName, ["Database"=>"myDB", "UID"=>$username, "PWD"=>'secret']);

    $sqlsrvConnInfo1 = array("Database"=>"myDB", "UID"=>$username, "PWD"=>$password);
    $conn = sqlsrv_connect($sqlsrvName, $sqlsrvConnInfo1);

    $sqlsrvConnInfo2 = array("Database"=>"myDB", "UID"=>$username, "PWD"=>''); // Noncompliant
//                                                                        ^^
    $conn = sqlsrv_connect($sqlsrvName, $sqlsrvConnInfo2);

    $sqlsrvConnInfo3 = array("Database"=>"myDB", "UID"=>$username, "PWD"=>'');
    $sqlsrvConnInfo3 = array("Database"=>"myDB", "UID"=>$username, "PWD"=>$password);
    $conn = sqlsrv_connect($sqlsrvName, $sqlsrvConnInfo3);

    $conn = sqlsrv_connect($sqlsrvName, $unknown);

    array( "Database"=>"myDB", "UID"=>$username, "PWD"=>'');
}

// PostgreSQL
// http://php.net/manual/en/function.pg-connect.php
function postgresql() {
    $conn = pg_connect();
    $conn = pg_connect("host=localhost port=5432 dbname=test user=john password=secret");
    $conn = pg_connect("host=localhost port=5432 dbname=test user=john password=" . "secret");
    $conn = pg_connect("host=localhost port=5432 dbname=test user=john password="); // Noncompliant
    $conn = pg_connect("host=localhost port=5432 dbname=test user=john password=''"); // Noncompliant
    $conn = pg_connect("host=localhost port=5432 dbname=test user=john password = '' port=1234"); // Noncompliant
    $conn = pg_connect("host=localhost port=5432 dbname=test user=" . $username . " password=" . $unknown);
    $conn = pg_connect("host=localhost port=5432 dbname=test user=" . $username . " password=" . 'secret');
    $conn = pg_connect("host=localhost port=5432 dbname=test user=" . $username . " password="); // Noncompliant
    $conn = pg_connect("host=localhost port=5432 dbname=test user=" . $username . " password='' port=" . $port); // Noncompliant

    $str1 = "host=localhost port=5432 dbname=test user=john password=secret";
    $conn = pg_connect($str1);

    $str2 = "host=localhost port=5432 dbname=test user=john password="; // Noncompliant
    $conn = pg_connect($str2);

    $str3 = "host=localhost port=5432 dbname=test user=john password=";
    $str3 = "host=localhost port=5432 dbname=test user=john password=secret";
    $conn = pg_connect($str3);
}
