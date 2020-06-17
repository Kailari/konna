Feature: The player can move around the game world.

  Background:
    Given the game world just finished loading
    And there are no spawners
    And there are no obstacles
    And the player max speed is 10.0, acceleration is 50.0 and friction is 10.0

  Scenario Outline: The player presses input towards a direction. They should move.
    When player presses key "<key>"
    And the game runs for a second
    Then the player should have moved approximately 9 units total on the "<axis>" axis
    But the player should not have moved at all on the axis "<otherAxis>"

    Examples:
      | key | axis       | otherAxis  |
      | W   | vertical   | horizontal |
      | S   | vertical   | horizontal |
      | A   | horizontal | vertical   |
      | D   | horizontal | vertical   |

  Scenario Outline: The player character has a bit of momentum to keep them moving after key is released.
    Given the player has held "<key>" for 1.0 seconds
    When player releases key "<key>"
    And the game runs for a single tick
    And the game runs for a single tick
    And the game runs for a single tick
    Then the player should have moved on the "<axis>" axis while the game ran for the last time

    Examples:
      | key | axis       |
      | W   | vertical   |
      | S   | vertical   |
      | A   | horizontal |
      | D   | horizontal |

  Scenario Outline: The player character stops after releasing the input.
    Given the player has held "<key>" for 1.0 seconds
    And the player has released the key "<key>"
    And the game has run for 2 seconds
    When the game runs for a single tick
    Then the player should not have moved at all while the game ran for the last time

    Examples:
      | key |
      | W   |
      | S   |
      | A   |
      | D   |