package _4_higher_kinded_types

import java.time.LocalDate

object HKTs:
  @main
  def main(): Unit =
    // The problem
    // These three functions are identical, except for the types
    def tuple[A, B](as: List[A], bs: List[B]): List[(A, B)] =
      as.flatMap { a => bs.map { b => (a, b) } }

    def tuple2[A, B](as: Option[A], bs: Option[B]): Option[(A, B)] =
      as.flatMap { a => bs.map { b => (a, b) } }

    def tuple3[E, B, C](as: Either[E, B], bs: Either[E, C]): Either[E, (B, C)] =
      as.flatMap { a => bs.map { b => (a, b) } }

    // How we abstract over flatMap and map?

    // The solution, part #1
    // Type constructor
    // F[_] is a type constructor, and declares a type parameter for tupleF
    // which has a "hole" (type parameter) in it
    def tupleF[F[_], A, B](as: F[A], bs: F[B]): F[(A, B)] = ???

    // Type constructor are also known as higher kinded types (HKTs)

    // Note the difference with (usual) generics
    // generic elements in a specific container List
    def listWithThings[A](value: A): List[A] = List(value)

    // generic container with specific element type Int
    def thingWithInts[F[_]](value: Int): F[Int] = ???
    def thingWithStrings[F[_]](value: String): F[String] = ???

    // The solution, part #2
    // Type classes over type constructors
    trait Bind[F[_]]:
      def map[A, B](fa: F[A])(f: A => B): F[B]
      def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

    // We can now define our generic function
    // We inject the bind with our common type constructor
    def finallyTupleF[F[_], A, B](fa: F[A], fb: F[B])(implicit F: Bind[F]): F[(A, B)] =
      F.flatMap(fa) { a => F.map(fb)((a, _)) }

    // Using HKT functions
    //    import cats.syntax.all._
    //    val eitherOptTuple = finallyTupleF(
    //      "Hello".asRight[Int],
    //      "world".asRight[Int]
    //    )
    //    println(eitherOptTuple)
    //    // Right((Hello, world))

  end main
end HKTs
