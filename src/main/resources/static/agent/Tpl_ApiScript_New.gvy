// Use RestAssured REST API test (https://rest-assured.io/)
//
// Comment out codes not needed and change get() to other http method.
// You may use DATA script to instantiate java/groovy objects to post  
// to API, e.g. .body($UTIL.toJSON($VAR.order1))
//
// use "resp" object to extract and test response elements. 
for (Map<String,String> row: $DATASET.getDataRows()) {
	try {
		resp = $REST.given()
				.header('Content-Type', 'application/json')
				.auth().preemptive().basic('username', 'password')
				.pathParam('name', row.varName)
				.queryParam('name', row.varName)
				.formParam('name', row.varName)
				.body(row.varName)
			.when().get('http://yourApiURL')
			.then().assertThat().statusCode(row.ResponseCode);

		// your code to check response
		row._status = true;
		row._result = "test passed";
	}
	catch (Throwable e) {
		row._status = false;
		row._result = e.getMessage();
	}
	$DATASET.notifyClient(row);
}
