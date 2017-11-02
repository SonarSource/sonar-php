<?php

if (true) {
    sleep(5);         // Noncompliant {{Remove this call to "sleep".}}
//  ^^^^^^^^
}

UserNS\slep(5);       // OK

$myObject->sleep(5);  // OK
sleep::memeber;       // OK
