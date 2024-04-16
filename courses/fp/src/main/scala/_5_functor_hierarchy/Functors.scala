package _5_functor_hierarchy

import cats.Functor

object Functors:
  @main
  def main(): Unit =
    // Values and type constructors
    // Value types are argument or result of a function: Int, Map[String, Int], etc
    // Type constructors are types with "holes" in them: List, Option, etc

    // Cats type classes
    // Value types: Eq, Order, Semigroup, Monoid, Show
    // Type constructors: Functor, Applicative, Traverse, Monad

    // Intuition for Functor
    trait MyList[T]:
      def map[U](f: T => U): MyList[U] = ???

    trait MyOption[T]:
      def map[U](f: T => U): MyOption[U] = ???

    trait MyEither[E, T]:
      def map[U](f: T => U): MyEither[E, U] = ???

    // Common pattern
    trait F[T]:
      def map[U](f: T => U): F[U] = ???

    // Functor type class
    // A type constructor is a Functor if it has a method called `map`
    trait MyFunctor[F[_]]:
      def map[A, B](fa: F[A])(f: A => B): F[B]
      def fmap[A, B](fa: F[A])(f: A => B): F[B] = map(fa)(f) // different order

    // Instances

    // or implicit
    given listFunctor: MyFunctor[List] = new MyFunctor[List]:
      def map[A, B](fa: List[A])(f: A => B): List[B] = fa.map(f)

    // or implicit
    given optionFunctor: MyFunctor[Option] = new MyFunctor[Option]:
      def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa match
        case None => None
        case Some(a) => Some(f(a))

    // Cats provides nicer syntax
    val summonFunctorForList = Functor[List]

    val plusOne1 = summonFunctorForList.map(List(1, 2, 3))(a => a + 1)
    println(plusOne1)
    // List(2, 3, 4)

    val plusOne2 = summonFunctorForList.map(List(1, 2, 3))(_ + 1)
    println(plusOne2)
    // List(2, 3, 4)

    // Functor laws
    // Identity law: mapping using identity must not change the value
    // Composition law: should not matter whether you compose or map, or vice versa

    // Lifting a function
    // Lets swap the order
    trait MyFunctor2[F[_]]:
      def map[A, B](fa: F[A])(f: A => B): F[B]
      def fmap[A, B](fa: F[A])(f: A => B): F[B] = map(fa)(f)

    // A function transforms a function A => B into a function F[A] => F[B]
    // We say we lift the function to F

  end main
end Functors
