package com.tcmdiff.SpringTcmDiff.models;

import jakarta.persistence.*;

@Entity
@Table(name = "diff_results")
public class DiffResult {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	private String name;
	private String identity;
	//large obj
	@Lob
	private String symptom; 
	
	@Lob
	private String result;
	
	@Lob
	private String other;
	private String dateTime;
	
	private String graphPath;
	
	//JPA反射邏輯必須 get,set,建構子...
	public DiffResult() {
		super();
	}
	
	//參數化建構子 自己要用entity物件時可以使用 eg. dto轉成entity,測試 站存 計算 
	public DiffResult(String name, String identity, String symptom, String result, String other, String dateTime, String graphPath) {
		this.name = name;
		this.identity = identity;
		this.symptom = symptom;
		this.result = result;
		this.other = other;
		this.dateTime = dateTime;
		this.graphPath = graphPath;
	}
	//JPA反射邏輯必須 get,set,建構子...
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getIdentity() {
		return identity;
	}
	
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	
	public String getSymptom() {
		return symptom;
	}
	
	public void setSymptom(String symptom) {
		this.symptom = symptom;
	}
	
	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public String getOther() {
		return other;
	}
	
	public String getGraphPath() {
		return graphPath;
	}
	
	public void setOther(String other) {
		this.other = other;
	}
	
	public String getDateTime() {
		return dateTime;
	}
	
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	
	public void setGraphPath(String graphPath) {
		this.graphPath = graphPath;
	}
}
