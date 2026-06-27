let syndromeObject = {}; // 儲存前次分數用
let tagSymptoms = [];

function render_result(data, causesData) {
	causesData = causesData || {};
	let output = `
		<table class="table table-bordered table-hover align-middle">
			<thead class="table-dark">
				<tr>
					<th>證候</th>
					<th>成因</th>
					<th>本次分數</th>
					<th>前次分數</th>
				</tr>
			</thead>
			<tbody>
	`;

	$.each(data, function(key, value) {
		const beforeScore = syndromeObject[key] !== undefined ? syndromeObject[key].toFixed(3) : "";
		
		// 直接從後端給的 Map 裡面拿這個證候的成因陣列
		// 如果這個證候剛好沒有定義成因，就給一個空陣列 [] 防止報錯
		let causesArray = causesData[key] || []; 

		let causeSelectHtml = `<select class="form-select form-select-sm cause-selector">`;
		causeSelectHtml += `<option value="">-- 不顯示 --</option>`;

		if (causesArray.length > 0) {
            // 在 JS 中組合出 "1. 先天不足\n2. 房事過度" 的格式
            let allCausesString = causesArray.map((c, index) => `${index + 1}.${c}`).join("\\n");
            causeSelectHtml += `<option value="${allCausesString}">-- 全選 --</option>`;

            causesArray.forEach((cause, index) => {
                // 選項顯示加上數字，例如 "1. 先天不足"
                causeSelectHtml += `<option value="${index + 1}.${cause}">${index + 1}. ${cause}</option>`;
            });
        }
				
		causeSelectHtml += `<option value="custom"> 自訂輸入...</option>`;
		causeSelectHtml += `</select>`;
		causeSelectHtml += `<input type="text" class="form-control form-control-sm custom-cause-input mt-1" placeholder="請輸入自訂成因" style="display: none;">`;		





		output += `
			<tr class="re_data">
				<td>${key}</td>
				<td>${causeSelectHtml}</td>
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

// 監聽下拉選單自訂輸入成因
$(document).on('change', '.cause-selector', function() {
    let customInput = $(this).siblings('.custom-cause-input');

    if ($(this).val() === 'custom') {
        customInput.show().focus();
    } else {
        customInput.hide().val(""); 
    }
});