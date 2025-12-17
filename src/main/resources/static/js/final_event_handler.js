$(document).ready(function(){
	function extractResult() {
		let rows = $("#result table tr.re_data");
		let output = [];
		rows.each(function(){
			let tds = $(this).find("td");
			let syndrome = $(tds[0]).text().trim();
			let score = $(tds[1]).text().trim();
			output.push(syndrome + ":" + score);
		});
		return output.join(", ");
	}
	$("#end_form").submit(function(event){
		event.preventDefault();
		const selectedOption = $("#patientSelect option:selected");
		const id = selectedOption.val();
		const name = selectedOption.text().split('(')[0];
		const identity = selectedOption.data("identity");
		const symptoms = $("#symptoms").val().trim();
		const result = extractResult();
		const other = $("#other").val().trim();
		
		console.log({id, name, identity, symptoms, result, other});
		
		if(!id || !name || !identity || !symptoms || !result){
			alert("請確認所有欄位皆以填寫");
			return;
		}
		
		$.ajax({
			type: "POST",
			url: "http://localhost:8070/save-diff",
			data:{
				id: id,
				name: name,
				identity: identity,
				symptoms: symptoms,
				result: result,
				other: other
			},
			success: function(res){
				if(res.success){
					alert("儲存成功");
				}
				else{
					alert("儲存失敗");
				}
			},
			error: function(xhr, status, error){
				console.error("AJAX錯誤", error);
				alert("儲存時發生錯誤");
			}
		});
	});
});