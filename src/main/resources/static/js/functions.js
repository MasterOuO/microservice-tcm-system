let syndromeObject = {}; // 儲存前次分數用
let tagSymptoms = [];

function render_result(data) {
	let output = `
		<table class="table table-bordered table-hover align-middle">
			<thead class="table-dark">
				<tr>
					<th>證候</th>
					<th>本次分數</th>
					<th>前次分數</th>
				</tr>
			</thead>
			<tbody>
	`;

	$.each(data, function(key, value) {
		const beforeScore = syndromeObject[key] !== undefined ? syndromeObject[key].toFixed(3) : "";
		output += `
			<tr class="re_data">
				<td>${key}</td>
				<td>${value.toFixed(3)}</td>
				<td>${beforeScore}</td>
			</tr>
		`;
	});

	output += `
			</tbody>
		</table>
	`;

	return output;
}

function render_suggested(name, data) {
	let output = `<h3>${name}:<br></h3>`;
	data.forEach(function(item) {
		output += `<button class="sbtn btn btn-outline-success" value="${item}">${item}</button> `;
	});
	return output;
}

function setting_color(element, value) {
		if (!value || isNaN(parseFloat(value))) return;
		
		const val = parseFloat(value);
		element.removeClass("bg-danger-subtle bg-warning-subtle bg-info-subtle bg-success-subtle bg-white");
		element.addClass("text-dark");

		if (val >= 0.8) {
			element.addClass("bg-danger-subtle");
		} else if (val >= 0.6) {
			element.addClass("bg-warning-subtle");
		} else if (val >= 0.4) {
			element.addClass("bg-info-subtle");
		} else if (val >= 0.2) {
			element.addClass("bg-success-subtle");
		} else {
			element.addClass("bg-white");
		}
}

// 症狀 tag 顯示/移除邏輯
function addSymptomTag(symptom) {
	symptom = symptom.trim();
	if (!symptom || tagSymptoms.includes(symptom)) return;
	tagSymptoms.push(symptom);
	updateTagDisplay();
}

function removeSymptomTag(symptom) {
	tagSymptoms = tagSymptoms.filter(s => s !== symptom);
	updateTagDisplay();
}

function updateTagDisplay() {
	const tagContainer = $("#symptomTags");
	tagContainer.empty();
	tagSymptoms.forEach(symptom => {
		const tag = $(`
			<span class="tag-badge">
				${symptom}
				<span class="remove-btn" data-symptom="${symptom}">&times;</span>
			</span>
		`);
		tag.find(".remove-btn").click(function () {
			removeSymptomTag($(this).data("symptom"));
		});
		tagContainer.append(tag);
	});
	$("#symptoms").val(tagSymptoms.join(", "));
}