# AI Integration

## Overview

The Jury Simulator application integrates with Ollama, a local AI model runner, to provide realistic and dynamic trial experiences. This integration enables the application to generate unique cases, conduct realistic conversations with AI-controlled characters, and simulate authentic jury deliberations.

## Ollama Integration

### Architecture

The AI integration is built around the `OllamaRepository` class, which handles all communication with the Ollama API:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ Simulation      │◄──►│ Ollama          │◄──►│ Ollama Instance │
│ ViewModel       │    │ Repository      │    │ (Local/Remote)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         ▲                       ▲                       │
         │                       │                       │
┌─────────────────┐    ┌──────────────────┐             │
│ Prompt          │    │ API Service     │             │
│ Templates       │    │ (Retrofit)      │             │
└─────────────────┘    └──────────────────┘             │
         │                       │                       │
         └───────────────────────┴───────────────────────┘
```

### Configuration

The Ollama endpoint is configurable by the user:

- Default: `http://localhost:11434`
- Can be changed to any accessible Ollama instance
- Supports both local and remote Ollama servers

### API Endpoints Used

- `GET /api/tags` - Retrieve available models
- `POST /api/generate` - Generate AI responses

## Prompt Engineering

### Prompt Templates

The application uses carefully crafted prompt templates located in `PromptTemplates.kt`. These templates guide the AI to behave appropriately in different contexts:

#### Case Generation
```
"You are a legal case generator for a courtroom simulation. Generate a realistic criminal court case with the following details:..."
```

#### Voir Dire
```
"You are the presiding judge in the case: [case]. This is the jury selection phase (voir dire)..."
```

#### Trial Phases
- Opening statements
- Witness testimony
- Evidence presentation
- Closing arguments

#### Deliberation
```
"You are simulating a jury deliberation room with 11 other jurors (the user is the 12th)..."
```

### Context Management

The application maintains context for coherent conversations:

- Conversation history is passed to the AI for continuity
- Character information is preserved throughout interactions
- Trial state is maintained to ensure logical progression

## AI Characters

### Judge
- Conducts voir dire questioning
- Manages trial proceedings
- Makes rulings on juror questions

### Attorneys
- Prosecution and defense attorneys
- Deliver opening and closing statements
- Present witnesses and evidence

### Witnesses
- Generated dynamically for each trial
- Consistent testimony throughout the trial
- Responsive to juror questions when permitted

### AI Jurors
- 11 unique jurors with distinct personalities
- Hidden biases that influence decision-making
- Initial leanings toward guilt or innocence
- Individual conversation capabilities during deliberations

## Response Processing

### Parsing
- Structured responses are parsed for specific information
- Juror characteristics are extracted from AI responses
- Evidence items are identified and stored

### Validation
- Responses are validated for appropriateness
- Fallback mechanisms handle parsing failures
- Default values are used when AI responses are incomplete

## Error Handling

### Network Issues
- Connection timeouts with 300-second limits
- Retry mechanisms for failed requests
- Graceful degradation when AI service is unavailable

### AI Failures
- Default juror generation when AI fails
- Fallback responses for critical failures
- User notifications for AI-related issues

## Model Selection

### Available Models
- Users can select from models available on their Ollama instance
- Different models may provide varying levels of realism
- Model performance affects response times

### Recommendations
- Larger models typically provide more nuanced responses
- Smaller models offer faster response times
- Experiment with different models for optimal experience

## Privacy and Security

### Data Handling
- All AI processing occurs locally or on user-controlled infrastructure
- No personal data is sent to third-party AI services
- Conversations remain private to the user's device

### Network Security
- Configurable network security settings
- Cleartext HTTP allowed for local development
- Supports secure connections to remote Ollama instances

## Performance Considerations

### Response Times
- Depends on model size and device capabilities
- Average response times range from 2-10 seconds
- Complex deliberation phases may take longer

### Resource Usage
- Local AI processing requires significant device resources
- Larger models consume more memory and processing power
- Background processing minimizes UI impact

## Troubleshooting

### Common Issues
- AI service not responding: Verify Ollama is running
- Poor response quality: Try different models
- Slow responses: Consider using smaller models
- Parsing failures: May indicate model incompatibility

### Optimization Tips
- Use models optimized for instruction following
- Ensure sufficient device resources are available
- Maintain stable network connection to Ollama instance
- Monitor device temperature during extended use

The AI integration is the core differentiator of the Jury Simulator, providing unique and engaging experiences that would be impossible with static content.