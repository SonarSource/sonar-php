<?php

namespace Foo;

$foo->loadFromExtension('security', ['session_fixation_strategy' => 'none']);  // Noncompliant
$foo->LoadFromExtension('SECURITY', array('SESSION_FIXATION_STRATEGY' => 'NONE'));  // Noncompliant
$foo->loadFromExtension('security', ['foo' => 'bar', 'session_fixation_strategy' => 'none']);  // Noncompliant

$array = ['foo' => 'bar', 'session_fixation_strategy' => 'none'];
$foo->loadFromExtension('security', $array); // Noncompliant

$foo->loadFromExtension(['session_fixation_strategy' => 'none'], 'security');  // Compliant
$foo->loadFromExtension(values:['session_fixation_strategy' => 'none'], extension:'security');  // Noncompliant

$foo->loadFromExtension('security', ['session_fixation_strategy' => 'invalidate']);  // Compliant
$foo->loadFromExtension('security', ['other_property' => 'none']);  // Compliant
$foo->loadFromExtension('bar', ['session_fixation_strategy' => 'none']);  // Compliant
$foo->loadFromExtension('security');  // Compliant
$foo->loadFromExtension(['session_fixation_strategy' => 'none']);  // Compliant
$foo->loadFromExtension('security', ['none']);  // Compliant
$foo->loadFromExtension();  // Compliant

$foo->extension('security', ['session_fixation_strategy' => 'none']);  // Noncompliant
$foo->extension('security', ['session_fixation_strategy' => 'invalidate']);  // compliant

$foo->Extension(config:['session_fixation_strategy' => 'none'], namespace:'security');  // Noncompliant
$foo->extension('foo', ['session_fixation_strategy' => 'none']);  // compliant

$foo->prependExtensionConfig('security', ['session_fixation_strategy' => 'none']);  // Noncompliant
$foo->PrependExtensionConfig(config:['session_fixation_strategy' => 'none'], name:'security');  // Noncompliant

$foo->prependExtensionConfig(['session_fixation_strategy' => 'none'], 'security');  // Compliant
$foo->prependExtensionConfig('security', ['session_fixation_strategy' => 'bar']);  // Compliant
$foo->other_function_call('security', ['session_fixation_strategy' => 'none']);  // Compliant
