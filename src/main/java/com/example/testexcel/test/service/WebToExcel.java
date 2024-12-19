package com.example.testexcel.test.service;


import com.example.testexcel.test.TokenManager;
import com.example.testexcel.test.UserContext;
import com.example.testexcel.test.pojo.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
@Service
public class WebToExcel {
    
    @Resource
    private RestTemplate restTemplate;
    
    @Value("${idsUrl}")
    private String idsUrl;
    
    @Value("${dataUrl}")
    private String dataUrl;
    @Value("${imageListUrl}")
    private String imageListUrl;
    
    @Value("${DOWNLOAD_DIR}")
    private String DOWNLOAD_DIR;
    
    private int cnt=0;

    public static List<Map<String, Object>> tableDataList = new ArrayList<>();

   public static List<String> ids=new ArrayList<>();
   public static List<Map<String,String>> buildIds=new ArrayList<>();
    
   public static Map<String,List<String>> imageListMap=new HashMap<>();

   private Map<String, Map<String, String>> photoRemarkMap = new HashMap<>();




    public String test() throws Exception {
//        this.idsUrl=idsUrl;
        String token = getToken("13641787676", "vE77EM9");
//        getIdSet(token,"13641787676");
//        fillTableData();
//        writeDataToExcel();
//        fillImageListMap();
//        downLoadPictureById();
        
//        2
        fillTableDataAndIDS();
        downLoadPictureById2();
        getPhotoRemarkMap();
        fillEstimateArea();
        exportPhotoRemarkToExcel();
        return "success";
    }

    public String getToken(String username, String password) throws URISyntaxException, JsonProcessingException {

        HashMap<String, String> map=new HashMap<>();
        map.put("userId",username);
        map.put("password",password);

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);

        // 发送请求
        ResponseEntity<String> response = restTemplate.exchange("http://10.83.248.236/restapi/web/login", HttpMethod.POST, request, String.class);



        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        String token = rootNode.path("data").path("pcToken").asText();
        TokenManager.setToken(token);
        UserContext.setUser(new User(username));
        return token;
    }

    public  List<String> getIdSet(String token, String userId) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.set("pcToken", TokenManager.getToken());
        headers.set("pcUserId", UserContext.getUser().getUserId());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(idsUrl, HttpMethod.GET, entity, String.class);
        // 解析响应并提取ID
        ids = extractIdsFromResponse(response.getBody());
