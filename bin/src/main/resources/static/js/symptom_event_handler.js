$(document).ready(function(){
	$("#opt").on("change", function() {
		$("#hints").empty();
		$("#other_input").val("");
		$("#input_symptoms_container").show();
		$("#form").submit();
		symptoms_data=Symptom_constants[$(this).val()];
	});
	
	$('#other_input').on('input', function() {
		$("#hints").hide();
		$("#hints").empty();
		let searchText = $(this).val().trim();
		let show = false;
		var symptomsArray=$('#symptoms').val().split(/[,\s，。;.\[\]\/\\]+/);
		for (const key in symptoms_data) {
			if (searchText !== ""&&symptoms_data[key].toLowerCase().includes(searchText) && !symptomsArray.includes(symptoms_data[key])) {
				show = true;
				const hint = $('<bottom class=\"sbtn btn btn-outline-success\">').text(symptoms_data[key]);
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
					
					/*if($('#symptoms').val().trim()===""){
						$('#symptoms').val($('#symptoms').val() + $(this).text() + " ")
					}else{
						$('#symptoms').val($('#symptoms').val() + ", " + $(this).text() + " ")
					}*/
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
	
	$("#suggests").on("click", ".sbtn", function() {
		/*if ($("#symptoms").val() === "") {
			$("#symptoms").val($(this).attr('value') + " ");
		} else {
			$("#symptoms").val($("#symptoms").val() + ", " + $(this).attr('value') + " ");
		}*/
		addSymptomTag($(this).val());
	});
	
	$("#clear").click(function() {
		$("#other_input").val("");
		$("#symptoms").val("");
		$("#form").submit();
	});
});