Feature: The player has died. They wish to return to the main menu.

  The player should be able to return to the main menu by pressing the escape key.

  Background:
    Given the game world just finished loading
    And there are no spawners

  Scenario: The player is still alive but presses escape by accident. The game does not return to menu.
    Given the player has 10 health
    When player presses key "ESCAPE"
    And the game runs for a single tick
    Then the game is not in the main menu

  Scenario: The player has died. They press escape and are returned to menu.
    Given the player has 0 health
    When the game runs for a single tick
    And player presses key "ESCAPE"
    Then the game now proceeds to the main menu after next tick
