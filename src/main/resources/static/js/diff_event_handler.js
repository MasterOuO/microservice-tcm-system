$(document).ready(function(){
	//辨證計算事件
	$("#form").submit(function(event) {
		event.preventDefault();
		const dataToSend = {   //建立要送到後端的資料物件 跟@RequestParam一致
					opt: $("#opt").val(),   
					sub_opt: $("#sub_opt").val(),
					symptoms: $("#symptoms").val()
				};
		console.log(dataToSend);
		$.ajax({
			type: "POST",
			url: gatewayBaseUrl + "/api/diff/compute",    // diff_url => /diff/compute in UserController
			data: dataToSend,
			xhrFields: {
			        withCredentials: true // 跨域認證必備
			},
			success: function(response) {  //response是後端回傳的JSON，若後端 @ResponseBody回傳Map jQuery自動當物件
				var data = response;
				$("#suggest_G1").html(render_suggested('望診症狀',data.output_suggested_G1));  // "#suggest_G1"是html定義的
				$("#suggest_G2").html(render_suggested('聞診症狀',data.output_suggested_G2));
				$("#suggest_G3").html(render_suggested('問診症狀',data.output_suggested_G3));
				$("#suggest_G4").html(render_suggested('切診症狀',data.output_suggested_G4));
				
				tagSymptoms = $("#symptoms").val().split(/\s*,\s*/).filter(s => s.trim() !== "");  //對已輸入症狀處理
				updateTagDisplay();  // 呼function.js
				
				if (Object.keys(data.output).length !== 0) {
					// 額外收到成因資料causesData
					$("#result").html(render_result(data.output, data.causesData));   //塞入doctor_page 的 id=result 這個div區域
	
					$("#result tr").each(function() {
						var resultRow = $(this);
						let item = resultRow.find('td:first').text().trim();
						let after_set = resultRow.find("td:nth-child(3)");
						//let before_set = resultRow.find("td:nth-child(3)");
						setting_color(after_set, after_set.text().trim());
						//setting_color(before_set, syndromeObject[item]);
					});
					
					//放圖
					if (data.graph) {
						$("#result").append(`
					    	<br>
					        <img src="http://localhost:8080${data.graph}"
					        	 alt="graph" 
					        	 style="max-width:100%; height:auto;">
					    `);
					
					}

					
					
					
					//$(".checkClass").prop("checked", true);
					$("#result").show();
					$("#end_form").show();
					$("#generate_graph_btn").show(); // <--- 請把這行加回去！
					$('#toastMessage').text('辨證結果已更新 !');
					$('.toast').toast('show');
					$("#hints").empty();
					$("#other_input").val("");
				}
			},
			error: function() {
				console.log("error");
			}
		});
	});
	
	// 產圖按鈕事件
	$("#generate_graph_btn").click(function() {
	    let selectedData = [];

	    // 掃描表格中的每一列 (tr.re_data)
	    $("#result tbody tr.re_data").each(function() {
	        // 抓證候名稱，下拉選單跟分數
	        let syndromeName = $(this).find("td:eq(0)").text().trim();
	        let causeSelect = $(this).find(".cause-selector").val();
			let scoreStr = $(this).find("td:eq(2)").text().trim();
	        let finalCause = "";

			    // 判斷是否為自訂輸入
			    if (causeSelect === "custom") {
			        finalCause = $(this).find(".custom-cause-input").val().trim();
			    } else {
			        finalCause = causeSelect;
			    }
				
			    // 把這一列的資料推進陣列
			    selectedData.push({
			        syndrome: syndromeName,
			        cause: finalCause,
					score: scoreStr
			    });
			});

			console.log("準備送給後端的產圖資料：", selectedData);

			// 發送第二次 POST 請求給generateGraph
			$.ajax({
			    type: "POST",
			    url: gatewayBaseUrl + "/api/diff/generateGraph",
			    contentType: "application/json", // JSON 格式
			    data: JSON.stringify(selectedData),
			    success: function(response) {
			        if (response.graphUrl) {
						let imageUrl = response.graphUrl.startsWith("/") 
			               ? response.graphUrl 
			               : "/" + response.graphUrl;
			            
						// 暫存路徑 之後永久存時候用
						$("#temp_graph_path").val(response.graphUrl);
						$("#graph_image_container").remove();
			            
			            // 將圖片放在表格的最下方 (result div 的內部)
			            $("#result").append(`
			                <div id="graph_image_container" class="text-center mt-4 mb-4">
			                    <hr>
			                    <h4 class="text-dark">證候演變圖</h4>
			                    <img src="${imageUrl}" alt="演變圖" style="max-width:100%; height:auto; border: 1px solid #ccc;">
			                </div>
			            `);
						$('#toastMessage').text('演變圖已生成!');
						$('.toast').toast('show');
			        }
			    },
			    error: function(err) {
			        console.log("產圖發生錯誤", err);
			    }
			});
		});	
});


