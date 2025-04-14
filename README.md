# Splurg World
Splurg world is a simple life simulator where the Splurgs try to live and evolve
## What is a Splurg
A Splurg is an asexual Amoeba. \
Splurgs love to fight, eat each other and steal energy from each others Hives, especially the Aggressive ones
# Keyboard Controls
* ESC - Stops the Game
* Space - Pauses and resumes the game
* Left Arrow - Slows down the game
* Right Arrow - Speeds up the game
* Up Arrow - Resets game to its default speed
* Mouse click - While the game is Paused will give you stats on all the Splurgs in the area
# Spawning
Creating new Splurgs is called spawning and can happen in 2 ways
## Hives
Hives will spawn random Splurgs providing they have enough energy (hive.default.spawn.energy)
## Splurg Breeding
When 2 Splurgs meet, providing they have enough energy they will spawn a new Splurg.
This Splurg is mostly random, however if the two parent Splurgs share a common attribute value then the
new spawn will inherit that attribute value.
# Splurg Degradation
Over time Splurgs will tire and there is a random chance of their Strength or Toughness going down. \
This is controlled by the 'splurg.degradation' property, setting this to 0 turns this effect off.
# Properties
Various parameters can be set for the simulation. These are in the splurg.properties file in the resources folder.
* splurg.default.max.attribute = Maximum attribute value
* splurg.default.max.toughness = Maximum Toughness attribute value
* splurg.default.spawn.age = Maximum Splurg age (Not  currently used)
* splurg.default.base.health = Maximum Splurg health value
* splurg.default.size.multiplier = Multiplier to Size attribute for display value
* splurg.default.pathing.randomness = Chance of a Splurg changing direction of movement
* splurg.default.aggression.multiplier = Multiplier for Aggression attribute, controls distance at which a Splurg will move to attack an enemy Splurg
* splurg.default.foraging.multiplier = Multiplier for Foraging attribute, controls distance at which a Splurg will move feed on an enemy Splurg Hive
* splurg.default.feeding.volume = Controls how much energy a Splurg will drain from an enemy Hive per turn
* splurg.default.breeding.delay = Delay in turns before a Splurg can breed
* splurg.default.stuck.break = Change of a Splurg moving while in combat, prevents stuck combats
* splurg.degradation = Change of a Splurg suffering from an attribute degradation
* gui.frame.width = Default window width
* gui.frame.height = Default window height
* gui.mouse.click.detection.range = Area of Splurgs detected when the mouse is clicked on the view
* gui.hive.default.number = Default number of Hives, 2 or 4
* world.game.loop.sleeptime = Delay between each turn, effects simulation visual speed
* world.game.loop.pausedelaytime = Delay in refresh loop when game is paused
* hive.default.position.inset = Position of Hives from frame edges
* hive.default.setup.energy = Default starting energy in Hives at game start
* hive.default.spawn.rate = Turns between Hive spawnings
* hive.default.spawn.energy = Energy cost to spawn a Splurg
* hive.default.size = Visual size of Hives
* game.default.zombie = true/false, controls the creation of Zombie Splurgs
* game default.zombie.change = Change of a Splurg death creating a Zombie Splurg

# Building
Building Splurg World for deployment creates an executable jar file
> mvn clean package

# Technology Requirements
* Java 21
* Maven
