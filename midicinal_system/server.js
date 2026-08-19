const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');
const app = express();
const fs = require('fs');

app.use(cors());
app.use(express.json());

// --- 1. 資料庫連線設定 ---
const db = mysql.createConnection({
    host: 'localhost',
    user: 'root', 
    password: 'andy8834', 
    database: 'tcm_project_new' 
});

// --- 2. 核心功能：新增資料 API ---
app.post('/api/medicinal/add', (req, res) => {
    const { name, symptoms, king, minister, assistant, envoy, relation } = req.body;
    const sql = "INSERT INTO prescriptions (name, symptoms, king, minister, assistant, envoy, relation) VALUES (?, ?, ?, ?, ?, ?, ?)";
    db.query(sql, [name, symptoms, king, minister, assistant, envoy, relation], (err, result) => {
        if (err) {
            console.error(err);
            return res.status(500).send({msg: "寫入失敗"});
        }
        res.send({msg: "成功新增一筆資料！"});
    });
});

// --- 3. 核心功能：讀取全部資料 API ---
app.get('/api/medicinal/all', (req, res) => {
    const sql = "SELECT * FROM prescriptions ORDER BY id DESC";
    db.query(sql, (err, results) => {
        if (err) return res.status(500).send(err);
        res.json(results);
    });
});

// --- 4. 核心演算法：推論運算邏輯 (增強防呆與例外捕捉) ---
function runInference(filterEffects = null) {
    return new Promise((resolve, reject) => {
        db.query("SELECT relation FROM prescriptions", (err, results) => {
            if (err) return reject(err);

            try {
                let effectGraphs = {};

                results.forEach(row => {
                    if (!row.relation) return;
                    const cleanRelation = row.relation.replace(/[\r\n]+/g, '');
                    const effects = cleanRelation.split(';');
                    
                    effects.forEach(eff => {
                        const parts = eff.split(':');
                        if (parts.length < 2) return;
                        const effName = parts[0].trim();
                        if (filterEffects && !filterEffects.includes(effName)) return;

                        if (!effectGraphs[effName]) {
                            effectGraphs[effName] = { edges: {}, nodes: new Set() };
                        }

                        const sequenceStr = parts[1].trim();
                        const drugs = sequenceStr.split('>').map(d => d.trim()).filter(d => d);
                        drugs.forEach(d => effectGraphs[effName].nodes.add(d));

                        if (sequenceStr.includes('>') && drugs.length > 1) {
                            for (let i = 0; i < drugs.length - 1; i++) {
                                const u = drugs[i], v = drugs[i+1];
                                if (!effectGraphs[effName].edges[u]) effectGraphs[effName].edges[u] = [];
                                if (!effectGraphs[effName].edges[u].includes(v)) effectGraphs[effName].edges[u].push(v);
                            }
                        }
                    });
                });

                let finalReport = {};
                Object.keys(effectGraphs).forEach(effName => {
                    const graph = effectGraphs[effName];
                    const nodes = Array.from(graph.nodes);

                    let dfn = {}, low = {}, stack = [], inStack = {}, index = 0, sccs = [], sccMap = {};

                    function tarjan(u) {
                        dfn[u] = low[u] = ++index; stack.push(u); inStack[u] = true;
                        (graph.edges[u] || []).forEach(v => {
                            if (!dfn[v]) { tarjan(v); low[u] = Math.min(low[u], low[v]); }
                            else if (inStack[v]) { low[u] = Math.min(low[u], dfn[v]); }
                        });
                        if (dfn[u] === low[u]) {
                            let component = [];
                            while (true) {
                                let node = stack.pop(); inStack[node] = false;
                                component.push(node); sccMap[node] = sccs.length;
                                if (node === u) break;
                            }
                            sccs.push(component);
                        }
                    }
                    
                    nodes.forEach(n => { if (!dfn[n]) tarjan(n); });

                    let sccGraph = Array.from({length: sccs.length}, () => new Set());
                    let indegree = Array(sccs.length).fill(0);
                    let outdegree = Array(sccs.length).fill(0);

                    nodes.forEach(u => {
                        (graph.edges[u] || []).forEach(v => {
                            if (sccMap[u] !== sccMap[v]) {
                                if (!sccGraph[sccMap[u]].has(sccMap[v])) {
                                    sccGraph[sccMap[u]].add(sccMap[v]);
                                    indegree[sccMap[v]]++;
                                    outdegree[sccMap[u]]++;
                                }
                            }
                        });
                    });

                    let queue = [], effectiveness = {};
                    for (let i = 0; i < sccs.length; i++) {
                        if (indegree[i] === 0 && outdegree[i] > 0) queue.push(i);
                    }

                    let score = 95;
                    while (queue.length > 0) {
                        let size = queue.length, nextQueue = [];
                        for (let i = 0; i < size; i++) {
                            let u = queue[i];
                            sccs[u].forEach(drug => effectiveness[drug] = score);
                            sccGraph[u].forEach(v => { 
                                indegree[v]--; if (indegree[v] === 0) nextQueue.push(v); 
                            });
                        }
                        queue = nextQueue; score -= 5; if (score < 75) score = 75;
                    }
                    
                    sccs.forEach((component, i) => {
                        if (indegree[i] === 0 && outdegree[i] === 0) {
                            component.forEach(drug => { if (effectiveness[drug] === undefined) effectiveness[drug] = 95; });
                        }
                    });

                    nodes.forEach(drug => { if (effectiveness[drug] === undefined) effectiveness[drug] = 75; });
                    finalReport[effName] = effectiveness;
                });

                resolve(finalReport);
            } catch (err) {
                console.error("❌ runInference 演算法運算異常:", err);
                reject(err);
            }
        });
    });
}

