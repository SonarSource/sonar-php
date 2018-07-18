<?php

const serverAddress = "1.20.33.145";  // Noncompliant {{Make sure using this hardcoded IP address is safe here.}}
//                    ^^^^^^^^^^^^^
const nonRelatedValue = 42;

class A {
  const constFieldNameWithPasswordInIt = "8.8.8.8"; // Noncompliant
  const otherConstFieldName = "http://1.2.3.4:8080/foo"; // Noncompliant
}

function foo($socket) {
  static $otherStaticVariableName = "xxx";
  socket_connect($socket, '1.1.1.1', 23); // Noncompliant
}

const localhost = "127.0.0.1"; // Compliant - exception for localhost
const localhostUrl = "ftp://127.0.0.1:22/bla"; // Compliant - exception for localhost
const notIPAddress = "1.20.33.345"; // Compliant - segment > 255
