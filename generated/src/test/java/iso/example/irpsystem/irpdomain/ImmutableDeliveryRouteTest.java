package iso.example.irpsystem.irpdomain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ImmutableDeliveryRouteTest {

    @Test
    public void testBuilderDefaultValue() {
        ImmutableDeliveryRoute route = ImmutableDeliveryRoute.builder()
            .vehicleId(1)    
            .build();
        assertNotNull(route.getDeliveries());
        assertTrue(route.getDeliveries().isEmpty());
    }

    @Test
    public void testBuilderWithValue() {
        ImmutableDelivery delivery = ImmutableDelivery.builder()
                .retailerId(1)
                .retailerLocation(ImmutableCoordinate.builder().x(0.0).y(0.0).build())
                .productAmount(10.0)
                .build();
        ImmutableDeliveryRoute route = ImmutableDeliveryRoute.builder()
                .vehicleId(1)
                .deliveries(List.of(delivery))
                .build();
        assertEquals(1, route.getDeliveries().size());
        assertEquals(1, route.getDeliveries().get(0).getRetailerId());
    }

    @Test
    public void testJsonCreatorWithNull() {
        ImmutableDeliveryRoute route = new ImmutableDeliveryRoute(1, (List<ImmutableDelivery>) null);
        assertNotNull(route.getDeliveries());
        assertTrue(route.getDeliveries().isEmpty());
    }

    @Test
    public void testJsonDeserializationEmpty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"@class\":\"iso.example.irpsystem.irpdomain.ImmutableDeliveryRoute\"}";
        ImmutableDeliveryRoute route = mapper.readValue(json, ImmutableDeliveryRoute.class);
        assertNotNull(route.getDeliveries());
        assertTrue(route.getDeliveries().isEmpty());
    }

    @Test
    public void testJsonDeserializationNullDeliveries() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"@class\":\"iso.example.irpsystem.irpdomain.ImmutableDeliveryRoute\", \"deliveries\": null}";
        ImmutableDeliveryRoute route = mapper.readValue(json, ImmutableDeliveryRoute.class);
        assertNotNull(route.getDeliveries());
        assertTrue(route.getDeliveries().isEmpty());
    }

    @Test
    public void testJsonDeserializationWithData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"@class\":\"iso.example.irpsystem.irpdomain.ImmutableDeliveryRoute\", \"deliveries\": [{\"@class\":\"iso.example.irpsystem.irpdomain.ImmutableDelivery\", \"retailerId\": 1, \"retailerLocation\": {\"@class\":\"iso.example.irpsystem.irpdomain.ImmutableCoordinate\", \"x\": 0.0, \"y\": 0.0}, \"productAmount\": 10.0}]}";
        ImmutableDeliveryRoute route = mapper.readValue(json, ImmutableDeliveryRoute.class);
        assertEquals(1, route.getDeliveries().size());
        assertEquals(1, route.getDeliveries().get(0).getRetailerId());
    }
}
