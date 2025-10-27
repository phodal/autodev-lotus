# AI Core Module

The `ai-core` module provides a unified interface for integrating multiple LLM (Large Language Model) providers into the Lotus Chat application. It uses the Koog framework to support various AI providers.

## Supported LLM Providers

- **DeepSeek** - High-performance open-source model
- **OpenAI** - GPT-4 and other OpenAI models
- **Claude** - Anthropic's Claude models
- **Gemini** - Google's Gemini models

## Architecture

### Core Components

1. **LLMConfig** - Configuration data class for LLM settings
   - Provider selection
   - API Key management
   - Model selection
   - Temperature and token limits

2. **LLMConfigManager** - Manages configuration persistence
   - Loads/saves configurations from local storage
   - Monitors configuration changes
   - Stores configs in `~/.lotus/ai/llm_config.properties`

3. **AIClient** - Interface for AI communication
   - `sendMessage()` - Send a message and get a response
   - `streamMessage()` - Stream responses for real-time updates
   - `isConfigured()` - Check if client is properly configured

4. **KoogAIClient** - Implementation using Koog framework
   - Supports all LLM providers
   - Handles API communication
   - Error handling and fallbacks

5. **AIServiceFactory** - Factory for creating AI service instances
   - Singleton pattern for AI client management
   - Configuration initialization
   - Client lifecycle management

## Usage

### In ChatViewModel

```kotlin
// Initialize with config manager
val configManager = LLMConfigManager()
val viewModel = ChatViewModel(coroutineScope, repository, configManager)

// Save AI configuration
viewModel.onAIConfigSaved(LLMProvider.DEEPSEEK, "your-api-key")

// Check if AI is configured
val isConfigured = viewModel.isAIConfigured.collectAsState()
```

### In ChatRepository

The repository automatically uses the configured AI service:

```kotlin
// If AI is configured, uses real AI service
// Otherwise, falls back to simulated responses
private suspend fun simulateAIResponse(userMessage: String) {
    val aiClient = AIServiceFactory.getAIClient()
    val response = if (aiClient?.isConfigured() == true) {
        aiClient.sendMessage(userMessage)
    } else {
        aiResponseGenerator.generateAIResponse(userMessage)
    }
}
```

## Configuration

### Setting Up API Keys

1. Open the Chat App
2. Click the Settings icon (⚙️) in the header
3. Select your preferred LLM provider
4. Enter your API key
5. Click Save

Your API key is stored locally in `~/.lotus/ai/llm_config.properties` and never sent to external servers.

### Environment Variables

You can also set API keys via environment variables:

```bash
# DeepSeek
export DEEPSEEK_API_KEY=your-api-key

# OpenAI
export OPENAI_API_KEY=your-api-key

# Claude (Anthropic)
export ANTHROPIC_API_KEY=your-api-key

# Gemini (Google)
export GOOGLE_API_KEY=your-api-key
```

## Future Enhancements

This module is designed to be independent and can be extracted into a standalone IDEA plugin or application. Future improvements include:

- Streaming response support
- Model-specific configurations
- Request/response logging
- Rate limiting and caching
- Multi-provider fallback strategies
- Custom prompt templates

## Dependencies

- Koog Framework (0.1.0+)
- Kotlin Coroutines (1.10.1+)
- Kotlin Standard Library

## Testing

To test the AI integration:

1. Configure an API key through the UI
2. Send a message in the chat
3. The response should come from the configured AI provider
4. If no API key is configured, responses will be simulated

## Troubleshooting

### "AI client is not configured"
- Open the Settings dialog and configure your API key
- Ensure the API key is valid for the selected provider

### "Failed to get response from [Provider]"
- Check your internet connection
- Verify your API key is correct
- Check if the API service is available
- Review the error message for more details

### Configuration not persisting
- Ensure `~/.lotus/ai/` directory is writable
- Check file permissions on `llm_config.properties`

