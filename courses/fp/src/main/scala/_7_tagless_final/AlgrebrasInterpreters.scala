package _7_tagless_final

import cats.*
import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.*
import cats.effect.*

import scala.concurrent.Future
import java.nio.file.{Files as JFiles, *}
import scala.collection.mutable.ListBuffer

object AlgrebrasInterpreters:
  @main
  def main(): Unit =
    // An application made with Cats is built on:
    // A core designed with Tagless Final: business logic specification
    // A collection of classes provided by Cats: abstracts from runtime, allows to stay referentially transparent
    // A runtime (IO) to run the business logic

    // Tagless Final
    // Programming to type classes
    // Do not commit to a particular effect system
    // One good principle is using inversion of control (program against well-defined interfaces)

    // Example running of any F, we just say it is a monad
    class Repeater[F[_]]:
      def repeatCollect[A](times: Int)(fa: F[A])(using F: Monad[F]): F[Vector[A]] =
        def loop(rem: Int, acc: Vector[A]): F[Vector[A]] =
          if rem <= 0 then F.pure(acc)
          else fa.flatMap(a => loop(rem - 1, acc :+ a))

        loop(times, Vector.empty[A])

    // At some point, we choose a particular instance, you can think of it as dependency injection
    val repeaterOption = new Repeater[Option]
    println(repeaterOption)
    // _7_tagless_final.AlgrebrasInterpreters$Repeater$1@43a25848

    // Return a vector wrapped in an option
    val option = repeaterOption.repeatCollect(2)(Option(1))
    println(option)
    // Some(Vector(1, 1))

    val repeaterIO = new Repeater[IO]
    println(repeaterIO)
    // _7_tagless_final.AlgrebrasInterpreters$Repeater$1@22eeefeb

    // Return a vector wrapped in an IO
    val io = repeaterIO.repeatCollect(3)(IO.pure(1))
    println(io)
    // IO(...)

    // Algebra and interpreter
    // The methods provided by a type class form their algebra
    // We may provide additional operations building over those
    trait MyMonad[F[_]]:
      // Algebra of Monad are these methods
      def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
      def map[A, B](fa: F[A])(f: A => B): F[B]
      def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]

    // Example towards our own algebra
    // Imagine you need to introduce a delete operation
    def deleteIO(path: Path): IO[Unit] =
      IO.blocking(JFiles.delete(path))

    // We abstract from the effect system by using Sync
    // But it's still very concrete...
    def delete[F[_]: Sync](path: Path): F[Unit] =
      Sync[F].blocking(JFiles.delete(path)) // effect

    // We can abstract more with our type class Files
    // No mention of low level concerns like Sync, but focus on purely file operations
    trait Files[F[_]]:
      // Separation of concerns
      def delete(path: Path): F[Unit] // operation

    // Computations should only mention the capabilities they need
    // We may delete some files, we may raise or catch errors... and nothing more!
    def doStuff[F[_]: Files]: F[Unit] = ???

    // Each particular instance forms an interpreter, and it describes how to run the program
    // It tells how to call the methods in the algebra, and worries about the lower level details
    // More than one interpreter can be used for the same algebra
    object Files:
      def production[F[_]: Sync]: Files[F] =
        // Same as: new Files[F]: def delete(path: Path): F[Unit] = Sync[F].blocking(JFiles.delete(path))
        (path: Path) => Sync[F].blocking(JFiles.delete(path))

    // This code is also easily testable!
    object FilesTest:
      def test[F[_]: Sync](deleted: ListBuffer[Path]): Files[F] =
        // We are just pulling the list of deleted paths
        (path: Path) => Sync[F].delay { deleted += path }
