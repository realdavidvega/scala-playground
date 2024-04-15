package _1_functional_error_handling

import cats.data.*
import cats.syntax.all.*

/*
 * Limitations of flatMap:
 * - Dependencies between the different computations are not explicit
 * - No error accumulation, if one of the computations fails, the whole computation fails
 */
object ValidatedFP:
  // Validated is a data type that can accumulate errors
  // Covariant, error-accumulating friendly type from Cats
  sealed trait Validated2[+E, +A]

  // Looks like Either, but has different operations
  case class Valid2[A](value: A)   extends Validated2[Nothing, A]
  case class Invalid2[E](error: E) extends Validated2[E, Nothing]

  class MyError(val message: String)

  // Can't use Validated directly, because it's not a Monad
  // Use ValidatedNec (NonEmptyChain) instead or ValidatedNel (NonEmptyList)
  // NonEmptyChain is a data type that can't be empty, same as NonEmptyList
  // This allows us to accumulate errors

  val y = NonEmptyChain.one(1)
  val z = y ++ NonEmptyChain.of(2, 3)

  // The motivation behind NonEmptyChain is because, with NonEmptyList, the append operation is O(nˆ2)
  // With NonEmptyChain, the append operation is O(1)
  // NonEmptyChain is a better choice for accumulating errors, because it's more efficient

  // Horizontal composition
  // Two pieces of data that depend on each other
  def ageIsPositive(age: Int): ValidatedNec[MyError, Int] =
    if age >= 0 then age.valid
    else MyError("Age must be positive").invalidNec

  def majorInTheUS(age: Int): ValidatedNec[MyError, Int] =
    if age >= 21 then age.valid
    else MyError("You must be 21 or older").invalidNec

  val age = -22

  // Combining the two computations
  // Can't use flatMap directly on Validated, because it's not a Monad
  // We need to use ValidatedNec or ValidatedNel
  // Because it's a Semigroup, it can accumulate errors
  val enterLiquorShop =
    ageIsPositive(age) *> majorInTheUS(
      age
    ) // Invalid(Chain(MyError(Age must be positive), MyError(You must be 21 or older)))

  // Vertical composition
  // Two pieces of data that are independent of each other
  class Customer(name: String, age: Int)

  def emptyCheck(name: String): ValidatedNec[MyError, String] =
    if name.isEmpty then MyError("Name can't be empty").invalidNec
    else name.validNec

  def validateCustomer(name: String, age: Int): ValidatedNec[MyError, Customer] =
    (emptyCheck(name), majorInTheUS(age)).mapN((_, _) => Customer(name, age))

  val customer = validateCustomer("John", 22)

  // *> Versus mapN
  // *> is used when we don't care about the result of the first computation
  // mapN is used when we care about the result of both computations
  // In both cases, the errors are accumulated

  // Composition restrictions
  // - Regarding the error type, the two Validated instances must have the same error type
  // - Existing Semigroup instance for the error type
  trait Semigroup2[A]:
    def combine(x: A, y: A): A // how you combine both error cases

  // Usually error types do not combine, and there's no meaningful way of creating a Semigroup instance for them
  sealed trait BusinessError
  case class MaxLengthExceeded(max: Int, actual: Int) extends BusinessError
  case class NumberParseError(inputString: String)    extends BusinessError
  case class CustomError(inputString: String)         extends BusinessError

  // Non-Empty List and Non-Empty Chain are Semigroups
  // They can be combined
  val x = NonEmptyList.of(1)

  // Combining two NonEmptyLists
  val xs = x ++ List(2, 3)

  // ValidatedNel
  // Uses NonEmptyList to accumulate errors
  val validNumber   = 123.validNel[String]
  val invalidNumber = "not a number".invalidNel[Int]
  val combined      = validNumber *> invalidNumber

  // Chaining validations
  // Sometimes fail-fast is desired, like in Either
  def parseAnInt(input: String): ValidatedNel[BusinessError, Int] =
    if input.matches("-?[0-9]+") then input.toInt.validNel
    else NumberParseError(input).invalidNel

  def someMajorInTheUSValidation(input: Int): ValidatedNel[BusinessError, Int] =
    if input >= 21 then input.validNel
    else CustomError("You must be 21 or older").invalidNel

  def someAgeValidation(input: String): ValidatedNel[BusinessError, Int] =
    parseAnInt(input).andThen(someMajorInTheUSValidation)

  val ageValidation = someAgeValidation("22") // Valid(22)

  // Traverse
  // Applies validation over a collection
  val rawNumbers = List("1", "2", "3", "4", "5")

  // Applies parseAnInt to each element of the list
  val validatedNumbers = rawNumbers.traverse(parseAnInt) // Valid(List(1, 2, 3, 4, 5))

  // Why not map?
  // 1. Validation function f: A => ValidatedNel[E, B]
  // 2. Collection type xs: List[A]
  val badMap = List(11, 12).map(
    someMajorInTheUSValidation
  ) // List(Invalid(NonEmptyList(CustomError(You must be 21 or older))), Invalid(NonEmptyList(CustomError(You must be 21 or older))))
  val badTraverse = List(11, 12).traverse(
    someMajorInTheUSValidation
  ) // Invalid(NonEmptyList(CustomError(You must be 21 or older), CustomError(You must be 21 or older)))

  // Often what we want, is what traverse does
  // Meaning it returns a whole validated list, or a list of errors
  val goodTraverse =
    List(22, 23, 24).traverse(someMajorInTheUSValidation) // Valid(List(22, 23, 24))

  // Parallel Either composition
  // Wrapping validations into non-empty chains
  type BusinessErrors = NonEmptyChain[BusinessError]

  def validateAnAge(age: Int): Either[BusinessErrors, Int] =
    Either.cond(age >= 21, age, NonEmptyChain.one(CustomError("You must be 21 or older")))

  def validateAName(name: String): Either[BusinessErrors, String] =
    Either.cond(name.nonEmpty, name, NonEmptyChain.one(CustomError("Name can't be empty")))

  // Parallel composition
  // Either would fail fast, but ValidatedNec would accumulate errors
  val parallelComposition = (
    validateAName("John").toValidated,
    validateAnAge(22).toValidated
  ).mapN((name, age) => Customer(name, age)).toEither // Right(Customer(John,22))

  // Parallel composition with parMapN
  // parMapN is a method that comes from the Semigroup type class
  // It's a parallel version of mapN, that works with Validated
  // The Parallel type-class does the conversions under the hood
  val parallelComposition2 = (
    validateAName("John"),
    validateAnAge(22)
  ).parMapN((name, age) => Customer(name, age)) // Right(Customer(John,22)

  // Functional validation takeaways
  // Small, composable, reusable, testable validations
  // Compose using *>, andThen, mapN...

  // Discerning Validation
  // When to fail fast, when to fail loudly

  def readFile(path: String): Either[MyError, String]                                      = ???
  def parseConfig(config: String): Either[MyError, Map[String, String]]                    = ???
  def validateConfig(config: Map[String, String]): Validated[MyError, Map[String, String]] = ???

  def startApp(configPath: String): Either[MyError, Map[String, String]] =
    for
      file            <- readFile(configPath)            // fail fast
      config          <- parseConfig(file)               // fail fast
      validatedConfig <- validateConfig(config).toEither // give back several errors, and then fail
    yield validatedConfig // if we get here, we have a valid config

  @main
  def main2(): Unit =
    println("enterLiquorShop : " + enterLiquorShop)
    println("customer : " + customer)
    println("combined : " + combined)
    println("ageValidation : " + ageValidation)
    println("validatedNumbers : " + validatedNumbers)

    println("xsMap : " + badMap)
    println("xsTraverse : " + badTraverse)
    println("goodTraverse : " + goodTraverse)

    println("parallelComposition : " + parallelComposition)
    println("parallelComposition2 : " + parallelComposition2)
end ValidatedFP
