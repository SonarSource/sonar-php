<?php


function do_something(): void
{
  $a = "Qm7vXpLr2FzT9baWtHx";                     // Compliant
  $apikey = "Qm7vXpLr2FzT9baWtHx";                // Noncompliant
  $secret001 = "sk_live_xf2fh0Hu3LqXlqqUg2DEWhEz";  // Noncompliant
  $secret002 = "examples/commit/16ad89c4172c259f15bce56e";
  $secret003 = "examples/commit/8e1d746900f5411e9700fea0"; // Compliant, "examples" recognized as a fake-value word by SecretClassifier
  $secret004 = "examples/commit/revision/469001e9700fea0";
  $secret005 = "xml/src/main/java/org/xwiki/xml/html/file";
  $secret006 = "abcdefghijklmnop"; // Compliant
  $secret007 = "Zk4mPq8Vr2Wn5TySb"; // Noncompliant
  $secret008 = "3fB9e7Dc2A6b81f5E"; // Noncompliant
  $secret009 = "098765432198765432109876543210"; // Noncompliant
  $secret010 = "QmVpLrFzTbWtHxNkYcSaJdReUiOp"; // Noncompliant
  $secret011 = "012345670123456701234567012345";
  $secret012 = "987654321098765432109876543210"; // Noncompliant
  $secret013 = "234.167.076.123";
  $ip_secret1 = "bfee:e3e1:9a92:6617:02d5:256a:b87a:fbcc"; // Compliant: ipv6 format
  $ip_secret2 = "2001:db8:1::ab9:C0A8:102"; // Compliant: ipv6 format
  $ip_secret3 = "::ab9:C0A8:102"; // Compliant: ipv6 format
  $secret015 = "org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH";
  // Example of Telegram bot token
  $secret016 = "bot987654:ABC-DEF9876ghIkl-zyx57W2v1u987ew11"; // Noncompliant
  // Secret with "&"
  $secret017 = "012&987654012987654012987&654012"; // Noncompliant
  $secret018 = "&98&765401298765401298&76540&"; // Noncompliant

  // Simple constants will be filtered thanks to the entropy check
  $SECRET_INPUT = "[id='secret']"; // Compliant
  $SECRET_PROPERTY = "custom.secret"; // Compliant
  $TRUSTSTORE_SECRET = "trustStoreSecret"; // Compliant
  $CONNECTION_SECRET = "connection.secret"; // Compliant
  $RESET_SECRET = "/users/resetUserSecret"; // Compliant
  $RESET_TOKEN = "/users/resetUserToken"; // Compliant
  $CA_SECRET = "ca-secret"; // Compliant
  $caSecret = $CA_SECRET; // Compliant
  $SECRET_VAULT_REF = "op:/x9F2kLpQ7vRtYcWa1"; // Compliant, SecretClassifier recognizes vault reference syntax
}
