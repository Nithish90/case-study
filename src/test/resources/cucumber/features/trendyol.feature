
Feature: Trendyol

  Scenario: Valid login
    Given I am a trendyol user
    When I login to trendyol with valid credentials
    Then I should be logged in

  Scenario: Add product into basket
    Given I am a trendyol user
    And I login to trendyol with valid credentials
    When I open a menu 1 category
    And I open product detail
    And I add a product into the basket
    Then Product should be in the basket
    And I should logged out

  Scenario Outline: Check tabbed menu pages <tabbed-menu-no>
    Given I am a trendyol user
    And I login to trendyol with valid credentials
    When I open a menu <tabbed-menu-no> category
    Then Page should be loaded properly
    Examples:
      |tabbed-menu-no|
      |1             |
      |2             |
      |3             |
      |4             |
      |5             |
      |6             |
      |7             |
      |8             |
      |9             |
      |10            |