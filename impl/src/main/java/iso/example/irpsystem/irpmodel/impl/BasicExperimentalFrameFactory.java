package iso.example.irpsystem.irpmodel.impl;

import com.typesafe.config.Config;
import iso.example.irpsystem.irpmodel.BasicExperimentalFrame.AbstractBasicExperimentalFrameFactory;
import iso.example.irpsystem.irpmodel.BasicInventoryRouting.AbstractBasicInventoryRoutingFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devs.CoupledModelFactory;
import devs.PDevsCouplings;
import devs.SimulatorProvider;
import devs.iso.time.LongSimTime;
import devs.utils.Schedule;
import iso.example.irpsystem.irpdomain.Coordinate;
import iso.example.irpsystem.irpdomain.Delivery;
import iso.example.irpsystem.irpdomain.DeliveryRoute;
import iso.example.irpsystem.irpdomain.DeliverySchedule;
import iso.example.irpsystem.irpdomain.ImmutableDeliverySchedule;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.DeliveryScheduleGenerator;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.DeliveryScheduleGeneratorState;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.Transducer;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.TransducerState;
import iso.example.irpsystem.irpmodel.impl.IrpData.RetailerData;

public class BasicExperimentalFrameFactory extends AbstractBasicExperimentalFrameFactory {

    protected final IrpData irpData;
    protected final String localSystemName;
    Config kafkaConsumerConfig;
    Config kafkaProducerConfig;

    public BasicExperimentalFrameFactory(IrpData irpData, String localSystemName) {
        this.irpData = irpData;
        this.localSystemName = localSystemName;
    }

    public BasicExperimentalFrameFactory(IrpData irpData, String localSystemName,
        Config kafkaConsumerConfig,
        Config kafkaProducerConfig) {
        this.irpData = irpData;
        this.localSystemName = localSystemName;
        this.kafkaConsumerConfig = kafkaConsumerConfig;
        this.kafkaProducerConfig = kafkaProducerConfig;
    }

    @Override
    protected List<DeliveryScheduleGenerator> buildDeliveryScheduleGenerators() {
        Map<Integer, Map<Integer, DeliveryRoute>> deliveriesByDayByVehicle = new HashMap<>();
        for (int day = 1; day <= irpData.numTimePeriods(); day++) { // For each day
            // Greedily load each vehicle up with retailer's daily usage until the vehicle if full
            int retailerId = 0;
            Map<Integer, DeliveryRoute> dailyDeliveries = new HashMap<>();
            for (int vehicleId = 0; vehicleId < irpData.numVehicles(); vehicleId++) {
                DeliveryRoute deliveryRoute = DeliveryRoute.builder().vehicleId(vehicleId).build();           
                RetailerData retailerData = irpData.retailers().get(retailerId);
                double loadedQuantity = retailerData.dailyConsumption();
                double vehicleLoad = 0.0;
                while (vehicleLoad + loadedQuantity < irpData.vehicleCapacity() && retailerId < irpData.retailers().size()) {
                    vehicleLoad += loadedQuantity;
                    Delivery delivery = Delivery.builder()
                        .productAmount(loadedQuantity)
                        .retailerId(retailerId)
                        .retailerLocation(new Coordinate(retailerData.x(), retailerData.y()))
                        .build();
                    deliveryRoute.getDeliveries().add(delivery);
                    retailerId ++;
                    if (retailerId - 1 < irpData.retailers().size()) {
                        retailerData = irpData.retailers().get(retailerId - 1);
                        loadedQuantity = retailerData.dailyConsumption();    
                    }                
                }
                if (deliveryRoute.getDeliveries().size() > 0) {
                    dailyDeliveries.put(vehicleId, deliveryRoute);
                }
            }
            deliveriesByDayByVehicle.put(day, dailyDeliveries);
        }
        
        ImmutableDeliverySchedule deliverySchedule = DeliverySchedule.builder()
            .deliveriesByDayByVehicle(deliveriesByDayByVehicle)
            .build()
            .toImmutable();

        Schedule<LongSimTime> schedule = new Schedule<>();
        DeliveryScheduleGeneratorState initialState = DeliveryScheduleGeneratorState.builder()
            .currentTime(LongSimTime.create(0))
            .deliverySchedule(deliverySchedule.toMutable())
            .schedule(schedule)
            .build();
        DeliveryScheduleGenerator deliveryScheduleGenerator = new  DeliveryScheduleGeneratorImpl(initialState.toImmutable(),
            generatorIdentifier);
        return List.of(deliveryScheduleGenerator);
    }

    @Override
    protected List<Transducer> buildTransducers() {
        TransducerState transducerState = TransducerState.builder()
            .currentTime(LongSimTime.create(0))
            .schedule(new Schedule<>())
            .build();
        Transducer transducer = new TransducerImpl(transducerState.toImmutable(), transducerIdentifier, irpData.numTimePeriods());
        return List.of(transducer);
    }

    @Override
    protected AbstractBasicInventoryRoutingFactory buildBasicInventoryRoutingFactory() {
        BasicInventoryRoutingFactory inventoryRoutingFactory;
        if (kafkaConsumerConfig != null && kafkaProducerConfig != null) {
            inventoryRoutingFactory = new BasicInventoryRoutingFactory(irpData, localSystemName,
                kafkaConsumerConfig, kafkaProducerConfig);
        } else  {
            inventoryRoutingFactory = new BasicInventoryRoutingFactory(irpData, localSystemName);
        }
        return inventoryRoutingFactory;
    }

    public CoupledModelFactory<LongSimTime> buiCoupledModelFactory() {
        List<SimulatorProvider<LongSimTime>> simulationProviders = new ArrayList<>();
        List<DeliveryScheduleGenerator> deliveryScheduleGenerators = buildDeliveryScheduleGenerators();
        for (DeliveryScheduleGenerator deliveryScheduleGenerator : deliveryScheduleGenerators) {
            SimulatorProvider<LongSimTime> simulatorProvider = deliveryScheduleGenerator.getDevsSimulatorProvider();
            simulationProviders.add(simulatorProvider);
        }
        List<Transducer> transducers = buildTransducers();
        for (Transducer transducer : transducers) {
            SimulatorProvider<LongSimTime> simulatorProvider = transducer.getDevsSimulatorProvider();
            simulationProviders.add(simulatorProvider);
        }
        //simulationProviders.addAll(buildDeliveryScheduleGenerators().stream().map(PDEVSModel::getDevsSimulatorProvider).toList());
        //simulationProviders.addAll(buildTransducers().stream().map(PDEVSModel::getDevsSimulatorProvider).toList());
        BasicInventoryRoutingFactory inventoryRoutingFactory = (BasicInventoryRoutingFactory) buildBasicInventoryRoutingFactory();
        simulationProviders.add(inventoryRoutingFactory.buildCoupledModelFactory());
        PDevsCouplings couplings = buildCouplings();
        return new CoupledModelFactory<>(basicExperimentalFrameIdentifier, simulationProviders,
            couplings);
    }

}
