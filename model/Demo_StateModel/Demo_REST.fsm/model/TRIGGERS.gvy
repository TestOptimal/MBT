// Model's Trigger script
import com.testoptimal.mscript.groovy.TRIGGER
import com.google.gson.Gson;
      
// --- Ctrl/Cmd-I to insert a trigger for a state or transition in the model.

@TRIGGER('MBT_START')
def 'MBT_START' () {
	$VAR.baseURL = "http://localhost:" + $UTIL.getPort() + "/api/v1/demo/insurance";
	$SYS.log('REST URL is: ' + $VAR.baseURL);
}


@TRIGGER('U8febef57')
def 'runREST: testScenario'() {
	try {
//		prem = io.restassured.RestAssured.given().auth().basic("admin", "admin")
		prem = $REST.given().auth().basic("admin", "admin")
		.param("age", $SYS.getData('Age'))
		.param("atFaultClaims", $SYS.getData('AtFaultClaims'))
		.param("goodStudent", $SYS.getData('GoodStudent'))
		.param("nonDrinker", $SYS.getData('NonDrinker'))
		.when().get($VAR.baseURL)
		.then().assertThat().statusCode(200).extract().path("premium");
		ExpectedPremium = $SYS.getData('ExpectedPremium');
		if (prem == ExpectedPremium) {
			$SYS.addReqPassed('Scenario_' + ($SYS.getCurDataSet().getCurrentRowIndex()+1), 'Passed, prem = ' + prem);
		}
      else throw new Exception ("Failed, expecting premium " + ExpectedPremium + ", received " + prem);
	}
	catch (Throwable e) {
		$SYS.addReqFailed('Scenario_' + ($SYS.getCurDataSet().getCurrentRowIndex()+1), e.getMessage() + ": " + (new Gson()).toJson($SYS.getCurDataSet().getCurRow()));
	}
}


//	io.restassured.RestAssured.given().auth().basic("yxl01", "123").when().get("http://localhost:8888/api/v1/runtime/model/list").then().body("modelList.findAll { it.modelName=='Demo_WebStore'}.size()", org.hamcrest.Matchers.equalTo(2));
//	io.restassured.RestAssured.given().auth().basic("yxl01", "123").when().get("http://localhost:8888/api/v1/runtime/model/list").then().body("modelList.size()", org.hamcrest.Matchers.greaterThan(5));
//body("modelList.modelName", org.hamcrest.Matchers.equalTo("anewmodeltesting"));
