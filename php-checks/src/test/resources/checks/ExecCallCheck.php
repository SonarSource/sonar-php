<?php

  foo($command);
  exec($command); // Noncompliant
//^^^^
  foo->exec($command);
