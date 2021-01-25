<?php

return [
    
    'paths' => ['api/*'],
    
    'allowed_origins' => ['foo'], // compliant
    
    'allowed_origins' => ['*'], // Noncompliant
    
    'allowed_origins' => ['foo', '*'], // Noncompliant
    
    'allowed_origins' => Array('*'), // Noncompliant
    
    'allowed_origins' => '*', // Compliant
    
    'allowed_origins' => [], // compliant
    
    'allowed_origins_patterns' => ['*'],
    
    'supports_credentials' => false,
    
];
