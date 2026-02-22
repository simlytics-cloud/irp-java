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

public class BasicInventoryRoutingFactory extends AbstractBasicInventoryRoutingFactory {

    private final IrpData irpData;
    private final Map<String, RemoteModel> remoteModels = new HashMap<>();
    Config kafkaConsumerConfig;
    Config kafkaProducerConfig;

    public BasicInventoryRoutingFactory(IrpData irpData) {
        this.irpData = irpData;
    }

    public BasicInventoryRoutingFactory(IrpData irpData, Map<String, RemoteModel> remoteModels,
        Config kafkaConsumerConfig, Config kafkaProducerConfig) {
        this.irpData = irpData;
        this.remoteModels.putAll(remoteModels);
        this.kafkaConsumerConfig = kafkaConsumerConfig;
        this.kafkaProducerConfig = kafkaProducerConfig;
    }

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

    @Override
    protected List<Manufacturer> buildManufacturers() {
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
        ManufacturerImpl manufacturerImpl = new ManufacturerImpl(initialState, manufacturerIdentifier, properties);
        return List.of(manufacturerImpl);
        
    }

    @Override
    protected List<Vehicle> buildVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < irpData.numVehicles(); i++) {
            ImmutableVehicleProperties vehicleProperties = ImmutableVehicleProperties.builder()
                .vehicleId(0)
                .capacity(irpData.vehicleCapacity())
                .costPerKm(irpData.vehicleCostPerKm())
                .speedKmHr(irpData.vehicleSpeekKmHr())
                .build();
            VehicleState vehicleState = VehicleState.builder()
                .location(Coordinate.builder()
                    .x(0.0)
                    .y(0.0)
                    .build())
                .deliveryRoute(DeliveryRoute.builder().vehicleId(i + 1).build())
                .dailyKmTraveled(0.0)
                .currentTime(LongSimTime.create(0))
                .schedule(new Schedule<>())
                .build();

            VehicleImpl vehicleImpl = new VehicleImpl(vehicleState.toImmutable(), "vehicle" + (i + 1), vehicleProperties);
            vehicles.add(vehicleImpl);     
        }
        return vehicles;
    }

    @Override
    protected List<Retailer> buildRetailers() {
        List<Retailer> retailers = new ArrayList<>();
        for (int i = 0; i < irpData.retailers().size(); i++) {
            retailers.add(buildRetailer(irpData.retailers().get(i)));       
        }
        return retailers;
    }

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

    protected List<SimulatorProvider<LongSimTime>> buildRetailerSimulatorProviders() {
        List<SimulatorProvider<LongSimTime>> retailerProviders = new ArrayList<>();
        for (int i = 0; i < irpData.retailers().size(); i++) {
            RetailerData retailerData = irpData.retailers().get(i);
            String componentName = "retailer" + retailerData.id();
            if (remoteModels.containsKey(componentName)) {
                String topic = remoteModels.get(componentName).topic();
                KafkaDevsStreamProxyProvider<LongSimTime> proxyProvider = new KafkaDevsStreamProxyProvider<>(componentName, topic, kafkaProducerConfig);    
                retailerProviders.add(proxyProvider);
            } else {
                retailerProviders.add(buildRetailer(retailerData).getDevsSimulatorProvider());
            }
        }
        return retailerProviders;
    }

    public CoupledModelFactory<LongSimTime> buildCoupledModelFactory() {
        List<SimulatorProvider<LongSimTime>> simulatorProviders = new ArrayList<>();
        for (Vehicle vehicle: buildVehicles()) {
            simulatorProviders.add(vehicle.getDevsSimulatorProvider());
        }
        simulatorProviders.addAll(buildRetailerSimulatorProviders());
        List<Manufacturer> manufacturers = buildManufacturers();
        for (Manufacturer manufacturer : manufacturers) {
            simulatorProviders.add(manufacturer.getDevsSimulatorProvider());
        }
        PDevsCouplings couplings = buildCouplings();
        if (!remoteModels.isEmpty()) {
            return new KafkaInventoryRoutingCoupledModelFactory(
                basicInventoryRoutingIdentifier,
                simulatorProviders, 
                couplings,
                kafkaConsumerConfig,
                ""
                );
        }
        return new CoupledModelFactory<>(basicInventoryRoutingIdentifier, simulatorProviders, couplings);
    }

}
