package ap2025.hw4;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class SchedulerMain {

    // TODO: define fields as you see fit
    public static final Object globalTaskNotificationLock = new Object();


    public static void main(String[] args) {

        // ===== CONFIGURATION =====
        int queueCapacity = 10;        // Max tasks per priority queue
        int numProducers = 2;          // Number of producer threads
        int numConsumers = 3;          // Number of consumer threads  
        int tasksPerProducer = 15;     // Tasks each producer creates

        // ===== CREATE PRIORITY QUEUES =====
        Map<Priority, BlockingTaskQueue> priorityQueues = new EnumMap<>(Priority.class);
        priorityQueues.put(Priority.HIGH, new BlockingTaskQueue(queueCapacity));
        priorityQueues.put(Priority.MEDIUM, new BlockingTaskQueue(queueCapacity));
        priorityQueues.put(Priority.LOW, new BlockingTaskQueue(queueCapacity));

        System.out.println("Task Scheduler Simulation Started.");

        // ===== START PRODUCERS =====
        List<Thread> producerThreads = new ArrayList<>();
        List<TaskProducer> producers = new ArrayList<>();

        for (int i = 0; i < numProducers; i++) {
            TaskProducer producer = new TaskProducer(priorityQueues, i + 1, tasksPerProducer);
            producers.add(producer);
            Thread producerThread = new Thread(producer, "Producer-" + (i + 1));
            producerThreads.add(producerThread);
            producerThread.start();  // Start producing tasks
        }

        // ===== START CONSUMERS =====
        List<Thread> consumerThreads = new ArrayList<>();
        List<TaskConsumer> consumers = new ArrayList<>();

        for (int i = 0; i < numConsumers; i++) {
            TaskConsumer consumer = new TaskConsumer(priorityQueues, i + 1);
            consumers.add(consumer);
            Thread consumerThread = new Thread(consumer, "Consumer-" + (i + 1));
            consumerThreads.add(consumerThread);
            consumerThread.start();  // Start consuming tasks
        }

        // ===== WAIT FOR PRODUCERS TO FINISH =====
        try {
            for (Thread t : producerThreads) {
                t.join();  // Wait for all producers to complete
            }
            System.out.println("\nAll producers have finished producing tasks.\n");
            Thread.sleep(2000);  // Give consumers time to process remaining tasks

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread was interrupted.");
        }

        // ===== SIGNAL SHUTDOWN TO CONSUMERS =====
        System.out.println("Signaling shutdown to all consumers...");
        for (TaskConsumer consumer : consumers) {
            consumer.signalShutdown();  // Tell consumers to stop after processing remaining tasks
        }

        // ===== WAIT FOR CONSUMERS TO FINISH =====
        try {
            for (Thread t : consumerThreads) {
                t.join();  // Wait for all consumers to finish processing
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread interrupted while waiting for consumers to finish.");
        }

        System.out.println("Task Scheduler Simulation Finished.");
    }
}
