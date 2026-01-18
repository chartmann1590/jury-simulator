# Frequently Asked Questions (FAQ)

## General Questions

### What is Jury Simulator?
Jury Simulator is an Android application that provides an immersive simulation of the jury duty experience. Using AI-powered conversations and realistic trial scenarios, users can experience the legal process from the perspective of a juror, participating in voir dire, witnessing trials, and engaging in deliberations with AI-controlled jurors.

### Who is Jury Simulator designed for?
The application is designed for:
- Law students and legal professionals
- Citizens preparing for jury duty
- Researchers studying jury behavior
- Anyone interested in legal processes
- Educational institutions teaching civics

### Is Jury Simulator free to use?
Yes, Jury Simulator is an open-source application available free of charge. However, you will need to run your own Ollama instance, which may incur costs depending on your hosting solution.

### What platforms does Jury Simulator support?
Jury Simulator is an Android application supporting devices running Android 8.0 (API level 26) or higher.

## Technical Questions

### What is Ollama and why do I need it?
Ollama is a tool that allows you to run large language models locally on your computer. Jury Simulator uses Ollama to generate realistic trial scenarios, conversations, and juror behaviors. You need to install and run Ollama separately to use the application.

### How do I install and set up Ollama?
1. Visit [ollama.ai](https://ollama.ai) and download Ollama for your operating system
2. Install and run Ollama on your computer
3. Pull a model: `ollama pull llama3` (or another model of your choice)
4. In the Jury Simulator app, enter your Ollama server URL (typically `http://localhost:11434` for local installations)

### What AI models are compatible with Jury Simulator?
Jury Simulator works with any model supported by Ollama, including:
- Llama 3, Llama 2
- Mistral
- Gemma
- Phi
- And many others

We recommend models that are good at following instructions and generating coherent text.

### Can I use Jury Simulator without an internet connection?
You need an internet connection to communicate with your Ollama instance. However, if you're running Ollama locally on the same device or local network, the data doesn't go to external servers.

## Trial Experience Questions

### How long does a typical trial simulation take?
Trial simulations vary in length based on user interaction, but typically last 15-45 minutes depending on the complexity of the case and how much time is spent in deliberation.

### Are the cases in Jury Simulator based on real cases?
No, all cases are AI-generated and completely fictional. The AI creates unique scenarios for each simulation to provide varied experiences.

### How many AI jurors are there in each simulation?
There are 11 AI-controlled jurors in each simulation, plus the user who acts as the 12th juror.

### Do the AI jurors have different personalities?
Yes, each AI juror has a unique personality, occupation, age, and hidden bias that influences their decision-making during deliberations.

### Can I have private conversations with individual jurors?
Yes, during the deliberation phase, you can have private conversations with individual jurors to understand their perspectives and potentially influence their opinions.

## Privacy and Security Questions

### Is my data secure when using Jury Simulator?
Yes, Jury Simulator prioritizes privacy:
- All AI processing occurs on your local Ollama instance
- No personal data is sent to external AI services
- Conversations remain private to your device
- Case history is stored locally on your device

### Does Jury Simulator collect any personal information?
No, Jury Simulator does not collect personal information. All data remains on your device, and no analytics or usage data is transmitted externally.

### Where is my case history stored?
Case history is stored locally on your device using Room database. It is not synced to any cloud service.

## Troubleshooting Questions

### Why can't I connect to my Ollama server?
Common connection issues and solutions:
- Ensure Ollama is running (`ollama serve`)
- Check the server URL format (should include `http://`)
- For Android emulators, use `http://10.0.2.2:11434`
- Verify the port number (default is 11434)
- Check firewall settings

### Why are AI responses taking too long?
Response times depend on:
- Your device's processing power
- The size of the AI model
- Complexity of the request
- Current system load
Consider using smaller models for faster responses.

### What should I do if the app crashes?
Try these steps:
1. Close and reopen the app
2. Check if your device has sufficient storage and memory
3. Ensure Ollama is still running
4. Restart your device if necessary
5. Update to the latest version of the app

### Why are some features not working properly?
- Ensure you have a compatible AI model installed
- Check that your Ollama instance is properly configured
- Verify your device meets the minimum requirements
- Update the application to the latest version

## Advanced Questions

### Can I customize the AI model used?
Yes, you can select different models from your Ollama instance in the app settings. Different models may provide varying levels of realism and response quality.

### How does the voting system work?
- All 12 jurors (11 AI + 1 human) must agree for a verdict
- If no unanimous decision is reached after 5 voting rounds, a mistrial is declared
- Each juror's hidden bias and personality influence their voting behavior

### Can I influence the AI jurors' decisions?
Yes, during deliberations, you can discuss the case with AI jurors and potentially influence their opinions through reasoned arguments and evidence review.

### How are the juror biases implemented?
Each AI juror has a hidden bias that subtly influences their decision-making without being explicitly stated. These biases might include trust in authority, skepticism of certain evidence types, or personal experiences that affect judgment.

## Development Questions

### Is Jury Simulator open source?
Yes, Jury Simulator is open source and available on GitHub. Contributions are welcome!

### How can I contribute to Jury Simulator?
You can contribute by:
- Reporting bugs and suggesting features
- Improving documentation
- Adding new features or fixing bugs
- Translating the application
- Providing feedback on user experience

See our [CONTRIBUTING.md](../CONTRIBUTING.md) file for detailed information.

### What technologies are used in Jury Simulator?
- Android (Kotlin)
- Jetpack Compose (UI)
- MVVM Architecture
- Room Database
- Retrofit (Networking)
- Moshi (JSON parsing)
- Ollama API (AI integration)

## Feature Requests

### Will there be iPhone/iOS support?
Currently, Jury Simulator is only available for Android. iOS support may be considered in the future based on demand and resources.

### Can I save and replay trials?
Trial history is saved locally on your device. While you cannot replay exact trials, you can review the case details and outcomes of previous simulations.

### Are there plans for multiplayer functionality?
Multiplayer functionality is a potential future feature. Currently, the focus is on the single-user experience with AI jurors.

## Getting Help

### Where can I get additional support?
- Check the documentation in the `docs/` folder
- Open an issue on the GitHub repository
- Join the community discussions
- Review the troubleshooting guide

### How do I report a bug?
Please report bugs by opening an issue on the GitHub repository with:
- Detailed description of the problem
- Steps to reproduce the issue
- Device information (model, Android version)
- Ollama version and model used
- Any relevant error messages