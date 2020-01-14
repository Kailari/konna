Feature: Projectiles have knockback

  Background:
    Given the game world just finished loading
    And the world is blank

  Scenario: Projectile hits an enemy, the enemy is knocked back
    Given there is an enemy and a projectile heading towards it
    When the projectile hits the enemy
    Then the enemy should have moved slightly

  Scenario: Projectile hits a flying enemy, the trajectory of said enemy is changed
    Given there is an enemy flying in straight line and a projectile heading towards it
    When the projectile hits the enemy
    Then the trajectory of said enemy changes
