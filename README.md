# Splurg World

Splurg World is a Java desktop life simulation. Colored colonies of small creatures called Splurgs move around a shared world, gather energy, fight, reproduce, age, and compete to survive. Optional zombie and herding rules change how the population behaves. The simulation runs continuously and displays the evolving world and population statistics.

![Splurg World screenshot](screen.png)

## What happens in the simulation

- Each colony has a hive, an energy reserve, and its own color. Hives spend energy to spawn Splurgs.
- Splurgs wander, look for rival hives to raid, and encounter rival Splurgs to fight. Foraging and aggression affect how far they detect targets.
- Energy supports healing, returning energy to a home hive, and breeding. Two nearby Splurgs from the same colony can breed when both have enough energy and are ready.
- Offspring receive randomized attributes, with matching parental values inherited. Strength and toughness contribute to size; size affects speed.
- Splurgs age and may lose strength or toughness through degradation. Zombies are optional; infected Splurgs become zombies when they die, and other deaths may also produce zombies. Zombies attack but do not gather or use energy.
- The game ends when at most one hive has living Splurgs or the live population exceeds the selected limit. Reaching the population limit can end a run before there is a sole surviving colony.

The simulation is stochastic. Set a fixed random seed to make a run repeatable; the default selects a fresh seed and writes it to the log.

## User controls

Use the **Game** menu to start, pause/resume, or stop a simulation. The **Settings** menu selects 2–4 starting hives, starting hive energy, a population limit, and whether herding or zombies are enabled. Settings take effect when a new game starts.

| Input | Action |
|---|---|
| `Space` | Pause or resume |
| `Esc` | Stop the simulation and show the end-game result |
| `Left Arrow` | Slow the simulation |
| `Right Arrow` | Speed up the simulation |
| `Up Arrow` | Reset to the configured default speed |
| Mouse click while paused | List nearby Splurgs in the clicked area. Click a Splurg in the list to open its health, energy, traits, and position; click the winner banner to dismiss it. |

## Technology

- Java 21 (Maven compiler source and target)
- Maven build and packaging
- Swing/AWT desktop UI and rendering
- SLF4J 2.0.12 with Logback 1.4.11
- Guava 31.1-jre and JSON-java 20231013
- JUnit Jupiter 5.10.0 and Mockito 5.10.0 for tests
- Java Platform Module System; module name: `Splurg.World`

## Build, test, and run

Install a JDK 21 and Maven 3.x, then run these commands from the repository root:

```shell
mvn clean package
```

The command compiles the application, runs the test suite, and packages the executable shaded JAR:

```shell
java -jar target/Splurg_World-1.0.jar
```

Run just the tests with `mvn test`. Surefire test reports are written to `target/surefire-reports/`. For a quicker local package without running tests, use `mvn -DskipTests package`.

## Implementation overview

- `org.jarec.Main` loads and validates configuration, initializes a random seed, and creates the Swing window.
- `org.jarec.game.GameLoop` owns the background thread, pause/stop behavior, and tick timing.
- `org.jarec.game.SimulationEngine` advances the simulation by one tick and produces metrics and a `WorldSnapshot` for rendering.
- `org.jarec.gui.WorldRenderer` renders that snapshot separately from the simulation update. Swing UI changes are dispatched on the event-dispatch thread.
- `org.jarec.game.resources.Splurgs` and `Hives` manage the current creature and hive collections. Creature behavior and attributes are implemented under `org.jarec.data`.
- `org.jarec.util.GameConfig` provides typed property access and validates configuration at startup. `PropertyHandler` preserves the application's existing configuration access API.
- `src/test/java` contains unit tests for attributes, movement, combat, hives, game-end checks, deterministic scenarios, configuration validation, and UI update dispatch.

The simulation currently uses shared singleton managers for its world state; the engine separates tick orchestration and rendering, but does not yet support multiple independent worlds in one process.

## Configuration and logs

Runtime settings and defaults are in `src/main/resources/splurg.properties`. Important groups include:

- `splurg.default.*`: creature attributes, movement, lifespan, energy, breeding, and degradation.
- `hive.default.*`: starting energy, spawn cost/rate, hive size, and positioning.
- `game.*`: default gameplay toggles and available population limits.
- `zombie.default.*`: zombie activation and combat behavior.
- `world.game.loop.*`: tick delays; `world.game.slow.tick.warning.ms`: slow-tick warning threshold.
- `world.random.seed`: use `-1` for a new seed at startup or a non-negative integer for a repeatable run.

The bundled properties file is loaded from the application classpath. Known numeric and boolean settings are validated at startup, including cross-setting constraints such as ordered population limits; invalid values stop startup with a descriptive error. Missing properties use the defaults supplied by the code. Runtime logs are appended to `logs/splurg.log` at INFO level and above; the console displays ERROR level messages and above.

## Development notes

- Keep simulation updates in `SimulationEngine` and draw from `WorldSnapshot` through `WorldRenderer`; Swing component updates belong on the event-dispatch thread.
- `SimulationEngine` currently reads and updates the singleton `Hives` and `Splurgs` managers. A separate engine instance does not create an isolated world.
- Run `mvn clean package` before submitting changes. Add or update focused JUnit tests for behavior changes; test reports are available under `target/surefire-reports/`.

### Inspecting Splurgs

Pause the simulation and click the game panel near the Splurgs you want to inspect. A list of nearby Splurgs appears; select one to open its details, including colony, health, energy, attributes, and position.
