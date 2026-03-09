package iso.example.irpsystem.irpmodel.impl;

import devs.couplings.CouplingTarget;
import devs.couplings.DynamicCouplingResolver;
import devs.couplings.StaticCouplingResolver;
import devs.iso.PortValue;
import iso.example.irpsystem.irpdomain.ImmutableDelivery;
import iso.example.irpsystem.irpdomain.ImmutableDeliveryRoute;
import iso.example.irpsystem.irpmodel.BasicInventoryRouting.AbstractBasicInventoryRoutingFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.typesafe.config.Config;

import devs.CoupledModelFactory;
import devs.PDevsCouplings;
import devs.SimulatorProvider;
import devs.iso.time.LongSimTime;
import devs.proxy.KafkaDevsStreamProxyProvider;
import devs.utils.ImmutableSchedule;
import devs.utils.Schedule;
import iso.example.irpsystem.irpdomain.Coordinate;
import iso.example.irpsystem.irpdomain.DeliveryRoute;
import iso.example.irpsystem.irpdomain.ImmutableCoordinate;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableFacilityProps;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableManufacturerProperties;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableManufacturerState;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableRetailerProperties;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableRetailerState;
import iso.example.irpsystem.irpmodel.InventoryRouting.ImmutableVehicleProperties;
import iso.example.irpsystem.irpmodel.InventoryRouting.InventoryRouting;
import iso.example.irpsystem.irpmodel.InventoryRouting.Manufacturer;
import iso.example.irpsystem.irpmodel.InventoryRouting.Retailer;
import iso.example.irpsystem.irpmodel.InventoryRouting.Vehicle;
import iso.example.irpsystem.irpmodel.InventoryRouting.VehicleState;
import iso.example.irpsystem.irpmodel.impl.IrpData.RetailerData;

/**
 * Factory for building the Basic Inventory Routing coupled model.
 * This factory creates and configures manufacturers, retailers, and vehicles,
 * and sets up the couplings between them. It also handles local and remote (Kafka-based)
 * simulator providers.
 */
public class BasicInventoryRoutingFactory extends AbstractBasicInventoryRoutingFactory {

    private final IrpData irpData;
    private final String localSystemName;
    Config kafkaConsumerConfig;
    Config kafkaProducerConfig;

    /**
     * Constructs a new BasicInventoryRoutingFactory.
     *
     * @param irpData         The data defining the IRP problem instance.
     * @param localSystemName The name of the local system (used to distinguish local vs. remote components).
     */
    public BasicInventoryRoutingFactory(IrpData irpData, String localSystemName) {
        this.irpData = irpData;
        this.localSystemName = localSystemName;
    }

    /**
     * Constructs a new BasicInventoryRoutingFactory with Kafka configurations.
     *
     * @param irpData              The data defining the IRP problem instance.
     * @param localSystemName      The name of the local system.
     * @param kafkaConsumerConfig  Configuration for Kafka consumers.
     * @param kafkaProducerConfig  Configuration for Kafka producers.
     */
    public BasicInventoryRoutingFactory(IrpData irpData, String localSystemName,
        Config kafkaConsumerConfig, Config kafkaProducerConfig) {
        this.irpData = irpData;
        this.localSystemName = localSystemName;
        this.kafkaConsumerConfig = kafkaConsumerConfig;
        this.kafkaProducerConfig = kafkaProducerConfig;
    }

    /**
     * Builds the couplings between the components of the Inventory Routing system.
     * Defines how messages (delivery schedules, routes, costs) flow between
     * the manufacturer, vehicles, and retailers.
     *
     * @return The configured PDevsCouplings.
     */
    @Override
    protected PDevsCouplings buildCouplings() {

        PDevsCouplings couplings = PDevsCouplings.builder(basicInventoryRoutingIdentifier)
            .addConnection("basicInventoryRouting", InventoryRouting.receiveDeliverySchedule.getPortName(),
                "manufacturer", Manufacturer.acceptDeliverySchedule.getPortName())
            .addResolver("manufacturer", Manufacturer.postDeliveryRoute.getPortName(),
                new DynamicCouplingResolver() {
                    @Override
                    public List<CouplingTarget> resolve(String sender, PortValue<?> portValue) {
                        ImmutableDeliveryRoute deliveryRoute = Manufacturer.postDeliveryRoute.getValue(portValue);
                        return List.of(CouplingTarget.of(
                            "vehicle" + deliveryRoute.getVehicleId(),
                            Vehicle.acceptDeliveryRoute.getPortName()));
                    }
                })
            .addPatternResolver("vehicle\\d+", Vehicle.dropDelivery.getPortName(),
                new DynamicCouplingResolver() {
                @Override
                    public List<CouplingTarget> resolve(String sender, PortValue<?> portValue) {
                        ImmutableDelivery immutableDelivery = Vehicle.dropDelivery.getValue(portValue);
                        return List.of(CouplingTarget.of(
                            "retailer" + immutableDelivery.getRetailerId(),
                            Retailer.receiveDelivery.getPortName()));
                    }
                })
            .addConnection("manufacturer", Retailer.dailyInventoryCost.getPortName(),
                "basicInventoryRouting", InventoryRouting.reportInventoryCost.getPortName())
            .addPatternResolver("retailer\\d+", Manufacturer.dailyInventoryCost.getPortName(),
                new StaticCouplingResolver(List.of(CouplingTarget.of(
                    "basicInventoryRouting", InventoryRouting.reportInventoryCost.getPortName()))))
            .addPatternResolver("vehicle\\d+", Vehicle.dailyDeliveryCost.getPortName(),
                new StaticCouplingResolver(List.of(CouplingTarget.of(
                    "basicInventoryRouting", InventoryRouting.reportVehicleCost.getPortName()))))
            .build();
        return couplings;
    }

