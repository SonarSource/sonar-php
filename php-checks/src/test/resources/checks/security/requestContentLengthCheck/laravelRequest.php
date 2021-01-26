<?php
namespace App\Http\Controllers;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class TestController extends Controller
{
    public function test(Request $request)
    {
        $validatedData = $request->validate([
            'upload1' => 'required|file|max:8000', // Compliant
            'upload2' => ['required', 'file', 'max:8000'], // Compliant
            'upload2' => ['required', 'file', 'max:8001'], // Noncompliant
            'upload3' => 'required|file|max:8001', // Noncompliant
            'upload4' => 'required|file', // Noncompliant
        ]);

        $validatedData = $request->validateWithBag('test', [
            'upload1' => 'required|file|max:8000', // Compliant
            'upload2' => ['required', 'file', 'max:8000'], // Compliant
            'upload3' => 'required|file|max:8001', // Noncompliant
            'upload4' => 'required|file', // Noncompliant
        ]);
    }
}
