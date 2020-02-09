Feature: Enemies that have taken damage recently have health-bars rendered near them.

  Any enemy taking damage has health-bar rendered near it. However, if enemies do not take more
  damage for a while, the health-bar is hidden, being non-relevant to the player.

  Background:
    Given the game world just finished loading
    And the world is blank with 20 enemies with 3 hp each scattered about

  Scenario: No enemies have taken damage. No health-bars should be rendered.
    Given no enemies have taken damage
    When the game runs for a single tick
    And the game is rendered
    Then there should be no health-bars rendered

  Scenario: A single enemy takes damage. The health-bar is rendered.
    Given one enemy has taken damage recently
    When the game runs for a single tick
    And the game is rendered
    Then there should be one health-bar visible
    And the health-bar should be close to the damaged enemy

  Scenario: A bunch of enemies take damage. There are as many health-bars as damaged enemies.
    Given 6 enemies have taken damage recently
    When the game runs for a single tick
    And the game is rendered
    Then there should be 6 health-bars visible
    And each health-bar should be close to a damaged enemy

  Scenario: A bunch of enemies have taken damage some time ago. No health-bars are rendered.
    Given 6 enemies have taken damage 10 seconds ago
    When the game runs for a single tick
    And the game is rendered
    Then there should be no health-bars rendered

  Scenario: A bunch of have taken earlier but some time has passed. A few enemies take damage now. Only recently damaged enemies have health-bars.
    Given 6 enemies have taken damage 10 seconds ago
    And 2 enemies have taken damage recently
    When the game runs for a single tick
    And the game is rendered
    Then there should be 2 health-bars visible
    And each health-bar should be close to a damaged enemy
