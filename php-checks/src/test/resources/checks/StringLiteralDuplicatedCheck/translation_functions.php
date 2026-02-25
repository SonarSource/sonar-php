<?php

// P0: Translation function arguments should NOT raise issues
echo __('Welcome back');
echo __('Welcome back');
echo __('Welcome back');

echo _e('Save changes');
echo _e('Save changes');
echo _e('Save changes');

echo t('Please enter');
echo t('Please enter');
echo t('Please enter');

echo esc_html__('Submit form');
echo esc_html__('Submit form');
echo esc_html__('Submit form');

echo esc_attr__('Click here');
echo esc_attr__('Click here');
echo esc_attr__('Click here');

echo esc_html_e('Contact us');
echo esc_html_e('Contact us');
echo esc_html_e('Contact us');

echo _n('One item', 'Many items', $count);
echo _n('One item', 'Many items', $count);
echo _n('One item', 'Many items', $count);

// Strings NOT in translation functions still raise issues
echo "some string here"; // Noncompliant {{Define a constant instead of duplicating this literal "some string here" 3 times.}}
//   ^^^^^^^^^^^^^^^^^^
echo "some string here";
//   ^^^^^^^^^^^^^^^^^^< {{Duplication.}}
echo "some string here";
//   ^^^^^^^^^^^^^^^^^^< {{Duplication.}}
