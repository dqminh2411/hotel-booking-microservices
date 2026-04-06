package com.hotelbooking.hotelservice.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hotelbooking.hotelservice.dto.HotelSummaryResponse;
import com.hotelbooking.hotelservice.dto.PagedResponse;
import com.hotelbooking.hotelservice.service.HotelService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HotelController.class)
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @Test
    void searchHotelsReturns200() throws Exception {
        PagedResponse<HotelSummaryResponse> response = new PagedResponse<>(
                List.of(new HotelSummaryResponse("HT-001", "Marriott Hanoi", "Hanoi", null, "https://img")),
                1,
                0,
                10
        );
        when(hotelService.searchHotels(eq("Marriott"), eq("Hanoi"), eq(0), eq(10))).thenReturn(response);

        mockMvc.perform(get("/hotels")
                        .param("name", "Marriott")
                        .param("address", "Hanoi")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].hotelId").value("HT-001"));
    }

    @Test
    void searchHotelsRejectsInvalidPageSize() throws Exception {
        mockMvc.perform(get("/hotels").param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}

