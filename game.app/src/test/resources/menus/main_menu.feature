Feature: When the game initially starts, player is greeted with a Title Screen.

  The title screen has buttons for accessing the game itself and exiting the application.

  Scenario: The user looks at the main menu. There are buttons for playing the game, shutting down the game and a title.
    Given the main menu has just loaded
    When the game runs for a single tick
    Then there is a title with text "Konna"
    And there is a button with text "Quit"
    And there is a button with text "Play"

  Scenario: The user no longer wishes to play and clicks "Quit" button. The game shuts down.
    Given the main menu has just loaded
    And the player clicks the "Quit" button
    Then the game should close

  Scenario: The user wishes to start playing and clicks "Play" button. The game should now start.
    Given the main menu has just loaded
    And the player clicks the "Play" button
    Then the game should start
