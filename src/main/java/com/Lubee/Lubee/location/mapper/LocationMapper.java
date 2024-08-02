package com.Lubee.Lubee.location.mapper;

import com.Lubee.Lubee.enumset.Spot;
import com.Lubee.Lubee.location.domain.Location;
import com.Lubee.Lubee.location.dto.SeoulLocationInfo;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public Location toEntity(SeoulLocationInfo seoulLocationInfo) {

        double xCoord = parseCoordinate(seoulLocationInfo.getX_coord());
        double yCoord = parseCoordinate(seoulLocationInfo.getY_coord());

        Point coordinate = new Point(xCoord, yCoord);

        return Location.builder()
                .name(seoulLocationInfo.getName())
                .parcelBaseAddress(seoulLocationInfo.getParcelBaseAddress())
                .coordinate(coordinate)
                .spot(Spot.ETC)
                .category(seoulLocationInfo.getCategory())
                .count(0)  // 기본값으로 0 설정
                .build();
    }

    private double parseCoordinate(String coordStr) {
        if (coordStr == null || coordStr.trim().isEmpty()) {
            return 0.0;  // 기본값을 설정
        }
        try {
            return Double.parseDouble(coordStr);
        } catch (NumberFormatException e) {
            return 0.0;  // 기본값을 설정
        }
    }

}