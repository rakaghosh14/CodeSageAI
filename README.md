# CodeSageAI 🚀

An AI-powered Android application that helps developers write, understand, review, and improve code using intelligent assistance. Built with modern Android development tools and clean architecture principles, CodeSageAI provides a seamless coding companion directly on your mobile device.

## ✨ Features

* 🤖 AI-powered code generation
* 🔍 Intelligent code review and suggestions
* 📝 Syntax highlighting for multiple programming languages
* 💬 Interactive AI chat assistant
* 📂 Code snippet management
* 📚 Conversation history
* ⚡ Fast and responsive Jetpack Compose UI
* 🌙 Modern Material Design interface

## 🛠️ Tech Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose
* **Architecture:** MVVM
* **Database:** Room Database
* **Networking:** Retrofit
* **Asynchronous Programming:** Kotlin Coroutines
* **API Integration:** Google Gemini API & Judge0 API
* **Build System:** Gradle (Kotlin DSL)

## 📂 Project Structure

```
CodeSageAI
│
├── app
│   ├── src
│   │   ├── main
│   │   │   ├── java/com/example/codesageai
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/      # Room database, Entities, DAOs
│   │   │   │   │   ├── remote/     # Retrofit endpoints and Models
│   │   │   │   │   └── repository/ # CodeReview and Settings Repositories
│   │   │   │   ├── theme/          # Custom Neon Obsidian styles
│   │   │   │   ├── ui/             # Jetpack Compose Screens and view components
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── Navigation.kt
│   │   │   │   └── NavigationKeys.kt
│   │   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── gradle/
└── build.gradle.kts
```

## 🚀 Getting Started

### Prerequisites

* Android Studio
* JDK 17+
* Android SDK
* Gemini API Key (Optional, Mock Mode is active by default)

### Installation

1. Clone the repository
```bash
git clone https://github.com/rakaghosh14/CodeSageAI.git
```
2. Open the project in Android Studio.
3. Sync Gradle.
4. Run the application on an emulator or Android device.

## 📄 License

This project is intended for educational and learning purposes.

## 👨‍💻 Author

**Raka Ghosh**

* GitHub: https://github.com/rakaghosh14
