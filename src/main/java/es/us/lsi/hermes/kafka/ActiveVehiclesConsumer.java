package es.us.lsi.hermes.kafka;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.us.lsi.hermes.domain.Vehicle;
import es.us.lsi.hermes.simulator.IControllerObserver;
import es.us.lsi.hermes.simulator.VisorController;
import es.us.lsi.hermes.util.Utils;
import kafka.utils.ShutdownableThread;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.util.Collections;

public class ActiveVehiclesConsumer extends ShutdownableThread {

    private static final String TOPIC_ACTIVE_VEHICLES = "ActiveVehicles";

    private final KafkaConsumer<Long, String> consumer;
    private final Gson gson;
    private final IControllerObserver observer;
    private final int consumerPollTimeout;

    public ActiveVehiclesConsumer(IControllerObserver observer) {
        super("ActiveVehiclesConsumer", true);
        this.consumer = new KafkaConsumer<>(VisorController.getKafkaProperties());
        this.gson = new Gson();
        this.observer = observer;

        consumerPollTimeout = Utils.getIntValue("consumer.poll.timeout.ms", 1000);
        consumer.subscribe(Collections.singletonList(TOPIC_ACTIVE_VEHICLES));
    }

    @Override
    public void doWork() {
        // The 'consumer' for each 'Vehicle Locations' will poll every 'pollTimeout' milliseconds, to get all the data received by Kafka.
        ConsumerRecords<Long, String> records = consumer.poll(consumerPollTimeout);
        for (ConsumerRecord<Long, String> record : records) {

            //FIXME
            System.out.println("RECORD FOUND");
            System.out.println(record.value());
            Vehicle[] activeVehicles = gson.fromJson(record.value(), Vehicle[].class);
//            Vehicle[] activeVehicles = gson.fromJson(record.value(), new TypeToken<Vehicle>(){}.getType());

            if(activeVehicles != null) {
                //FIXME
                System.out.println("RECORD SENT");
                observer.update(activeVehicles);
            }
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
