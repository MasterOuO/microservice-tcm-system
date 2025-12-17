$(document).ready(function(){
	$("#patientSelect").on("change", function(){
		const identity = $("#patientSelect option:selected").data("identity");		
		let optionsToShow = [];
		let optionsToHide = [];
		
		if(identity.charAt(1) === '1'){
			optionsToShow = ['DE01','PS01','PS02'];
			optionsToHide = ['DE02','OG01','OG02','OG03'];
		}
		else{
			optionsToShow = ['DE02','OG01','OG02','OG03','PS01','PS02'];
			optionsToHide = ['DE01'];
		}
		
		$("#opt option").each(function(){
			const val = $(this).val();
			if(optionsToShow.includes(val)){
				$(this).show();
			} else if(optionsToHide.includes(val)){
				$(this).hide();
			}
		});
		$("#opt").val("");
		
		$.ajax({
			type: "GET",
			url: "/api/load-diff",
			data: {identity: identity},
			success: function(res){
				if(res.success){
					$("#symptoms").val(res.symptom);
					fillPreviousResult(res.result);
				}else{
					$("#symptoms").val("");
					clearPreviousResult();
				}
			},
			error: function(){
				$("#history").text("查詢失敗");
			}
		});
	});
	
	function fillPreviousResult(resultText){
		const resultMap = {};
		resultText.split(',').forEach(item => {
			let parts = item.split(":");
			if(parts.length === 2){
				resultMap[parts[0].trim()] = parts[1].trim();
			}
		});
		previousScores = resultMap;
		updatePreviousScoreUI();
	}
	
	function updatePreviousScoreUI(){
		$("#result tr.re_data").each(function(){
			let key = $(this).find("td:first").text().trim();
			let beforeCell = $(this).find("td").eq(2);
			if(previousScores[key]){
				beforeCell.text(previousScores[key]);
				setting_color(beforeCell, previousScores[key]);
			}
			else{
				beforeCell.text("");
			}
		});
	}	
	function clearPreviousResult(){
		$("#result tr.re_data").each(function(){
			$(this).find("td").eq(2).text("");
		});
	}
	
	$(document).ajaxSuccess(function(event, xhr, settings){
		if(settings.url === "/diff/compute"){
			updatePreviousScoreUI()
		}
	});
});