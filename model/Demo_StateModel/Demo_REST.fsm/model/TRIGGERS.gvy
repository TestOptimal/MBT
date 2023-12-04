// Model's Trigger script
import com.testoptimal.mscript.groovy.TRIGGER
import com.google.gson.Gson;

// --- Ctrl/Cmd-I to insert a trigger for a state or transition in the model.

@TRIGGER('MBT_START')
def 'MBT_START' () {
	$VAR.baseURL = "http://localhost:" + $UTIL.getPort() + "/api/v1/demo/insurance";
	$EXEC.log('REST URL is: ' + $VAR.baseURL);
	$EXEC.log($EXEC.datasets()*.dsName);
	$VAR.ds = $EXEC.dataset('PremiumScenarios');
	$EXEC.log("Dataset rows: " + $VAR.ds.rows.size());
	$EXEC.log("Dataset coloums: " + $VAR.ds.colList);
// 	api_response = io.restassured.RestAssured.given().auth().preemptive().basic("my id", "my password")
	api_response = io.restassured.RestAssured.given()
		.param("age", 32)
		.param("atFaultClaims", 1)
		.param("goodStudent", true)
		.param("nonDrinker", false)
		.when()
		.get($VAR.baseURL)
		.then()
		.extract().asPrettyString();
	$EXEC.log(api_response);
}


@TRIGGER('U8febef57')
def 'runREST: testScenario'() {
	try {
		prem = io.restassured.RestAssured.given()
			.param("age", $VAR.ds.get('Age'))
			.param("atFaultClaims", $VAR.ds.get('AtFaultClaims'))
			.param("goodStudent", $VAR.ds.get('GoodStudent'))
			.param("nonDrinker", $VAR.ds.get('NonDrinker'))
			.when().get($VAR.baseURL)
			.then().assertThat().statusCode(200).extract().path("premium");
		ExpectedPremium = $VAR.ds.get('ExpectedPremium').toDouble();
		if (prem == ExpectedPremium) {
			$EXEC.getCurTraverseObj().addReqPassed('Scenario_' + ($VAR.ds.idx+1), 'Passed, prem = ' + prem);
		}
      else throw new Exception ("Failed, expecting premium " + ExpectedPremium + ", received " + prem);
	}
	catch (Throwable e) {
		$EXEC.getCurTraverseObj().addReqFailed('Scenario_' + ($VAR.ds.idx+1), e.getMessage() + ": " + (new Gson()).toJson($VAR.ds.rows.get($VAR.ds.idx)));
	}
	$VAR.ds.next();
}
