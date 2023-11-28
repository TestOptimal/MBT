# TestOptimal_Build
TestOptimal Build Package

## Clone and Setup
Git ignore was set up to ignore a set of working and binary folders to reduce the workspace size. These directories must be manually created:
work/
lib/
build/
target/
h2db/

On windows, you must also install Graphviz and copy the files to lib/Graphviz folder in order for model graph function to work:
* bin/
* etc/
* fonts/
* include/
* lib/

You must also copy statsDB.mv.db from buildDef/shared/h2db/ folder to h2db/.

TestOptimal uses nist acts combinatorial package.  You must manually copy/download acts-cmd.jar into your local .mvn repo as shown below:

---
	C:\Users\yxl01\.m2\repository\nist\acts-cmd\2.92
		acts-cmd-2.92.jar	
---



