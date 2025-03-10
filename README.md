# MineCoreLib

MineCoreLib is a Java library for PaperMC plugins, providing utilities for config management and localization.

## Features
- **Configuration Management**: Easily load, save, and manage configuration files.
- **Localization Management**: Support for multiple languages and easy localization of strings.
- **Custom Logger**: Provides a custom logger for better logging and debugging.
- **ChatUtils**: Supports legacy and MiniMessage formats for chat messages.
- **Version Check**: Checks if the plugin is up to date by comparing the current version with the latest release version.


## Getting Started

### Prerequisites

- Java 8 or higher
- Gradle

### Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/tavstal/minecorelib.git
    cd minecorelib
    ```

2. Build the project using Gradle:
    ```sh
    ./gradlew build
    ```

3. Add the generated JAR file to your Minecraft plugin project.

### Usage

Create the main class that extends `PluginBase` to manage your plugin's configuration.

```java
public class ExamplePlugin extends PluginBase {
    public static ExamplePlugin Instance;
    private final PluginLogger _logger;
    private final PluginTranslator _translator;
    public static PluginLogger Logger() {
        return Instance.getCustomLogger();
    }
    public static PluginTranslator Translator() {
        return Instance.getTranslator();
    }
    public static FileConfiguration GetConfig(){
        return Instance.getConfig();
    }

    public ExamplePlugin() {
        super("ExamplePlugin",
                "1.0.0",
                "YourName",
                "https://github.com/YourName/ExamplePlugin/releases/latest",
                // ISO 639 three letter language codes
                new String[]{"eng", "ger", "spa", "hun"}
        );
        _logger = getCustomLogger();
        _translator = getTranslator();
    }

    @Override
    public void onEnable() {
        Instance = this;
        _logger.Info(String.format("Loading %s...", getProjectName()));

        // Generate config file
        saveDefaultConfig();

        // Load Localizations
        if (!_translator.Load())
        {
            _logger.Error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        _logger.Info(String.format("%s has been successfully loaded.", getProjectName()));
        if (!isUpToDate())
            _logger.Warn(String.format("A new version of %s is available! Download it at %s", getProjectName(), getDownloadUrl()));
    }

    @Override
    public void onDisable() {
        _logger.Info(String.format("%s has been successfully unloaded.", getProjectName()));
    }

    @Override
    protected String replacePlaceholders(String message) {
        String result = super.replacePlaceholders(message);
        // Example to add global placeholders in chat messages
        if (result.contains("%my_placeholder%")) {
            result = result.replace("%my_placeholder%", "result!");
        }
        return result;
    }

    /**
     * Reloads the plugin configuration and localizations.
     */
    public void reload() {
        _logger.Info(String.format("Reloading %s...", getProjectName()));
        _logger.Debug("Reloading localizations...");
        _translator.Load();
        _logger.Debug("Localizations reloaded.");
        _logger.Debug("Reloading configuration...");
        this.reloadConfig();
        _logger.Debug("Configuration reloaded.");
    }
}
```

#### Localization

Use the `PluginTranslator` class to manage localization.

```java
var translator = ExamplePlugin.Translator();
String message = translator.Localize(player, "welcome.message");
```

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the GNU License. See the `LICENSE` file for details.

## Contact

For any issues, please open an issue on GitHub or contact with me on discord.