# UI Components

## Overview

The Jury Simulator application uses Jetpack Compose for its user interface, following Material Design 3 guidelines. The UI is built with a component-based architecture that promotes reusability and maintainability.

## Component Architecture

### Navigation Structure

The application uses a single-activity architecture with Compose Navigation:

```
MainActivity
└── JurySimApp (Composable)
    └── JurySimNavGraph (Navigation Controller)
        ├── HomeScreen
        ├── SetupScreen
        ├── ModelSelectionScreen
        ├── Simulation Screens
        │   ├── IntroScreen
        │   ├── VoirDireScreen
        │   ├── TrialScreen
        │   ├── DeliberationScreen
        │   └── VerdictScreen
        ├── SettingsScreen
        ├── JurorProfileScreen
        └── HistoryScreen
```

### Core Components

#### Navigation Components

**NavGraph.kt**
- Manages all application navigation
- Defines routes using sealed class `Screen`
- Handles ViewModel instantiation
- Orchestrates the trial flow

#### Reusable UI Components

**ChatBubble.kt**
- Displays messages in conversation format
- Differentiates between user and AI messages
- Shows speaker information
- Handles message formatting

**LoadingIndicator.kt**
- Visual indicator for ongoing operations
- Contextual messaging (e.g., "Judge is thinking...")
- Smooth animations

**PhaseIndicator.kt**
- Shows current trial phase
- Visual progress indication
- Clear phase identification

**NotebookScreen.kt**
- Interactive juror notebook
- Tabbed interface for different fact types
- Note-taking capabilities
- Fact categorization (People, Evidence, Other)

### Screen Components

#### Home Screen
- Entry point for the application
- Menu cards for different actions
- Animated welcome elements
- Navigation to all main sections

#### Setup Screens
- **SetupScreen**: Configure Ollama server connection
- **ModelSelectionScreen**: Choose AI model for simulation

#### Simulation Screens
- **IntroScreen**: Case introduction and preparation
- **VoirDireScreen**: Jury selection phase with chat interface
- **TrialScreen**: Multi-phase trial experience
- **DeliberationScreen**: Jury discussion and voting
- **VerdictScreen**: Final outcome presentation

#### Utility Screens
- **SettingsScreen**: Application preferences
- **JurorProfileScreen**: User profile management
- **HistoryScreen**: Past case review

## Design System

### Color Palette

The application follows a courtroom-themed color scheme:

- **Primary**: Deep blue representing justice and authority
- **Secondary**: Gold accents representing prestige
- **Tertiary**: Neutral tones for readability
- **Error**: Red for error states
- **Success**: Green for positive feedback

### Typography

- **Headlines**: Bold, readable fonts for important information
- **Body text**: Comfortable reading size for legal content
- **Labels**: Clear, distinguishable text for UI elements

### Spacing and Layout

- Consistent padding and margins
- Responsive layouts for different screen sizes
- Adaptive components for various orientations

## Interaction Patterns

### Chat Interface

Used extensively throughout the application:

- Message bubbles with clear sender identification
- Smooth scrolling to latest messages
- Loading indicators during AI processing
- Error handling with retry options

### Form Inputs

- Text fields with appropriate validation
- Action buttons with clear affordances
- Loading states for network operations
- Success/error feedback

### Navigation

- Bottom navigation for main sections
- Modal dialogs for secondary actions
- Smooth transitions between screens
- Back navigation handling

## Accessibility

### Screen Reader Support

- Proper content descriptions
- Semantic structure
- Focus management
- Announcements for important changes

### Visual Accessibility

- Sufficient color contrast
- Scalable text elements
- Alternative interaction methods
- Clear visual hierarchy

## Responsive Design

### Screen Sizes

- Optimized for phones and tablets
- Adaptable layouts for different aspect ratios
- Touch-friendly targets
- Readable text at various sizes

### Orientation Changes

- Smooth transitions between portrait and landscape
- Preserved state during orientation changes
- Adaptive component sizing

## Animation and Transitions

### Micro-interactions

- Button presses with ripple effects
- Loading animations
- State transitions
- Error feedback animations

### Screen Transitions

- Smooth navigation between screens
- Contextual transition animations
- Loading states during transitions

## Custom Components

### Notebook System

A unique feature allowing users to track important information:

- Tabbed interface for different content types
- Expandable fact cards
- Note-taking capabilities
- Categorization system

### Juror Profiles

Visual representation of AI jurors:

- Avatar images
- Personality indicators
- Bias visualization
- Voting status indicators

## State Management

### UI State Patterns

- CompositionLocal for theme and context
- State hoisting for shared state
- ViewModel integration for business logic
- Reactive updates with StateFlow

### Loading States

- Skeleton screens during initial load
- Progress indicators for ongoing operations
- Empty states for no-content scenarios
- Error states with recovery options

## Theming

The application uses Material 3 theming with custom courtroom-inspired colors:

- Dark and light theme support
- Dynamic color adaptation
- Custom color roles for legal context
- Consistent branding across all screens

This component architecture ensures a consistent, accessible, and engaging user experience throughout the Jury Simulator application.