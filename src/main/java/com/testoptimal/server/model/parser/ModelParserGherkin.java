package com.testoptimal.server.model.parser;

import java.util.List;

public class ModelParserGherkin {

	/**
	 * 
	 * 
Feature: AccountSetup |
  Scenario: Account has sufficient funds | check_funds
    Given the account balance is $100 | Account_Balance
      And the card is valid
      And the machine contains enough money
    When the Account Holder requests $20
      And the Account Holder enters correct PIN
    Then the ATM should dispense $20 | Dispense
      And the account balance should be $80
      And the card should be returned
      
Feature: AccountSetup
  Scenario: Start_S1
    Given START
    When Script1
      And Script 2
    Then S1
    
  Scenario: Start_S2
    Given START
    When Script1
      And Script 2
    Then S2
      
  Scenario: S1_S2
    Given S1
    When Script1
      And Script 2
    Then S2
    
  Scenario: S2_End
    Given S2
    When Script1
      And Script 2
    Then END
    
    
	 * 
	 * @param args_p
	 * @throws Exception
	 */
	
	public static GherkinModel parse (List<String> gherkinList_p) {
		
		GherkinModel gm = new GherkinModel();
		GherkinScenario sc = null;
		String lastToken = "";
		boolean inScenario = false;
		for (String line: gherkinList_p) {
			line = line.trim();
			if (line.equals("")) continue;
			if (line.startsWith("Feature:") || line.startsWith("FEATURE:")) {
				gm.featureName = line.substring(8).trim();
				inScenario = false;
			}
			else if (line.startsWith("Scenario:") || line.startsWith("SCENARIO:")) {
				sc = new GherkinScenario();
				sc.scenarioName = line.substring(9).trim();
				gm.scenarioList.add(sc);
				lastToken = "";
				inScenario = true;
			}
			else if (inScenario) {
				int idx = line.indexOf(" ");
				String token = line.substring(0, idx);
				idx++;
				if (token.equalsIgnoreCase("Given")) {
					sc.givenState = line.substring(idx).trim();
				}
				else if (token.equalsIgnoreCase("When")) {
					sc.whenScriptList.add(line.substring(idx).trim());
				}
				else if (token.equalsIgnoreCase("And") && lastToken.equalsIgnoreCase("When")) {
					sc.whenScriptList.add(line.substring(idx).trim());
				}
				else if (token.equalsIgnoreCase("Then")) {
					sc.thenState = line.substring(idx).trim();
				}
				else {
					System.out.println("Ignored: " + line);
				}
				lastToken = token;
			}
			else {
				System.out.println("Ignored: " + line);
			}

		}
		return gm;
	}
	

}
