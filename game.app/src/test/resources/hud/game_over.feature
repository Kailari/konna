Feature: When the player loses, a "Game Over" splash-text is displayed.

  The splash text should contain large text element with message "You Suck" or "Game Over".
  Additionally, a smaller print below it should be displayed, telling the player how to access
  the menu or how to restart the game.

  Background:
    Given the game world just finished loading
    And there are no spawners

  Scenario: The player is alive. Game Over -splash is hidden.
    Given the player has 10 health
    When the game runs for a single tick
    And the game is rendered
    Then the game over splash should be hidden.

  Scenario: The player is dead. Game Over -splash is visible.
    Given the player has 0 health
    When the game runs for a single tick
    And the game is rendered
    Then the game over splash should be visible

  Scenario: The player is dead. Game Over -splash contains message "Game Over"
    Given the player has 0 health
    When the game runs for a single tick
    And the game is rendered
    Then the game over splash should have text "Game Over".

  Scenario: The player is dead. Game Over -splash contains strings "<SPACE>" and "<ESC>"
    Given the player has 0 health
    When the game runs for a single tick
    And the game is rendered
    Then the game over splash should have text "<SPACE>" and "<ESC>".
