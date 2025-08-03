package com.example.RegistrationLoginPage.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeocodingService {

    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/reverse?lat={lat}&lon={lon}&format=json";

    // Method to fetch address from latitude and longitude
    public String getAddressFromCoordinates(double latitude, double longitude) {
        RestTemplate restTemplate = new RestTemplate();
        // Make the API call to Nominatim API
        String response = restTemplate.getForObject(NOMINATIM_API_URL, String.class, latitude, longitude);

        // Parse the response to extract the address
        return parseAddressFromResponse(response);
    }

    // A simple method to parse the address from the API response using Jackson
    private String parseAddressFromResponse(String response) {
        if (response != null && !response.isEmpty()) {
            try {
                // Create an ObjectMapper instance to parse JSON
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response);
                JsonNode address = jsonResponse.path("address");

                // Extract road, city, and district
                String road = address.path("road").asText("");
                String city = address.path("city").asText("");
                String district = address.path("state_district").asText("");

                // Construct a user-friendly address format
                return road + (road.isEmpty() ? "" : ", ") + city + (city.isEmpty() ? "" : ", ") + district;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "Address not found";
    }
}
