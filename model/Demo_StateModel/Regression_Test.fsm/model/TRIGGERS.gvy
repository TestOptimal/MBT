// Model's Trigger script
import com.testoptimal.exec.mscript.TRIGGER
import com.google.gson.Gson;
import groovy.json.JsonOutput;
import com.testoptimal.server.model.RunRequest;

import io.restassured.RestAssured.*
import io.restassured.matcher.RestAssuredMatchers.*
import org.hamcrest.Matchers.*


// --- Ctrl/Cmd-I to insert a trigger for a state or transition in the model.

@TRIGGER('MBT_START')
def 'MBT_START' () {
	$VAR.url_runmodel = "http://localhost:" + $UTIL.getPort() + "/api/v1/runtime/model/run";
	$VAR.url_status = "http://localhost:" + $UTIL.getPort() + "/api/v1/runtime/model/{modelName}/session/{mbtSessID}/status";
	$VAR.url_stats = "http://localhost:" + $UTIL.getPort() + "/api/v1/stats/model/{modelName}/session/{mbtSessID}";	
	$VAR.userid = "lin@to.com";
	$VAR.password = "test";
}

@TRIGGER('U05450e42')
def 'LoginSubModel' () {
	$VAR.runReq = [modelName: "DEMO_Login_SubModel", options: [:]];
}

@TRIGGER('Ua80e9180')
def 'LoginSubModel_start: run_gen_only'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = true;
}


@TRIGGER('Ube59826d')
def 'LoginSubModel_start: run_model'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = false;
}

@TRIGGER('Uaceae12f')
def 'wait_for_complete' () {
	$VAR.runReq.statDesc = $VAR.runReq.modelName + "-" + $VAR.runReq.mbtMode + "-" + ($VAR.runReq.options.generateOnly?'gen':'run');
	submit_run_request($VAR.runReq);
	$EXEC.setPathName($VAR.runReq.statDesc);
	$EXEC.log 'checing status for model/' + $VAR.runReq.modelName + '/session/' + $VAR.runReq.mbtSessID;
	def count = 0;
	while(count <= 5) {
		println "$count"
		sleep(1000)
		api_response = io.restassured.RestAssured.given().auth().preemptive().basic($VAR.userid, $VAR.password)
			.header('Content-Type', 'application/json')
			.pathParam('modelName', $VAR.runReq.modelName)
			.pathParam('mbtSessID', $VAR.runReq.mbtSessID)
			.when()
			.get($VAR.url_status)
			.then()
			.assertThat().statusCode(200);
		$EXEC.log('monitor: ' + api_response.extract().asPrettyString());
		if (api_response.extract().path("execStatus")=="NOT-RUNNING") {
			// model execution completed (hence status returns NOT-RUNNING
			break;
		}
		count++
	}
	$EXEC.log 'retrieving stats for model/' + $VAR.runReq.modelName + '/session/' + $VAR.runReq.mbtSessID;
	stats = io.restassured.RestAssured.given().auth().preemptive().basic($VAR.userid, $VAR.password)
		.header('Content-Type', 'application/json')
		.pathParam('modelName', $VAR.runReq.modelName)
		.pathParam('mbtSessID', $VAR.runReq.mbtSessID)
		.when()
		.get($VAR.url_stats)
		.then()
		.extract().path("execSummary");
	$EXEC.log('stats: ' + JsonOutput.prettyPrint(JsonOutput.toJson(stats)));
	if (stats!=null && stats.status=="passed") {
		$EXEC.getCurTraverseObj().addReqPassed($VAR.runReq.modelName, 'model exec successful');
	}
	else {
		$EXEC.getCurTraverseObj().addReqFailed($VAR.runReq.modelName, 'model exec failed: ' + JsonOutput.prettyPrint(JsonOutput.toJson(stats)));
	}
}

def submit_run_request(runReq) {
	$EXEC.log 'submitting run request for model ' + $VAR.runReq.modelName;
	api_response = io.restassured.RestAssured.given().auth().preemptive().basic($VAR.userid, $VAR.password)
		.body(JsonOutput.toJson(runReq))
		.header('Content-Type', 'application/json')
		.when()
		.post($VAR.url_runmodel)
		.then()
		.assertThat().statusCode(200);
	$EXEC.log api_response.extract().asPrettyString();
	runReq.mbtSessID = api_response.extract().path("mbtSessID");
	runReq.monitorURL = api_response.extract().path("urlMonitor");
	runReq.statsURL = api_response.extract().path("urlStats");
	if (runReq.mbtSessID == null) {
		$EXEC.getCurTraverseObj().addReqFailed(runReq.modelName, 'failed to run');
	}
}


@TRIGGER('U078d8a76')
def 'LoginMainModel' () {
	$VAR.runReq = [modelName: "DEMO_Login_MainModel", options: [:]];
}


@TRIGGER('Ufadbfaf4')
def 'LoginMainModel_start: run_model'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = false;
}

@TRIGGER('Ua064cf86')
def 'LoginMainModel_start: run_gen_only'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = true;

}


@TRIGGER('U1e6d4c38')
def 'WebAppAutomation' () {
	$VAR.runReq = [modelName: "DEMO_WebAppAutomation", options: [:]];
}

@TRIGGER('Ud7abf326')
def 'WebAppAutomation_start: run_model'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = false;
}

@TRIGGER('U4cc77289')
def 'WebAppAutomation_start: run_gen_only'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = true;
}


@TRIGGER('Udcf06b97')
def 'WorkflowTesting' () {
	$VAR.runReq = [modelName: "DEMO_WorkflowTesting", options: [:]];
}

@TRIGGER('Ue7750c17')
def 'WorkflowTesting_start: run_gen_only'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = true;
}

@TRIGGER('U83c7255d')
def 'WorkflowTesting_start: run_model'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = false;
}


@TRIGGER('U7464e09a')
def 'BehaviorDrivenTesting' () {
	$VAR.runReq = [modelName: "DEMO_Behavior_Scripting", options: [:]];
}

@TRIGGER('U606e5068')
def 'BehaviorDrivenTesting_start: run_model'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = false;
}

@TRIGGER('Uf66ebabb')
def 'BehaviorDrivenTesting_start: run_gen_only'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = true;
}


@TRIGGER('Ubdd9d5d3')
def 'DEMO_Guard' () {
	$VAR.runReq = [modelName: "DEMO_Guard", options: [:]];
}

@TRIGGER('Ua40ae68f')
def 'Guard_start: run_model'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = false;
}

@TRIGGER('U7159f8c0')
def 'Guard_start: run_gen_only'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = true;
}


@TRIGGER('U37ea4339')
def 'DEMO_OutputTestCases' () {
	$VAR.runReq = [modelName: "DEMO_OutputTestCases", options: [:]];
}

@TRIGGER('U1bfb219f')
def 'DEMO_OutputTestCases: run_model'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = false;
}

@TRIGGER('Ucb58e666')
def 'DEMO_OutputTestCases: run_gen_only'() {
	$VAR.runReq.mbtMode = "Optimal";
	$VAR.runReq.options.generateOnly = true;
}
