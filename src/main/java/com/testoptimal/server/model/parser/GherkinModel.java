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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.testoptimal.scxml.ScxmlNode;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.server.config.Config;
import com.testoptimal.util.FileUtil;

public class GherkinModel {
	public String featureName;
	public List<GherkinScenario> scenarioList = new java.util.ArrayList<>();
	private transient ScxmlNode scxmlNode;
	private transient List<String> scripts;
	
	public String getInitialState() {
		if (this.scenarioList.isEmpty()) return null;
		else return GherkinModel.getCode(this.scenarioList.get(0).givenState);
	}
	
	public List<String> getFinalStates() {
		List<String> sourceStateList = this.scenarioList.stream()
				.map( s -> s.getGivenState())
				.distinct()
			    .collect(Collectors.toList());
		List<String> retList = this.scenarioList.stream()
				.filter( s -> !sourceStateList.contains(GherkinModel.getCode(s.thenState)))
				.map( s -> GherkinModel.getCode(s.thenState))
				.collect(Collectors.toList());
		return retList;
	}

	protected static String getCode (String text_p) {
		String code = text_p.trim();
		int idx = text_p.lastIndexOf("|");
		if (idx > 0) {
			code = text_p.substring(idx+1).trim();
			if (code.equals("")) {
				code = text_p.substring(0, idx).trim();
			}
		}		
		return code;
	}

	protected static String getDesc (String text_p) {
		String desc = text_p;
		int idx = text_p.lastIndexOf("|");
		if (idx > 0) {
			desc = text_p.substring(0, idx).trim();
		}		
		return desc;
	}

	public ScxmlNode toScxml () {
		this.scxmlNode = new ScxmlNode();
		this.scripts = new java.util.ArrayList<>();
		
		this.scxmlNode.setModelName(getCode(this.featureName));
//		this.scxmlNode.getPluginList().add("SEQOUT");
		
		List<StateNode> stateList = this.scxmlNode.getChildrenStates();
		List<String> slist = this.scenarioList.stream()
				  .map(s -> s.getGivenState())
				  .distinct()
			      .collect(Collectors.toList());
		slist.addAll(this.scenarioList.stream()
				.map(s -> s.getThenState())
				  .distinct()
			      .collect(Collectors.toList()));
		slist = slist.stream().distinct().collect(Collectors.toList());
		Map<String, StateNode> stateMap = new java.util.HashMap<>();
		slist.stream()
			.forEach( s -> {
				StateNode st = new StateNode();
				st.setStateIDandUID(s, genUID());
				stateList.add(st);
				stateMap.put(s, st);
			});
		
		String initState = this.getInitialState();
		StateNode st = stateMap.get(initState);
		st.setIsInitial(true);
		
		this.getFinalStates().forEach(s -> {
			stateMap.get(s).setIsFinal(true);
		});
		
		this.scenarioList.forEach(s -> {
			StateNode st2 = stateMap.get(getCode(s.givenState));
			List<TransitionNode> transList = st2.getTransitions();
			TransitionNode trans = new TransitionNode();
			trans.setEvent(getCode(s.scenarioName));
			trans.setUID (GherkinModel.genUID());
			trans.setTargetNode(stateMap.get(getCode(s.thenState)));
			transList.add(trans);
			
			if (!s.whenScriptList.isEmpty()) {
				this.scripts.add("");
				this.scripts.add("@TRIGGER('" + trans.getUID() + "')");
				this.scripts.add("def '" + trans.getEvent() + "' () {");
			    this.scripts.add("  $SEQOUT.startStep();");
			    
			    // script formats supported: ([] indicates optional)
			    //   [STEP:] xyz
			    //   ASSERT: message [| req]
				this.scripts.addAll(s.whenScriptList.stream()
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
		});
		
		return this.scxmlNode;
	}
	
	public String getScripts () throws Exception {
		if (this.scripts==null) return "";
		StringBuffer s0 = FileUtil.readFile(Config.getModelRoot() + ".tpl/TRIGGERS_gherkin.gvy");
		String s = this.scripts.stream().collect(Collectors.joining("\n"));
		s0.append(s);
		return s0.toString();
	}
	
	public static String genUID () {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
}
