<?php

  zip_entry_read($zip_entry, zip_entry_filesize($zip_entry)); // Noncompliant {{Make sure that expanding this archive file is safe here.}}
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  zip_entry_read($zip_entry, 1024); // OK
  zip_entry_read($zip_entry); // OK

  my_zip_function($zip_entry, zip_entry_filesize($zip_entry));
  $function($zip_entry, zip_entry_filesize($zip_entry));


  $zip1 = new ZipArchive();
  $zip1->extractTo('.'); // Noncompliant

  $zip2 = new ZipArchive;
  $zip2->extractTo('.'); // Noncompliant
//^^^^^^^^^^^^^^^^^^^^^

  (new ZipArchive())->extractTo('.'); // Noncompliant

  $otherZipClass = new OtherZipClass();
  $otherZipClass->extractTo('.');

  $unknown->extractTo('.');
  $unknown::extractTo('.');
  $unknown->otherMethod('.');
  $unknown::otherMethod('.');
