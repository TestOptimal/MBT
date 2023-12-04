package com.testoptimal.graphing.plantuml;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.graphing.MsgNode;
import com.testoptimal.graphing.MsgSeq;
import com.testoptimal.graphing.NodeGroup;
import com.testoptimal.server.config.Config;
import com.testoptimal.util.FileUtil;
import com.testoptimal.util.StringUtil;

import net.sourceforge.plantuml.SourceStringReader;

public class MSC_Graph {
	private static Logger logger = LoggerFactory.getLogger(MSC_Graph.class);

	public static final float wrapRatioNode = 2.5f;
	public static final float wrapRatioEdge = 4.0f;
	public static final String newLineString = "\\n";

	private String chartDesc;
	
	/**
	 * list of group ids.
	 */
	private List<String> groupList = new java.util.ArrayList<>();
	
	/**
	 * keyed on uid
	 */
	private Map<String, MsgNode> nodeMapList = new java.util.HashMap<>();
	
	/**
	 * keyed on uid, value group id
	 */
	private Map<String, String> nodeGroupMapList = new java.util.HashMap<>();
	
	private int seqNum = 0;
	
	/**
	 * for an instance of MSC chart.
	 */
	public MSC_Graph (String chartDesc_p) {
		this.chartDesc = chartDesc_p;
	}
	
	private List<MsgSeq> msgList = new java.util.ArrayList<MsgSeq>();

	private MsgSeq newMsgSeq (String fromUID_p, String toUID_p, String msg_p, String stereotype_p, 
			String color_p, int priority_p, String tcName_p) {
		MsgSeq seq = new MsgSeq(fromUID_p, toUID_p, msg_p, stereotype_p, color_p, priority_p, tcName_p);
		return seq;
	}

	public void addMsg(String fromUID_p, String toUID_p, String msg_p, String stereotype_p, 
			String color_p, int priority_p, String tcName_p) {
		if (!this.nodeGroupMapList.containsKey(fromUID_p)) {
			logger.warn ("from UID not registered: " + fromUID_p);
		}
		if (!this.nodeGroupMapList.containsKey(toUID_p)) {
			logger.warn ("to UID not registered: " + toUID_p);
		}
		this.msgList.add(this.newMsgSeq(fromUID_p, toUID_p, msg_p, stereotype_p, color_p, priority_p, tcName_p));
	}

	public void insertMsg(String fromUID_p, String toUID_p, String msg_p, String stereotype_p, 
			String color_p, int priority_p, String tcName_p) {
		this.msgList.add(0, this.newMsgSeq(fromUID_p, toUID_p, msg_p, stereotype_p, color_p, priority_p, tcName_p));
	}

	public void addSection(String sectionName_p)  {
		MsgSeq seq = new MsgSeq(null, null, sectionName_p, null, null, 0, "");
		this.msgList.add(seq);
	}

	
	/**
	 * adds a node to a group. If a group does not exist yet, it creates the box automatically.
	 * 
	 * If the node is already in another group, it removes it from that group.
	 * 
	 * A node can only be in one group.
	 * 
	 * @param uid_p
	 * @param groupID_p
	 * @return false if node isn't found
	 */
	public boolean addNodeToGroup (String uid_p, String groupID_p) {
		MsgNode node = this.nodeMapList.get(uid_p);
		if (node==null) return false;
		this.nodeGroupMapList.put(uid_p, groupID_p);
		if (!this.groupList.contains(groupID_p)) {
			this.groupList.add(groupID_p);
		}
		return true;
	}
	
	public MsgNode addMsgNode(String groupID_p, String label_p, String uid_p, String stereotype_p) {
		MsgNode node = this.nodeMapList.get(uid_p);
		if (node == null) {
			node = new MsgNode(label_p, uid_p, stereotype_p);
			node.setSortNum(++this.seqNum);
			this.nodeMapList.put(uid_p, node);
		}
		this.addNodeToGroup(uid_p, groupID_p);
		return node;
	}

	
	public boolean genMSC(String outFilePath_p, String activateNodeName_p, String includeFile_p) 
		throws Exception {
		return this.genMSC(outFilePath_p, null, null, includeFile_p);
	}	

