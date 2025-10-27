# AI Core Module

The `ai-core` module provides a unified interface for integrating multiple LLM (Large Language Model) providers into the Lotus Chat application. It uses the LangChain4j framework to support various AI providers.

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

4. **LangChain4jAIClient** - Implementation using LangChain4j framework
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

// Save AI configuration with model selection
viewModel.onAIConfigSaved(
    provider = LLMProvider.OPENAI,
    apiKey = "your-api-key",
    model = "gpt-4o"  // or use custom model name
)

// Check if AI is configured
val isConfigured = viewModel.isAIConfigured.collectAsState()
```

### Available Models

Each provider has a set of recommended models:

```kotlin
// Get available models for a provider
val openaiModels = LLMConfig.getAvailableModels(LLMProvider.OPENAI)
// Returns: [o4-mini, o3-mini, gpt-4o, gpt-4o-mini]

val claudeModels = LLMConfig.getAvailableModels(LLMProvider.CLAUDE)
// Returns: [claude-3-5-sonnet-latest, claude-3-5-haiku-latest, ...]

// Get default model for a provider
val defaultModel = LLMConfig.getDefaultModel(LLMProvider.CLAUDE)
// Returns: claude-3-5-sonnet-latest
```

### Custom Models

You can also use custom model names (useful for new models or special configurations):

```kotlin
val config = LLMConfig(
    provider = LLMProvider.OPENAI,
    apiKey = "your-api-key",
    model = "gpt-4-turbo-2024-04-09"  // Custom model name
)
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

### Setting Up API Keys and Models

1. Open the Chat App
2. Click the Settings icon (⚙️) in the header
3. Select your preferred LLM provider
4. Choose a model from the dropdown list, or select "Custom Model..." to enter a custom model name
5. Enter your API key
6. Click Save

Your API key and model configuration are stored locally in `~/.lotus/ai/llm_config.properties` and never sent to external servers.

### Model Selection

- **Predefined Models**: Each provider has a curated list of recommended models. Select from the dropdown for quick access.
- **Custom Models**: If you need to use a newer model or a specific model variant, select "Custom Model..." and enter the model name directly.
- **Default Models**: If no model is specified, the system uses the latest recommended model for the selected provider.

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

- LangChain4j (0.31.0+)
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

