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

package com.testoptimal.graphing;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeGroup {
	private int sortNum;
	private String groupName;
	private List<MsgNode> nodeList;
	
	public NodeGroup (String groupName_p) {
		this.groupName = groupName_p;
		this.nodeList = new java.util.ArrayList<>();
	}
	
	public boolean isSame (String groupID_p) {
		return this.groupName.equals(groupID_p);
	}
	
	public static List<NodeGroup> makeGroupList (Map<String, String> groupMap_p, Map<String, MsgNode> nodeMap_p,
			List<String> sortIDList_p) {
		Map<String, List<Map.Entry<String, String>>> groupList = groupMap_p.entrySet().stream().collect(Collectors.groupingBy(entry -> entry.getValue()));
		List<NodeGroup> retList = groupList.entrySet().stream()
			.map(entry -> { 
				NodeGroup group = new NodeGroup (entry.getKey());
				group.sortNum = sortIDList_p.indexOf(group.groupName);
				group.nodeList = entry.getValue().stream()
						.map ( row -> nodeMap_p.get(row.getKey()))
						.filter( n -> n!=null)
						.sorted(Comparator.comparingInt(MsgNode::getSortNum))
						.collect(Collectors.toList());
				return group;
			})
			.filter(g -> !g.nodeList.isEmpty())
			.sorted(Comparator.comparingInt(NodeGroup::getSortNum))
			.collect(Collectors.toList());
		return retList;
	}
	
	private int getSortNum () {
		return this.sortNum;
	}
	
	public String getGroupName () {
		return this.groupName;
	}
	
	public boolean isDefaultGroup () {
		return this.groupName.equals("");
	}
	
	public List<MsgNode> getNodeList() {
		return this.nodeList;
	}
}
