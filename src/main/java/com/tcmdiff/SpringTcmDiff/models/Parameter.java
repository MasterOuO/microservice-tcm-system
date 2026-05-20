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
	
	/*  職權拆給repo
	public Parameter(String FilePath) throws IOException, ParseException {
	    groupWeights=new double[5];
	    JSONParser parser = new JSONParser();
        
        //java8
        //FileReader reader = new FileReader(FilePath);
        
        FileInputStream fileInputStream = new FileInputStream(FilePath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        
        // 使用JSONParser解析JSON文件
        Object obj = parser.parse(reader);

        // 將解析結果轉為JSONObject
        JSONObject jsonObject = (JSONObject) obj;
        
        this.groupWeights[0] = ((Double) jsonObject.get("G1_weight")).doubleValue();
        this.groupWeights[1] = ((Double) jsonObject.get("G2_weight")).doubleValue();
        this.groupWeights[2] = ((Double) jsonObject.get("G3_weight")).doubleValue();
        this.groupWeights[3] = ((Double) jsonObject.get("G4_weight")).doubleValue();
        this.groupWeights[4] = ((Double) jsonObject.get("G5_weight")).doubleValue();
        this.primaryWeight = ((Double) jsonObject.get("primary_weight")).doubleValue();
        this.secondaryWeight = ((Double) jsonObject.get("secondary_weight")).doubleValue();      
	}
	*/
}
