<?php

// Drupal form API - uses string literals as array keys
// Array keys like 'name', '#type', '#title', etc. should NOT raise issues when duplicated

function mymodule_form($form, &$form_state) {
  $form['name'] = [
    '#type' => 'textfield',
    '#title' => 'Your Name',
    '#required' => TRUE,
    '#default_value' => '',
  ];

  $form['email'] = [
    '#type' => 'email',
    '#title' => 'Email Address',
    '#required' => TRUE,
  ];

  $form['phone'] = [
    '#type' => 'textfield',
    '#title' => 'Phone Number',
    '#required' => FALSE,
  ];

  $form['message'] = [
    '#type' => 'textfield',
    '#title' => 'Message',
    '#required' => TRUE,
  ];

  // Checkbox elements
  $form['checkboxes']['option1'] = [
    '#type' => 'checkbox',
    '#title' => 'Option 1',
  ];

  $form['checkboxes']['option2'] = [
    '#type' => 'checkbox',
    '#title' => 'Option 2',
  ];

  $form['checkboxes']['option3'] = [
    '#type' => 'checkbox',
    '#title' => 'Option 3',
  ];

  $form['submit'] = [
    '#type' => 'submit',
    '#value' => 'Submit',
  ];

  return $form;
}

// More Drupal form examples
$element = [
  '#type' => 'container',
  '#attributes' => ['class' => ['my-class']],
];

$field = [
  '#type' => 'select',
  '#title' => 'Select an option',
  '#options' => ['option1' => 'Option 1', 'option2' => 'Option 2'],
  '#required' => TRUE,
];

// MongoDB aggregation pipeline example
$pipeline = [
  [
    '$match' => $matchConditions,
  ],
  [
    '$match' => $otherConditions,
  ],
  [
    '$group' => $groupSpec,
  ],
];

// Array keys used multiple times - should NOT raise issues
$config['database']['host'] = 'localhost';
$config['database']['port'] = 3306;
$config['database']['name'] = 'mydb';
$config['cache']['driver'] = 'redis';
$config['cache']['host'] = 'localhost';
$config['cache']['port'] = 6379;

// But duplicated strings NOT used as array keys SHOULD raise issues
function test() {
  echo "value/type"; // Noncompliant {{Define a constant instead of duplicating this literal "value/type" 5 times.}}
  //   ^^^^^^^^^^^^
  echo "value/type";
  //   ^^^^^^^^^^^^< {{Duplication.}}
  echo "value/type";
  //   ^^^^^^^^^^^^< {{Duplication.}}
  echo "value/type";
  //   ^^^^^^^^^^^^< {{Duplication.}}
  echo "value/type";
  //   ^^^^^^^^^^^^< {{Duplication.}}
}
