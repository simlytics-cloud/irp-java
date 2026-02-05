package iso.example.irpsystem.irpmodel.impl;

import com.typesafe.config.Config;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import devs.CoupledModelFactory;
import devs.PDEVSModel;
import devs.PDevsCouplings;
import devs.SimulatorProvider;
import devs.iso.time.LongSimTime;
import devs.utils.Schedule;
import iso.example.irpsystem.irpdomain.Coordinate;
import iso.example.irpsystem.irpdomain.Delivery;
import iso.example.irpsystem.irpdomain.DeliveryRoute;
import iso.example.irpsystem.irpdomain.DeliverySchedule;
import iso.example.irpsystem.irpdomain.ImmutableDeliverySchedule;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.AbstractExperimentalFrameFactory;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.DeliveryScheduleGenerator;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.DeliveryScheduleGeneratorProperties;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.DeliveryScheduleGeneratorState;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.ExperimentalFrame;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.ExperimentalFrameInputCouplingHandler;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.ExperimentalFrameOutputCouplingHandler;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.Transducer;
import iso.example.irpsystem.irpmodel.ExperimentalFrame.TransducerState;
import iso.example.irpsystem.irpmodel.InventoryRouting.AbstractInventoryRoutingFactory;
import iso.example.irpsystem.irpmodel.InventoryRouting.InventoryRouting;
import iso.example.irpsystem.irpmodel.InventoryRouting.InventoryRoutingInputCouplingHandler;
import iso.example.irpsystem.irpmodel.impl.IrpData.RetailerData;

public class ExperimentalFrameFactory extends AbstractExperimentalFrameFactory {

    protected final IrpData irpData;
    private final Map<String, RemoteModel> remoteModels = new HashMap<>();
    Config kafkaConsumerConfig;
    Config kafkaProducerConfig;

    public ExperimentalFrameFactory(IrpData irpData) {
        this.irpData = irpData;
    }

    public ExperimentalFrameFactory(IrpData irpData, Map<String, RemoteModel> remoteModels,
        Config kafkaConsumerConfig,
        Config kafkaProducerConfig) {
        this.irpData = irpData;
        this.kafkaConsumerConfig = kafkaConsumerConfig;
        this.kafkaProducerConfig = kafkaProducerConfig;
        this.remoteModels.putAll(remoteModels);
    }

    @Override
    protected List<DeliveryScheduleGenerator> buildDeliveryScheduleGenerators() {
        Map<Integer, Map<Integer, DeliveryRoute>> deliveriesByDayByVehicle = new HashMap<>();
        for (int day = 1; day <= irpData.numTimePeriods(); day++) { // For each day
            // Greedily load each vehicle up with retailer's daily usage until the vehicle if full
            int retailerId = 1;
            Map<Integer, DeliveryRoute> dailyDeliveries = new HashMap<>();
            for (int vehicleId = 1; vehicleId <= irpData.numVehicles(); vehicleId++) {
                DeliveryRoute deliveryRoute = DeliveryRoute.builder().vehicleId(vehicleId).build();           
                RetailerData retailerData = irpData.retailers().get(retailerId - 1);
                double loadedQuantity = retailerData.dailyConsumption();
                double vehicleLoad = 0.0;
                while (vehicleLoad + loadedQuantity < irpData.vehicleCapacity() && retailerId <= irpData.retailers().size()) {
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
        DeliveryScheduleGenerator deliveryScheduleGenerator = new  DeliveryScheduleGeneratorImpl(initialState.toImmutable());
        return List.of(deliveryScheduleGenerator);
    }

    @Override
    protected List<Transducer> buildTransducers() {
        TransducerState transducerState = TransducerState.builder()
            .currentTime(LongSimTime.create(0))
            .schedule(new Schedule<>())
            .build();
        Transducer transducer = new TransducerImpl(transducerState.toImmutable(), irpData.numTimePeriods());
        return List.of(transducer);
    }

    @Override
    protected AbstractInventoryRoutingFactory buildInventoryRoutingFactory() {
        InventoryRoutingFactory inventoryRoutingFactory;
        if (kafkaConsumerConfig != null && kafkaProducerConfig != null) {
            inventoryRoutingFactory = new InventoryRoutingFactory(irpData, remoteModels,
                kafkaConsumerConfig, kafkaProducerConfig);
        } else  {
            inventoryRoutingFactory = new InventoryRoutingFactory(irpData);
        }
        return inventoryRoutingFactory;
    }

    public CoupledModelFactory<LongSimTime> buiCoupledModelFactory() {
        List<SimulatorProvider<LongSimTime>> simulationProviders = new ArrayList<>();
        simulationProviders.addAll(buildDeliveryScheduleGenerators().stream().map(PDEVSModel::getDevsSimulatorProvider).toList());
        simulationProviders.addAll(buildTransducers().stream().map(PDEVSModel::getDevsSimulatorProvider).toList());
        InventoryRoutingFactory inventoryRoutingFactory = (InventoryRoutingFactory) buildInventoryRoutingFactory();
        simulationProviders.add(inventoryRoutingFactory.buildCoupledModelFactory());
        PDevsCouplings couplings = new PDevsCouplings(List.of(new ExperimentalFrameInputCouplingHandler()), 
            List.of(new ExperimentalFrameOutputCouplingHandler()));
        return new CoupledModelFactory<>(ExperimentalFrame.modelIdentifier, simulationProviders,
            couplings);
    }

}
