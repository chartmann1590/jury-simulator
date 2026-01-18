# Troubleshooting

## Overview

This troubleshooting guide addresses common issues users may encounter while using the Jury Simulator application. It provides solutions for setup problems, AI integration issues, and general application errors.

## Common Issues

### 1. Connection Problems

#### Issue: Cannot connect to Ollama server
**Symptoms:**
- "Connection failed" error when testing connection
- Unable to proceed past setup screen
- Timeout errors

**Solutions:**
1. **Verify Ollama is running:**
   - Open terminal/command prompt
   - Run `ollama serve`
   - Keep the terminal window open

2. **Check server URL:**
   - For local development: `http://localhost:11434` or `http://10.0.2.2:11434` (for Android emulator)
   - For remote server: Use the IP address of the machine running Ollama
   - Ensure URL includes protocol (`http://` or `https://`)

3. **Network connectivity:**
   - Ensure device and Ollama server are on the same network
   - Check firewall settings
   - Verify port 11434 is not blocked

4. **Restart Ollama:**
   - Stop the Ollama service
   - Clear any cached data if needed
   - Restart the service

### 2. AI Model Issues

#### Issue: No models available
**Symptoms:**
- "No models found" message
- Model selection screen empty
- Cannot proceed to simulation

**Solutions:**
1. **Install a model:**
   - Open terminal
   - Run `ollama pull llama3` (or another model of your choice)
   - Wait for download to complete

2. **Verify model installation:**
   - Run `ollama list`
   - Confirm the model appears in the list

3. **Check model compatibility:**
   - Ensure the model supports text generation
   - Some specialized models may not work for this application

#### Issue: Poor AI responses
**Symptoms:**
- Generic or irrelevant responses
- Repetitive content
- Inappropriate behavior

**Solutions:**
1. **Try different models:**
   - Switch to a different model in settings
   - Recommended: Llama 3, Mistral, or Gemma models

2. **Adjust model parameters:**
   - If supported by your model, adjust temperature settings
   - Try different system prompts if available

3. **Update model:**
   - Pull the latest version of your model
   - Run `ollama pull <model-name>`

### 3. Application Crashes

#### Issue: App crashes during simulation
**Symptoms:**
- Application closes unexpectedly
- Force close messages
- ANR (Application Not Responding) errors

**Solutions:**
1. **Check device resources:**
   - Close other applications
   - Ensure sufficient RAM is available
   - Check available storage space

2. **AI processing time:**
   - Some complex prompts take longer to process
   - Be patient during AI generation
   - Avoid rapid consecutive requests

3. **Clear app data:**
   - Go to Settings > Apps > Jury Simulator
   - Clear cache and data
   - Restart the application

4. **Update application:**
   - Check for app updates
   - Install the latest version

### 4. Performance Issues

#### Issue: Slow response times
**Symptoms:**
- Long delays between user input and AI response
- Laggy UI interactions
- Frequent loading indicators

**Solutions:**
1. **Device performance:**
   - Close background applications
   - Restart the device
   - Check for system updates

2. **AI model optimization:**
   - Use smaller models for faster responses
   - Consider models optimized for speed vs. quality

3. **Network optimization:**
   - Ensure stable internet connection
   - Use local Ollama instance when possible
   - Avoid congested networks

4. **Application settings:**
   - Reduce complexity of prompts if possible
   - Limit concurrent operations

### 5. UI/UX Issues

#### Issue: Interface elements not responding
**Symptoms:**
- Buttons not responding to taps
- Scrolling issues
- Missing content

**Solutions:**
1. **Touch sensitivity:**
   - Clean device screen
   - Check for protective case interference
   - Adjust touch sensitivity in device settings

2. **App-specific fixes:**
   - Force stop the app and restart
   - Clear app cache
   - Update to the latest version

3. **Device compatibility:**
   - Verify device meets minimum requirements
   - Check for Android version compatibility

### 6. Data and Storage Issues

#### Issue: Case history not saving
**Symptoms:**
- Previous cases not appearing in history
- Lost simulation data
- Database errors

**Solutions:**
1. **Storage permissions:**
   - Verify app has storage permissions
   - Grant necessary permissions in settings

2. **Database integrity:**
   - Clear app data (note: this will remove all local data)
   - Reinstall the application if necessary

3. **Sync issues:**
   - Wait for operations to complete
   - Check for error messages indicating sync problems

## Advanced Troubleshooting

### Log Analysis
1. **Enable developer options:**
   - Go to Settings > About Phone
   - Tap Build Number 7 times
   - Return to Settings > Developer Options
   - Enable USB Debugging

2. **Access logs:**
   - Connect device to computer
   - Use `adb logcat` to view application logs
   - Look for error messages related to Jury Simulator

### Network Diagnostics
1. **Test connectivity:**
   - Use ping to test server connectivity
   - Verify DNS resolution
   - Check for proxy settings that might interfere

2. **Port availability:**
   - Use `telnet` or `nc` to test port 11434
   - Verify no other applications are using the same port

### Model-Specific Issues
1. **Memory requirements:**
   - Check if your model requires more RAM than available
   - Consider using quantized versions of models
   - Monitor system resources during operation

2. **Model configuration:**
   - Verify model parameters are appropriate
   - Check if context window is sufficient for application needs

## Prevention Tips

### Before Starting
- Ensure Ollama is properly installed and running
- Verify network connectivity
- Check device storage and memory
- Install compatible AI models

### During Use
- Be patient with AI generation times
- Maintain stable network connection
- Monitor device temperature
- Save progress regularly

### Maintenance
- Regularly update Ollama and models
- Clear app cache periodically
- Monitor storage usage
- Keep device software updated

## When to Seek Help

Contact support or community forums when:
- Following troubleshooting steps doesn't resolve the issue
- Encountering unexpected behavior not covered in this guide
- Experiencing repeated crashes or data loss
- Need assistance with advanced configuration

## Support Resources

- GitHub Issues: Report bugs and feature requests
- Community Forums: Get help from other users
- Documentation: Refer to the complete documentation
- Developer Contact: For critical issues

Remember to include detailed information when seeking help:
- Device model and Android version
- Ollama version and model used
- Specific error messages
- Steps to reproduce the issue