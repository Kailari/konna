Feature: The player has limited health and dies if taking too much damage.

  Scenario: The player should be able to survive for a short time, even if they do nothing.
    Given the game world just finished loading
    When player does nothing
    And the game runs for 5 seconds
    Then the player should still be alive.

  Scenario: The player should not be able to survive for a longer periods of time if they do nothing.
    Given the game world just finished loading
    When player does nothing
    And the game runs for 20 seconds
    Then the player should be dead.

  Scenario: The player should die if they are surrounded by follower enemies and do nothing.
    Given the game world just finished loading
    And the player is surrounded by follower enemies
    When player does nothing
    And the game runs for 10 seconds
    Then the player should be dead.
