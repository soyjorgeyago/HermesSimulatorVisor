package es.us.lsi.hermes.kafka;

import com.google.gson.Gson;
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
        ConsumerRecords<Long, String> records = consumer.poll(consumerPollTimeout);

        ConsumerRecord<Long, String> last = null;
        for (ConsumerRecord<Long, String> record : records) {
            last = record;
        }

        if(last != null) {
            Vehicle[] activeVehicles = gson.fromJson(last.value(), Vehicle[].class);
            observer.update(activeVehicles);
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
