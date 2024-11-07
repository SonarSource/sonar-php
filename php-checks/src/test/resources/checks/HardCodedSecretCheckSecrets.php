<?php


function do_something(): void
{
  $a = "abcdefghijklmnopqrs";                     // Compliant
  $apikey = "abcdefghijklmnopqrs";                // Noncompliant
  $secret001 = "sk_live_xf2fh0Hu3LqXlqqUg2DEWhEz";  // Noncompliant
  $secret002 = "examples/commit/16ad89c4172c259f15bce56e";
  $secret003 = "examples/commit/8e1d746900f5411e9700fea0"; // Noncompliant
  $secret004 = "examples/commit/revision/469001e9700fea0";
  $secret005 = "xml/src/main/java/org/xwiki/xml/html/file";
  $secret006 = "abcdefghijklmnop"; // Compliant
  $secret007 = "abcdefghijklmnopq"; // Noncompliant
  $secret008 = "0123456789abcdef0"; // Noncompliant
  $secret009 = "012345678901234567890123456789"; // Noncompliant
  $secret010 = "abcdefghijklmnopabcdefghijkl"; // Noncompliant
  $secret011 = "012345670123456701234567012345";
  $secret012 = "012345678012345678012345678012"; // Noncompliant
  $secret013 = "234.167.076.123";
  $ip_secret1 = "bfee:e3e1:9a92:6617:02d5:256a:b87a:fbcc"; // Compliant: ipv6 format
  $ip_secret2 = "2001:db8:1::ab9:C0A8:102"; // Compliant: ipv6 format
  $ip_secret3 = "::ab9:C0A8:102"; // Compliant: ipv6 format
  $secret015 = "org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH";
  // Example of Telegram bot token
  $secret016 = "bot123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11"; // Noncompliant
  // Secret with "&"
  $secret017 = "012&345678012345678012345&678012"; // Noncompliant
  $secret018 = "&12&345678012345678012345&67801&"; // Noncompliant

  // Simple constants will be filtered thanks to the entropy check
  $SECRET_INPUT = "[id='secret']"; // Compliant
  $SECRET_PROPERTY = "custom.secret"; // Compliant
  $TRUSTSTORE_SECRET = "trustStoreSecret"; // Compliant
  $CONNECTION_SECRET = "connection.secret"; // Compliant
  $RESET_SECRET = "/users/resetUserSecret"; // Compliant
  $RESET_TOKEN = "/users/resetUserToken"; // Compliant
  $CA_SECRET = "ca-secret"; // Compliant
  $caSecret = $CA_SECRET; // Compliant
}
