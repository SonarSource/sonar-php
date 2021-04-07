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
    $conn = new MySQLI($servername, $username, ""); // Noncompliant
    $conn = new mysqli($servername, $username, $secretPassword);
    $conn = new mysqli($servername, $username, $unknown);
    $conn = new mysqli($servername, $username, $secretPassword . 'somethingElse');
    $conn = new mysqli($servername, $username, $empty); // Noncompliant
//                                             ^^^^^^
    $conn = new mysqli($servername, username: '');
    $conn = new mysqli($servername, passwd: ''); // Noncompliant
    $conn = new mysqli($servername, passwd: $secretPassword);
    $conn = new mysqli($servername, $username, $maybeEmpty);
    $conn = new MyClass($servername, $username, '');

    $conn = mysqli_connect($servername, $username);
    $conn = mysqli_connect($servername, $username, ''); // Noncompliant
    $conn = mysqli_connect($servername, $username, $pwd);
    $conn = mysqli_connect($servername, passwd: ''); // Noncompliant
}

// PDO
// http://php.net/manual/en/pdo.construct.php
function pdo() {
    $conn = new PDO("mysql:host=$servername;dbname=myDB", $username);
    $conn = new PDO("mysql:host=$servername;dbname=myDB", $username, ''); // Noncompliant
    $conn = new PDO("mysql:host=$servername;dbname=myDB", $username, $pwd);
    $conn = new PDO("mysql:host=$servername;dbname=myDB", $username, 'secret');
    $conn = new PDO("mysql:host=$servername;dbname=myDB", passwd: '', username: $username); // Noncompliant
    $conn = new PDO("mysql:host=$servername;dbname=myDB", passwd: 'secret', username: $username);
}

// Oracle
// http://php.net/manual/en/function.oci-connect.php
function oracle() {
    $conn = oci_connect($username);
    $conn = oci_connect($username, '', $servername); // Noncompliant
    $conn = oci_connect($username, $pwd, $servername);
    $conn = oci_connect($username, 'secret', $servername);
    $conn = oci_connect(password:'', username: $username); // Noncompliant
    $conn = oci_connect(password:'secret', username: $username);
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
    $conn = sqlsrv_connect(connectionInfo: ["Database"=>"myDB", "UID"=>$username, "PWD"=>''], serverName:$sqlsrvName); // Noncompliant
    $conn = sqlsrv_connect(connectionInfo: ["Database"=>"myDB", "UID"=>$username, "PWD"=>'secret'], serverName:$sqlsrvName);

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

    $conn = sqlsrv_connect($sqlsrvName, getConfig());
    $conn = sqlsrv_connect($sqlsrvName, SQL_CONFIG);
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
    $conn = pg_connect(connect_type: 1, connection_string: "host=localhost port=5432 dbname=test user=john password="); // Noncompliant
    $conn = pg_connect(connection_type: "host=localhost port=5432 dbname=test user=john password=");

    $str1 = "host=localhost port=5432 dbname=test user=john password=secret";
    $conn = pg_connect($str1);

    $str2 = "host=localhost port=5432 dbname=test user=john password="; // Noncompliant
    $conn = pg_connect($str2);

    $str3 = "host=localhost port=5432 dbname=test user=john password=";
    $str3 = "host=localhost port=5432 dbname=test user=john password=secret";
    $conn = pg_connect($str3);
}

function withList() {
  list($emptyUser, $emptyPwd) = ["", ""];
  $conn = new mysqli($servername, $emptyUser, $emptyPwd); // Noncompliant

  $user = '';
  $pwd = '';
  list($user, $pwd) = getConnectionData();
  $conn = new mysqli($servername, $user, $pwd);

  list(1 => $emptyUser_2, 0 => $emptyPwd_2) = ["", ""];
  $conn = new mysqli($servername, $emptyUser_2, $emptyPwd_2); // FN - list() with keys is not handled

  list($emptyUser_3, $emptyPwd_3) = [1 => "", 0 => ""];
  $conn = new mysqli($servername, $emptyUser_3, $emptyPwd_3); // FN - list() with keys is not handled
}
