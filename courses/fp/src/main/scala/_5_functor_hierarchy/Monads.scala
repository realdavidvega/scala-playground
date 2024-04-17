package _5_functor_hierarchy

import cats.*
import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.*

object Monads:
  @main
  def main4(): Unit =
    // Intuition for Monad
    // Functor, Applicative and Traverse only handle independent computations
    // What about dependencies?

    // Monad examples
    // One Future requires the previous one to be completed
    import scala.concurrent.*

    def retrieveOrder(orderId: Int): Future[List[Int]] = ???
    def retrievePrice(orderId: Int): Future[Int]       = ???

    // or implicit in Scala 2
    def total(orderId: Int)(using ec: ExecutionContext): Future[Int] =
      retrieveOrder(orderId)
        .flatMap(Future.traverse(_)(retrievePrice))
        .map(_.sum)

    // Executing the next step depends on the previous one not failing
    def getStudentById(id: Int): Option[String] = ???
    def getAddress(s: String): Option[String]   = ???

    def getStudentAddress = getStudentById(42).flatMap { s =>
      // explicitly doing this for clarity
      getAddress("student")
    }

    // for brevity
    def getStudentAddress2 = getStudentById(42).flatMap(getAddress)

    // The Monad type class
    // Captures data dependencies
    trait MyMonad[F[_]] extends Applicative[F]:
      // primitive
      def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

      // derived
      override def map[A, B](fa: F[A])(f: A => B): F[B] =
        flatMap(fa)(a => pure(f(a)))

      def myAp[A, B](fa: F[A])(f: F[A => B]): F[B] =
        flatMap(fa)(a => map(f)(f => f(a)))

    // Making dependencies explicit
    // Types play a big role in the Monad type class
    // With map you cannot change whether you fail or not (the effect)
    // def map[A, B](fa: F[A])(f: A => B): F[B]
    // With flatMap, you can do so at each step
    // def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

    // For-comprehensions
    // Syntactic sugar for chained flatMap calls
    // Sort of imperative programming
    def getStudentAddress3 = getStudentById(43).flatMap(getAddress)

    // Still Monad
    def getStudentAddress4 =
      for
        student <- getStudentById(43)
        address <- getAddress(student)
      yield address

    // Monad instance for lists
    // Several (or no) results
    // Non deterministic
    val oneOne = Monad[List].pure(1)
    println(oneOne)
    // List(1)

    val several = for
      x <- List(1, 2)
      y <- List(x + 1, x + 2)
    yield y
    println(several)
    // List(2, 3, 3, 4)

    // Monad Laws
    // In summary: pure adds no effect, for works as expected

    // Left identity: lifting toi pure and directly flatMap is equivalent to simply feeding the value
    // pure(a).flatMap(f) === f(a)
    // using for-comprehensions
    // for {
    //   a <- pure(a) === f(a)
    //   b <- f(a)
    // } yield b

    // Right identity: flatMap with pure does nothing
    // oa.flatMap(a => pure(a)) === oa
    // using for-comprehensions
    // for {
    //   a <- oa === oa
    //   b <- pure(a)
    // } yield b

    // Associativity
    // Nesting flatMap or for does not matter
    // a.flatMap(f).flatMap(g) === a.flatMap(x => f(x).flatMap(g))
    // using for-comprehensions
    //  for {                   for {
    //    a <- oa                 x <- oa
    //    b <- f(a)       ===     r <- for {
    //    c <- g(b)                 y <- f(x)
    //  } yield c                   z <- g(y)
    //                            } yield z
    //                          } yield r

    // Stack safety: tailRecM
    // Cats requires tailRecM in order to ensure monadic stack safety

    // In summary of what we have
    // Using for-comprehensions is the best way to write monads
    // We can use these abstractions for validation, error handling, etc.
    // Also for dealing with concurrency and side effects
    // We most of the times don't have to define new abstractions

    //              Applies                To
    // Functor      Pure fn.               One F[A]
    // Applicative  Pure fn.               Fixed number of F[A]
    // Traverse     Effect fn.             Every element in a container
    // Monad        Effect fn. with deps.  One F[A]

  end main4
end Monads
