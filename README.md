# VeloCLI Assistant

Android Studio / IntelliJ plugin for driving the VeloCLI Flutter project generator.

This repository contains only the plugin UI and integration code. It does not include
the VeloCLI binary or backend implementation.

## Features

- Tool window to run the `velocli` command with your selections
- Quick link to open the VeloCLI backend admin page
- Simple wizard to create a new project using VeloCLI

## Requirements

- Android Studio or IntelliJ IDEA with Android support
- VeloCLI binary installed and available on your machine
- Access to a running VeloCLI backend API

## Usage

1. Install the plugin from the JetBrains Marketplace.
2. Ensure the `velocli` binary is installed (for example via Homebrew).
3. Open the **VeloCli** tool window.
4. Set:
   - `velocli path` to the `velocli` executable
   - `VELOCLI_DATA_KEY` if required by your backend
   - Output directory, project name, organization, platforms, and blocks
5. Click **Generate** to run VeloCLI and scaffold the project.

## License

See the `LICENSE` file for licensing terms.
