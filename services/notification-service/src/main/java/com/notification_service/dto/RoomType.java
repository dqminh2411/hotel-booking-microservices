package com.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomType {
    private String roomTypeId;
    private String name;
    private int bedCount;
    private int bookingQuantity;
    private int totalQuantity;
    private double price;

}
