for (Map<String,String> row: $DATASET.getDataRows()) {
	try {
		resp = $REST.@API_CODE@
		row._status = true;
		row._result = "test passed";
	}
	catch (Throwable e) {
		row._status = false;
		row._result = e.getMessage();
	}
	$DATASET.notifyClient(row);
}
