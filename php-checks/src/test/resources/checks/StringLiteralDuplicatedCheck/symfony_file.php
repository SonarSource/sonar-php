<?php

use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;

class CreateNewUser extends FormRequest
{
    /**
     * Get the validation rules that apply to the request.
     * Array keys (field names) should not raise issues when duplicated.
     * Laravel validation rule strings (like 'required', 'string', 'max:100') should also not raise issues.
     */
    public function rules(): array
    {
        return [
            'email' => ['required', 'email:rfc', 'max:100'],
            'token' => ['required', 'max:100', new TokenRule()],
            'password' => ['required', 'string', new PasswordRule()],
            'firstname' => ['required', 'string', 'max:100'],
            'surname' => ['required', 'string', 'max:100'],
            'company' => ['nullable', 'string', 'max:100'],
        ];
    }
}

// Validator::make with pipe-separated validation strings
// The field name keys should not raise issues, and neither should the validation strings
Validator::make($request->all(), [
    'email' => 'required|email:rfc|max:100',
    'firstname' => 'required|string|max:100',
    'surname' => 'required|string|max:100',
    'phone' => 'required|string|max:100',
    'age' => 'required|integer|min:18',
]);

// Array access with duplicated keys - these keys should NOT raise issues
$data['email'] = 'test@example.com';
$data['firstname'] = 'John';
$data['surname'] = 'Doe';

// More examples with various validation rules
$rules = [
    'username' => ['required', 'string', 'min:3', 'max:255'],
    'bio' => ['nullable', 'string', 'max:255'],
    'website' => ['nullable', 'string', 'max:255', 'url'],
    'avatar' => ['nullable', 'string', 'max:255'],
];

// Common validation strings used multiple times
$emailRules = ['required', 'email', 'max:255', 'unique:users'];
$passwordRules = ['required', 'string', 'min:8', 'max:255', 'confirmed'];
$nameRules = ['required', 'string', 'max:255'];
$optionalStringRules = ['nullable', 'string', 'max:255'];

// But duplicated strings NOT used as array keys or values SHOULD raise issues
echo "test/value"; // Noncompliant {{Define a constant instead of duplicating this literal "test/value" 5 times.}}
//   ^^^^^^^^^^^^
echo "test/value";
//   ^^^^^^^^^^^^< {{Duplication.}}
echo "test/value";
//   ^^^^^^^^^^^^< {{Duplication.}}
echo "test/value";
//   ^^^^^^^^^^^^< {{Duplication.}}
echo "test/value";
//   ^^^^^^^^^^^^< {{Duplication.}}
