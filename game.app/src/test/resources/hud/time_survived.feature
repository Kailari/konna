Feature: The player can see how long they have survived on the HUD

  There is a simple timer on middle-top of the screen, telling the player how long they have been
  fighting on. The timer has HH:MM:SS format, where minutes and seconds tick up to 60. Minutes or
  hours are always visible. Minutes and seconds should be padded with leading zeros.

  Background:
    Given the game world just finished loading

  Scenario: The game starts. There is label for the timer with text "00:00:00" on the top of the screen.
    Given the current game time is at 0 seconds
    When the game is rendered
    Then there is a timer label on the top-middle of the screen
    And the timer label reads "00:00:00"
