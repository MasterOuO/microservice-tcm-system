package com.tcmdiff.SpringTcmDiff.models;


public class Syndrome {
	private Integer id;// 症候id
	private String name;// 症候名稱
	private SymptomSet[] primarySymptomSets;
	private SymptomSet[] secondarySymptomSets;
	
	@SuppressWarnings("unused")
	private Syndrome() {}
	
	public Syndrome(Integer id, String name, SymptomSet[] primarySymptomSets, SymptomSet[] secondarySymptomSets) {
		// 儲存外面配置的空間指標，所以不用再new空間
		this.id = id;// 編號
		this.name = name;// 症候名
		this.primarySymptomSets = primarySymptomSets;// 主
		this.secondarySymptomSets = secondarySymptomSets;// 次

	}

	public Integer getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
	
	public SymptomSet[] getPrimSymptSets() {
        return this.primarySymptomSets;
    }

    public SymptomSet[] getSecSymptSets() {
        return this.secondarySymptomSets;
    }
}
