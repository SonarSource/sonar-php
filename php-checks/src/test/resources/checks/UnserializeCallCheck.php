<?php

  serialize($str);
  unserialize($str); // Noncompliant {{Make sure deserializing objects is safe here.}}
//^^^^^^^^^^^
  foo->unserialize($command);
