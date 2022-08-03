<?php

namespace ns1 {
  use Cake\Utility\Xml;
  $xml = Xml::build($content, ['loadEntities' => true]);                  // Noncompliant
}

namespace ns2 {
  use Cake\Utility;
  $xml = Utility\Xml::build($content, ['loadEntities' => true]);          // Noncompliant
}

namespace ns3 {
  use Cake;
  $xml = Cake\Utility\Xml::build($content, ['loadEntities' => true]);     // Noncompliant
}

namespace ns4 {
  use Cake\Utility\Xml;
  $xml = Cake\Utility\Xml::build($content, ['loadEntities' => true]);     // Noncompliant
}

namespace ns5 {
  use Cake\Utility\Xml as CakeXML;
  $xml = CakeXML::build($content, ['loadEntities' => true]);              // Noncompliant
}

namespace ns6 {
  use Cake\Utility\Xml;

  function trueValue() {
    return true;
  }

  function trueEntities() {
    return array("loadEntities" => true);
  }

  function xmlBuild($param) {
    $xml = Xml::build($content, ['loadEntities' => true]);      // Noncompliant {{Disable access to external entities in XML parsing.}}
//                              ^^^^^^^^^^^^^^^^^^^^^^^^

    $xml = Cake\Utility\Xml::build($content, ['loadEntities' => true]); // Noncompliant

    $val = true;
//         ^^^^> {{This value enables external entities in XML parsing.}}
    $xml = Xml::build($content, ['loadEntities' => $val]);      // Noncompliant
//                              ^^^^^^^^^^^^^^^^^^^^^^^^
    $options = array('loadEntities' => true);
//                                     ^^^^>  {{This value enables external entities in XML parsing.}}
    $xml = Xml::build($content, $options);                      // Noncompliant
//                              ^^^^^^^^
    $var = true;
//         ^^^^> {{This value enables external entities in XML parsing.}}
    $secondValue = $var;
//                 ^^^^> {{Propagated settings.}}
    $xml = Xml::build($content, ['loadEntities' => $secondValue]); // Noncompliant {{Disable access to external entities in XML parsing.}}
//                              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    $xml = Xml::build($content, ['loadEntities' => trueValue()]);// Noncompliant

    $xml = Xml::build($content, trueEntities());                // FN should raise issue

    $xml = Xml::build($content);                                // Compliant by default
    $xml = Xml::build($content, ['loadEntities' => false]);     // Compliant
    $options2 = array('loadEntities' => false);
    $xml = Xml::build($content, $options2);                     // Compliant
    $xml = Xml::build($content, $valueOutOfScope);              // Compliant if value is unknown
    $xml = Xml::build($content, ['loadEntities' => NULL]);     // Compliant

    $options3 = $options4;
    $options4 = $options3;

    $xml = Xml::build($content, $options3);

    $option5 = $options3;
    $xml = Xml::build($content, $options5);

    $valueForCoverage = Xml::otherFunction();
  }

}
