package com.testoptimal.server.model;

public class ModelProp {
	public String modelName;
	
//	@ApiModelProperty(notes = "relative model path", required = true)
	public String folderPath;

//	@ApiModelProperty(notes = "template to clone the model from")
	public String template;

	public String desc;
	
	public ModelProp () {
		
	}
}