// --- 5. 增量計算 API ---
app.post('/api/medicinal/inference/incremental', (req, res) => {
    const reportPath = './inference_report.json';
    let currentReport = { _metadata: {} };
    
    // 安全解析暫存 JSON 檔案
    if (fs.existsSync(reportPath)) {
        try {
            currentReport = JSON.parse(fs.readFileSync(reportPath, 'utf8'));
            if (!currentReport._metadata) currentReport._metadata = {};
        } catch (e) {
            console.warn("⚠️ 本地 inference_report.json 損毀或格式不合，已重設暫存物件。");
            currentReport = { _metadata: {} };
        }
    }

    const sql = `SELECT relation, updated_at FROM prescriptions WHERE relation IS NOT NULL AND relation != ''`;

    db.query(sql, async (err, results) => {
        if (err) {
            console.error("❌ 增量計算 SQL 查詢失敗:", err);
            return res.status(500).json({ msg: "資料庫查詢失敗" });
        }

        try {
            let dbIndicators = {}; 
            results.forEach(row => {
                const cleanRel = (row.relation || '').replace(/[\r\n]+/g, '');
                const effects = cleanRel.split(';'); 
                const rowTime = row.updated_at ? new Date(row.updated_at).getTime() : Date.now();
                
                effects.forEach(eff => {
                    const parts = eff.split(':'); 
                    if (parts.length < 2) return;
                    const name = parts[0].trim();
                    if (!dbIndicators[name]) dbIndicators[name] = { count: 0, maxTime: 0 };
                    dbIndicators[name].count += 1;
                    if (rowTime > dbIndicators[name].maxTime) dbIndicators[name].maxTime = rowTime;
                });
            });

            let affected = [];
            Object.keys(dbIndicators).forEach(name => {
                const dbCount = dbIndicators[name].count;
                const dbTime = dbIndicators[name].maxTime;
                const meta = currentReport._metadata[name] || { count: 0, last_updated: 0 };
                if (dbCount !== meta.count || dbTime > meta.last_updated) affected.push(name);
            });

            if (affected.length > 0) {
                const partialUpdate = await runInference(affected);
                affected.forEach(name => {
                    currentReport[name] = partialUpdate[name];
                    currentReport._metadata[name] = { 
                        count: dbIndicators[name].count, 
                        last_updated: dbIndicators[name].maxTime 
                    };
                });
                
                const finalOutput = {};
                Object.keys(currentReport).forEach(key => { 
                    if (key !== '_metadata') finalOutput[key] = currentReport[key]; 
                });
                finalOutput['_metadata'] = currentReport._metadata;
                
                fs.writeFileSync(reportPath, JSON.stringify(finalOutput, null, 2));
                res.json({ 
                    msg: `暫存成功！已更新本地暫存 JSON (受影響療效: ${affected.join(', ')})，請在前端確認無誤後上傳資料庫！`, 
                    data: finalOutput 
                });
            } else {
                const finalOutput = {};
                Object.keys(currentReport).forEach(key => { 
                    if (key !== '_metadata') finalOutput[key] = currentReport[key]; 
                });
                finalOutput['_metadata'] = currentReport._metadata;
                res.json({ msg: "本地數據已與原始資料一致，無需更新暫存檔。", data: finalOutput });
            }
        } catch (calcErr) {
            console.error("❌ 增量計算處理流程異常:", calcErr);
            res.status(500).json({ msg: "演算法運算異常" });
        }
    });
});

