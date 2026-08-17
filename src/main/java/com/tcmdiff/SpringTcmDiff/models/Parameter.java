package com.tcmdiff.SpringTcmDiff.models;



public class Parameter {
	//病因,病位,舌診,脈診,特徵
	private double[] groupWeights;
	//主症權重
	private double primaryWeight;
	//次症權重
	private double secondaryWeight;
	
	// 傳遞資料的建構子
    public Parameter(double[] groupWeights, double primaryWeight, double secondaryWeight) {
        this.groupWeights = groupWeights;
        this.primaryWeight = primaryWeight;
        this.secondaryWeight = secondaryWeight;
    }
    
	public double[] getGroupWeights(){
		return this.groupWeights;
	}

	public double getPrimWeight(){
		return this.primaryWeight;
	}

	public double getSecWeight(){
		return this.secondaryWeight;
	}
}
