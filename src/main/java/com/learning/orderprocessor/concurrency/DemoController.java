package com.learning.orderprocessor.concurrency;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final ExecutorDemo executor;
    private final CallableFutureDemo callableFuture;
    private final CompletableFutureDemo completableFuture;
    private final ScheduledExecutorDemo scheduledExecutor;
    private final ForkJoinDemo forkJoin;
    private final ReentrantLockDemo reentrantLock;
    private final ReadWriteLockDemo readWriteLock;
    private final StampedLockDemo stampedLock;
    private final SemaphoreDemo semaphore;
    private final CountDownLatchDemo countDownLatch;
    private final CyclicBarrierDemo cyclicBarrier;
    private final PhaserDemo phaser;
    private final ExchangerDemo exchanger;
    private final AtomicsDemo atomics;
    private final ConcurrentCollectionsDemo concurrentCollections;
    private final BlockingQueueDemo blockingQueue;
    private final ProducerConsumerWaitNotifyDemo producerConsumer;
    private final SynchronizedVolatileDemo syncVolatile;
    private final ThreadLocalDemo threadLocal;
    private final DeadlockDemo deadlock;

    public DemoController(ExecutorDemo executor, CallableFutureDemo callableFuture,
                          CompletableFutureDemo completableFuture, ScheduledExecutorDemo scheduledExecutor,
                          ForkJoinDemo forkJoin, ReentrantLockDemo reentrantLock,
                          ReadWriteLockDemo readWriteLock, StampedLockDemo stampedLock,
                          SemaphoreDemo semaphore, CountDownLatchDemo countDownLatch,
                          CyclicBarrierDemo cyclicBarrier, PhaserDemo phaser,
                          ExchangerDemo exchanger, AtomicsDemo atomics,
                          ConcurrentCollectionsDemo concurrentCollections, BlockingQueueDemo blockingQueue,
                          ProducerConsumerWaitNotifyDemo producerConsumer, SynchronizedVolatileDemo syncVolatile,
                          ThreadLocalDemo threadLocal, DeadlockDemo deadlock) {
        this.executor = executor;
        this.callableFuture = callableFuture;
        this.completableFuture = completableFuture;
        this.scheduledExecutor = scheduledExecutor;
        this.forkJoin = forkJoin;
        this.reentrantLock = reentrantLock;
        this.readWriteLock = readWriteLock;
        this.stampedLock = stampedLock;
        this.semaphore = semaphore;
        this.countDownLatch = countDownLatch;
        this.cyclicBarrier = cyclicBarrier;
        this.phaser = phaser;
        this.exchanger = exchanger;
        this.atomics = atomics;
        this.concurrentCollections = concurrentCollections;
        this.blockingQueue = blockingQueue;
        this.producerConsumer = producerConsumer;
        this.syncVolatile = syncVolatile;
        this.threadLocal = threadLocal;
        this.deadlock = deadlock;
    }

    @GetMapping("/executor")              public Map<String, Object> executor()              throws Exception { return executor.run(); }
    @GetMapping("/callable-future")       public Map<String, Object> callableFuture()        throws Exception { return callableFuture.run(); }
    @GetMapping("/completable-future")    public Map<String, Object> completableFuture()     throws Exception { return completableFuture.run(); }
    @GetMapping("/scheduled-executor")    public Map<String, Object> scheduledExecutor()     throws Exception { return scheduledExecutor.run(); }
    @GetMapping("/fork-join")             public Map<String, Object> forkJoin()                                 { return forkJoin.run(); }
    @GetMapping("/reentrant-lock")        public Map<String, Object> reentrantLock()         throws Exception { return reentrantLock.run(); }
    @GetMapping("/read-write-lock")       public Map<String, Object> readWriteLock()         throws Exception { return readWriteLock.run(); }
    @GetMapping("/stamped-lock")          public Map<String, Object> stampedLock()                              { return stampedLock.run(); }
    @GetMapping("/semaphore")             public Map<String, Object> semaphore()             throws Exception { return semaphore.run(); }
    @GetMapping("/count-down-latch")      public Map<String, Object> countDownLatch()        throws Exception { return countDownLatch.run(); }
    @GetMapping("/cyclic-barrier")        public Map<String, Object> cyclicBarrier()         throws Exception { return cyclicBarrier.run(); }
    @GetMapping("/phaser")                public Map<String, Object> phaser()                throws Exception { return phaser.run(); }
    @GetMapping("/exchanger")             public Map<String, Object> exchanger()             throws Exception { return exchanger.run(); }
    @GetMapping("/atomics")               public Map<String, Object> atomics()               throws Exception { return atomics.run(); }
    @GetMapping("/concurrent-collections") public Map<String, Object> concurrentCollections() throws Exception { return concurrentCollections.run(); }
    @GetMapping("/blocking-queue")        public Map<String, Object> blockingQueue()         throws Exception { return blockingQueue.run(); }
    @GetMapping("/producer-consumer")     public Map<String, Object> producerConsumer()      throws Exception { return producerConsumer.run(); }
    @GetMapping("/synchronized-volatile") public Map<String, Object> syncVolatile()          throws Exception { return syncVolatile.run(); }
    @GetMapping("/thread-local")          public Map<String, Object> threadLocal()           throws Exception { return threadLocal.run(); }
    @GetMapping("/deadlock")              public Map<String, Object> deadlock(@RequestParam(defaultValue = "false") boolean force)
                                                                                              throws Exception { return deadlock.run(force); }
}
