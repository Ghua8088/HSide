    # HSIDE – Resurrecting Old Frameworks with Modern Twist AI code editor
    ![Banner](https://github.com/Ghua8088/HSide/blob/main/banner.png)
    HSIDE is a clean, fast, fully offline Java IDE built 100% in Java.  
    It features:
    - AI-powered autocomplete
    - A custom terminal
    - A sleek dark UI
    - Future support for an AI chat sidebar
    No tabs (yet) — but they’re coming.
    ---
    ## 🚀 Features
    - ✅ **Offline Java IDE** (fully standalone)
    - 🧠 **AI-assisted autocomplete** via local models (Qwen + Ollama)
    - 💻 **Integrated terminal** with full input/output support
    - 🌒 **Dark, distraction-free UI** — clean layout, minimalist design
    - 🧾 **Word & character counter**, UTF-16 encoding support
    - 🖱️ **Simple `.exe` installer** — run HSIDE instantly on Windows
    ---
    ## 🧠 Upcoming AI Features
    - 🤖 AI **Chat Sidebar** (Copilot-style assistant panel)
    - 🧠 Support for **code explanation, debugging, and refactoring**
    - 📎 AI context injection via file parsing
    - ✨ Embedded prompt editing / voice chat ideas
    ---
    ## 📦 Download
    [⬇️ Download HSIDE 1.0.1 (.exe)](https://github.com/Ghua8088/HSide/releases/latest)
    > One-click install. No setup required. Runs locally, no internet needed.
    ---

    ## 📁 Project Structure

    - `src/` → Java source code
    - `resources/` → Assets (icons, fonts, themes)
    - `lib/` → External dependencies (JARs)
    - `buildtools/` → Build outputs (`.exe`, `.jar`)
    - `manifest.txt` → Manifest for packaging

    ## 📂 Preferences Location

    HSide stores user preferences in:
    - **Windows**: `%USERPROFILE%\.hside\hside_preferences.json`
    - **Linux/macOS**: `~/.hside/hside_preferences.json`
    - **Fallback**: Current directory if home directory is not accessible
    ---
    ## 📸 Screenshots

    ### UI
    ![HSIDE](https://github.com/Ghua8088/HSide/blob/main/UI.png)
    ---
    ## ⚙️ Tech Stack
    -  **Java (Swing-based UI)**
    -  Local LLM backend via [Ollama](https://ollama.com/)
    -  Custom API integration for AI code completions
    - 📦 `.exe` built via jpackage + manual packaging

    ---

    ## 🛣️ Roadmap

    - [X] Tabbed document support
    - [X] AI Chat Sidebar (LLM-driven, local)(beta not agentic yet)
    - [X] add api tokens for better models (beta)
    - [X] added a theme manager
    - [X] Snippet and  Layout manager
    - [ ] Agentic Editing (Experimental)
    - [ ] Extensions (Out of reach for now)

    ---

    ## 🙌 Author

    Made with ☕ and 🚀 by [@Ghua8088](https://github.com/Ghua8088)  
    First full Java project — inspired by VS Code, powered by AI.

    ---

    ## 📜 License

    MIT License — free to use, modify, and distribute.

    ---

    > HSIDE isn’t just a Java IDE — it’s the start of a lightweight AI-native dev environment.