    /**
     * Helper method to build a ManufacturerImpl from IrpData.
     *
     * @param irpData The problem data.
     * @return A new ManufacturerImpl instance.
     */
    public static ManufacturerImpl buildManufacturer(IrpData irpData) {
        ImmutableManufacturerProperties properties = ImmutableManufacturerProperties.builder()
            .dailyProduction(irpData.manufacturer().dailyProduction())
            .facilityProperties(ImmutableFacilityProps.builder()
                .coordinate(new ImmutableCoordinate(0.0, 0.0))
                .startingInventory(irpData.manufacturer().startingInventory())
                .inventoryCost(irpData.manufacturer().inventoryCost())
                .build())
            .build();
        ImmutableManufacturerState initialState = ImmutableManufacturerState.builder()
            .currentInventory(irpData.manufacturer().startingInventory())
            .currentTime(LongSimTime.create(0))
            .schedule(new ImmutableSchedule<>(new TreeMap<>()))
            .build();
        return new ManufacturerImpl(initialState, manufacturerIdentifier, properties);
    }

    @Override
    protected List<Manufacturer> buildManufacturers() {
        return List.of(buildManufacturer(irpData));
    }

    @Override
    protected List<Vehicle> buildVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < irpData.numVehicles(); i++) {
            Vehicle vehicleImpl = buildVehicle(i, irpData);
            vehicles.add(vehicleImpl);
        }
        return vehicles;
    }

    /**
     * Helper method to build a VehicleImpl from vehicle ID and IrpData.
     *
     * @param vehicleId The ID of the vehicle.
     * @param irpData   The problem data.
     * @return A new VehicleImpl instance.
     */
    public static VehicleImpl buildVehicle(int vehicleId, IrpData irpData) {
        ImmutableVehicleProperties vehicleProperties = ImmutableVehicleProperties.builder()
            .vehicleId(vehicleId)
            .capacity(irpData.vehicleCapacity())
            .costPerKm(irpData.vehicleCostPerKm())
            .speedKmHr(irpData.vehicleSpeedKmHr())
            .manufacturerLocation(ImmutableCoordinate.builder()
                .x(irpData.manufacturer().x())
                .y(irpData.manufacturer().y())
                .build())
            .build();
        VehicleState vehicleState = VehicleState.builder()
            .location(Coordinate.builder()
                .x(irpData.manufacturer().x())
                .y(irpData.manufacturer().y())
                .build())
            .deliveryRoute(DeliveryRoute.builder().vehicleId(vehicleId).build())
            .dailyKmTraveled(0.0)
            .currentTime(LongSimTime.create(0))
            .schedule(new Schedule<>())
            .build();

        VehicleImpl vehicleImpl = new VehicleImpl(vehicleState.toImmutable(), "vehicle" + vehicleId, vehicleProperties);
        return vehicleImpl;
    }

    @Override
    protected List<Retailer> buildRetailers() {
        List<Retailer> retailers = new ArrayList<>();
        for (int i = 0; i < irpData.retailers().size(); i++) {
            retailers.add(buildRetailer(irpData.retailers().get(i)));       
        }
        return retailers;
    }

    /**
     * Helper method to build a Retailer from RetailerData.
     *
     * @param retailerData The data for a single retailer.
     * @return A new RetailerImpl instance.
     */
    public static Retailer buildRetailer(RetailerData retailerData) {
        ImmutableRetailerState retailerState = ImmutableRetailerState.builder()
            .currentInventory(retailerData.startingInventory())
            .currentTime(LongSimTime.create(0))
            .schedule(new ImmutableSchedule(new TreeMap<>()))
            .build();
        ImmutableRetailerProperties properties = ImmutableRetailerProperties.builder()
            .retailerId(retailerData.id())
            .dailyConsumption(retailerData.dailyConsumption())
            .facilityProperties(ImmutableFacilityProps.builder()
                .coordinate(new ImmutableCoordinate(retailerData.x(), retailerData.y()))
                .inventoryCost(retailerData.inventoryCost())
                .startingInventory(retailerData.startingInventory())
                .build())
            .minInventory(retailerData.minInventory())
            .maxInventory(retailerData.maxInventory())
            .build();
        return new RetailerImpl(retailerState, "retailer" + retailerData.id(), properties);
    }

    protected List<SimulatorProvider<LongSimTime>> buildVehicleSimulatorProviders() {
        List<SimulatorProvider<LongSimTime>> vehicleProviders = new ArrayList<>();
        for (int i = 0; i < irpData.numVehicles(); i++) {
            String componentName = "vehicle" + (i);
            String host = irpData.vehicleHosts().get(componentName);
            if (host != null && !localSystemName.equals(host)) {
                String topic = irpData.coordinatorTopic();
                KafkaDevsStreamProxyProvider<LongSimTime> proxyProvider = new KafkaDevsStreamProxyProvider<>(componentName, topic, kafkaProducerConfig);
                vehicleProviders.add(proxyProvider);
            } else {
                vehicleProviders.add(buildVehicle(i, irpData).getDevsSimulatorProvider());
            }
        }
        return vehicleProviders;
    }

    protected List<SimulatorProvider<LongSimTime>> buildManufacturerSimulatorProviders() {
        List<SimulatorProvider<LongSimTime>> manufacturerProviders = new ArrayList<>();
        String componentName = manufacturerIdentifier;
        String host = irpData.manufacturer().host();
        if (host != null && !localSystemName.equals(host)) {
            String topic = irpData.coordinatorTopic();
            KafkaDevsStreamProxyProvider<LongSimTime> proxyProvider = new KafkaDevsStreamProxyProvider<>(componentName, topic, kafkaProducerConfig);
            manufacturerProviders.add(proxyProvider);
        } else {
            for (Manufacturer manufacturer : buildManufacturers()) {
                manufacturerProviders.add(manufacturer.getDevsSimulatorProvider());
            }
        }
        return manufacturerProviders;
    }

    protected List<SimulatorProvider<LongSimTime>> buildRetailerSimulatorProviders() {
        List<SimulatorProvider<LongSimTime>> retailerProviders = new ArrayList<>();
        for (int i = 0; i < irpData.retailers().size(); i++) {
            RetailerData retailerData = irpData.retailers().get(i);
            String componentName = "retailer" + retailerData.id();
            if (!localSystemName.equals(retailerData.host())) {
                String topic = irpData.coordinatorTopic();
                KafkaDevsStreamProxyProvider<LongSimTime> proxyProvider = new KafkaDevsStreamProxyProvider<>(componentName, topic, kafkaProducerConfig);    
                retailerProviders.add(proxyProvider);
            } else {
                retailerProviders.add(buildRetailer(retailerData).getDevsSimulatorProvider());
            }
        }
        return retailerProviders;
    }

    /**
     * Builds and returns a CoupledModelFactory for the Inventory Routing system.
     * Decides whether to use a standard CoupledModelFactory or a Kafka-based one
     * depending on if any components are remote.
     *
     * @return The configured CoupledModelFactory.
     */
    public CoupledModelFactory<LongSimTime> buildCoupledModelFactory() {
        List<SimulatorProvider<LongSimTime>> simulatorProviders = new ArrayList<>();
        simulatorProviders.addAll(buildVehicleSimulatorProviders());
        simulatorProviders.addAll(buildRetailerSimulatorProviders());
        simulatorProviders.addAll(buildManufacturerSimulatorProviders());
        PDevsCouplings couplings = buildCouplings();
        
        boolean hasRemoteRetailers = irpData.retailers().stream().anyMatch(r -> !localSystemName.equals(r.host()));
        boolean hasRemoteVehicles = irpData.vehicleHosts().values().stream().anyMatch(h -> !localSystemName.equals(h));
        boolean hasRemoteManufacturer = !localSystemName.equals(irpData.manufacturer().host());
        
        if (hasRemoteRetailers || hasRemoteVehicles || hasRemoteManufacturer) {
            return new KafkaInventoryRoutingCoupledModelFactory(
                basicInventoryRoutingIdentifier,
                simulatorProviders, 
                couplings,
                kafkaConsumerConfig,
                irpData.coordinatorTopic() != null ? irpData.coordinatorTopic() : "irp-system"
                );
        }
        return new CoupledModelFactory<>(basicInventoryRoutingIdentifier, simulatorProviders, couplings);
    }

}
