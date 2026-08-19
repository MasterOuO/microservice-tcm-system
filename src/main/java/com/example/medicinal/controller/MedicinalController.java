package com.example.medicinal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/medicinal")
public class MedicinalController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String REPORT_PATH = "./inference_report.json";

    // --- 1. 新增正式資料 API ---
    @PostMapping("/add")
    public ResponseEntity<?> addPrescription(@RequestBody Map<String, Object> body) {
        String sql = "INSERT INTO prescriptions (name, symptoms, king, minister, assistant, envoy, relation) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, 
                body.get("name"), body.get("symptoms"), body.get("king"), 
                body.get("minister"), body.get("assistant"), body.get("envoy"), body.get("relation"));
            return ResponseEntity.ok(Map.of("msg", "成功新增一筆資料！"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("msg", "寫入失敗"));
        }
    }

    // --- 2. 讀取全部資料 API ---
    @GetMapping("/all")
    public ResponseEntity<?> getAllPrescriptions() {
        String sql = "SELECT * FROM prescriptions ORDER BY id DESC";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(results);
    }

    // --- 3. 讀取報表 API (從 MySQL 讀取) ---
    @GetMapping("/inference/report")
    public ResponseEntity<?> getInferenceReport() {
        String sql = "SELECT effect_name, drug_name, score FROM inference_scores ORDER BY effect_name, score DESC";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        Map<String, Map<String, Double>> dbReport = new HashMap<>();
        for (Map<String, Object> row : results) {
            String effectName = (String) row.get("effect_name");
            String drugName = (String) row.get("drug_name");
            Double score = Double.parseDouble(row.get("score").toString());

            dbReport.computeIfAbsent(effectName, k -> new HashMap<>()).put(drugName, score);
        }
        return ResponseEntity.ok(dbReport);
    }

    // --- 4. 覆蓋寫入資料庫 API ---
    @PostMapping("/inference/save-to-db")
    public ResponseEntity<?> saveToDb() {
        File file = new File(REPORT_PATH);
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("msg", "找不到本地暫存的 inference_report.json，請先點擊計算！"));
        }

        try {
            Map<String, Object> localData = objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {});
            List<Object[]> insertValues = new ArrayList<>();

            for (Map.Entry<String, Object> entry : localData.entrySet()) {
                String effName = entry.getKey();
                if ("_metadata".equals(effName)) continue;

                @SuppressWarnings("unchecked")
                Map<String, Object> drugs = (Map<String, Object>) entry.getValue();
                for (Map.Entry<String, Object> drugEntry : drugs.entrySet()) {
                    insertValues.add(new Object[]{effName, drugEntry.getKey(), drugEntry.getValue()});
                }
            }

            if (insertValues.isEmpty()) {
                return ResponseEntity.ok(Map.of("msg", "暫存檔案中無有效數據，無需同步。"));
            }

            jdbcTemplate.execute("TRUNCATE TABLE inference_scores");
            String insertSql = "INSERT INTO inference_scores (effect_name, drug_name, score) VALUES (?, ?, ?)";
            jdbcTemplate.batchUpdate(insertSql, insertValues);

            return ResponseEntity.ok(Map.of("msg", "🎉 審核查驗完畢！已成功「完全覆蓋」並同步 " + insertValues.size() + " 筆效力值數據至正式資料庫！"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("msg", "解析本地暫存檔案失敗或資料庫操作失敗"));
        }
    }

    // --- 5. 讀取禁忌表 API ---
    @GetMapping("/dispensing/contraindications")
    public ResponseEntity<?> getContraindications() {
        String sql = "SELECT drug_name, toxic_companions FROM contraindications";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        Map<String, Object> contraMap = new HashMap<>();
        for (Map<String, Object> row : results) {
            String drugName = (String) row.get("drug_name");
            String toxicCompanions = (String) row.get("toxic_companions");

            try {
                contraMap.put(drugName, objectMapper.readValue(toxicCompanions, List.class));
            } catch (Exception e) {
                contraMap.put(drugName, Arrays.asList(toxicCompanions.split(",")));
            }
        }
        return ResponseEntity.ok(contraMap);
    }

    // --- 6. 根據證候查詢方劑名稱 ---
    @GetMapping("/syndrome/prescriptions")
    public ResponseEntity<?> getPrescriptionsBySyndrome(@RequestParam(value = "syndrome", required = false) String syndrome) {
        if (syndrome == null || syndrome.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("msg", "缺少證候參數"));
        }

        String sql = "SELECT prescription, MAX(effect_value) AS score " +
                     "FROM medicinal_prescription_syndrome_mapping_all " +
                     "WHERE syndrome = ? " +
                     "GROUP BY prescription " +
                     "ORDER BY score DESC";

        try {
            List<String> prescriptions = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("prescription"), syndrome);
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("msg", "資料庫查詢失敗: " + e.getMessage()));
        }
    }

    // --- 7. 取得指定證候的方劑與分數 ---
    @GetMapping("/syndrome/prescriptions-with-score")
    public ResponseEntity<?> getPrescriptionsWithScore(@RequestParam(value = "syndrome", required = false) String syndrome) {
        if (syndrome == null || syndrome.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("msg", "缺少證候參數"));
        }

        String sql = "SELECT prescription, effect_value FROM medicinal_prescription_syndrome_mapping_all WHERE syndrome = ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, syndrome);
        return ResponseEntity.ok(results);
    }

    // --- 8. 取得證候選單清單 ---
    @GetMapping("/syndrome/list")
    public ResponseEntity<?> getSyndromeList() {
        String sql = "SELECT DISTINCT `syndrome` FROM `medicinal_prescription_syndrome_mapping_all`";
        try {
            List<String> syndromes = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("syndrome"));
            return ResponseEntity.ok(syndromes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("msg", "無法取得證候清單"));
        }
    }

    // --- 9. 草稿 API ---
    @PostMapping("/draft/add")
    public ResponseEntity<?> addDraft(@RequestBody Map<String, Object> body) {
        String sql = "INSERT INTO draft_prescriptions (name, symptoms, king, minister, assistant, envoy, relation) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, 
                body.get("name"), body.get("symptoms"), body.get("king"), 
                body.get("minister"), body.get("assistant"), body.get("envoy"), body.get("relation"));
            return ResponseEntity.ok(Map.of("msg", "成功新增一筆草稿資料！（已安全寫入 draft_prescriptions 表）"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("msg", "草稿寫入失敗"));
        }
    }

    @GetMapping("/draft/all")
    public ResponseEntity<?> getAllDrafts() {
        String sql = "SELECT * FROM draft_prescriptions ORDER BY id DESC";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(results);
    }

    // --- 10. 網址測試 API (對應原本 /api/test) ---
    @GetMapping("/dbtest")
    public ResponseEntity<?> dbTest(
            @RequestParam(value = "medicine", required = false) String medicine,
            @RequestParam(value = "qty", required = false) String qty) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "資料庫連線與 API 測試成功！");
        response.put("medicine", medicine != null ? medicine : "未提供藥材");
        response.put("qty", qty != null ? qty : "0");
        
        return ResponseEntity.ok(response);
    }
    
    
}

