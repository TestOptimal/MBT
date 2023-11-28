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