//        List<String> list =List.of("id","buildIdGov","residenceType","addrAreaName","addrStreetName","addrCommunityName","addrRoadName","addrNo","addrBuildNo","addrCellNo","buildName","houseOwnership","houseTypeResidentialPeopleNum","selfBuildFlag","houseTypeResidential","buildDate","buildHigh","buildArea","buildAreaUnder","buildPliesOver","buildPliesUnder","protectBuildFlag","structureType","subJglx","trabecularPlate","useChanged","usedForBusiness","peopleAggregated","abnormalSituation","modificationFlag","modificationContent","modificationYear","approvalFormality","damageStatus","censusUserName","censusDate","censusMobile","allayEarthquakeFlag","antiEarthquakeFlag","antiEarthquakeDate","antiEarthquakeWay","realtyManagementFlag","realtyManagementName","emergencyFlag","outWallType","outWallWarmFlag","tdxzxflx","qttdxzxflx");
//        writeDataToExcel(tableDataList, "output.xlsx");
        System.out.println("IDsSize: " + ids.size());
        return ids; // Placeholder
    }


    private void fillTableData() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("pcToken", TokenManager.getToken());
        headers.set("pcUserId", UserContext.getUser().getUserId());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        for (String id : ids) {
            Thread.sleep(5);
            String detailUrl = dataUrl + id;
            ResponseEntity<String> detailResponse = restTemplate.exchange(detailUrl, HttpMethod.GET, entity, String.class);
            Map<String, Object> data = extractDataFromResponse(detailResponse.getBody());
            tableDataList.add(data);
        }
    }
    
    private void fillImageListMap() throws InterruptedException, JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("pcToken", TokenManager.getToken());
        headers.set("pcUserId", UserContext.getUser().getUserId());
        headers.set("Cookie","accessArea=30; innerTaskUrl=; pcRole=0_1_11_27_28_43_53;"+" pcToken="+TokenManager.getToken()+" ;pcUserId=13641787676");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        for (String id : ids) {
            Thread.sleep(5);
            String imageListUrlById = imageListUrl + id;
            ResponseEntity<String> detailResponse = restTemplate.exchange(imageListUrlById, HttpMethod.GET, entity, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(detailResponse.getBody());
            JsonNode dataNode = rootNode.path("data");
            List<String> imagePaths = new ArrayList<>();

            if (dataNode.isArray()) {
                for (JsonNode node : dataNode) {
                    String imagePath = node.path("imageLocalFullPath").asText();
                    String imageType = node.path("imageType").asText();
                    if ("1".equals(imageType)||"2".equals(imageType)) {
                        imagePaths.add(imagePath);
                    }
                }
            }
            imageListMap.put(id, imagePaths);
        }
        System.out.println("imageFillOver,size:"+imageListMap.size());
    }
    public void downLoadPictureById() throws IOException, InterruptedException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("pcToken", TokenManager.getToken());
        headers.set("pcUserId", UserContext.getUser().getUserId());
        headers.set("Cookie","accessArea=30; innerTaskUrl=; pcRole=0_1_11_27_28_43_53; pcToken=569EDB7029E447F9B4AA87582428C7CE; pcUserId=13641787676");


        for (Map.Entry<String, List<String>> entry : imageListMap.entrySet()) {
            String id = entry.getKey();
            List<String> urls = entry.getValue();
            String dirPath = DOWNLOAD_DIR + File.separator + id;

            // 创建ID子目录
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (String url : urls) {
//                if (cnt>=100){
//                    Thread.sleep(1000*60);
//                    cnt=0;
//                }
                Thread.sleep(10);
                try {
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        String fileName = url.substring(url.lastIndexOf('/') + 1);
                        File file = new File(dirPath + File.separator + fileName);

                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(response.getBody());
                        }
                        cnt++;

                        System.out.println("Downloaded: " + file.getAbsolutePath());
                    } else {
                        System.err.println("Failed to download image from URL: " + url + ", Status Code: " + response.getStatusCode());
                    }
                } catch (Exception e) {
                    System.err.println("Error downloading image from URL: " + url + ", Message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        System.out.println("picture download over");
    }
    public void downLoadPictureById2() throws IOException, InterruptedException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("pcToken", TokenManager.getToken());
        headers.set("pcUserId", UserContext.getUser().getUserId());

        // 清空 photoRemarkMap
        photoRemarkMap.clear();

        for (String id : ids) {
            // 构建获取图片列表的URL
            String photoListUrl = "http://10.83.248.236/restapi/web/nf/yhpc/getByBh/" + id;
            
            try {
                // 获取图片列表
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> photoListResponse = restTemplate.exchange(
                    photoListUrl, 
                    HttpMethod.GET, 
                    entity, 
                    String.class
                );

                // 解析响应获取图片URL列表
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(photoListResponse.getBody());
                JsonNode photoList = rootNode.path("data").path("photoList");

                // 创建当前id的下载目录
                String dirPath = DOWNLOAD_DIR + File.separator + id;
                File dir = new File(dirPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // 获取 photoRemark
                JsonNode photoRemarkNode = rootNode.path("data").path("photoRemark");
                if (!photoRemarkNode.isMissingNode() && !photoRemarkNode.isNull()) {
                    Map<String, String> remarkProperties = new HashMap<>();
                    remarkProperties.put("photoRemark",photoRemarkNode.asText());
                    photoRemarkMap.put(id, remarkProperties);
                }

                // 下载每张图片
                if (photoList.isArray()) {
                    for (JsonNode photo : photoList) {
                        String imageUrl = photo.path("imageLocalFullPath").asText();
                        Thread.sleep(10);

                        try {
                            ResponseEntity<byte[]> response = restTemplate.exchange(
                                imageUrl, 
                                HttpMethod.GET, 
                                entity, 
                                byte[].class
                            );

                            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                                String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                                File file = new File(dirPath + File.separator + fileName);

                                try (FileOutputStream fos = new FileOutputStream(file)) {
                                    fos.write(response.getBody());
                                }
                                cnt++;
                                System.out.println("Downloaded: " + file.getAbsolutePath());
                            } else {
                                System.err.println("Failed to download image from URL: " + imageUrl + 
                                                ", Status Code: " + response.getStatusCode());
                            }
                        } catch (Exception e) {
                            System.err.println("Error downloading image from URL: " + imageUrl + 
                                            ", Message: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing ID: " + id + ", Message: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Picture download over, collected photoRemarks for " + photoRemarkMap.size() + " IDs");
    }

    private Map<String, Object> extractDataFromResponse(String jsonResponse) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode dataNode = root.path("data");

        return objectMapper.convertValue(dataNode, Map.class);
    }
    private  void writeDataToExcel() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        // 写入表头
        Row headerRow = sheet.createRow(0);
        if (!tableDataList.isEmpty()) {
            Map<String, Object> firstData = tableDataList.get(0);
            int colNum = 0;
            for (String key : firstData.keySet()) {
                headerRow.createCell(colNum++).setCellValue(key);
            }
        }

        int rowNum = 1;
        for (Map<String, Object> data : tableDataList) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Object value = entry.getValue();
                if ("censusDate".equals(entry.getKey()) && value instanceof Number) {
                    long timestamp = ((Number) value).longValue();
                    String formattedDate = convertTimestampToDateTime(timestamp);
                    row.createCell(colNum++).setCellValue(formattedDate);
                } else {
                    row.createCell(colNum++).setCellValue(value != null ? value.toString() : "");
                }
            }
        }

        try (FileOutputStream fileOut = new FileOutputStream("output.xlsx")) {
            workbook.write(fileOut);
        }
        System.out.println("excel over");

        workbook.close();
    }


    public static String convertTimestampToDateTime(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTime.format(formatter);
    }

    private List<String> extractIdsFromResponse(String jsonResponse) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode escrList = root.path("data").path("escrList");

        List<String> ids = new ArrayList<>();
        if (escrList.isArray()) {
            for (JsonNode node : escrList) {
                String id = node.path("id").asText();
                ids.add(id);
            }
        }

        return ids;
    }
    public void empty() {
        ids=new ArrayList<>();
        tableDataList = new ArrayList<>();
        imageListMap=new HashMap<>();
    }

    private void fillTableDataAndIDS() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("pcToken", TokenManager.getToken());
        headers.set("pcUserId", UserContext.getUser().getUserId());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("pageNum", 1);
        requestBody.put("pageSize", 2000);
        requestBody.put("yhpcStatus", "2");
        requestBody.put("addrAreaCode", "30");
        requestBody.put("addrStreetCode", "1909");
        requestBody.put("bxypcyy", List.of("isEmpty"));
        // 其他参数...

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            dataUrl,
            HttpMethod.POST,
            entity,
            String.class
        );

        // 解析响应数据
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.getBody());
        JsonNode listNode = rootNode.path("data").path("list");
        tableDataList=new ArrayList<>();
        if (listNode.isArray()) {
            for (JsonNode item : listNode) {
                // 提取id并添加到ids列表中
                String id = item.path("id").asText();
                ids.add(id);
                String buildId = item.path("buildIdGov").asText();
                Map<String,String> buildIdMap=new HashMap<>();
                buildIdMap.put("id",id);
                buildIdMap.put("buildId",buildId);
                buildIds.add(buildIdMap);
                // 原有的数据处理逻辑
                Map<String, Object> data = objectMapper.convertValue(item, Map.class);
                tableDataList.add(data);
            }
        }

    }

    public Map<String, Map<String, String>> getPhotoRemarkMap() {
        return photoRemarkMap;
    }

    public void fillEstimateArea() throws InterruptedException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("pcToken", TokenManager.getToken());
        headers.set("pcUserId", UserContext.getUser().getUserId());

        for (Map<String, String> buildIdMap : buildIds) {
            String id = buildIdMap.get("id");
            String buildId = buildIdMap.get("buildId");
            
            if (buildId == null || buildId.isEmpty()) {
                continue;
            }

            String estimateAreaUrl = "http://10.83.248.236/restapi/web/area/shadowArea/" + buildId;
            
            try {
//                Thread.sleep(10); // 添加短暂延迟避免请求过快
                
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(
                    estimateAreaUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
                );

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                double estimateArea = rootNode.path("data").asDouble();

                // 获取或创建该id的remarkMap
                Map<String, String> remarkMap = photoRemarkMap.computeIfAbsent(id, k -> new HashMap<>());
                // 添加estimateArea信息
                remarkMap.put("projectedArea", String.valueOf(estimateArea));

            } catch (Exception e) {
                System.err.println("Error processing buildId: " + buildId + ", Message: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("Estimate area data collection completed for " + buildIds.size() + " buildings");
    }

    public void exportPhotoRemarkToExcel() throws IOException {
        System.out.println("totle:" + photoRemarkMap.size());
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("PhotoRemarkData");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("id");
        headerRow.createCell(1).setCellValue("projectedArea");
        headerRow.createCell(2).setCellValue("photoRemark");

        // 写入数据
        int rowNum = 1;
        for (Map.Entry<String, Map<String, String>> entry : photoRemarkMap.entrySet()) {
            String id = entry.getKey();
            Map<String, String> properties = entry.getValue();
            
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(id);
            row.createCell(1).setCellValue(properties.getOrDefault("projectedArea", ""));
            row.createCell(2).setCellValue(properties.getOrDefault("photoRemark", ""));
        }

        // 自动调整列宽
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }

        // 保存文件
        try (FileOutputStream fileOut = new FileOutputStream("otherProperties.xlsx")) {
            workbook.write(fileOut);
        }

        workbook.close();
        System.out.println("Excel file created successfully: ");
    }
}