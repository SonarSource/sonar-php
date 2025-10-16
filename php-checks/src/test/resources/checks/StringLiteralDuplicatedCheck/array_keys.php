<?php

// General test for array keys - these should NOT raise issues
// Array initialization with keys
$user1 = [
    'name' => 'John',
    'email' => 'john@example.com',
    'age' => 30,
];
$user2 = [
    'name' => 'Bob',
    'email' => 'bob@example.com',
    'age' => 28,
];
$user3 = [
    'name' => 'Alfred',
    'email' => 'alfred@example.com',
    'age' => 86,
];

// Array access with string keys
$config1['host'] = 'localhost';
$config1['port'] = 3306;
$config1['username'] = 'root';

$config2['host'] = 'localhost';
$config2['port'] = 3306;
$config2['username'] = 'root';

$config3['host'] = 'localhost';
$config3['port'] = 3306;
$config3['username'] = 'root';

// Nested array keys
$data['user1']['name'] = 'Alice';
$data['user1']['email'] = 'alice@example.com';
$data['user1']['age'] = 40;

$data['user2']['name'] = 'Bob';
$data['user2']['email'] = 'bob@example.com';
$data['user2']['age'] = 28;

$data['user3']['name'] = 'Alfred';
$data['user3']['email'] = 'alfred@example.com';
$data['user3']['age'] = 86;

// Mixed: keys and values
// Keys should not raise issues, but values should if duplicated enough times
$map = [
    'key' => 'my value',// Noncompliant {{Define a constant instead of duplicating this literal "my value" 3 times.}}
//           ^^^^^^^^^^
    'key' => 'my value',
    'key' => 'my value',
];

// Array access in expressions
if (isset($array['name'])) {
    echo $array['name'];
}

if (isset($otherArray['name'])) {
    echo $otherArray['name'];
}

// Function calls with array access
someFunction($array['name'], $array['email']);
anotherFunction($data['name'], $data['email']);
anotherAgainFunction($data['name'], $data['email']);

// Verify that non-key strings still raise issues when duplicated
echo "value/path"; // Noncompliant {{Define a constant instead of duplicating this literal "value/path" 5 times.}}
//   ^^^^^^^^^^^^
$x = "value/path";
//   ^^^^^^^^^^^^< {{Duplication.}}
$y = "value/path";
//   ^^^^^^^^^^^^< {{Duplication.}}
$z = "value/path";
//   ^^^^^^^^^^^^< {{Duplication.}}
return "value/path";
//     ^^^^^^^^^^^^< {{Duplication.}}


"mystr"; // OK - too short, special cases in regards of how we detect array keys (getting the parent)
"mystr";
"mystr";
