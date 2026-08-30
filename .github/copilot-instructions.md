# Engineering Guidelines for Copilot & AI Assistants

Please follow the engineering principles and review standards defined in [AGENTS.md](../AGENTS.md):
- Maintain Unidirectional Data Flow (UDF) through Kotlin StateFlow and ViewModel.
- Follow Material 3 best practices, theme tokens, and lambda-based progress indicators.
- Provide input validation and mobile-friendly numeric keyboards in forms.
- Maintain comprehensive unit tests with `MainDispatcherRule` and zero Kotlin compiler warnings.

