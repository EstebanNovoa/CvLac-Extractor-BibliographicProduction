Feature: Extract Researchers CV Data

  @Search_Cv_Researchers
  Scenario: Search Researcher CV into CVLac system
    Given I am in CvLAC home page
    When I select CVLac system
    Then I select educational level
    And I start to extract the researchers cv data



