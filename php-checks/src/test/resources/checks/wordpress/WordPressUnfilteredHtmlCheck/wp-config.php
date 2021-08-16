<?php

  define("DISALLOW_UNFILTERED_HTML", false); // Noncompliant {{Make sure allowing unfiltered HTML is intended.}}
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  define("DISALLOW_UNFILTERED_HTML", true);
  define("DISALLOW_UNFILTERED_HTML", 0); // Noncompliant
  define("DISALLOW_UNFILTERED_HTML", $unkown);
