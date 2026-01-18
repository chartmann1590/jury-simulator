# Trial Flow

## Overview

The Jury Simulator application guides users through a complete jury trial experience, from initial setup to final verdict. The trial flow is carefully designed to mirror real-world legal proceedings while maintaining engagement through AI-powered interactions.

## Complete Trial Journey

```
┌─────────────────┐
│   Home Screen   │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│   Setup Phase   │
│ (Server Config) │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│ Model Selection │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│   Intro Phase   │
│ (Case Gen)      │
└─────────┬───────┘
          │
          ▼
┌─────────────────┐
│  Voir Dire      │
│ (Jury Sel)      │
└─────────┬───────┘
          │
    ┌─────▼─────┐
    │ Selected? │
    └─────┬─────┘
          │
    No ┌───▼───┐ Yes
    ┌──┤ Dismiss │◄──┐
    │  └───────┘   │
    │              │
    │    ┌─────────▼─────────┐
    │    │ Generate AI Jurors │
    │    └─────────┬─────────┘
    │              │
    └──────────────┼──────────────┐
                     │              │
                     ▼              │
        ┌─────────────────────────┐ │
        │      Trial Phase        │ │
        │                         │ │
        │ • Opening Statements    │ │
        │ • Witness Testimony     │ │
        │ • Evidence Presentation │ │
        │ • Closing Arguments     │ │
        └─────────┬───────────────┘ │
                  │                 │
                  ▼                 │
        ┌─────────────────┐         │
        │ Deliberation    │         │
        │ Phase           │         │
        └─────────┬───────┘         │
                  │                 │
                  ▼                 │
        ┌─────────────────┐         │
        │   Voting        │         │
        │   Rounds        │         │
        └─────────┬───────┘         │
                  │                 │
        ┌─────────▼─────┐           │
        │ Unanimous?    │           │
        └─────┬─────────┘           │
              │                     │
        No ┌──▼──┐ Yes              │
    ┌──────┤ More  ├────────────────┘
    │      │ Rounds│
    │      └──┬────┘
    │         │
    │    Max Rounds?
    │         │
    │    No ┌──▼──┐ Yes
    │    ┌─┤ More  │ Mistrial
    │    │ │ Rounds│◄───────────────┐
    │    │ └──┬────┘                │
    │    │    │                     │
    └────┼────┼─────────────────────┘
         │    │
         ▼    ▼
    ┌─────────────────┐
    │   Verdict       │
    │   Phase         │
    └─────────┬───────┘
              │
              ▼
    ┌─────────────────┐
    │   Case Saved    │
    │   to History    │
    └─────────────────┘
```

## Phase Details

### 1. Setup Phase
**Location**: `SetupScreen.kt`

**Purpose**: Configure connection to Ollama server
- User enters server URL
- Connection testing functionality
- Error handling for connectivity issues

**Key Actions**:
- Validate server URL
- Test connection to Ollama
- Proceed to model selection

### 2. Model Selection Phase
**Location**: `ModelSelectionScreen.kt`

**Purpose**: Select AI model for trial simulation
- Fetch available models from Ollama
- Display model information
- Allow user to select preferred model

**Key Actions**:
- Retrieve model list from Ollama
- Display model details
- Save selection to preferences

### 3. Intro Phase
**Location**: `IntroScreen.kt`

**Purpose**: Generate and present the case
- AI generates unique case details
- Present case title, defendant, charges
- Prepare user for voir dire

**Key Actions**:
- Generate case using AI
- Parse case details
- Transition to voir dire

### 4. Voir Dire Phase
**Location**: `VoirDireScreen.kt`

**Purpose**: Jury selection process
- Judge asks questions to assess suitability
- User responds to voir dire questions
- Determine if selected or dismissed

**Key Actions**:
- Conduct jury selection interview
- Process user responses
- Determine selection outcome
- Generate AI jurors if selected

**Special Features**:
- Natural conversation with AI judge
- Realistic questioning process
- Selection/dismissal determination

### 5. Trial Phase
**Location**: `TrialScreen.kt`

The trial phase consists of multiple sub-phases:

#### 5a. Opening Statements
- Prosecution presents case overview
- Defense presents their perspective
- Sets stage for evidence presentation

#### 5b. Witness Testimony
- Multiple witnesses called by each side
- User can submit questions to judge
- Judge rules on question admissibility
- Witness responds if question allowed

#### 5c. Evidence Presentation
- Physical evidence introduced
- Significance explained
- Added to juror notebook

#### 5d. Closing Arguments
- Prosecution summarizes case
- Defense presents final arguments
- Prepares jury for deliberation

### 6. Deliberation Phase
**Location**: `DeliberationScreen.kt`

**Purpose**: Jury discussion and decision-making
- Group deliberation with AI jurors
- Individual conversations with specific jurors
- Note-taking and evidence review
- Voting process

**Key Features**:
- Group chat with all jurors
- Private conversations with individual jurors
- Interactive notebook for facts
- Voting rounds with results

### 7. Verdict Phase
**Location**: `VerdictScreen.kt`

**Purpose**: Present final outcome
- Announce unanimous verdict or mistrial
- Provide reasoning for decision
- Save case to history

**Possible Outcomes**:
- **GUILTY**: Unanimous guilty verdict
- **NOT GUILTY**: Unanimous not guilty verdict
- **MISTRIAL**: Failure to reach unanimous decision after 5 rounds

## Special Features Throughout Flow

### Interactive Notebook
Available during trial and deliberation phases:
- Automatically tags witnesses and evidence
- Allows user to add personal notes
- Organized by category (People, Evidence, Other)
- Accessible as overlay during trial

### Juror Profiles
- 11 unique AI jurors with distinct personalities
- Hidden biases that influence decision-making
- Initial leanings toward guilt or innocence
- Individual conversation capabilities

### Voting System
- Unanimous verdict required (all 12 jurors including user)
- Maximum 5 voting rounds before mistrial
- Individual juror votes tracked
- Results displayed after each round

### Question Submission
During witness testimony:
- User can submit questions to judge
- Judge determines admissibility
- Witness responds if question is allowed
- Adds interactivity to trial phase

## Error Handling in Flow

### Network Issues
- Retry mechanisms throughout
- Graceful degradation when AI unavailable
- Clear error messages and recovery options

### AI Failures
- Default juror generation if AI fails
- Fallback responses for critical failures
- User notification of any issues

### Data Persistence
- Case saved to history upon completion
- Progress preserved during interruptions
- Resume functionality for incomplete trials

## User Control Points

### Navigation
- Clear progression through phases
- Ability to review previous phases
- Access to settings throughout

### Interaction
- Multiple ways to engage with content
- Note-taking capabilities
- Question submission during trial

### Personalization
- Customizable juror profile
- Model selection
- Preference settings

This comprehensive trial flow provides an authentic jury experience while leveraging AI to create unique and engaging scenarios for each simulation.