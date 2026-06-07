package com.learning.orderprocessor.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.locks.StampedLock;

// Demonstrates: StampedLock — optimistic read upgraded to read lock if validate() fails
@Component
public class StampedLockDemo {

    static class Point {
        double x, y;
        final StampedLock sl = new StampedLock();

        void move(double dx, double dy) {
            long stamp = sl.writeLock();
            try { x += dx; y += dy; } finally { sl.unlockWrite(stamp); }
        }

        double distanceFromOrigin() {
            long stamp = sl.tryOptimisticRead();
            double cx = x, cy = y;
            if (!sl.validate(stamp)) {
                stamp = sl.readLock();
                try { cx = x; cy = y; } finally { sl.unlockRead(stamp); }
            }
            return Math.hypot(cx, cy);
        }
    }

    public Map<String, Object> run() {
        Point p = new Point();
        p.move(3, 4);
        return Map.of("primitive", "StampedLock", "distance", p.distanceFromOrigin());
    }
}
