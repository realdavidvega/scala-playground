package _5_functor_hierarchy

import cats.*
import cats.data.ValidatedNec
import cats.syntax.all.*

object Applicatives:
  @main
  def main2(): Unit =
    // Intuition for Applicative
    // Function gives the ability to map over values
    // What if we need to work with multiple F[_]?
    val v1 = 1.valid[String]
    println(v1)
    // Valid(1)

    val v2 = 2.valid[String]
    println(v2)
    // Valid(2)

    val result = (v1, v2).mapN(_ + _)
    println(result)
    // Valid(3)

    // Applicative encodes the ability to work with multiple F[_], or multiple independent effects
    trait MyApplicative[F[_]] extends Functor[F]:
      // 2 or more arguments
      def zip[A, B](fa: F[A], fb: F[B]): F[(A, B)] // combines two effects

      // By iterating this mapN we can build it for any number of effects
      def mapN[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C]

      // What if our function is nullary (no arguments)?
      def lift[A](a: A): F[A]
      def pure[A](a: A): F[A] = lift(a)

      // 1 argument: map from Functor

    // Applicative example
    val one: ValidatedNec[String, Int] = "hello".invalidNec
    println(one)
    // Invalid(NonEmptyChain(hello))

    val two: ValidatedNec[String, Int] = 1.validNec
    println(two)
    // Valid(1)

    val three: ValidatedNec[String, Int] = "kaboom".invalidNec
    println(three)
    // Invalid(NonEmptyChain(kaboom))

    // Syntax that uses the Applicative type class
    val product = one.product(two).product(three)
    println(product)
    // Invalid(NonEmptyChain(hello, kaboom))

    // Same as: (one, two, three).mapN { case (o, tw, th ) => (o, tw, th) }
    val mapN = (one, two, three).mapN(_ * _ * _)
    println(mapN)
    // Invalid(NonEmptyChain(hello, kaboom))

    // Forgetful operations
    // Keep only the information in one side
    // The point is to validation or functional effects
    // Something that must simply happen
    def productL[F[_]: Applicative, A, B](fa: F[A], fb: F[B]): F[A] = ???
    def productR[F[_]: Applicative, A, B](fa: F[A], fb: F[B]): F[B] = ???

    // < points towards what we want to keep
    val four: ValidatedNec[String, Int] = 1.validNec
    println(four)
    // Valid(1)

    val five: ValidatedNec[String, String] = "hola".validNec
    println(five)
    // Invalid(NonEmptyChain(hola))

    val six: ValidatedNec[String, Int] = "hello".invalidNec
    println(six)
    // Invalid(NonEmptyChain(hello))

    val a = four <* five
    println(a)
    // Valid(1)

    val b = four *> five
    println(b)
    // Valid(hola)

    val c = six *> four
    println(c)
    // Invalid(NonEmptyChain(hello))

    // Applicative laws
    // Associativity: the way in which you zip makes no difference
    // val fa: F[A] = ???
    // val fb: F[B] = ???
    // val fc: F[C] = ???
    //
    // fa.product(fb).product(fc) === fa.product(fb.product(fc))
    //  .map { case (a, (b, c)) => ((a, b), c) }

    // Left and right identity: zipping with unit makes no difference
    // Left identity
    // pure(()).product(fa).map(_._2) === fa
    // Right identity
    // fa.product(pure(())).map(_._1) === fa

  end main2
end Applicatives
