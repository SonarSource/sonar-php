<?php

  @doSomething();  // Noncompliant {{Remove the '@' symbol from this function call to un-silence errors.}}
//^

doSomething();   // OK
