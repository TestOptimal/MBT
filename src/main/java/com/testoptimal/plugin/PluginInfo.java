package com.testoptimal.plugin;

public class PluginInfo {
	private String pluginID;
	private String pluginDesc;
	private Class<?> pluginClass;
	
	public PluginInfo (String pluginID_p, String pluginDesc_p, Class<?> pluginClass_p) {
		this.pluginID = pluginID_p;
		this.pluginDesc = pluginDesc_p;
		this.pluginClass = pluginClass_p;
	}
	
	public String getPluginID () {
		return this.pluginID;
	}
	
	public String getPluginDesc() {
		return this.pluginDesc;
	}
	
	public Class getPluginClass() {
		return this.pluginClass;
	}
	
	public PluginAncestor newInstance () throws IllegalAccessException, InstantiationException {
		return (PluginAncestor) this.pluginClass.newInstance();
	}
}
