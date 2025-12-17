$(document).ready(function(){

	// ============================================================
	//  opt改變時的邏輯
	// ============================================================
    $("#opt").on("change", function() {
        let mainCat = $(this).val(); 
        
        // 重置輸入框
        $("#hints").empty();
        $("#other_input").val("");
        
        // 取得子選單 DOM
        let $subContainer = $("#sub_opt_container"); 
        let $subSelect = $("#sub_opt");              


        $subSelect.empty().append('<option value="">--請選擇細項--</option>');

        // 判斷是否有子分類
        if (CATEGORY_MAPPING[mainCat]) {
            // 有子分類
            CATEGORY_MAPPING[mainCat].forEach(function(item) {
                $subSelect.append(new Option(item.text, item.val));
            });
            
            // 顯示子選單區域
            $subContainer.show();
            $subSelect.prop('required', true); 

            // 先隱藏症狀輸入區
            $("#input_symptoms_container").hide(); 
            

        } else {
            // 沒有子分類
            $subContainer.hide();
            $subSelect.prop('required', false);
			let autoSubOpt = ""; 
			        
	        if (mainCat === "DE") {
	            let identity = $("#patientSelect option:selected").data("identity");
	            
	            if (identity) {
	                let genderChar = identity.charAt(1);
	                if (genderChar === '1') {
	                    autoSubOpt = "01"; // 男性 -> DE01
	                } else {
	                    autoSubOpt = "02"; // 女性 -> DE02
	                }
	            } else { 
	                autoSubOpt = "01"; 
	            }
	        }
			
	        if (mainCat === "ICD") {
	            autoSubOpt = "11";
	        }

			// subSelect empty()清空了，val() 無效先 append一個隱藏的option，再設值。
			if (autoSubOpt !== "") {
				$subSelect.empty();
	            $subSelect.append(new Option("Auto", autoSubOpt)); 
	            $subSelect.val(autoSubOpt);                        
				        }

	        // 組合 Key
	        let logicKey = autoSubOpt ? (mainCat + autoSubOpt) : mainCat;
	        console.log("Auto Selected Key:", logicKey);
			
			//載入資料
			if (Symptom_constants[logicKey]) {
			            symptoms_data = Symptom_constants[logicKey];
			            $("#input_symptoms_container").show();
			            $("#form").submit(); // 觸發後端計算
			} else {
				console.error("找不到症狀資料:", logicKey);    
			}
        }
    });

    // ============================================================
    //  sub_opt改變時的邏輯
    // ============================================================
    $("#sub_opt").on("change", function() {
        let mainCat = $("#opt").val();      
        let subCat = $(this).val();         

        if (subCat === "") return; // 如果選回預設值，不做事

        let combinedKey = mainCat + subCat; 
        console.log("切換科別:", combinedKey);

        // 載入對應的症狀資料
        if (Symptom_constants[combinedKey]) {
            symptoms_data = Symptom_constants[combinedKey];
            
            // 重置輸入框
            $("#hints").empty();
            $("#other_input").val("");
			
            $("#input_symptoms_container").show();
            $("#form").submit();
        } else {
            console.error("找不到對應的症狀資料:", combinedKey);
        }
    });

    // ============================================================
    //  搜尋與輸入邏輯
    // ============================================================
    $('#other_input').on('input', function() {
        $("#hints").hide();
        $("#hints").empty();
        let searchText = $(this).val().trim();
        let show = false;
        
        // 取得目前已輸入的症狀
        var symptomsArray = $('#symptoms').val().split(/[,\s，。;.\[\]\/\\]+/);
        

        for (const key in symptoms_data) {
            if (searchText !== "" && symptoms_data[key].toLowerCase().includes(searchText) && !symptomsArray.includes(symptoms_data[key])) {
                show = true;
                const hint = $('<bottom class="sbtn btn btn-outline-success">').text(symptoms_data[key]);
                if (symptoms_data[key] === searchText) {
                    hint.css({
                        'color': 'green',
                        'text-decoration': 'underline',
                        'font-weight': 'bold',
                        'font-size': 'larger'
                    });
                }
                
                hint.click(function(e) {
                    e.preventDefault();
                    addSymptomTag($(this).text()); 
                    $(this).hide();
                });
                $("#hints").append(hint).append(" ");
            }
        }
        if (show) {
            $("#hints").show();
        }
    });
    
    // 點擊建議症狀 (Tag)
    $("#suggests").on("click", ".sbtn", function() {
        addSymptomTag($(this).val()); // functions.js 裡的函式
    });
    
    // 清除按鈕
    $("#clear").click(function() {
        $("#other_input").val("");
        $("#symptoms").val("");
        $("#symptomTags").empty(); 
        
        if (typeof tagSymptoms !== 'undefined') {
            tagSymptoms = [];
        }
        
        $("#form").submit();
    });
});