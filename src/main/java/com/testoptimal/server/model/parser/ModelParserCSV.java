/***********************************************************************************************
 * Copyright (c) 2009-2024 TestOptimal.com
 *
 * This file is part of TestOptimal MBT.
 *
 * TestOptimal MBT is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * TestOptimal MBT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with TestOptimal MBT. 
 * If not, see <https://www.gnu.org/licenses/>.
 ***********************************************************************************************/

package com.testoptimal.server.model.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.TransitionNode;

public class ModelParserCSV {
	private int FromIdx = -1;
	private int EventIdx = -1;
	private int ToIdx = -1;
	private int WeightIdx = -1;
	private int TraverseTimesIdx = -1;
	private int ScriptIdx = -1;
	private int startLineIdx = 0;
	
	private boolean valid = false;
	private String errMsg = "Not parsed";
	
	private List<String[]> tsvList;
	private ScxmlNode scxmlNode;
	private List<String> scripts;
	
	public boolean parse (List<String[]> tsvList_p) {
		this.tsvList = tsvList_p;
		for (int i=0; i < tsvList_p.size(); i++) {
			if (this.checkHeader(tsvList_p.get(i))) {
				this.startLineIdx = i + 1;
				break;
			}
		}
		
		try {
			if (this.startLineIdx == 0) {
				this.errMsg = "Missing headers containing required columns";
				this.valid = false;
			}
			else this.parse();
		}
		catch (Exception e) {
			this.errMsg = e.getMessage();
			this.valid = false;
		}
		return this.valid;
	}
	
	public boolean isValid () {
		return this.valid;
	}
	
	public String getErrMsg () {
		return this.errMsg;
	}
	
	public ScxmlNode getScxmlNode() {
		return this.valid? this.scxmlNode: null;
	}

	public List<String> getScripts() {
		return this.valid? this.scripts: null;
	}

	private boolean checkHeader (String[] headerCols) {
		this.FromIdx = -1;
		this.EventIdx = -1;
		this.ToIdx = -1;
		this.WeightIdx = -1;
		this.TraverseTimesIdx = -1;
		this.ScriptIdx = -1;
		
		for (int i=0; i < headerCols.length; i++) {
			String c = headerCols[i].toUpperCase().trim();
			if (c.equalsIgnoreCase("FROM") || c.equalsIgnoreCase("GIVEN")) {
				this.FromIdx = i;
			}
			else if (c.equalsIgnoreCase("TO") || c.equalsIgnoreCase("THEN")) {
				this.ToIdx = i;
			}
			else if (c.equalsIgnoreCase("EVENT") || c.equalsIgnoreCase("SCENARIO")) {
				this.EventIdx = i;
			}
			else if (c.equalsIgnoreCase("WEIGHT")) {
				this.WeightIdx = i;
			}
			else if (c.equalsIgnoreCase("TRAVERSE_TIMES")) {
				this.TraverseTimesIdx = i;
			}
			else if (c.equalsIgnoreCase("SCRIPT") || c.equalsIgnoreCase("WHEN")) {
				this.ScriptIdx = i;
			}
		}
		return this.FromIdx >= 0 && this.ToIdx >= 0 && this.EventIdx >= 0;
	}
	
	
	private void parse () throws Exception {
		this.scxmlNode = new ScxmlNode();
//		this.scxmlNode.getPluginList().add("SEQOUT");
		this.scripts = new java.util.ArrayList<>();
		this.valid = false;
		this.errMsg = null;
		
		// find all states
		List<StateNode> stateList = this.scxmlNode.getChildrenStates();
		List<String> fromList = this.tsvList.stream()
				  .skip(this.startLineIdx)
				  .map(s -> GherkinModel.getCode(s[this.FromIdx]))
				  .distinct()
			      .collect(Collectors.toList());
		List<String> toList = this.tsvList.stream()
				  .skip(this.startLineIdx)
				  .map(s -> GherkinModel.getCode(s[this.ToIdx]))
				  .distinct()
			      .collect(Collectors.toList());
		List<String> allList = new java.util.ArrayList<>(fromList.size()+toList.size());
		allList.addAll(fromList);
		allList.addAll(toList);
		allList = allList.stream().distinct().collect(Collectors.toList());

		// create all states first, then transitions
		Map<String, StateNode> stateMap = new java.util.HashMap<>();
		allList.stream()
			.forEach( s -> {
				StateNode st = new StateNode();
				st.setStateIDandUID(s, GherkinModel.genUID());
				stateList.add(st);
				stateMap.put(s, st);
			});
		
		// FROM in the first row is the initial state
		String initState = GherkinModel.getCode(this.tsvList.get(this.startLineIdx)[FromIdx]);
		StateNode st = stateMap.get(initState);
		st.setIsInitial(true);
		
		// TO without present in FROM are final states
		toList.stream().filter( s -> !fromList.contains(s)).forEach(s -> {
			stateMap.get(s).setIsFinal(true);
		});
		
		this.tsvList.stream().skip(this.startLineIdx).forEach(row -> {
			StateNode st2 = stateMap.get(GherkinModel.getCode(row[this.FromIdx]));
			List<TransitionNode> transList = st2.getTransitions();
			TransitionNode trans = new TransitionNode();
			trans.setEvent(GherkinModel.getCode(row[this.EventIdx]));
			trans.setUID (GherkinModel.genUID());
			trans.setTargetNode(stateMap.get(GherkinModel.getCode(row[this.ToIdx])));
			transList.add(trans);
			
			if (this.ScriptIdx >= 0) {
				String rscript = row[ScriptIdx].trim();
				if (!rscript.equals("")) {
					List<String> rowScript = Arrays.asList(rscript.split("\n"));
					this.scripts.add("");
					this.scripts.add("@TRIGGER('" + trans.getUID() + "')");
					this.scripts.add("def '" + trans.getEvent() + "' () {");
					this.scripts.add("  $SEQOUT.startStep();");
				    
				    // script formats supported: ([] indicates optional)
				    //   [STEP:] xyz
				    //   ASSERT: message [| req]
					this.scripts.addAll(rowScript.stream()
						.map( script -> {
							if (script.startsWith("Step:") || script.startsWith("STEP:")) {
								return "  $SEQOUT.writeStepAction(\"" + script.substring(5).trim() + "\");";
							}
							else if (script.startsWith("Assert:") || script.startsWith("ASSERT:")) {
								script = script.substring(7).trim();
								int idx = script.indexOf("|");
								String reqCode = "";
								String msg = script;
								if (idx > 0) {
									msg = script.substring(0, idx).trim();
									reqCode = script.substring(idx+1).trim();
								}
								return "  $SEQOUT.writeStepAssert(\"" + reqCode + "\", \"" + msg + "\");";
							}
							else {
								return "  $SEQOUT.writeStepAction(\"" + script + "\");";
							}
						})
						.collect(Collectors.toList()));
					this.scripts.add("}");
				}
			}
		});
		
		if (!this.scripts.isEmpty()) {
			this.scripts.add(0, "import com.testoptimal.mscript.groovy.TRIGGER\n\n"
					+ "@TRIGGER('MBT_START')\n"
					+ "def 'MBT_START' () {\n"
					+ "   $SEQOUT.setOutputFileXLS('testOutput.xls');\n"
					+ "}\n\n");
		}
		this.valid = true;
	}
}
