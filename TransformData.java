import org.json.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

public class TransformData {
    public static void main(String[] args) {
        try {
            // อ่านไฟล์ JSON
            String content = new String(Files.readAllBytes(Paths.get("rawdata.txt")));
            JSONObject rawData = new JSONObject(content);

            // ดึงข้อมูลจาก JSON
            JSONArray nodesArray = rawData.getJSONArray("nodes");
            JSONArray edgesArray = rawData.getJSONArray("edges");

            // ใช้ LinkedHashSet เพื่อเรียงลำดับ nodes ตามที่พบใน edges
            LinkedHashSet<String> nodeOrderSet = new LinkedHashSet<>();

            // ดึงลำดับของโหนดจาก edges
            for (int i = 0; i < edgesArray.length(); i++) {
                JSONObject edge = edgesArray.getJSONObject(i);
                nodeOrderSet.add(edge.getString("source").trim());
                nodeOrderSet.add(edge.getString("target").trim());
            }

            // Mapping nodeId -> nodeType
            Map<String, String> nodeTypeMap = new HashMap<>();
            for (int i = 0; i < nodesArray.length(); i++) {
                JSONObject node = nodesArray.getJSONObject(i);
                nodeTypeMap.put(node.getString("id").trim(), node.getString("type").trim());
            }

            // แปลงเป็น List ตามลำดับที่พบใน edges
            List<String> orderedNodes = new ArrayList<>(nodeOrderSet);
            List<String> Nodes = new ArrayList<>();
            List<String> addressIn = new ArrayList<>(Collections.nCopies(orderedNodes.size(), ""));
            List<String> addressOut = new ArrayList<>(Collections.nCopies(orderedNodes.size(), ""));

            // Map id -> index
            Map<String, Integer> nodeIndexMap = new HashMap<>();
            for (int i = 0; i < orderedNodes.size(); i++) {
                String nodeId = orderedNodes.get(i);
                Nodes.add(nodeTypeMap.getOrDefault(nodeId, nodeId));
                nodeIndexMap.put(nodeId, i);
            }

            // จัดการข้อมูล Edge
            for (int i = 0; i < edgesArray.length(); i++) {
                JSONObject edge = edgesArray.getJSONObject(i);
                String source = edge.getString("source").trim();
                String target = edge.getString("target").trim();

                Integer sourceIndex = nodeIndexMap.get(source);
                Integer targetIndex = nodeIndexMap.get(target);

                if (sourceIndex != null && targetIndex != null) {
                    // เพิ่มข้อมูล addressOut
                    if (addressOut.get(sourceIndex).isEmpty()) {
                        addressOut.set(sourceIndex, target);
                    } else {
                        addressOut.set(sourceIndex, addressOut.get(sourceIndex) + ", " + target);
                    }

                    // เพิ่มข้อมูล addressIn
                    if (addressIn.get(targetIndex).isEmpty()) {
                        addressIn.set(targetIndex, source);
                    } else {
                        addressIn.set(targetIndex, addressIn.get(targetIndex) + ", " + source);
                    }
                }
            }

            // ลบเฉพาะ 'input-node-1' ออกจาก addressIn ก่อนแสดงผล
            List<String> updatedAddressIn = new ArrayList<>();
            for (int i = 0; i < addressIn.size(); i++) {
                // ลบ 'input-node-1' แต่เก็บค่า empty string ('') เอาไว้
                if (addressIn.get(i).equals("input-node-1")) {
                    continue;
                }
                updatedAddressIn.add(addressIn.get(i));
            }

            // นำค่าจาก updatedAddressIn มาใส่ใน addressIn
            addressIn.clear();
            addressIn.addAll(updatedAddressIn);


            // แสดงผลลัพธ์ในรูปแบบที่มีเครื่องหมายคำพูด
            System.out.println("Nodes = " + formatListWithQuotes(Nodes));
            System.out.println("addressIn = " + formatListWithQuotes(addressIn));
            System.out.println("addressOut = " + formatListWithQuotes(addressOut));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    // จัดรูปแบบให้มีเครื่องหมายคำพูด
    private static String formatListWithQuotes(List<String> list) {
        List<String> quotedList = new ArrayList<>();
        for (String item : list) {
            quotedList.add("'" + item + "'");
        }
        return quotedList.toString();
    }
}