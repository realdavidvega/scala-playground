package _6_functional_effects

import cats.*
import cats.effect.{Async, Fiber, GenSpawn, IO, MonadCancel, Outcome, Sync, Temporal}
import cats.effect.unsafe.implicits.global
import cats.effect.unsafe.IORuntime
import cats.syntax.all.*
import concurrent.duration.DurationInt

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

object Fibers:
  @main
  def main3(): Unit =
    // Concurrency vs Parallelism

    // Concurrency => fairness
    // Interleaving multiple tasks, maybe on a single processor
    // Two queues, one executor

    // Parallelism => efficiency
    // Executing independent tasks, on multiple processors
    // Two queues, two executors

    // Concurrency in the JVM (before LOOM)
    // Thread => unit of execution
    // Maps 1:1 with the Operating System threads
    // At most, 1 can be executed per CPU core at a time
    // Scarce and expensive: creation and teardown, context switching too
    object MyTask extends Runnable:
      def run(): Unit = println(s"Hello from thread ${Thread.currentThread().getName}")

    val thread1 = new Thread(MyTask)
    thread1.run()
    thread1.interrupt()

    // Thread Pool (group of threads)
    // Preallocate threads and reuse them in a round-robin fashion
    // Each pool is assigned to a work queue
    val pool = Executors.newFixedThreadPool(2)

    pool.submit(MyTask)
    pool.submit(MyTask)
    pool.close()

    // Scheduler
    // Decides which thread to execute next
    // Two strategies: Preemptive and Cooperative
    // Preemptive: scheduler decides which thread should run and how long (JVM Scheduler)

    // Cooperative: threads themselves yield control, after some time, or when waiting for some event
    // An example is Cats Fibers

    // Fibers or lightweight threads
    // Logical threads implemented in a library: reduced context-switching, more efficient
    // Map M:N with JVM threads, where M > N
    // Many fibers can run on the same thread and cooperative scheduling

    // Execution Context
    // Parallelism in Scala => execution context
    // Decides which thread to run on each computation, and it may create additional threads
    // By default, ExecutionContext.global uses a pool of as many threads as there are cores
    import concurrent.ExecutionContext.Implicits.global

    val context = ExecutionContext.global
    val result  = Future(42)(context)
    result.onComplete(_ => println(s"Future completed"))

    // Running IO in a different context
    // Cats Effect has the notion of IORuntime
    // It has its own ExecutionContext, Scheduler and other information for execution
    // The default is IORuntime.global

    // We create a program that prints "Hello World!"
    def program[F[_]](using F: Sync[F]): F[Unit] =
      F.delay(println("Hello World!"))

    // We run the program in the global context
    program[IO].unsafeRunSync()(IORuntime.global)

    // Context shifting
    // Async has operations that shift the context
    trait MyAsync[F[_]] extends Sync[F]:
      // gets the current execution context
      def executionContext: F[ExecutionContext]

      // execute the given computation in a different context, then shift back to the original context
      def evalOn[A](fa: F[A], ec: ExecutionContext): F[A]

    // some program
    def programSpan[F[_]: Async]: F[Unit] =
      // print the current thread
      val printThread: F[Unit] =
        Async[F].executionContext.flatMap(t => Async[F].delay(println(t)))

      // execute the program in a different context
      val someEc: ExecutionContext =
        ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

      // print the current thread, eval in the new context, and print again
      for
        _ <- printThread
        _ <- Async[F].evalOn(printThread, someEc)
        _ <- printThread
      yield ()

    // run the program
    // programSpan[IO].unsafeRunSync()

    // Blocking operations
    // Some operations block the current thread
    // Most IO operations are blocking, also server socket waiting for connections
    // In cooperative scheduling, those are perfect places to yield control

    // Blocking and suspension
    // They are handled by a different thread pool
    trait MySync[F[_]] extends MonadCancel[F, Throwable]:
      // blocking and interruptible mark blocking operations
      def blocking[A](thunk: => A): F[A]

      // interruptible tries to abort the blocking operation in the event of cancellation
      def interruptible[A](many: Boolean)(thunk: => A): F[A]

    // Good practices for IO
    // Latest version of the JVM includes NIO (Non-Blocking IO)
    // FS2 is an alternative in the Typelevel ecosystem, with nicer API based on streams and cats.effect

    // Clock
    trait MyClock[F[_]]:
      // Monotonic clock: analogous to System.nanoTime()
      // It is guaranteed to increment monotonically
      def monotonic: F[FiniteDuration]

      // Real time clock: analogous to System.currentTimeMillis()
      def realtime: F[FiniteDuration]

    // Temporal
    // Suspend fibers until the given time
    trait MyTemporal[F[_]]:
      // The scheduler knows about this operation
      // More performant than Thread.sleep
      def sleep(duration: FiniteDuration): F[Unit]
      def delayBy[A](fa: F[A], duration: FiniteDuration): F[A]
      def andWait[A](fa: F[A], duration: FiniteDuration): F[A]
      def timeout[A](fa: F[A], duration: FiniteDuration): F[A]
      def timeoutTo[A](fa: F[A], duration: FiniteDuration, fallback: F[A]): F[A]

    // Explicit fiber handling
    // The Spawn type class exposes raw fibers
    // Other type classes build on top of it, so try to use higher level concepts when possible
    // Misuse can result in resource leaks or deadlocks
    trait MySpawn[F[_], E] extends MonadCancel[F, E]:
      // We can start a computation in a new fiber
      // This fiber will be scheduled for execution
      // Semantically, it starts as soon as the function is called
      def start[A](fa: F[A]): F[Fiber[F, E, A]]
      def never[A]: F[A]

    trait MyFiber[F[_], E, A]:
      // We can cancel a fiber, which stops it from executing
      // For example when a computation is no longer needed
      // Other effects may arise on fibers waiting on that one
      def cancel: F[Unit]

      // Join stops the current fiber until another fiber is completed
      // With join, errors are propagated
      def join: F[Outcome[F, E, A]]

      // Other variations of join
      // With joinWith, we can attach a finalizer for cancellation
      def joinWith(onCancel: F[A])(using F: MonadCancel[F, E]): F[A]

      // With joinWithNever, we block indefinitely in case of cancellation
      def joinWithNever(using F: GenSpawn[F, E]): F[A] = joinWith(F.never)

    // Example of manual fiber handling
    // A method to print something to the screen non-stop
    def repeat[F[_]: Sync](s: String): F[Unit] =
      for
        _ <- Sync[F].delay(println(s))
        _ <- repeat[F](s)
      yield ()

    // Example of cancellation
    import cats.effect.syntax.all.genSpawnOps
    import concurrent.duration.DurationInt

    def cancelRepeat[F[_]: Async]: F[Unit] =
      for
        // Spawn two never-ending fibers
        f1 <- repeat[F]("hello").start     // background
        f2 <- repeat[F]("world").start     // background
        _  <- Temporal[F].sleep(5.seconds) // main fiber of execution, wait 5s
        _  <- f1.cancel                    // cancel the first fiber
        _  <- f2.cancel                    // cancel the second fiber
      yield ()

    // Waiting for 2 fibers
    def slowOp[F[_]: Async](s: String): F[String] =
      Temporal[F].sleep(5.seconds) *> Sync[F].delay(s)

    // Waiting for 2 fibers with join we are forced to wait in some order
    def wait2Fibers[F[_]: Async]: F[Unit] =
      for
        f1 <- slowOp[F]("hello").start
        f2 <- slowOp[F]("world").start
        a  <- f1.joinWithNever
        b  <- f2.joinWithNever
        _  <- Sync[F].delay(println(s"$a, $b"))
      yield ()

    // But the other may end up first! what if we care about just one of them?
    // Race returns the first fiber to complete
    // Takes care of graceful termination of all fibers
    case class ApiResult(value: Int)

    def apiCall[F[_]: Async]: F[ApiResult] =
      // fake impl
      val cheapApi: F[ApiResult]     = Temporal[F].sleep(5.seconds).as(ApiResult(1))
      val expensiveApi: F[ApiResult] = Temporal[F].sleep(100.millis).as(ApiResult(1))

      Async[F].race(cheapApi, expensiveApi).map {
        case Left(value)  => value
        case Right(value) => value
      }

    // Summary
    // Cats Effects provide complex capabilities for async ops
    // From high-level (Async) to low-level (Spawn)
    // Also includes useful data types: Resources, Ref (concurrent mutable references), ...

  end main3
end Fibers
