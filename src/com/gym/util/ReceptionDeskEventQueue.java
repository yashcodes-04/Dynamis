/**
 * Date: 2026-11-18 - Yash
 * Syllabus: Unit 2 (Lectures 5, 6, 7, 8) -
 *           Queue, Deque, PriorityQueue, TreeSet, LinkedHashMap, Iterator, ListIterator
 *           Unit 3 & 4 (Lectures 9, 10, 11, 12, 13, 14, 15, 16) -
 *           Multithreading, Thread Lifecycle, Inter-Thread Communication:
 *           wait(), notify(), notifyAll(), Guarded Blocks, Producer-Consumer Pattern,
 *           Daemon Threads, Synchronization & Intrinsic Locks.
 */
package com.gym.util;

import com.gym.model.FeePayment;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ReceptionDeskEventQueue {

    // Singleton instance
    private static final ReceptionDeskEventQueue INSTANCE = new ReceptionDeskEventQueue();

    public static ReceptionDeskEventQueue getInstance() {
        return INSTANCE;
    }

    // ==============================================================
    // Syllabus Unit 2 (Lectures 5, 6, 7): Collections Data Structures
    // ==============================================================

    // Bounded buffer capacity for Producer-Consumer (Lecture 15-16)
    private static final int MAX_BUFFER_CAPACITY = 20;

    // LinkedList implemented as a Queue/Deque for FIFO desk events (Lectures 6-7)
    private final Deque<String> eventBuffer = new LinkedList<>();

    // PriorityQueue: orders upcoming renewal alerts by due date / fee priority (Lecture 6-7, 8)
    private final PriorityQueue<FeePayment> urgentRenewalQueue = new PriorityQueue<>(
            Comparator.comparing(FeePayment::getRenewalDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
    );

    // TreeSet: maintains unique, sorted set of active Gym IDs (Lecture 5 - TreeSet)
    private final TreeSet<String> activeGymIdSet = new TreeSet<>();

    // LinkedHashMap: maintains access/audit history preserving insertion order (Lecture 6-7)
    private final LinkedHashMap<String, String> deskAuditLog = new LinkedHashMap<>();

    // AtomicInteger: thread-safe counter for event sequence numbering (Lecture 13-14 - Atomicity)
    private final AtomicInteger eventCounter = new AtomicInteger(1000);

    // Multithreading state (Lecture 9, 11)
    private volatile boolean running = false;
    private Thread consumerThread;
    private Consumer<String> eventListener;

    private ReceptionDeskEventQueue() {
        startConsumer();
    }

    public void setEventListener(Consumer<String> listener) {
        this.eventListener = listener;
    }

    // ==============================================================
    // Syllabus Unit 4 (Lectures 15-16): Producer-Consumer with wait() & notifyAll()
    // ==============================================================

    /**
     * PRODUCER method (Lecture 15-16):
     * Guarded block: waits if buffer is full, adds event, then notifies waiting consumers.
     */
    public synchronized void produceDeskEvent(String eventText) {
        while (eventBuffer.size() >= MAX_BUFFER_CAPACITY) {
            try {
                // Monitor wait (Lecture 15) - releases intrinsic lock
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        int seqId = eventCounter.incrementAndGet();
        String formattedEvent = String.format("[#%d] %s", seqId, eventText);
        eventBuffer.addLast(formattedEvent);

        // Audit log in LinkedHashMap (Lecture 6-7)
        deskAuditLog.put(String.valueOf(seqId), eventText);

        // Notify consumer threads waiting on this monitor (Lecture 15)
        notifyAll();
    }

    /**
     * CONSUMER method (Lecture 15-16):
     * Guarded block: waits if buffer is empty, dequeues event, then notifies waiting producers.
     */
    public synchronized String consumeDeskEvent() throws InterruptedException {
        while (eventBuffer.isEmpty() && running) {
            // Monitor wait: thread state changes to WAITING (Lecture 11 & 15)
            wait();
        }

        if (!running && eventBuffer.isEmpty()) {
            return null;
        }

        String event = eventBuffer.removeFirst(); // FIFO Deque consumption (Lecture 6-7)

        // Wake up any waiting producers (Lecture 15)
        notifyAll();
        return event;
    }

    // ==============================================================
    // Syllabus Unit 3 (Lectures 9-12): Thread Lifecycle & Scheduling
    // ==============================================================

    /**
     * Starts the background consumer daemon thread (Lecture 9, 11, 12).
     */
    public synchronized void startConsumer() {
        if (consumerThread != null && consumerThread.isAlive()) {
            return;
        }

        running = true;
        consumerThread = new Thread(() -> {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    String event = consumeDeskEvent();
                    if (event != null && eventListener != null) {
                        eventListener.accept(event);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "DeskEvent-Consumer-Thread");

        consumerThread.setDaemon(true); // Daemon thread (Lecture 11)
        consumerThread.setPriority(Thread.NORM_PRIORITY); // Thread priority (Lecture 11)
        consumerThread.start(); // Moves thread from NEW to RUNNABLE state (Lecture 11)
    }

    public synchronized void stopConsumer() {
        running = false;
        notifyAll();
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }

    // ==============================================================
    // Syllabus Unit 2 (Lectures 5-8): Collections Demonstrations
    // ==============================================================

    /**
     * Adds an active Gym ID to the TreeSet (ensures uniqueness and natural alphabetical/numerical order).
     */
    public synchronized void registerActiveGymId(String gymId) {
        if (gymId != null && !gymId.trim().isEmpty()) {
            activeGymIdSet.add(gymId.trim());
        }
    }

    public synchronized TreeSet<String> getActiveGymIdSet() {
        return new TreeSet<>(activeGymIdSet);
    }

    /**
     * Adds an urgent renewal to PriorityQueue (Lecture 6-7).
     */
    public synchronized void addUrgentRenewal(FeePayment payment) {
        if (payment != null) {
            urgentRenewalQueue.offer(payment);
        }
    }

    /**
     * Traverses the PriorityQueue using an Iterator (Lecture 6-7 - Traversal using Iterator).
     */
    public synchronized List<FeePayment> getUrgentRenewalsList() {
        List<FeePayment> list = new ArrayList<>();
        Iterator<FeePayment> it = urgentRenewalQueue.iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return Collections.unmodifiableList(list); // Collections utility method (Lecture 8)
    }

    /**
     * Traverses a list using ListIterator in reverse order (Lecture 6-7 - ListIterator).
     */
    public static List<String> reverseListWithListIterator(List<String> inputList) {
        List<String> reversed = new ArrayList<>();
        if (inputList == null || inputList.isEmpty()) {
            return reversed;
        }

        ListIterator<String> listIt = inputList.listIterator(inputList.size());
        while (listIt.hasPrevious()) {
            reversed.add(listIt.previous());
        }
        return reversed;
    }
}
