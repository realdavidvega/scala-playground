package _6_functional_effects

import cats.*
import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.*

import scala.concurrent.Future

object IntroductionToIO:
  @main
  def main(): Unit =
    // Cats Effect
    // IO is provided in a separate package (cats-effect)

    // The IO type
    // Describes a computation
    // On evaluation it produces a value of type A
    // Can end in either a value of type A or an error of type E
    // Can be cancellable
    import cats.effect.IO

    // doesn't actually invoke println
    // it just describes the computation
    val ioHello = IO {
      println("Hello World, IO!")
    }

    val program = for
      _ <- ioHello
      _ <- ioHello
    yield ()
    // Hello World!
    // Hello World!

    // We must explicitly ask for execution
    // Separation of description and execution
    // This gives capabilities like cancellation or timeouts
    import cats.effect.unsafe.implicits.global

    val result: Unit = program.unsafeRunSync()

    // IO is not Future
    // Future are executed eagerly
    // Immediately start forking threads
    // Once computation is done, the result is returned
    import concurrent.ExecutionContext.Implicits.global

    val futureHello = Future {
      println("Hello World, Future!")
    }

    val futureProgram = for
      _ <- futureHello
      _ <- futureHello
    yield ()
    // Hello World!

    // The Simplest IO
    // No Async, No Cancellation, No Suspension of side effects
    // MyIO is monadic, so we can use for
    // Also, it uses functions () => A to suspend execution
    case class MyIO[A](private val value: () => A):
      // Create a bigger computation
      // Execute the value and pass the value to the next computation
      def flatMap[B](f: A => MyIO[B]): MyIO[B] = MyIO { () =>
        val a   = value()
        val iob = f(a)
        iob.value()
      }

      // Same with map
      def map[B](f: A => B): MyIO[B] = MyIO { () =>
        f(value())
      }

      // Execute the computation
      def unsafeRun(): A = value()

    // Modes of evaluation
    //                    Eager        Lazy
    // Synchronous         A          () => A
    //                                Eval[A]
    // Asynchronous       Future[A]   IO[A]

    // Running our IO
    // IO[A] describes a computation which produces a value of type A
    // You need to run it to get the result
    object MyIOExample extends App:
      val myIOHello = MyIO(() => println("Hello World, MyIO!"))

      val myProgram: MyIO[Unit] = for
        _ <- myIOHello
        _ <- myIOHello
      yield ()

      myProgram.unsafeRun()

    // Execute the App
    val args = Array[String]()
    MyIOExample.main(args)

    // By-name parameters
    // Scala has a nice syntax for delaying evaluation
    // => A are used to suspend execution
    object NicerIO:
      def apply[A](body: => A): MyIO[A] = MyIO(() => body)

    val betterHello = NicerIO(println("Hello World, NicerIO!"))
    betterHello.unsafeRun()

    // Running Cats Effect IO
    import cats.effect.*
    import concurrent.duration.DurationInt

    val ioLife = IO(println("Hello World, Cats IO!"))
    ioLife.unsafeRunSync()

    // We can run IO in any different ways
    val ioLife2 = IO.pure(42)
    ioLife2.unsafeRunSync()           // 42
    ioLife2.unsafeRunTimed(5.seconds) // Some(42)
    ioLife2.unsafeRunAsync {
      case Left(e)  => println(s"Computation failed with exception $e")
      case Right(a) => println(s"Computation succeeded with result $a")
    }                        // Computation succeeded with result 42
    ioLife2.unsafeToFuture() // Future(42)

    // IO Applications
    // We use IOApp so we never have to call unsafe operators
    import cats.syntax.all.*

    object MyIOApp extends IOApp:
      def run(args: List[String]): IO[ExitCode] =
        IO.delay(println("Hello World, IOApp!")).as(ExitCode.Success)

    val args2 = List[String]()
    MyIOApp.run(args2)

    // Conclusion
    // Basic blocks are side effectful operations
    // Compose them with map, flatmap, for-comprehensions
    // Call an unsafe operation at the very end
    // - Those will perform side effects
    // - Or even avoid them altogether using IOApp

  end main
end IntroductionToIO
