package iso.example.irpsystem.irpdomain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryRouteTest {

    @Test
    public void testBuilderDefaultValue() {
        DeliveryRoute route = DeliveryRoute.builder()
            .vehicleId(1)
            .build();
        assertNotNull(route.getDeliveries());
        assertTrue(route.getDeliveries().isEmpty());
    }

    @Test
    public void testBuilderWithValue() {
        Delivery delivery = Delivery.builder()
                .retailerId(1)
                .retailerLocation(Coordinate.builder().x(0.0).y(0.0).build())
                .productAmount(10.0)
                .build();
        DeliveryRoute route = DeliveryRoute.builder()
                .vehicleId(1)
                .deliveries(List.of(delivery))
                .build();
        assertEquals(1, route.getDeliveries().size());
        assertEquals(1, route.getDeliveries().get(0).getRetailerId());
    }

    @Test
    public void testJsonCreatorWithNull() {
        List<Delivery> deliveries = null;
        DeliveryRoute route = new DeliveryRoute(1, deliveries);
        assertNotNull(route.getDeliveries());
        assertTrue(route.getDeliveries().isEmpty());
    }

    @Test
    public void testJsonDeserializationEmpty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"@class\":\"iso.example.irpsystem.irpdomain.DeliveryRoute\"}";
        DeliveryRoute route = mapper.readValue(json, DeliveryRoute.class);
        assertNotNull(route.getDeliveries());
        assertTrue(route.getDeliveries().isEmpty());
    }

    @Test
    public void testJsonDeserializationNullDeliveries() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"@class\":\"iso.example.irpsystem.irpdomain.DeliveryRoute\", \"deliveries\": null}, \"vehicleId\": 1";
        DeliveryRoute route = mapper.readValue(json, DeliveryRoute.class);
        assertNotNull(route.getDeliveries());
        assertTrue(route.getDeliveries().isEmpty());
    }

    @Test
    public void testJsonDeserializationWithData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"@class\":\"iso.example.irpsystem.irpdomain.DeliveryRoute\", \"vehicleId\": 1, \"deliveries\": [{\"@class\":\"iso.example.irpsystem.irpdomain.Delivery\", \"retailerId\": 1, \"retailerLocation\": {\"@class\":\"iso.example.irpsystem.irpdomain.Coordinate\", \"x\": 0.0, \"y\": 0.0}, \"productAmount\": 10.0}]}";
        DeliveryRoute route = mapper.readValue(json, DeliveryRoute.class);
        assertEquals(1, route.getDeliveries().size());
        assertEquals(1, route.getDeliveries().get(0).getRetailerId());
    }
}
