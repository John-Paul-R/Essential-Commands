# CLAUDE.md - Essential Commands

**AI Assistant Guide for Essential Commands Development**

This document provides comprehensive guidance for AI assistants working on the Essential Commands codebase. It covers project structure, conventions, workflows, and key architectural patterns.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Codebase Structure](#codebase-structure)
3. [Development Setup](#development-setup)
4. [Build & Test Commands](#build--test-commands)
5. [Code Style & Conventions](#code-style--conventions)
6. [Git Workflow](#git-workflow)
7. [Architecture & Patterns](#architecture--patterns)
8. [Common Development Tasks](#common-development-tasks)
9. [Testing Guidelines](#testing-guidelines)
10. [Publishing & Release](#publishing--release)
11. [Key Files Reference](#key-files-reference)

---

## Project Overview

**Essential Commands** is a server-side Minecraft (Fabric) mod that adds configurable, permissions-backed utility commands for Fabric servers.

### Key Information

- **Language**: Java 21
- **Framework**: Fabric (Minecraft modding platform)
- **Build System**: Gradle 8.x with Fabric Loom
- **Target**: Minecraft 1.21.x
- **License**: MIT
- **Main Branch**: Version-specific branches (e.g., `1.21.x`)

### Core Features

- **Teleportation**: TPA requests, homes, warps, back, random teleport (RTP)
- **Utility Commands**: Spawn, workbench, anvil, flight, invulnerability, time control
- **Player Management**: Nicknames, AFK status
- **Permissions**: Full LuckPerms integration via Fabric Permissions API
- **Configuration**: 60+ configurable options
- **Localization**: Multi-language support via JSON language files

### Module Structure

```
essential_commands (root)
├── ec-core (submodule)         # Configuration framework & utilities
└── src                         # Main mod implementation
```

**ec-core** is a separate Gradle submodule providing:
- Configuration system (`Config<T>`, `@ConfigOption`)
- Expression evaluation framework
- Shared utilities
- Published as standalone Maven artifact (`dev.jpcode:eccore`)

---

## Codebase Structure

### Directory Layout

```
Essential-Commands/
├── .github/workflows/          # CI/CD pipelines (gradle.yml, publish.yml)
├── docs/                       # Documentation (hosted on GitHub Pages)
├── ec-core/                    # Core library submodule
│   ├── src/main/java/         # Configuration framework, utilities
│   └── build.gradle           # ec-core build configuration
├── gradle/                     # Gradle wrapper
├── src/
│   ├── main/
│   │   ├── java/com/fibermc/essentialcommands/
│   │   │   ├── EssentialCommands.java        # Mod entry point
│   │   │   ├── commands/                     # Command implementations
│   │   │   │   ├── bench/                    # Workbench-like commands
│   │   │   │   ├── utility/                  # Utility commands
│   │   │   │   ├── helpers/                  # Command helpers
│   │   │   │   ├── suggestions/              # Tab completion
│   │   │   │   └── exceptions/               # Custom exceptions
│   │   │   ├── config/                       # Configuration system
│   │   │   ├── playerdata/                   # Player data persistence
│   │   │   ├── teleportation/                # Teleport management
│   │   │   ├── types/                        # Domain models
│   │   │   ├── codec/                        # NBT serialization
│   │   │   ├── mixin/                        # Minecraft hooks
│   │   │   ├── access/                       # Mixin accessors
│   │   │   ├── events/                       # Custom events
│   │   │   ├── util/                         # Utilities
│   │   │   ├── text/                         # Localization
│   │   │   └── screen/                       # GUI handlers
│   │   └── resources/
│   │       ├── fabric.mod.json               # Mod metadata
│   │       ├── essential_commands.mixins.json # Mixin configuration
│   │       └── assets/essential_commands/
│   │           ├── lang/                     # Localization files
│   │           └── icon.jpg                  # Mod icon
│   └── test/java/                            # JUnit 5 tests
├── build.gradle                              # Root build configuration
├── gradle.properties                         # Version & dependency configuration
├── settings.gradle                           # Multi-module setup
├── .editorconfig                             # Code style rules
├── .checkstyle.xml                           # Checkstyle configuration
├── changelog.md                              # Release changelog
└── README.md                                 # User-facing documentation
```

### Key Packages

| Package | Purpose |
|---------|---------|
| `commands/` | All command implementations (40+ files) |
| `config/` | Configuration system (`EssentialCommandsConfig`) |
| `playerdata/` | Player data persistence (`PlayerData`, `PlayerDataManager`) |
| `teleportation/` | Teleport request handling (`TeleportManager`, `TeleportRequest`) |
| `types/` | Domain models (`MinecraftLocation`, `WarpStorage`, etc.) |
| `codec/` | Data serialization codecs for NBT format |
| `mixin/` | Fabric Mixins (12 files) for hooking into Minecraft |
| `access/` | Mixin accessors for adding data to vanilla classes |
| `events/` | Custom Fabric events (player join, death, damage, etc.) |
| `util/` | Utility classes (player utils, file I/O, conversions) |
| `text/` | Text rendering and localization (`ECText`) |

---

## Development Setup

### Prerequisites

1. **JDK 21** (required for Minecraft 1.21.x)
   - Use AdoptOpenJDK, Eclipse Temurin, or similar
2. **Git** for version control
3. **IDE** (IntelliJ IDEA recommended)
   - Install Minecraft Development plugin for IntelliJ

### Initial Setup

```bash
# Clone repository
git clone https://github.com/John-Paul-R/Essential-Commands.git
cd Essential-Commands

# Checkout appropriate branch
git checkout 1.21.x

# Build project (downloads dependencies)
./gradlew build

# Generate IDE files (IntelliJ)
./gradlew idea

# Generate IDE files (Eclipse)
./gradlew eclipse
```

### IDE Configuration

**IntelliJ IDEA:**
1. Open project as Gradle project
2. Install Minecraft Development plugin
3. Import `.editorconfig` settings (automatic)
4. Run `genSources` Gradle task for Minecraft source access

**EditorConfig:**
- Project uses `.editorconfig` for consistent formatting
- Ensure your IDE supports EditorConfig

---

## Build & Test Commands

### Gradle Tasks

```bash
# Clean build directory
./gradlew clean

# Build mod (compiles + packages JAR)
./gradlew build

# Build without tests
./gradlew build -x test

# Run tests only
./gradlew test

# Generate Minecraft sources for IDE
./gradlew genSources

# Run checkstyle
./gradlew checkstyleMain

# Publish to local Maven repository
./gradlew publishToMavenLocal

# Publish to Modrinth (requires API key)
./gradlew publishModrinth

# Publish to CurseForge (requires API key)
./gradlew publishCurseforge

# Publish to GitHub Packages
./gradlew publishMavenJavaPublicationToGitHubRepository
```

### Build Outputs

Compiled artifacts are located in:
```
build/libs/
├── essential_commands-{version}-mc{mc-version}.jar        # Main mod JAR
├── essential_commands-{version}-mc{mc-version}-sources.jar # Source JAR
└── ec-core-{version}.jar                                  # ec-core artifact
```

### Version Suffixes

The build system supports version suffixes via `VERSION_SUFFIX` environment variable:
```bash
# Build with git hash suffix
export VERSION_SUFFIX=$(git rev-parse --short HEAD)
./gradlew build
# Output: essential_commands-0.38.6-mc1.21.9+abc123.jar
```

---

## Code Style & Conventions

### Java Conventions

**EditorConfig Settings:**
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: 120 characters (max 200 for checkstyle)
- **Encoding**: UTF-8
- **Line Endings**: LF (Unix-style)
- **Final Newline**: Required

**Checkstyle Rules:**
- No trailing whitespace
- No multiple adjacent blank lines
- No tab characters in source files
- File length limits enforced

### Code Organization

**Imports:**
```java
// Import order (from .editorconfig)
import java.**;
import javax.**;
import *; // Other libraries
import com.mojang.**;
import net.minecraft.**;
import net.fabricmc.**;
import dev.jpcode.**;
import static *; // Static imports last
```

**Class Structure:**
```java
public class ExampleCommand implements Command<ServerCommandSource> {
    // 1. Static constants
    private static final Logger LOGGER = ...;

    // 2. Instance fields
    private final SomeManager manager;

    // 3. Constructor
    public ExampleCommand(SomeManager manager) { ... }

    // 4. Public methods
    @Override
    public int run(CommandContext<ServerCommandSource> context) { ... }

    // 5. Private helper methods
    private void helperMethod() { ... }
}
```

### Naming Conventions

- **Classes**: `PascalCase` (e.g., `TeleportManager`, `PlayerData`)
- **Methods**: `camelCase` (e.g., `initializePlayerData()`, `getTeleportRequest()`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_HOME_COUNT`, `TELEPORT_COOLDOWN`)
- **Variables**: `camelCase` (e.g., `playerData`, `homeName`)
- **Packages**: `lowercase` (e.g., `commands`, `playerdata`)

### Command Implementation Pattern

All commands should:
1. Implement `Command<ServerCommandSource>`
2. Return `0` for success (Brigadier convention)
3. Throw `CommandSyntaxException` for errors
4. Use `ECText` for localized messages
5. Check permissions via `ECPerms`
6. Use config snapshot for settings

**Example:**
```java
public class HomeCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String homeName = StringArgumentType.getString(context, "home_name");

        PlayerData playerData = ((ServerPlayerEntityAccess) player).ec$getPlayerData();
        MinecraftLocation home = playerData.getHome(homeName);

        if (home == null) {
            throw new SimpleCommandExceptionType(
                ECText.getInstance().getText("cmd.home.error.not_found", homeName)
            ).create();
        }

        PlayerTeleporter.requestTeleport(player, home);
        return Command.SINGLE_SUCCESS;
    }
}
```

### Configuration Access

**Always use snapshot for thread safety:**
```java
// Correct
EssentialCommandsConfigSnapshot config = CONFIG.getSnapshot();
if (config.ENABLE_HOME) { ... }

// Incorrect (direct access)
if (CONFIG.ENABLE_HOME.getValue()) { ... }
```

### Mixin Conventions

- Place mixins in `mixin/` package
- Name pattern: `{TargetClass}Mixin.java`
- Use minimal invasiveness
- Document injection points
- Prefer `@Inject` over `@Overwrite`

**Example:**
```java
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource source, CallbackInfo ci) {
        // Store death location for /back
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ((ServerPlayerEntityAccess) player).ec$setPreviousLocation(
            MinecraftLocation.from(player)
        );
    }
}
```

---

## Git Workflow

### Branch Strategy

- **Main Branches**: Version-specific (e.g., `1.21.x`, `1.20.x`, `1.19.x`)
- **Feature Branches**: `feature/description` or `fix/description`
- **Claude Branches**: `claude/claude-md-{session-id}` (for AI assistant work)

### Commit Messages

**Format:**
```
<type>: <description>

[optional body]

[optional footer]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `doc`: Documentation changes
- `refactor`: Code refactoring
- `test`: Test additions/changes
- `build`: Build system changes
- `chore`: Maintenance tasks

**Examples:**
```
feat: add /rtp center configuration option

Adds RTP_CENTER config to allow choosing spawn or custom coordinates
as the center point for random teleportation.

Closes #349
```

```
fix: prevent homes from not saving under certain conditions

Fixes a bug in PlayerDataManager where homes weren't saved if
the player logged out during a teleport delay.
```

### Pull Request Process

1. Create feature branch from version branch
2. Make changes with clear commits
3. Update changelog.md if applicable
4. Ensure tests pass (`./gradlew test`)
5. Push and create PR
6. CI runs automatically (build + tests)
7. Merge after review

---

## Architecture & Patterns

### Data Persistence

**Three-Tier Storage:**

1. **Player Data** (Per-player, persisted)
   - File: `world/playerdata/<UUID>.dat` (NBT format)
   - Manager: `PlayerDataManager` (singleton)
   - Data: Homes, /back location, nickname, cooldowns, AFK state
   - Codec: `PlayerData.CODEC` (Mojang DataFixers compatible)

2. **World Data** (Global, persisted)
   - File: `world/essentialcommands/world_data.dat` (NBT format)
   - Manager: `WorldDataManager extends PersistentState`
   - Data: Spawn location, warps

3. **Player Abilities** (Stateful)
   - System: `PlayerAbilityLib` (Ladysnake PAL)
   - Sources: `FLY_COMMAND`, `INVULN_COMMAND`, `AFK_INVULN`, `SLEEP_INVULN`

**Serialization Pattern:**
```java
public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance ->
    instance.group(
        Codec.STRING.fieldOf("uuid").forGetter(PlayerData::getUuid),
        NamedLocationStorage.CODEC.fieldOf("homes").forGetter(PlayerData::getHomes),
        // ...
    ).apply(instance, PlayerData::new)
);
```

### Event System

**Custom Fabric Events:**
```java
// Event registration
PlayerDeathCallback.EVENT.register((player, source) -> {
    MinecraftLocation deathLocation = MinecraftLocation.from(player);
    ((ServerPlayerEntityAccess) player).ec$setPreviousLocation(deathLocation);
});
```

**Available Events:**
- `PlayerConnectCallback` - Player joins
- `PlayerLeaveCallback` - Player quits
- `PlayerDeathCallback` - Player dies
- `PlayerDamageCallback` - Player takes damage
- `PlayerRespawnCallback` - Player respawns
- `PlayerActCallback` - Generic player action

### Mixin Access Pattern

**Adding data to vanilla classes:**

1. Create accessor interface:
```java
public interface ServerPlayerEntityAccess {
    PlayerData ec$getPlayerData();
    void ec$setPlayerData(PlayerData data);
}
```

2. Implement in mixin:
```java
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerEntityAccess {
    @Unique
    private PlayerData ec$playerData;

    @Override
    public PlayerData ec$getPlayerData() { return ec$playerData; }

    @Override
    public void ec$setPlayerData(PlayerData data) { this.ec$playerData = data; }
}
```

3. Use via casting:
```java
PlayerData data = ((ServerPlayerEntityAccess) player).ec$getPlayerData();
```

### Permission System

**LuckPerms Integration:**
```java
// Permission node format
"essentialcommands.<command>.<subcommand>"

// Check permission
if (!ECPerms.check(source, "essentialcommands.home.set")) {
    throw ECPerms.NOT_PERMITTED_EXCEPTION;
}

// Register permission
ECPerms.Registry.registerPermission("essentialcommands.home.set", PermissionLevel.ALL);
```

**Config Toggle:**
- Set `USE_PERMISSIONS_API=true` in config to enable
- Falls back to vanilla op levels if disabled

### Localization System

**ECText Usage:**
```java
// Simple translation
ECText.getInstance().getText("cmd.home.success")

// With interpolation
ECText.getInstance().getText("cmd.home.set.success", homeName, location)

// With styling
ECText text = ECText.getInstance();
Text message = text.getText("cmd.error", TextFormatType.Error);
```

**Language Files:**
- Location: `src/main/resources/assets/essential_commands/lang/`
- Format: JSON with key-value pairs
- Default: `en_us.json`
- Supports PlaceholderAPI integration

### Teleportation System

**Delayed Teleports:**
```java
QueuedTeleport teleport = new QueuedTeleport(
    player,
    targetLocation,
    CONFIG.getSnapshot().TELEPORT_DELAY
);

TeleportManager.getInstance().queueTeleport(player, teleport);
```

**Teleport Interruption:**
- Movement: `TELEPORT_INTERRUPT_ON_DAMAGED`
- Damage: `TELEPORT_INTERRUPT_ON_MOVEMENT`

**Cooldowns:**
- Managed by `TeleportManager`
- Configurable via `TELEPORT_COOLDOWN` option
- Per-command cooldown support

---

## Common Development Tasks

### Adding a New Command

1. **Create command class** in `commands/`:
```java
public class NewCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // Implementation
        return Command.SINGLE_SUCCESS;
    }
}
```

2. **Register in `EssentialCommandRegistry`**:
```java
dispatcher.register(
    literal("newcommand")
        .requires(ECPerms.require("essentialcommands.newcommand", PermissionLevel.MODERATORS_ONLY))
        .executes(new NewCommand())
);
```

3. **Add permission** in `ECPerms`:
```java
Registry.registerPermission("essentialcommands.newcommand", PermissionLevel.MODERATORS_ONLY);
```

4. **Add localization** in `lang/en_us.json`:
```json
{
  "cmd.newcommand.success": "Command executed successfully!",
  "cmd.newcommand.error": "An error occurred."
}
```

5. **Add config option** (if needed) in `EssentialCommandsConfig`:
```java
@ConfigOption
public final Option<Boolean> ENABLE_NEWCOMMAND = new Option<>(true);
```

6. **Add tests** in `src/test/java/`.

7. **Update documentation** in `docs/` and `README.md`.

### Adding Configuration Options

1. **Add to `EssentialCommandsConfig.java`**:
```java
@ConfigOption
public final Option<Integer> NEW_OPTION = new Option<>(100);
```

2. **Add description comment**:
```java
// Maximum allowed value for the new feature
@ConfigOption
public final Option<Integer> NEW_OPTION = new Option<>(100);
```

3. **Use snapshot pattern**:
```java
EssentialCommandsConfigSnapshot config = CONFIG.getSnapshot();
int value = config.NEW_OPTION;
```

4. **Document in `docs/Config-Documentation.md`**.

### Adding Player Data Fields

1. **Add field to `PlayerData.java`**:
```java
private String newField;
```

2. **Update codec**:
```java
public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance ->
    instance.group(
        // Existing fields...
        Codec.STRING.fieldOf("new_field").forGetter(PlayerData::getNewField)
    ).apply(instance, PlayerData::new)
);
```

3. **Add getter/setter**:
```java
public String getNewField() { return newField; }
public void setNewField(String value) {
    this.newField = value;
    markDirty(); // Important for persistence
}
```

4. **Increment data version** if schema changes:
```java
private static final int DATA_VERSION = 2; // Increment
```

5. **Add migration logic** in `PlayerDataDataFixer` if needed.

### Working with Mixins

**When to use mixins:**
- Hook into vanilla game events
- Add data to vanilla classes
- Modify vanilla behavior minimally

**When NOT to use mixins:**
- If Fabric API provides an event
- If a feature can be implemented without modifying vanilla code

**Best practices:**
- Use `@Inject` instead of `@Overwrite` when possible
- Keep mixins minimal and focused
- Document why the mixin is needed
- Test thoroughly (mixins can cause crashes)

### Updating Minecraft Version

1. **Update `gradle.properties`**:
```properties
minecraft_version=1.21.10
yarn_mappings=1.21.10+build.1
fabric_version=0.135.0+1.21.10
```

2. **Check dependency updates**:
- Fabric Loader
- Fabric API
- Placeholder API
- PlayerAbilityLib

3. **Update `game_versions` for publishing**:
```properties
game_versions=1.21.10
```

4. **Build and test**:
```bash
./gradlew clean build
```

5. **Update changelog.md**:
```markdown
## Essential Commands `v0.38.7` (mc 1.21.10)

- upgrade to Minecraft 1.21.10

--- --- ---
```

6. **Create new branch** if major version:
```bash
git checkout -b 1.22.x
```

---

## Testing Guidelines

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests ECTextTests

# Run tests with verbose output
./gradlew test --info

# Generate test report
./gradlew test
# View: build/reports/tests/test/index.html
```

### Writing Tests

**Test Structure:**
```java
@DisplayName("ECText Tests")
public class ExampleTests {

    @BeforeAll
    static void setUp() {
        // Initialize shared resources
    }

    @Test
    @DisplayName("Should correctly handle localization")
    void testLocalization() {
        // Arrange
        ECText text = ECText.getInstance();

        // Act
        Text result = text.getText("test.key");

        // Assert
        assertNotNull(result);
        assertEquals("Expected Text", result.getString());
    }
}
```

### Test Coverage

**Critical areas to test:**
- Command execution logic
- Data serialization/deserialization
- Configuration parsing
- Permission checks
- Teleport delay/cooldown logic
- Localization text rendering

**Use Fabric Loader JUnit:**
```java
testImplementation "net.fabricmc:fabric-loader-junit:${project.loader_version}"
```

---

## Publishing & Release

### Pre-Release Checklist

1. **Update version** in `gradle.properties`:
```properties
mod_version=0.38.7
```

2. **Update changelog.md** with release notes:
```markdown
## Essential Commands `v0.38.7` (mc 1.21.9)

- feat: add new feature X
- fix: resolve bug Y
- update dependency Z

--- --- ---
```

3. **Verify changelog format** (used by build script):
- Must contain version number from `mod_version`
- Sections separated by `--- --- ---`

4. **Run full build**:
```bash
./gradlew clean build
```

5. **Test in development environment**.

### Publishing Workflow

**Automated via GitHub Actions:**

The `publish.yml` workflow handles all publishing when manually triggered.

**Trigger via GitHub UI:**
1. Go to Actions → Publish Release
2. Select workflow inputs:
   - `abort_on_tag_conflict`: true/false
   - `publish_github_packages`: true/false
   - `publish_modrinth`: true/false
   - `publish_curseforge`: true/false
   - `publish_github_release`: true/false
3. Click "Run workflow"

**Workflow steps:**
1. Extracts version from `gradle.properties`
2. Creates git tag: `{mod_version}-mc{minecraft_version}`
3. Builds artifacts
4. Publishes to selected platforms
5. Creates GitHub Release with changelog

**Required secrets:**
- `MODRINTH`: Modrinth API token
- `CURSEFORGE`: CurseForge API token
- `GITHUB_TOKEN`: Provided automatically

### Manual Publishing

**GitHub Packages:**
```bash
export GITHUB_ACTOR=your-username
export GITHUB_TOKEN=your-token
./gradlew publishMavenJavaPublicationToGitHubRepository
```

**Modrinth:**
```bash
export MODRINTH=your-api-key
./gradlew publishModrinth
```

**CurseForge:**
```bash
export CURSEFORGE=your-api-key
./gradlew publishCurseforge
```

### Version Tagging

Tags follow format: `{mod_version}-mc{minecraft_version}`

**Example:**
- Version `0.38.6` for Minecraft `1.21.9`
- Tag: `0.38.6-mc1.21.9`

---

## Key Files Reference

### Configuration Files

| File | Purpose |
|------|---------|
| `gradle.properties` | Mod version, Minecraft version, dependencies |
| `build.gradle` | Build configuration, publishing setup |
| `settings.gradle` | Multi-module configuration |
| `.editorconfig` | Code style rules for all editors |
| `.checkstyle.xml` | Checkstyle validation rules |
| `changelog.md` | Release notes (used by publishing) |

### Source Files

| File | Purpose |
|------|---------|
| `EssentialCommands.java` | Mod entry point, initialization |
| `EssentialCommandsConfig.java` | All configuration options |
| `EssentialCommandRegistry.java` | Command registration |
| `PlayerDataManager.java` | Player data persistence |
| `WorldDataManager.java` | World data persistence |
| `TeleportManager.java` | Teleport request handling |
| `ECPerms.java` | Permission system |
| `ECText.java` | Localization system |

### Resource Files

| File | Purpose |
|------|---------|
| `fabric.mod.json` | Mod metadata (name, version, dependencies) |
| `essential_commands.mixins.json` | Mixin configuration |
| `assets/essential_commands/lang/*.json` | Localization files |
| `config/EssentialCommands.properties` | Runtime configuration (generated) |

### Documentation

| File | Purpose |
|------|---------|
| `README.md` | User-facing documentation |
| `docs/Config-Documentation.md` | Configuration reference |
| `docs/List-of-Commands-&-Permissions.md` | Commands & permissions list |
| `docs/Permissions-Quickstart.md` | Permission setup guide |
| `CLAUDE.md` | AI assistant development guide (this file) |

---

## Important Notes for AI Assistants

### When Making Changes

1. **Always read existing code** before making changes
2. **Follow established patterns** in the codebase
3. **Update tests** when adding/modifying features
4. **Update documentation** (comments, docs/, README.md)
5. **Respect .editorconfig** settings
6. **Check changelog format** before updating
7. **Test builds locally** before committing

### Common Pitfalls to Avoid

1. **Don't** modify ec-core unless absolutely necessary
2. **Don't** use `CONFIG.OPTION.getValue()` directly (use snapshot)
3. **Don't** forget to call `markDirty()` on data changes
4. **Don't** use `@Overwrite` mixins without justification
5. **Don't** hardcode text (use ECText localization)
6. **Don't** skip permission checks on commands
7. **Don't** forget to register permissions in `ECPerms`

### Best Practices

1. **Use existing utilities** (PlayerUtilities, TextUtil, etc.)
2. **Prefer Fabric events** over mixins when available
3. **Write descriptive commit messages**
4. **Keep methods focused** (single responsibility)
5. **Add JavaDoc** for public APIs
6. **Handle edge cases** (null checks, empty collections)
7. **Use immutable patterns** where possible

### Getting Help

- **Issues**: https://github.com/John-Paul-R/Essential-Commands/issues
- **Discord**: https://discord.jpcode.dev/essential-commands
- **Documentation**: https://john-paul-r.github.io/Essential-Commands/
- **Fabric Wiki**: https://fabricmc.net/wiki/

---

## Changelog

**Last Updated**: 2025-11-16
**Current Version**: v0.38.6 (mc 1.21.9)
**Maintained By**: John-Paul-R (JP79194)

This document will be updated as the project evolves. If you notice outdated information, please submit a PR or open an issue.
