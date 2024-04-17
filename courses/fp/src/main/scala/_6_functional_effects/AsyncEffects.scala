package _6_functional_effects

import cats.*
import cats.syntax.all.*

import java.util.concurrent.CompletableFuture
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object AsyncEffects:
  @main
  def main2(): Unit =
    // Asynchronous Effects
    // IO is a particular implementation, Monix is another
    // Popular libraries like FS2, Doobie, http4s, build over them
    // They do not commit to a particular effect system or implementation
    // One good principle is using inversion of control (program against well-defined interfaces)
    // Another is the principle of least power (use as little power as possible)

    // Cats Effect type classes
    // MonadCancel only adds resource safety
    // Async give us complete freedom with fibers (more lightweight than threads)

    // Quick intro to fibers
    // Can be cancelled
    sealed trait Outcome[F[_], E, A]
    final case class Succeeded[F[_], E, A](fa: A) extends Outcome[F, E, A]
    final case class Failed[F[_], E, A](e: E)     extends Outcome[F, E, A]
    final case class Canceled[F[_], E, A]()       extends Outcome[F, E, A]

    // When writing resource safe code, you have to worry about exceptions and cancellation

    // MonadError
    // Models exceptions as functional effects
    trait MyMonadError[F[_], E] extends Monad[F]:
      // raise errors
      def raiseError[A](e: E): F[A]
      def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]
      // try to handle and return the Either
      def attempt[A](fa: F[A]): F[Either[E, A]] =
        handleErrorWith(
          map(fa)(Right(_): Either[E, A])
        )(e => pure(Left(e)))

    // Error handling
    def safeDivide[F[_]](a: Int, b: Int)(using err: MonadError[F, String]): F[Int] =
      if b == 0 then err.raiseError("Divide by zero")
      else err.pure(a / b)

    // MonadCancel
    // Safe resource acquisition and release
    // Even in the face of exceptions or cancellation

    // What happens when there's an error in...?
    // Acquisition: return the error
    // Use: run the release... also an error? report it
    // Release: depends on the effect system
    trait MyMonadCancel[F[_], E] extends MonadError[F, E]:
      def bracket[A, B](acquire: F[A])(use: A => F[B])(release: A => F[Unit]): F[B]

    // MonadCancel example
    import cats.effect.*
    import java.io.FileInputStream

    def open(fileName: String): IO[FileInputStream]                = ???
    def readLine(inputStream: FileInputStream): IO[Option[String]] = ???
    def close(inputStream: FileInputStream): IO[Unit]              = ???

    def monadCancelExample =
      MonadCancel[IO, Throwable]
        .bracket(acquire = open("test.txt"))(use = readLine)(release = close)

    // Handicap: not so nice when we have multiple resources

    // Resource
    // Encapsulates acquisition and release, with a nicer API than MonadCancel
    // Acquire all the resources, use them, and release them
    // The code becomes more readable
    import cats.effect.Resource

    // Resource example
    def resourceExample = (for
      file1 <- Resource.make(open("test.txt"))(close)
      file2 <- Resource.make(open("test2.txt"))(close)
      file3 <- Resource.make(open("test3.txt"))(close)
    yield (file1, file2, file3)).use { (file1, file2, file3) =>
      ??? // do stuff
    }

    // MonadCancel masking
    // uncancelable is lower-level operation
    // Perform cancellation-sensitive operations
    // A fiber which cannot be cancelled is said to be masked
    import cats.effect.kernel.Poll

    trait MyMonadCancel2[F[_], E] extends MonadError[F, E]:
      def uncancelable[A](body: Poll[F] => F[A]): F[A]

    // Masks cancellation of the current fiber
    // With poll, we can unmask a fiber within a masked region (cancellation can be observed again)
    // Cancellation can be observer only in fb and nowhere else
    // def call[F[_]](fa: F[Unit], fb: F[Unit], fc: F[String])
    //               (using F: MonadCancel[F, Throwable]): F[String] =
    //  F.uncancelable { poll =>
    //    fa *> poll(fb) *> fc

    // Sync
    // Suspend synchronous code
    // Everything coming from MonadCancel
    trait MySync[F[_]] extends MonadCancel[F, Throwable]:
      // ...
      def pure[A](a: A): F[A]
      def delay[A](thunk: => A): F[A]

    // Generalization of the IO constructor
    IO(println("Hello World!"))

    // Sync example
    // Just descriptions!
    def myProgram[F[_]](using F: Sync[F]): F[Unit] =
      // pure
      val f1: F[String] = F.pure("hello")

      // error
      val boom: F[Int] = Sync[F].raiseError(new RuntimeException("boom!"))

      // still error, but with an attempt
      val safe: F[Either[Throwable, Int]] = boom.attempt

      // delay of something
      val f3: F[Unit] = F.delay(println("Hello World from my program!"))

      // build computations
      f1 *> safe *> f3 // f1 productR safe productR f3

    // Commit to IO and then use it
    import cats.effect.unsafe.implicits.global
    myProgram[IO].unsafeRunSync()

    // Async
    // Execute asynchronous code
    // Computations which product only one result
    // Same principle of callbacks or continuations
    trait MyAsync[F[_]] extends Sync[F]:
      // expects another function (callback)
      def async_[A](k: (Either[Throwable, A] => Unit) => Unit): F[A]
      def async[A](k: (Either[Throwable, A] => Unit) =>
                  F[Option[F[Unit]]]): F[A]

    // Async example
    def fromFuture[F[_]: Async, A](fa: => Future[A])(ec: ExecutionContext): F[A] =
      Async[F].async_ { cb =>
        // wait until completed or failed
        fa.onComplete {
          // we need to call the callback
          case Success(a) => cb(Right(a))
          case Failure(e) => cb(Left(e))
        }(ec)
      }

    // Async example with finalizer
    // async allows us to return a finalizer
    // Such finalizer is called if the fiber is cancelled
    def fromCompletableFuture[F[_]: Async, A](cf: => CompletableFuture[A]): F[A] =
      // create a callback
      Async[F].async_ { (cb: Either[Throwable, A] => Unit) =>
        Sync[F].delay {
          // create the stage
          val stage: CompletableFuture[Unit] = cf.handle[Unit] {
            // I'm done, call the callback
            case (a, null) => cb(Right(a))
            case (_, e) => cb(Left(e))
          }
          // register the finalizer, that will cancel the stage
          val finalizer: Option[F[Unit]] =
            Some(Sync[F].delay(stage.cancel(false)).map(_ => ()))
          // return the finalizer
          finalizer
        }
      }

    // IO revisited
    // Best practice: program to the interfaces
    // Defer commiting to an implementation for as long as possible

    // Core
    def logic[F[_]: Sync](): F[Int] =
      Sync[F].pure(42) // sync effect

    // Outer Layer
    val ioa: IO[Int] = logic[IO]()
    
    // However, it introduces a level of indirection
    // Some libraries like Monix or ZIO provide additional combinations directly to their data types
    // Depends on the skill and experience of the library
    
  end main2
end AsyncEffects
