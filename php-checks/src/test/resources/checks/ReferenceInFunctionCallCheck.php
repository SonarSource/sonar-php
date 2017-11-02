<?php

doSomething(&$p1, $p2); // Noncompliant {{Remove the '&' to pass "$p1" by value.}}
//          ^^^^

doSomething($p1, $p2);  // OK
