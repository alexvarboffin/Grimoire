# Grimoire Implementation Details & System Requirements

This document provides a comprehensive overview of the features, architecture, and technical specifications implemented in the Grimoire project (part of the KotlinCodeGen suite). It is designed for AI-driven verification and replication.

## 1. Project Overview
**Grimoire** is a cross-platform (targeted at Desktop/JVM) automation and developer utility hub built with **Compose Multiplatform**. It integrates code generation, command execution orchestration, and Android debugging tools into a single GUI.

## 2. Technical Stack
- **Language:** Kotlin 2.x
- **UI Framework:** Compose Multiplatform (Desktop)
- **Navigation:** PreCompose (Route-based navigation)
- **Dependency Injection:** Koin
- **Persistence:** 
    - **Room Database:** For complex entities (Commands, History, Pipelines).
    - **DataStore (Preferences):** For application settings and global variables.
- **Process Management:** Java `ProcessBuilder` with real-time stream consumption.
- **Serialization:** Kotlinx Serialization (JSON).

## 3. Core Modules & Features

### A. Custom OpenAPI Generator (`my-codegen`)
- **Base:** Extends `KotlinClientCodegen`.
- **Target:** JVM-Ktor with Kotlinx Serialization.
- **Customizations:** 
    - Mustache templates for `api_repository.mustache` and `api_executor.mustache`.
    - Support for custom package naming and library overrides via CLI.
- **GUI Integration:** 
    - Path selection for Java EXE, Spec (JSON/YAML), and Output.
    - Real-time log console with copy-to-clipboard functionality.
    - "Rebuild" flag to trigger `gradlew build` on the generator before execution.

### B. Command Dashboard & Pipelines
- **Presets:** Grouped command templates stored in Room.
- **Macros:** Supports `{project_root}`, `{user_home}`, `{timestamp}`, `{date}`.
- **Global Variables:** Key-Value pairs defined in Settings, accessible via `{VAR_NAME}` in any command.
- **Dynamic Inputs:** Support for `{?prompt_name}` syntax which triggers a UI dialog for user input before execution.
- **Pipelines:** Sequential execution of presets. Automatic stop on failure of any step.
- **History:** Stores last 50 executions including exit codes, full logs, and timestamps.
- **Export/Import:** Full backup of presets and pipelines to a single JSON file.

### C. ADB Tools (Android Debugging)
- **Package Manager:** List apps, uninstall, clear data, force stop, extract APK to desktop.
- **ADB Viewer (Logcat):**
    - Real-time streamed logs with color-coding (Error, Warn, Info, Debug).
    - Filtering by text and log level (Verbose to Error).
    - Pause/Resume functionality without losing the stream buffer.
    - Screenshot tool: Captures device screen and saves to a selected folder.
    - Device Info: Parses `getprop` into a readable key-value table.
- **Device File Explorer:**
    - Browse device internal storage and root (if available).
    - Navigation: Folder exploration and breadcrumb navigation.
    - Transfer: Pull (Download) and Push (Upload) files/directories.
    - Management: Create directories and recursively delete files/folders via `rm -rf`.

### D. System Logic
- **MacroParser:** A robust utility for recursive string replacement of static, global, and dynamic macros.
- **AdbRepository:** Interface-driven management of ADB commands, providing Flows for Logcat and suspend functions for shell operations.
- **Migration Logic:** Automatic Room database migration (Current Version: 5) with specific SQL paths for jumping versions without data loss.

## 4. Database Schema (Room)
1. **`command_presets`**: Stores executable path, arguments, working dir, and grouping.
2. **`command_history`**: Stores execution results and full log outputs.
3. **`command_pipelines`**: Header for command chains.
4. **`pipeline_steps`**: Junction table for presets within pipelines with ordering.
5. **`list_generator_projects`**: (Existing) Logic for batch template processing.

## 5. Requirements for Replication
- **Environment:** Java 17+ (required for the generator and build system).
- **Environment Variables:** `ANDROID_HOME` or ADB in system `PATH`.
- **Dependencies:** 
    - `com.darkrockstudios:mpfilepicker` for cross-platform file dialogs.
    - `moe.tlaster:precompose` for ViewModel and Nav support.
    - `androidx.room:room-runtime` (KMP version).

## 6. Execution Logic for AI Agents
To execute a command through the system:
1. Fetch `GlobalVariables` from `SettingsDataStore`.
2. Parse the command string using `MacroParser.parse()`.
3. If `DYNAMIC_MACRO_REGEX` matches, interrupt for user input.
4. Initialize `ProcessBuilder`, setting the `directory` to the resolved `workingDir`.
5. Redirect Error Stream and consume Input Stream line-by-line to update the UI/History log.
