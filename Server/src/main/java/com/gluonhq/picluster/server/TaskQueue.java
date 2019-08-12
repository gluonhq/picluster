package com.gluonhq.picluster.server;

import java.util.LinkedList;
import java.util.Optional;
import java.util.logging.Logger;

public class TaskQueue {

    private static LinkedList<Task> queue = new LinkedList<>();
    static Logger logger = Logger.getLogger("ExternalRequest");

    static void add(Task task) {
        logger.info("Adding a task to the queue");
        queue.add(task);
        logger.fine("Done adding task, notify waiters");
        synchronized (queue) {
            queue.notifyAll();
        }
        logger.fine("Notified all waiters");
    }

    static int getQueueSize() {
        return queue.size();
    }

    static Task getAvailableTask (boolean blocking) {
        Optional<Task> opt = queue.stream().filter(t -> !t.processing).findFirst();
        if (opt.isPresent()) {
            Task candidate = opt.get();
            candidate.processing = true;
            return candidate;
        }
        if (blocking) {
            try {
                Task answer = null;
                while (answer == null) {
                    logger.fine("Waiting on activity in queue");
                    synchronized (queue) {
                        queue.wait();
                    }
                    logger.fine("Did get activity, try again now");
                    answer = getAvailableTask(false);
                }
                return answer;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static Task getTaskById(String id) {
        return queue.stream().filter(t -> t.id.equals(id)).findFirst().get();
    }

}