	// MSC graph
	public boolean genMSC(String outFilePath_p, String skinName_p, String bkgColor_p, String includeFile_p) throws Exception {
		includeFile_p = FileUtil.concatFilePath(Config.getConfigPath(), includeFile_p);
		if (this.msgList.isEmpty()) return false;

		StringBuffer retBuf = new StringBuffer();
		retBuf.append("@startuml\n")
			  .append("title <font size=18 color=#555555>").append(this.chartDesc).append("</font>\n");
		retBuf.append("!include ").append(includeFile_p).append("\n");

		if (!StringUtil.isEmpty(bkgColor_p)) {
			retBuf.append("skinparam backgroundColor " + bkgColor_p + "\n");
		}

		if (!StringUtil.isEmpty(skinName_p)) {
			retBuf.append("skin " + skinName_p + "\n");
		}
		
		List<NodeGroup> sortedGroupList = NodeGroup.makeGroupList(this.nodeGroupMapList, this.nodeMapList, this.groupList);
		for (NodeGroup g: sortedGroupList) {
			if (!g.isDefaultGroup()) {
				retBuf.append ("box \"").append(g.getGroupName()).append("\"  #EFEFEF\n");
			}
			retBuf.append (g.getNodeList().stream()
				.map (node -> this.genActorUML(node)).collect(Collectors.joining("\n"))).append("\n");
			if (!g.isDefaultGroup()) {
				retBuf.append("end box\n");
			}
		}
		retBuf.append("\n");
		String prevTcName = "";
		String activateName = "";
		for (MsgSeq seq: this.msgList) {
			if (seq.getFromUID()==null || seq.getToUID()==null) {
				retBuf.append ("== ").append(seq.getMsg()).append(" ==\n");
			}
			else {
				String curTcName = seq.getTcName();
				if (!curTcName.equals(prevTcName)) {
					if (!prevTcName.equals("")) {
						retBuf.append ("deactivate \"").append(activateName).append("\"\n");
					}
				}
				retBuf.append (this.genMsgUML (seq)).append("\n");
				if (!curTcName.equals(prevTcName)) {
					if (!curTcName.equals("")) {
						activateName = seq.getFromUID();
						retBuf.append ("activate \"").append(activateName).append("\"\n");
					}
					prevTcName = curTcName;
				}
			}
		}
		retBuf.append("@enduml\n");
		OutputStream png =new FileOutputStream(outFilePath_p);
		if (StateDiagram.printPlantUML) {
			FileUtil.writeToFile(outFilePath_p + ".plantuml", retBuf);
		}

		SourceStringReader reader = new SourceStringReader(retBuf.toString());
		try {
			// Write the first image to "png"
			String desc = reader.generateImage(png);
	
			// Return a null string if no generation
			if (desc==null) {
				throw new Exception ("Unable to generate MSC");
			}
			else {
				return true;
			}
		}
		finally {
			png.close();
		}
	}

	private String genActorUML (MsgNode msgNode_p) {
		String actorLabel = StringUtil.wrapText(msgNode_p.getNodeName(), wrapRatioNode, newLineString);
		String actor1 = "\"" + actorLabel + "\" as " + msgNode_p.getUID(); 
		if (!StringUtil.isEmpty(msgNode_p.getStereotype())) {
			actor1 = actor1 + " << (" + msgNode_p.getStereotype() + ", #ADD1B2) >>";
		}
		String role = "participant";
		if (msgNode_p.hasStereotype("R")) {
			role = "actor";
		}
		return role + " " + actor1;
	}
	
	private String genMsgUML (MsgSeq seq_p) {
		StringBuffer retBuf = new StringBuffer();
		retBuf.append(seq_p.getFromUID())
			  .append(" -> ")
			  .append(seq_p.getToUID())
			  .append(": ").append(seq_p.getLabel());
		return retBuf.toString();
	}
}


