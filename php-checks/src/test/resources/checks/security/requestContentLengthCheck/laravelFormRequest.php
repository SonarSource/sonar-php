<?php

use Illuminate\Foundation\Http\FormRequest;

class TestRequest extends FormRequest
{
    public function rules()
    {
        return [
            'upload1' => 'required|file|max:8000', // Compliant
            'upload2' => ['required', 'file', 'max:8000'], // Compliant
            'upload3' => 'required|file|max:8001', // Noncompliant
            'upload4' => 'required|file', // Noncompliant
        ];
    }
}
