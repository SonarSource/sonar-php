<?php

function evaluate_xpath($doc, $xpathstring, $xmlstring)
{
    $xpath = new DOMXpath($doc);
    $xpath->query($xpathstring); // Noncompliant {{Make sure that executing this XPATH expression is safe.}}
    $xpath->evaluate($xpathstring); // Noncompliant

    // There is no risk if the xpath is hardcoded
    $xpath->query("/users/user[@name='alice']"); // Compliant
    $xpath->evaluate("/users/user[@name='alice']"); // Compliant

    $xpath2 = NULL;
    if ($doc) {
        $xpath2 = new DOMXpath($doc);
    }
    $xpath2->query($xpathstring); // Noncompliant

    // An issue will also be created if the SimpleXMLElement is created
    // by simplexml_load_file, simplexml_load_string or simplexml_import_dom
    $xml = new SimpleXMLElement($doc);
    $xml->xpath($xpathstring); // Noncompliant

    // There is no risk if the xpath is hardcoded
    $xml->xpath("/users/user[@name='alice']"); // Compliant

    // coverage
    $xpath->evaluate();
    $xpath->xpath($xpathstring);
    $xml->query($xpathstring);
    $xml->evaluate($xpathstring);

    $other = new Other($doc);
    $other->query($xpathstring);
    $other->evaluate($xpathstring);
    $other->xpath($xpathstring);
}
