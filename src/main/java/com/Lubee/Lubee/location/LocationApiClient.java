package com.Lubee.Lubee.location;

import com.Lubee.Lubee.enumset.Category;
import com.Lubee.Lubee.location.domain.Location;
import com.Lubee.Lubee.location.dto.SeoulLocationInfo;
import com.Lubee.Lubee.location.mapper.LocationMapper;
import com.Lubee.Lubee.location.repository.LocationRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationApiClient {

    @Value("${RESTAURANT_API_KEY}")
    private String restaurantApiKey;             // 서울시 일반음식점
    @Value("${CULTURE_API_KEY}")
    private String cultureApiKey;             // 서울시 문화 공간

    private static final String BASE_URL = "http://openapi.seoul.go.kr:8088/";
    private static final String RESTAURANT_SUFFIX = "/json/LOCALDATA_072404/";
    private static final String CULTURE_SUFFIX = "/json/culturalSpaceInfo/";
    private static final int MAX_ITEMS = 1000;      // API당 한번에 최대 1000개 요청 가능

    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private LocationMapper locationMapper;

    public void loadRestaurantLocations() {
        loadData(restaurantApiKey, RESTAURANT_SUFFIX, Category.RESTAURANT);
    }

    public void loadCultureLocations() {
        loadData(cultureApiKey, CULTURE_SUFFIX, Category.CULTURE);
    }

    // API에서 데이터를 가져옴 -> 저장
    private void loadData(String apiKey, String urlSuffix, Category category) {
        int startReq = 1;
        int endReq = MAX_ITEMS;
        boolean hasNext = true;

        while (hasNext) {
            try {
                String urlStr = BASE_URL + apiKey + urlSuffix + startReq + "/" + endReq;
                String jsonData = fetchDataFromUrl(urlStr);     // String으로 변환
                hasNext = saveLocationData(jsonData, category); // DB에 저장

                startReq += MAX_ITEMS;
                endReq += MAX_ITEMS;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    // API에서 데이터를 가져와 String으로 반환
    private String fetchDataFromUrl(String urlStr) throws Exception {

        StringBuilder result = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(urlStr).openStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }

    // API로부터 받은 JSON 데이터를 파싱 -> 각각의 항목을 SeoulLocationInfo 객체로 변환 -> 데이터베이스에 저장
    private boolean saveLocationData(String jsonData, Category category) {

        List<SeoulLocationInfo> seoulLocationInfos = new ArrayList<>();

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
            JSONObject dataObject = (JSONObject) jsonObject.get(category == Category.RESTAURANT ? "LOCALDATA_072404" : "culturalSpaceInfo");
            long totalLocations = (long) dataObject.get("list_total_count");        // API에서 제공하는 총 공간 개수
            JSONArray jsonArray = (JSONArray) dataObject.get("row");

            for (Object o : jsonArray) {
                JSONObject obj = (JSONObject) o;

                // null 체크 후 기본값 설정
                String name = obj.get(category == Category.RESTAURANT ? "BPLCNM" : "FAC_NAME") != null ?
                        obj.get(category == Category.RESTAURANT ? "BPLCNM" : "FAC_NAME").toString() : "";
                String address = obj.get(category == Category.RESTAURANT ? "RDNWHLADDR" : "ADDR") != null ?
                        obj.get(category == Category.RESTAURANT ? "RDNWHLADDR" : "ADDR").toString() : "";
                String xCoord = obj.get("X") != null ? obj.get("X").toString() : "0.0";
                String yCoord = obj.get("Y") != null ? obj.get("Y").toString() : "0.0";

                SeoulLocationInfo info = SeoulLocationInfo.builder()
                        .name(name)
                        .parcelBaseAddress(address)
                        .x_coord(xCoord)
                        .y_coord(yCoord)
                        .category(category)
                        .build();

                seoulLocationInfos.add(info);
                //locationRepository.save(locationMapper.toEntity(info));
            }

            // LocationMapper를 이용하여 SeoulLocationInfo 리스트를 Location 리스트로 변환
            List<Location> locations = seoulLocationInfos.stream()
                    .map(locationMapper::toEntity)
                    .collect(Collectors.toList());

            // 배치로 한번에 DB 저장
            locationRepository.saveAll(locations);

            long savedCount = locationRepository.countByCategory(category);

            System.out.println(category + "  locations size : " + savedCount );

            return jsonArray.size() == MAX_ITEMS && locations.size() < totalLocations;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}