// --- 5. 推論與報表 API (從 MYSQL 讀取現有資料) ---
app.get('/api/medicinal/inference/report', (req, res) => {
    const sql = "SELECT effect_name, drug_name, score FROM inference_scores ORDER BY effect_name, score DESC";
    db.query(sql, (err, results) => {
        if (err) return res.status(500).send(err);
        
        let dbReport = {};
        results.forEach(row => {
            if (!dbReport[row.effect_name]) dbReport[row.effect_name] = {};
            dbReport[row.effect_name][row.drug_name] = parseFloat(row.score);
        });
        res.json(dbReport);
    });
});

// 將確認完畢後的效力值「完全覆蓋」寫入資料庫的專屬 API
app.post('/api/medicinal/inference/save-to-db', (req, res) => {
    const reportPath = './inference_report.json';
    if (!fs.existsSync(reportPath)) {
        return res.status(404).send({ msg: "找不到本地暫存的 inference_report.json，請先點擊計算！" });
    }

    try {
        const localData = JSON.parse(fs.readFileSync(reportPath, 'utf8'));
        let insertValues = [];

        Object.keys(localData).forEach(effName => {
            if (effName === '_metadata') return;
            Object.keys(localData[effName]).forEach(drugName => {
                const score = localData[effName][drugName];
                insertValues.push([effName, drugName, score]);
            });
        });

        if (insertValues.length === 0) {
            return res.send({ msg: "暫存檔案中無有效數據，無需同步。" });
        }

        const truncateSql = "TRUNCATE TABLE inference_scores";
        db.query(truncateSql, (truncErr) => {
            if (truncErr) {
                console.error("清空資料庫失敗:", truncErr);
                return res.status(500).send({ msg: "無法清空舊有資料庫數據", error: truncErr });
            }

            const insertSql = "INSERT INTO inference_scores (effect_name, drug_name, score) VALUES ?";
            db.query(insertSql, [insertValues], (insertErr, result) => {
                if (insertErr) {
                    console.error("寫入新資料失敗:", insertErr);
                    return res.status(500).send({ msg: "舊資料已刪除，但寫入新數據失敗！", error: insertErr });
                }
                res.send({ msg: `🎉 審核查驗完畢！已成功「完全覆蓋」並同步 ${insertValues.length} 筆效力值數據至正式資料庫！` });
            });
        });

    } catch (e) {
        console.error("解析本地檔案失敗:", e);
        res.status(500).send({ msg: "解析本地暫存檔案失敗" });
    }
});



// 計算完成先將資料存在本地 JSON (全量)
app.post('/api/medicinal/inference/global', async (req, res) => {
    const fullReport = await runInference();
    const sql = `SELECT relation, updated_at FROM prescriptions WHERE relation IS NOT NULL AND relation != ''`;
    db.query(sql, (err, results) => {
        if (err) return res.status(500).send(err);
        let metadata = {};
        results.forEach(row => {
            const effects = row.relation.split(';');
            const rowTime = new Date(row.updated_at).getTime();
            effects.forEach(eff => {
                const parts = eff.split(':'); if (parts.length < 2) return;
                const name = parts[0].trim();
                if (!metadata[name]) metadata[name] = { count: 0, last_updated: 0 };
                metadata[name].count += 1;
                if (rowTime > metadata[name].last_updated) metadata[name].last_updated = rowTime;
            });
        });
        const finalOutput = {};
        Object.keys(fullReport).forEach(key => finalOutput[key] = fullReport[key]);
        finalOutput['_metadata'] = metadata;
        fs.writeFileSync('./inference_report.json', JSON.stringify(finalOutput, null, 2));
        res.json({ msg: "全域重新校準完成！最新效力值已成功存於本地暫存 JSON，請查驗後發布至資料庫！", data: finalOutput });
    });
});

