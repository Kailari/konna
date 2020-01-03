Feature: There is a simple counter on the HUD, counting the player kills.

  Background:
    Given the game world just finished loading
    And there are no spawners

  Scenario: The player has no kills. Counter reads 00
    Given the player has no kills
    When the game runs for a single tick
    And the game is rendered
    Then the kill counter has text "00"

  Scenario: The player has 69 kills. Counter reads 69
    Given the player has 69 kills
    When the game runs for a single tick
    And the game is rendered
    Then the kill counter has text "69"

  Scenario: The player has 420 kills. Counter reads 420
    Given the player has 420 kills
    When the game runs for a single tick
    And the game is rendered
    Then the kill counter has text "420"
