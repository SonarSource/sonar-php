<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class TestController extends Controller
{
    public function store(Request $request)
    {
        $validator = validator($request->all(), [
            'upload1' => 'required|file|max:8000', // Compliant
            'upload2' => ['required', 'file', 'max:8000'], // Compliant
            'upload3' => 'required|file|max:8001', // Noncompliant
            'upload4' => 'required|file', // Noncompliant
        ]);

        $validator = Validator::make($request->all(), [
            'upload1' => 'required|file|max:8000', // Compliant
            'upload2' => ['required', 'file', 'max:8000'], // Compliant
            'upload3' => 'required|file|max:8001', // Noncompliant
            'upload4' => 'required|file', // Noncompliant
        ]);

        $validator = $unknown::make($request->all(), [
            'upload3' => 'required|file|max:8001', // Compliant
        ]);
    }
}

class NotController
{
    public function store(Request $request)
    {
        $validator = validator($request->all(), [
            'upload3' => 'required|file|max:8001', // Compliant - not in controller
        ]);
    }
}

$validator = validator($request->all(), [
    'upload3' => 'required|file|max:8001', // Compliant - Not in controller
]);
