package ap2025.hw4;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class SchedulerMain {

    // TODO: define fields as you see fit
    public static final Object globalTaskNotificationLock = new Object();


    public static void main(String[] args) {

        // TODO: Test and simulate the TaskScheduler

        int queueCapacity = 10;
        int numProducers = 2;
        int numConsumers = 3;
        int tasksPerProducer = 15;

        Map<Priority, BlockingTaskQueue> priorityQueues = new EnumMap<>(Priority.class);
        priorityQueues.put(Priority.HIGH, new BlockingTaskQueue(queueCapacity));
        priorityQueues.put(Priority.MEDIUM, new BlockingTaskQueue(queueCapacity));
        priorityQueues.put(Priority.LOW, new BlockingTaskQueue(queueCapacity));

        System.out.println("Task Scheduler Simulation Started.");

        List<Thread> producerThreads = new ArrayList<>();
        List<TaskProducer> producers = new ArrayList<>();

        for (int i = 0; i < numProducers; i++) {
            TaskProducer producer = new TaskProducer(priorityQueues, i + 1, tasksPerProducer);
            producers.add(producer);
            Thread producerThread = new Thread(producer, "Producer-" + (i + 1));
            producerThreads.add(producerThread);
            producerThread.start();
        }

        List<Thread> consumerThreads = new ArrayList<>();
        List<TaskConsumer> consumers = new ArrayList<>();

        for (int i = 0; i < numConsumers; i++) {
            TaskConsumer consumer = new TaskConsumer(priorityQueues, i + 1);
            consumers.add(consumer);
            Thread consumerThread = new Thread(consumer, "Consumer-" + (i + 1));
            consumerThreads.add(consumerThread);
            consumerThread.start();
        }

        try {
            for (Thread t : producerThreads) {
                t.join();
            }
            System.out.println("\nAll producers have finished producing tasks.\n");
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread was interrupted.");
        }

        System.out.println("Signaling shutdown to all consumers...");
        for (TaskConsumer consumer : consumers) {
            consumer.signalShutdown();
        }

        try {
            for (Thread t : consumerThreads) {
                t.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread interrupted while waiting for consumers to finish.");
        }

        System.out.println("Task Scheduler Simulation Finished.");
    }
}
