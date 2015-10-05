<?php

if (true) {
    sleep(5);         // NOK {{Remove this call to "sleep".}}
}

$myObject->sleep(5);  // OK

sleep::memeber;       // OK
