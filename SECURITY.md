# Security Policy

## Supported Versions

The following versions of Jury Simulator are currently supported with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.x.x   | ✅ Yes             |
| < 1.0   | ❌ No              |

## Reporting a Vulnerability

We take security seriously. If you discover a security vulnerability, please report it to us responsibly.

### How to Report

**Do not** create a public GitHub issue for security vulnerabilities. Instead:

1. Email your findings to: [security@jurysimulator.example.com](mailto:security@jurysimulator.example.com)
2. Include the following information:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested remediation (if any)

### Response Timeline

- **Within 48 hours**: Acknowledgment of your report
- **Within 1 week**: Assessment and preliminary response
- **Within 2 weeks**: Update on remediation progress
- **Within 1 month**: Final resolution or timeline for fix

### What to Expect

After reporting a vulnerability:
1. We will investigate and confirm the issue
2. We will develop a fix if the vulnerability is confirmed
3. We will notify you when the fix is implemented
4. We will publicly acknowledge your responsible disclosure (unless you prefer anonymity)

## Security Best Practices

### For Users

- Keep your Ollama instance secure and properly configured
- Use strong passwords for any authentication systems
- Regularly update the application to the latest version
- Only connect to trusted Ollama servers

### For Developers

- Follow secure coding practices
- Validate and sanitize all inputs
- Implement proper error handling
- Use HTTPS when connecting to external services
- Regularly update dependencies

## Application Security Features

- Network security configuration allows cleartext traffic only for local development
- Input validation for all user-provided data
- Secure storage of preferences using DataStore
- Proper error handling to prevent information disclosure
- Isolated AI model connections with configurable endpoints

## Dependencies Security

We regularly monitor our dependencies for known vulnerabilities:
- Retrofit for networking
- Room for local database
- Moshi for JSON parsing
- OkHttp for HTTP client
- Kotlin Coroutines for async operations

## Questions?

If you have any questions about this security policy, please contact us at [security@jurysimulator.example.com](mailto:security@jurysimulator.example.com).