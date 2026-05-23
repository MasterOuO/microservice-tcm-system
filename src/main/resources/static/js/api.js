// 你的 JS 檔案 (例如 api.js)
const GATEWAY_URL = "http://localhost:8080"; // 這是你 Gateway 的 Port

async function getSchedule(doctorId) {
    const response = await fetch(`${GATEWAY_URL}/api/user/schedule?doctorId=${doctorId}`, {
        headers: {
            // 記得帶上 JWT Token
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        }
    });
    return await response.json();
}