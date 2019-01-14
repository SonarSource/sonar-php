<?php

function handle_sockets($domain, $type, $protocol, $port, $backlog, $addr, $hostname, $local_socket, $remote_socket, $fd) {
    socket_create($domain, $type, $protocol); // Noncompliant {{Make sure that sockets are used safely here.}}
    socket_create_listen($port, $backlog); // Noncompliant
    Socket_Create_Listen($port, $backlog); // Noncompliant
    socket_addrinfo_bind($addr); // Noncompliant
    socket_addrinfo_connect($addr); // Noncompliant
    socket_create_pair($domain, $type, $protocol, $fd); // Noncompliant

    fsockopen($hostname); // Noncompliant
    pfsockopen($hostname); // Noncompliant
    stream_socket_server($local_socket); // Noncompliant
    stream_socket_client($remote_socket); // Noncompliant
    stream_socket_pair($domain, $type, $protocol); // Noncompliant
}

stream_socket_pair($domain, $type, $protocol); // Noncompliant

$abc->fsockopen($hostname); // Ok - not the PHP core function
