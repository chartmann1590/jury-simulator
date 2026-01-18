# Contributing to Jury Simulator

Thank you for your interest in contributing to Jury Simulator! We appreciate your help in improving this open-source project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Pull Request Process](#pull-request-process)
- [Style Guides](#style-guides)
- [Commit Messages](#commit-messages)
- [Testing](#testing)
- [Questions](#questions)

## Code of Conduct

This project and everyone participating in it is governed by our [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to [conduct@jurysimulator.example.com](mailto:conduct@jurysimulator.example.com).

## Getting Started

### Prerequisites

- Android Studio (latest version recommended)
- Android SDK with API Level 26+
- Ollama running locally for testing AI features
- Git

### Setting Up Your Development Environment

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/jury-simulator.git
   cd jury-simulator
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/ORG_NAME/jury-simulator.git
   ```
4. Install dependencies:
   ```bash
   ./gradlew build
   ```

### Running the Application

1. Start Ollama locally:
   ```bash
   ollama serve
   ```
2. Pull a model:
   ```bash
   ollama pull llama3
   ```
3. Open the project in Android Studio
4. Run the application on an emulator or physical device

## Development Workflow

### Creating a Branch

1. Create a new branch for your feature or bug fix:
   ```bash
   git checkout -b feature/my-new-feature
   ```
   or
   ```bash
   git checkout -b bugfix/issue-description
   ```

2. Make your changes in the new branch

### Making Changes

1. Follow the existing code style and patterns
2. Write clear, descriptive commit messages
3. Add tests for new functionality
4. Update documentation as needed
5. Ensure all tests pass before submitting

### Keeping Your Branch Updated

1. Switch to your main branch:
   ```bash
   git checkout main
   ```

2. Pull the latest changes from upstream:
   ```bash
   git pull upstream main
   ```

3. Switch back to your feature branch:
   ```bash
   git checkout feature/my-new-feature
   ```

4. Update your feature branch with the latest changes:
   ```bash
   git rebase main
   ```

## Pull Request Process

1. Ensure your branch is up to date with the main branch
2. Run all tests and ensure they pass
3. Update the README.md with details of changes if applicable
4. Submit a pull request through the GitHub website
5. Fill out the pull request template completely
6. Link any related issues using keywords like "Fixes #123" or "Closes #456"
7. Wait for review and address any feedback

### Pull Request Checklist

- [ ] Code follows the project's style guidelines
- [ ] Self-review completed
- [ ] Tests added/updated for new functionality
- [ ] Documentation updated
- [ ] All CI checks pass
- [ ] PR title follows the conventional commits format

## Style Guides

### Kotlin Style Guide

- Follow the [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Use 4 spaces for indentation (no tabs)
- Maximum line length of 100 characters
- Use PascalCase for classes and interfaces
- Use camelCase for functions, variables, and properties
- Use UPPER_SNAKE_CASE for constants

### Architecture Guidelines

- Follow MVVM architecture pattern
- Keep activities and fragments thin
- Place business logic in ViewModels
- Use repositories for data operations
- Separate UI, domain, and data layers appropriately

### UI/UX Guidelines

- Follow Material Design 3 guidelines
- Maintain consistent color scheme and typography
- Ensure accessibility compliance
- Use appropriate content descriptions for accessibility
- Support different screen sizes and orientations

## Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Types

- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Changes that do not affect the meaning of the code
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `perf`: A code change that improves performance
- `test`: Adding missing tests or correcting existing tests
- `chore`: Other changes that don't modify src or test files

### Examples

```
feat(ui): add juror profile screen

Add a new screen where users can customize their juror profile
information including name, occupation, and legal experience.
```

```
fix(data): resolve crash in case history retrieval

Fixed a null pointer exception that occurred when retrieving
case history from the database when no cases existed.
```

## Testing

### Writing Tests

- Write unit tests for ViewModels and utility functions
- Write integration tests for data layer operations
- Write UI tests for critical user flows
- Aim for high test coverage on business logic

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run all instrumented tests
./gradlew connectedAndroidTest

# Run all tests
./gradlew check
```

### Test Structure

Follow the AAA pattern (Arrange, Act, Assert) for test organization:

```kotlin
@Test
fun `when user submits valid voir dire response then judge responds`() {
    // Arrange
    val viewModel = SimulationViewModel(mockOllamaRepo, mockPrefsRepo, mockCaseHistoryRepo)
    
    // Act
    viewModel.respondToVoirDire("I understand the importance of being impartial.")
    
    // Assert
    assertThat(viewModel.state.value.messages).isNotEmpty()
}
```

## Questions?

If you have any questions about contributing, feel free to:

1. Open an issue in the repository
2. Contact the maintainers at [dev@jurysimulator.example.com](mailto:dev@jurysimulator.example.com)
3. Join our community discussions

Thank you for contributing to Jury Simulator!