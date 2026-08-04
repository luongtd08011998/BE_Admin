package vn.hoidanit.springrestwithai.qlkh.paybill;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PaybillChecksumUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaybillChecksumUtil.class);

    public static String createCheckSum(Map<String, String> parameters, String password) {
        // Dùng natural TreeMap (case-sensitive, ordinal) giống C# StringComparer.Ordinal
        // Uppercase (A-Z = 65-90) sẽ đứng trước lowercase (a-z = 97-122)
        Map<String, String> sortedParams = new TreeMap<>();
        sortedParams.putAll(parameters);

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> kv : sortedParams.entrySet()) {
            if (kv.getKey() == null || kv.getKey().isEmpty() || kv.getValue() == null || kv.getValue().isEmpty()) {
                continue;
            }
            if (kv.getKey().equalsIgnoreCase("checksum")) {
                continue;
            }
            query.append(kv.getKey()).append(kv.getValue());
        }

        String data = query.toString() + password + query.toString();
        log.info("[Paybill Checksum] Parameters: {}", sortedParams);
        log.info("[Paybill Checksum] Data to hash: {}", data);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String result = hexString.toString();
            log.info("[Paybill Checksum] Result: {}", result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error generating SHA-256 checksum", e);
        }
    }

    public static Map<String, String> getDicParamFromJson(String jsonData) {
        try {
            Map<String, String> dicTemp = new HashMap<>();
            if (jsonData == null || jsonData.isEmpty()) {
                return null;
            }
            JsonNode jsonNode = mapper.readTree(jsonData);

            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey().toLowerCase(); // C#: item.Path.ToLower()
                JsonNode value = field.getValue();

                if (key.equals("bills")) {
                    StringBuilder billIds = new StringBuilder();
                    long moneys = 0;
                    TreeSet<String> setBillIds = new TreeSet<>();

                    if (value.isArray()) {
                        for (JsonNode bill : value) {
                            String billId = null;
                            if (bill.has("BillId")) billId = bill.get("BillId").asText();
                            else if (bill.has("billId")) billId = bill.get("billId").asText();
                            else if (bill.has("billid")) billId = bill.get("billid").asText();
                            if (billId != null) setBillIds.add(billId);

                            if (bill.has("Money")) moneys += bill.get("Money").asLong();
                            else if (bill.has("money")) moneys += bill.get("money").asLong();
                        }
                    }

                    for (String val : setBillIds) {
                        billIds.append(val);
                    }

                    dicTemp.put("billids", billIds.toString());
                    dicTemp.put("moneys", String.valueOf(moneys));
                    continue;
                }

                if (!value.isNull() && !value.isArray() && !value.isObject()) {
                    // C#: item.First.ToString() keeps original case of the value.
                    dicTemp.put(key, value.asText());
                } else if (value.isNull()) {
                    // C# JToken null .ToString() gives empty string
                    dicTemp.put(key, "");
                }
            }
            return dicTemp;
        } catch (Exception ex) {
            return null;
        }
    }
}
