<?php

  define("DISALLOW_FILE_MODS", false); // Noncompliant {{Make sure allowing modification of themes and plugins is intended.}}
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  define("DISALLOW_FILE_MODS", true);
  define("DISALLOW_FILE_MODS", 0); // Noncompliant
  define("DISALLOW_FILE_MODS", $unkown);
