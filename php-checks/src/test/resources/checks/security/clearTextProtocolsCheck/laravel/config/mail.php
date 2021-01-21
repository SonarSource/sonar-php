<?php

return [
    'mailers' => [
        'smtp_sens1' => [
            'transport' => 'smtp',
            'host' => 'xxx',
            'encryption' => null, // Noncompliant {{Mail transport without encryption is insecure. Specify an encryption}}
        ],

        'smtp_sens2' => [ // Noncompliant
            'transport' => 'smtp',
            'host' => 'xxx'
        ],

        'smtp_sens3' => [
            'transport' => 'smtp',
            'host' => 'xxx',
            'encryption' => 'unknown', // Noncompliant
        ],
    ]
];

return [
    'mailers' => [
        'smtp_comp1' => [
            'transport' => 'smtp',
            'host' => 'xxx',
            'encryption' => env('MAIL_ENC') // Compliant
        ],

        'smtp_comp2' => [
            'transport' => 'smtp',
            'host' => 'xxx',
            'encryption' => 'tls' // Compliant
        ],

        'smtp_comp3' => [
            'transport' => 'smtp',
            'host' => 'xxx',
            'encryption' => 'ssl' // Compliant
        ],

        'smtp_comp4' => [
            'transport' => 'smtp',
            'host' => env('MAIL_HOST') // Compliant - host might start with tls:// or ssl://
        ],

        'smtp_comp5' => [
            'transport' => 'smtp',
            'host' => 'tls://xxx' // Compliant
        ],

        'smtp_comp6' => [
            'transport' => 'smtp',
            'host' => 'ssl://xxx' // Compliant
        ],

        'smtp_comp7' => [
            'transport' => 'smtp',
            'host' => '127.0.0.1', // Compliant - localhost
            'auth_mode' => null,
        ],

        'smtp_comp8' => [
          'transport' => 'smtp',
        ],

        'smtp_comp8' => [
          'transport' => $unknownTransport,
        ],

        'mailgun' => [
            'transport' => 'mailgun',
        ],

        'smtp_invalid' => [
          'foo'
        ],

        'foo',
    ]
];

$mailersConfigs = [
	'mailers' => [
		'smtp_sens1' => [
			'transport' => 'smtp',
			'host' => 'xxx',
			'encryption' => null, // Compliant - FN not handled
		]
	]
];
return $mailersConfigs;

return ["foo"];
return;
return "foo";
