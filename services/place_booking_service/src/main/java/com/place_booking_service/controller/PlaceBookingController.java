package com.place_booking_service.controller;


import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.place_booking_service.client.HotelServiceClient;
import com.place_booking_service.client.UserServiceClient;
import com.place_booking_service.dto.Hotel;
import com.place_booking_service.dto.PlaceBookingRequest;
import com.place_booking_service.dto.User;
import com.place_booking_service.service.PlaceBookingService;

@RestController
@RequestMapping("/place-booking")
public class PlaceBookingController {

    @Autowired
    private PlaceBookingService placeBookingService;

    @Autowired
    UserServiceClient userServiceClient;

    @Autowired
    HotelServiceClient hotelServiceClient;

    @PostMapping
    public ResponseEntity<?> placeBooking(@RequestBody PlaceBookingRequest placeBookingRequest) {
//        User user= userServiceClient.getUserById(placeBookingRequest.getUserId());
//        Hotel hotel= hotelServiceClient.getHotelById(placeBookingRequest.getHotelId());
//
//        if(user==null){
//            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
//                "code", "USER_NOT_FOUND",
//                "message", "User không tồn tại trong hệ thống"
//            ));
//        }
//        if(hotel==null){
//            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
//                "code", "HOTEL_NOT_FOUND",
//                "message", "Hotel không tồn tại trong hệ thống"
//            ));
//        }

        User user = new User("US-001","nguyen van a", "a@mail.com");
        Hotel hotel= new Hotel("HT-001","luxury hotel","so 1, le a");

        LocalDate checkin= LocalDate.parse(placeBookingRequest.getCheckin());
        LocalDate checkout= LocalDate.parse(placeBookingRequest.getCheckout());

        if(checkin.isAfter(checkout)||checkin.isBefore(LocalDate.now())){
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "code", "VALIDATION_ERROR",
                "message", "checkin phải trước checkout và không được là ngày trong quá khứ"
            ));
        }

        try{
            String bookingId = placeBookingService.startSaga(placeBookingRequest,user,hotel);

            return ResponseEntity.accepted().body(Map.of(
                "bookingId", bookingId,
                "status", "PENDING",
                "message", "Yêu cầu đặt phòng đang được xử lý. Vui lòng kiểm tra trạng thái bằng bookingId.",
                "pollingUrl", "/bookings/"+bookingId
            ));

        }catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", "ERROR",
                "message",e.getMessage()
            ));
        }



    }

}
