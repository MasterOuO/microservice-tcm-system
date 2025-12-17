$(document).ready(function(){
	$("#form").submit(function(event) {
		event.preventDefault();
		const dataToSend = {
					opt: $("#opt").val(), 
					symptoms: $("#symptoms").val()
				};
		console.log(dataToSend);
		$.ajax({
			type: "POST",
			url: diff_url,    // diff_url => /diff/compute in UserController
			data: dataToSend,
			success: function(response) {
				var data = response;
				$("#suggest_G1").html(render_suggested('望診症狀',data.output_suggested_G1));
				$("#suggest_G2").html(render_suggested('聞診症狀',data.output_suggested_G2));
				$("#suggest_G3").html(render_suggested('問診症狀',data.output_suggested_G3));
				$("#suggest_G4").html(render_suggested('切診症狀',data.output_suggested_G4));
				
				tagSymptoms = $("#symptoms").val().split(/\s*,\s*/).filter(s => s.trim() !== "");
				updateTagDisplay();
				
				if (Object.keys(data.output).length !== 0) {
					$("#result").html(render_result(data.output));
	
					$("#result tr").each(function() {
						var resultRow = $(this);
	
						let item = resultRow.find('td:first').text().trim();
						let after_set = resultRow.find("td:nth-child(2)");
						//let before_set = resultRow.find("td:nth-child(3)");
						setting_color(after_set, after_set.text().trim());
						//setting_color(before_set, syndromeObject[item]);
					});
	
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


