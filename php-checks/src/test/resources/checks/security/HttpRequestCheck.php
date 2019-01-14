<?php

function sendHttpRequest($url) {
    $unknown();
    file_get_contents();
    file_get_contents($url);
    file_get_contents('https://example.com'); // Noncompliant {{Make sure that this http request is sent safely.}}
    fopen('http://example.com', 'r');  // Noncompliant
    readfile('http://example.com'); // Noncompliant
    copy('http://example.com', 'test.txt'); // Noncompliant
    copy('test.txt', 'http://example.com'); // Noncompliant
    copy('test.txt', 'test.txt');
    file('http://example.com'); // Noncompliant
    file('zip://http://example.com'); // Noncompliant
    other('http://example.com');

    file_get_contents('http://example.com', false, $context); // Noncompliant
    fopen('http://example.com', 'r', false, $context); // Noncompliant
    file('http://example.com', 0, $context); // Noncompliant
    readfile('http://example.com', False, $context); // Noncompliant

    get_headers('http://example.com'); // Noncompliant
    get_meta_tags('http://example.com'); // Noncompliant

    $data = curl_exec($ch); // Noncompliant

    new GuzzleHttp\Client(); // Noncompliant
    new GuzzleHttp\Client; // Noncompliant
}

namespace http\Client {
    new Request('GET', 'http://example.com'); // Noncompliant
}

namespace other {
    new Request('GET', 'http://example.com');
    new \http\Client\Request('GET', 'http://example.com'); // Noncompliant
}

new other\Request('GET', 'http://example.com');
