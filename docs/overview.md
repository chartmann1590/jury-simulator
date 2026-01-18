# Jury Simulator Overview

## What is Jury Simulator?

Jury Simulator is an Android application that provides an immersive simulation of the jury duty experience. Using AI-powered conversations and realistic trial scenarios, users can experience the legal process from the perspective of a juror, participating in voir dire, witnessing trials, and engaging in deliberations with AI-controlled jurors.

## Purpose

The application serves multiple purposes:

- **Educational**: Teaching users about the jury system and legal process
- **Training**: Providing practice for potential jurors
- **Research**: Studying jury behavior and decision-making
- **Entertainment**: Offering an engaging legal simulation experience

## Key Features

### Realistic Trial Simulation
- Complete trial experience from jury selection to verdict
- Dynamic case generation with unique scenarios
- Professional courtroom atmosphere

### AI-Powered Interactions
- Natural conversations with AI-generated judges, attorneys, and witnesses
- 11 unique AI jurors with distinct personalities and biases
- Context-aware responses based on trial progression

### Interactive Elements
- Voir dire participation with realistic questioning
- Witness questioning capabilities
- Evidence examination and note-taking
- Private conversations with individual jurors

### Personalization
- Customizable juror profile
- Case history tracking
- Personal notes and observations
- Model selection for AI customization

## Technical Approach

The application combines modern Android development practices with advanced AI integration:

- **Native Android**: Built with Kotlin and Jetpack Compose for optimal performance
- **AI Integration**: Connects to local Ollama instance for privacy and performance
- **MVVM Architecture**: Clean separation of concerns for maintainability
- **Reactive Programming**: Uses StateFlow and Flow for responsive UI updates

## Target Audience

- Law students and legal professionals
- Citizens preparing for jury duty
- Researchers studying jury behavior
- Anyone interested in legal processes
- Educational institutions teaching civics

## System Requirements

### Device Requirements
- Android 8.0 (API level 26) or higher
- Minimum 2GB RAM
- Stable internet connection for AI communication

### AI Requirements
- Local Ollama installation
- Compatible language model (recommended: Llama 3, Mistral, or similar)
- Adequate hardware to run AI models (varies by model size)

## Getting Started

For installation and setup instructions, see the main [README.md](../README.md) file.