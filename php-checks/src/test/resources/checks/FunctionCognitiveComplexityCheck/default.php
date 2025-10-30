<?php

function ko() // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 16 to the 15 allowed.}} [[effortToFix=1]]
{
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
}

function ok() {
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
  if (true) { }// +1
}

function pipeOperator() // Compliant
{
  $result = "Hello World"
      |> strtoupper(...)
      |> str_shuffle(...)
      |> trim(...)
      |> strtoupper(...)
      |> str_shuffle(...)
      |> trim(...)
      |> strtoupper(...)
      |> str_shuffle(...)
      |> trim(...)
      |> strtoupper(...)
      |> str_shuffle(...)
      |> trim(...)
      |> strtoupper(...)
      |> str_shuffle(...)
      |> trim(...);
}

function pipeOperator2() // Noncompliant
{
  $result = "Hello World"
      |> strtoupper(...)
      |> str_shuffle(...)
      |> trim(...)
      |> strtoupper(...)
      |> str_shuffle(...)
      |> trim(...)
      |> strtoupper(...)
      |> str_shuffle(...)
      |> trim(...)
      |> strtoupper(...)
      |> str_shuffle(...)
      |> trim(...)
      |> strtoupper(...)
      |> str_shuffle(...)
      |> str_shuffle(...)
      |> trim(...);
}
