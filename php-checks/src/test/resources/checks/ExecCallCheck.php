<?php

  foo($command);
  exec($command); // Noncompliant {{Make sure that executing this OS command is safe here.}}
//^^^^
  foo->exec($command);

  passthru($str); // Noncompliant
  proc_open($str); // Noncompliant
  popen($str, $mode); // Noncompliant
  shell_exec($str); // Noncompliant
  system($str); // Noncompliant
  pcntl_exec($str); // Noncompliant
  closedir($str);

  `cd {$var}`; // Noncompliant
  'cd {$var}';
  "cd {$var}";