// 讀取 MYSQL 的 contraindications 表
app.get('/api/medicinal/dispensing/contraindications', (req, res) => {
    const sql = "SELECT drug_name, toxic_companions FROM contraindications";
    db.query(sql, (err, results) => {
        if (err) return res.status(500).send(err);
        
        let contraMap = {};
        results.forEach(row => {
            try {
                contraMap[row.drug_name] = JSON.parse(row.toxic_companions);
            } catch(e) {
                contraMap[row.drug_name] = row.toxic_companions.split(',');
            }
        });
        res.json(contraMap);
    });
});

// --- 新增：根據證候查詢對應方劑名稱 ---
// --- 根據證候查詢對應方劑名稱 api/medicinal ---
app.get('/api/medicinal/syndrome/prescriptions', (req, res) => {
    const { syndrome } = req.query;
    if (!syndrome) return res.status(400).send({ msg: "缺少證候參數" });

    // ✅ 使用 GROUP BY + MAX(effect_value) 完美破解 MySQL ONLY_FULL_GROUP_BY 限制！
    const sql = `
        SELECT prescription, MAX(effect_value) AS score 
        FROM medicinal_prescription_syndrome_mapping_all 
        WHERE syndrome = ? 
        GROUP BY prescription 
        ORDER BY score DESC
    `;
    
    db.query(sql, [syndrome], (err, results) => {
        if (err) {
            console.error("SQL 查詢詳細錯誤訊息:", err);
            return res.status(500).send({ msg: `資料庫查詢失敗: ${err.message}` });
        }
        const prescriptions = results.map(row => row.prescription);
        res.json(prescriptions);
    });
});

// --- 新增：取得指定證候的方劑與原始分數 (供多證候加權計算使用) ---
app.get('/api/medicinal/syndrome/prescriptions-with-score', (req, res) => {
    const { syndrome } = req.query;
    if (!syndrome) return res.status(400).send({ msg: "缺少證候參數" });

    const sql = "SELECT prescription, effect_value FROM medicinal_prescription_syndrome_mapping_all WHERE syndrome = ?";
    db.query(sql, [syndrome], (err, results) => {
        if (err) return res.status(500).send(err);
        res.json(results); // 回傳 [{ prescription: '麻黃湯', effect_value: 100 }, ...]
    });
});

// --- 新增：取得所有可用證候選單清單 ---
app.get('/api/medicinal/syndrome/list', (req, res) => {
    const sql = "SELECT DISTINCT `syndrome` FROM `medicinal_prescription_syndrome_mapping_all`";
    db.query(sql, (err, results) => {
        if (err) {
            console.error("SQL 證候清單查詢失敗:", err);
            return res.status(500).send({ msg: "無法取得證候清單" });
        }
        res.json(results.map(row => row.syndrome));
    });
});

// --- 6. 專屬草稿功能 ---
app.post('/api/medicinal/draft/add', (req, res) => {
    const { name, symptoms, king, minister, assistant, envoy, relation } = req.body;
    const sql = "INSERT INTO draft_prescriptions (name, symptoms, king, minister, assistant, envoy, relation) VALUES (?, ?, ?, ?, ?, ?, ?)";
    db.query(sql, [name, symptoms, king, minister, assistant, envoy, relation], (err, result) => {
        if (err) {
            console.error(err);
            return res.status(500).send({msg: "草稿寫入失敗"});
        }
        res.send({msg: "成功新增一筆草稿資料！（已安全寫入 draft_prescriptions 表）"});
    });
});

app.get('/api/medicinal/draft/all', (req, res) => {
    const sql = "SELECT * FROM draft_prescriptions ORDER BY id DESC"; 
    db.query(sql, (err, results) => {
        if (err) return res.status(500).send(err);
        res.json(results);
    });
});

app.listen(3000, () => console.log('TCM Server Running: http://localhost:3000'));