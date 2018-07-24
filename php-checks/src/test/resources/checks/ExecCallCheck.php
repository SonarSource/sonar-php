<?php

  foo($command);
  exec($command); // Noncompliant {{Make sure that executing this OS command is safe here.}}
//^^^^
  foo->exec($command);
