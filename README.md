# Jury Simulator

Jury Simulator is an Android application that provides an immersive simulation of the jury duty experience. Using AI-powered conversations and realistic trial scenarios, users can experience the legal process from the perspective of a juror, participating in voir dire, witnessing trials, and engaging in deliberations with AI-controlled jurors.

## Table of Contents
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Installation](#installation)
- [Usage](#usage)
- [Architecture](#architecture)
- [AI Integration](#ai-integration)
- [Contributing](#contributing)
- [License](#license)
- [Security](#security)

## Features

- **Realistic Trial Simulation**: Experience the complete jury duty process from selection to verdict
- **AI-Powered Interactions**: Engage with AI-generated judges, attorneys, witnesses, and fellow jurors
- **Dynamic Case Generation**: Each trial presents unique scenarios with different defendants, charges, and evidence
- **Interactive Voir Dire**: Participate in jury selection with realistic questioning
- **Witness Testimony**: Listen to and question witnesses during the trial
- **Evidence Presentation**: Review physical evidence relevant to the case
- **Deliberation Phase**: Discuss the case with 11 AI jurors, each with unique personalities and biases
- **Individual Juror Chat**: Have private conversations with specific jurors during deliberations
- **Note-Taking System**: Record observations and thoughts during the trial
- **Case History**: Track and review past simulated cases
- **Customizable Juror Profile**: Personalize your juror identity

## Technology Stack

- **Platform**: Android (API Level 26+)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **AI Integration**: Ollama API
- **Database**: Room (SQLite)
- **Preferences**: DataStore
- **Networking**: Retrofit, OkHttp, Moshi
- **Build System**: Gradle (Kotlin DSL)

## Installation

### Prerequisites

1. **Ollama**: Install and run Ollama on your local machine or accessible server
   - Download from [ollama.ai](https://ollama.ai)
   - Pull a model: `ollama pull llama3` or similar

2. **Android Environment**:
   - Android Studio (latest version recommended)
   - Android SDK with API Level 26+

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/jury-simulator.git
   cd jury-simulator
   ```

2. Open the project in Android Studio

3. Build the project:
   ```bash
   ./gradlew build
   ```

4. Install on your device/emulator:
   ```bash
   ./gradlew installDebug
   ```

## Usage

1. **Initial Setup**: Enter your Ollama server URL when prompted (e.g., `http://192.168.1.100:11434`)

2. **Select Model**: Choose an AI model from your Ollama instance

3. **Jury Selection (Voir Dire)**: Answer questions from the judge to determine eligibility

4. **Trial Phases**:
   - Opening Statements
   - Witness Testimony
   - Evidence Presentation
   - Closing Arguments

5. **Deliberation**: Discuss the case with AI jurors and participate in voting

6. **Verdict**: Receive the final outcome of the trial

### Customizing Your Experience

- Update your juror profile in Settings
- Adjust AI model preferences
- Review case history from previous simulations

## Architecture

The application follows the MVVM (Model-View-ViewModel) architectural pattern:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   UI Layer      │◄──►│  ViewModel Layer │◄──►│  Data Layer     │
│ (Compose)       │    │ (State holders)  │    │ (Repositories)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Key Components

- **MainActivity**: Single activity that hosts the Compose UI
- **NavGraph**: Manages navigation between screens
- **ViewModels**: Handle business logic and state management
- **Repositories**: Abstract data sources (AI API, local storage)
- **Models**: Define data structures and business logic
- **Components**: Reusable UI elements

### Data Flow

The application uses reactive programming with StateFlow and Flow for state management:

1. UI collects state from ViewModels
2. User actions trigger ViewModel methods
3. ViewModels update state and perform business logic
4. Repository layers handle data operations
5. Updated state flows back to UI

## AI Integration

The application integrates with Ollama to provide AI-powered trial experiences:

### How It Works

1. **Ollama Connection**: Connects to a local Ollama instance via REST API
2. **Prompt Templates**: Uses carefully crafted prompts to guide AI responses
3. **Response Parsing**: Extracts structured information from AI responses
4. **Context Management**: Maintains conversation history for coherent interactions

### Supported Models

The application works with any model supported by Ollama, including:
- Llama 3
- Mistral
- Gemma
- Phi
- And many others

### Prompt Engineering

The application uses specialized prompt templates for different scenarios:
- Case generation
- Voir dire questioning
- Opening/closing statements
- Witness testimony
- Juror deliberations
- Individual juror interactions

## Contributing

We welcome contributions to improve the Jury Simulator! Please see our [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to get started.

### Development Guidelines

- Follow Android/Kotlin coding conventions
- Maintain MVVM architecture principles
- Write unit tests for new features
- Update documentation as needed
- Follow accessibility best practices

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Security

Please see our [SECURITY.md](SECURITY.md) for information about reporting security vulnerabilities.

## Support

For support, please open an issue in the GitHub repository or contact the development team.