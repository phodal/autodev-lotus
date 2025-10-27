# autodev-lotus

![Build](https://github.com/phodal/autodev-lotus/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/28853.svg)](https://plugins.jetbrains.com/plugin/28853)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/28853.svg)](https://plugins.jetbrains.com/plugin/28853)

<!-- Plugin description -->
AutoDev CodeLotus is a context-intelligence middleware layer designed to serve as the “context brain” for
AI-driven coding tools. Rather than being another end-user code-assistant, CodeLotus provides upstream capabilities —
rich semantic context, version-history awareness, graph‐based relationships and context-compression services — enabling
other AI coding tools to be far more effective and accurate. 
<!-- Plugin description end -->

Key Value Proposition:

* **MCP & A2A native** — built for model-to-tool and agent-to-agent interoperability, enabling seamless context sharing across AI assistants.
* **Deep semantic understanding** — parses ASTs, dependencies, and code graphs to deliver structured, high-fidelity context.
* **History-aware** — integrates Git evolution and refactor tracking for time-aware contextual insight.
* **Graph + compression engine** — extracts, ranks, and compresses relevant context into model-ready blocks.
* **Shared context fabric** — one unified context backbone for all AI coding tools and agents.

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "autodev-lotus"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/phodal/autodev-lotus/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
