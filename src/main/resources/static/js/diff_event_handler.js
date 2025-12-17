$(document).ready(function(){
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
			url: "http://localhost:8070/diff/compute",    // diff_url => /diff/compute in UserController
			data: dataToSend,
			success: function(response) {  //response是後端回傳的JSON，若後端 @ResponseBody回傳Map jQuery自動當物件
				var data = response;
				$("#suggest_G1").html(render_suggested('望診症狀',data.output_suggested_G1));  // "#suggest_G1"是html定義的
				$("#suggest_G2").html(render_suggested('聞診症狀',data.output_suggested_G2));
				$("#suggest_G3").html(render_suggested('問診症狀',data.output_suggested_G3));
				$("#suggest_G4").html(render_suggested('切診症狀',data.output_suggested_G4));
				
				tagSymptoms = $("#symptoms").val().split(/\s*,\s*/).filter(s => s.trim() !== "");  //對已輸入症狀處理
				updateTagDisplay();  // 呼function.js
				
				if (Object.keys(data.output).length !== 0) {
					$("#result").html(render_result(data.output));   //塞入doctor_page 的 id=result 這個div區域
	
					$("#result tr").each(function() {
						var resultRow = $(this);
						let item = resultRow.find('td:first').text().trim();
						let after_set = resultRow.find("td:nth-child(2)");
						//let before_set = resultRow.find("td:nth-child(3)");
						setting_color(after_set, after_set.text().trim());
						//setting_color(before_set, syndromeObject[item]);
					});
					
					//放圖
					if (data.graph) {
					            $("#result").append(`
					                <br>
					                <img src="http://localhost:8070${data.graph}"
					                     alt="graph" 
					                     style="max-width:100%; height:auto;">
					            `);
					        }

					
					
					
					//$(".checkClass").prop("checked", true);
					$("#result").show();
					$("#end_form").show();
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
});


