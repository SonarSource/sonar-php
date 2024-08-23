<?php

function literals() {
  if(true); // Noncompliant {{Replace this expression; used as a condition it will always be constant.}}
  // ^^^^
  if (42); // Noncompliant
  if ("foo"); // Noncompliant
  if (null); // Noncompliant
  if (array()); // Noncompliant
}

