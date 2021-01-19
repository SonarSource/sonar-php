<?php

function simpleXML() {
  $xml = file_get_contents("xxe.xml");
  $doc = simplexml_load_string($xml, "SimpleXMLElement");
  $doc = simplexml_load_string($xml, "SimpleXMLElement", 42);
  $doc = simplexml_load_string($xml, "SimpleXMLElement", LIBXML_NOENT); // Noncompliant
//                                                       ^^^^^^^^^^^^
  $doc = simplexml_load_string($xml, "SimpleXMLElement", LIBXML_NOBLANKS);
  $doc = simplexml_load_string($xml, 'SimpleXMLElement', LIBXML_NOBLANKS | LIBXML_NOENT); // Noncompliant
  $doc = simplexml_load_string($xml, 'SimpleXMLElement', LIBXML_NOENT | LIBXML_NOBLANKS); // Noncompliant
  $doc = simplexml_load_string($xml, 'SimpleXMLElement', LIBXML_COMPACT | LIBXML_NOBLANKS);
  $doc = simplexml_load_string($xml, 'SimpleXMLElement', LIBXML_COMPACT | (LIBXML_NOBLANKS | LIBXML_NOENT)); // Noncompliant
  $doc = simplexml_load_string($xml, LIBXML_NOENT);
  $doc = simplexml_load_string($xml, options: LIBXML_NOENT); // Noncompliant
  $doc = $obj->simplexml_load_string($xml, "SimpleXMLElement", LIBXML_NOENT);
  $doc = \simplexml_load_string($xml, "SimpleXMLElement", LIBXML_NOENT); // Noncompliant
  $doc = Simplexml_load_string($xml, "SimpleXMLElement", LIBXML_NOENT); // Noncompliant
  $doc = unrelated_function($xml, "SimpleXMLElement", LIBXML_NOENT);

  $options1 = LIBXML_NOBLANKS | LIBXML_NOENT;
  $doc = simplexml_load_string($xml, "SimpleXMLElement", $options1); // Noncompliant

  $options2 = LIBXML_NOBLANKS | LIBXML_NOENT;
  $doc = unrelated_function($xml, "SimpleXMLElement", $options2);
}

function domDocument($param) {
  $doc = new DOMDocument();
  $doc->load("xxe.xml", LIBXML_NOENT); // Noncompliant
//                      ^^^^^^^^^^^^
  $doc->load("xxe.xml", LIBXML_NOBLANKS);
  $doc->load("xxe.xml", LIBXML_NOBLANKS | LIBXML_NOENT); // Noncompliant
  $doc->load(LIBXML_NOENT, "xxe.xml");
  $doc->load(options: LIBXML_NOENT, filename: "xxe.xml"); // Noncompliant
  $doc->Load("xxe.xml", LIBXML_NOENT); // Noncompliant
  $doc->loadXML("xxe.xml", LIBXML_NOENT); // Noncompliant
  $doc->LoadXML("xxe.xml", LIBXML_NOENT); // Noncompliant
  $doc->save("xxe.xml", LIBXML_NOENT);
  // we cannot be sure of the class of $param, but the method has the right name and LIBXML_NOENT is used
  $param->load("xxe.xml", LIBXML_NOENT); // Noncompliant
  load("xxe.xml", LIBXML_NOENT);
}

function xmlReader($param) {
  $reader = new XMLReader();
  $reader->open("xxe.xml");
  $reader->setParserProperty();
  $reader->setParserProperty(XMLReader::SUBST_ENTITIES);
  $reader->setParserProperty(XMLReader::SUBST_ENTITIES, true); // Noncompliant
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  $reader->setParserProperty(xmlReader::SUBST_ENTITIES, true); // Noncompliant
  $reader->setParserProperty(XMLReader::SUBST_ENTITIES, false);
  $reader->setParserProperty(XMLReader::SUBST_ENTITIES, unknownValue());
  $reader->setParserProperty(SUBST_ENTITIES, true);
  $reader->setParserProperty(XMLReader::VALIDATE, true);
  $reader->setRelaxNGSchema(XMLReader::SUBST_ENTITIES, true);
  $reader->setParserProperty(true, XMLReader::SUBST_ENTITIES);
  $reader->setParserProperty(value:true, property:XMLReader::SUBST_ENTITIES); // Noncompliant
  // we cannot be sure of the class of $param, but the method has the right name and XMLReader::SUBST_ENTITIES is used
  $param->setParserProperty(XMLReader::SUBST_ENTITIES, true); // Noncompliant
}
