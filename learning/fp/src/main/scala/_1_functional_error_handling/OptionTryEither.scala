package _1_functional_error_handling

import scala.util.Try

/*
 * There are many ways a program can fail and different ways to handle those failures:
 * - Incoming data can be invalid -> validation (expected, solvable)
 * - Out coming data can be invalid -> error (expected, solvable)
 * - Bad and unexpected things can happen -> exception (unexpected, unsolvable)
 * As general rule, fail fast and fail early:
 * - Report unexpected errors as soon as possible
 * - Report with as much information as possible
 * - Never go silent about errors
 */
object OptionTryEither:
  /* Type system makes errors explicit:
   * - Free documentation
   * - Enforces error handling on compile time
   * - Option, Try, Either, Validated
   */

  /* Option: represents a value that might not be there
   * - Some: value is present
   * - None: value is absent
   * Safe way to communicate the possibility of an error
   * As long never use unsafe operations, it's impossible to get a NullPointerException
   */
  def safeDivide(a: Float, b: Float): Option[Float] =
    if b == 0 then None else Some(a / b)

  /* Try: represents a computation that might fail, wraps dangerous code
   * - Success: computation succeeded
   * - Failure: computation failed (contains a Throwable) - no more try and catch
   * Useful dealing with Java APIs that throw exceptions
   */
  def safeParseInt(s: String): Try[Float] = Try(s.toFloat)

  val success = safeParseInt("42") // Success(42)
  val failure = safeParseInt(
    "01"
  ) // Failure(java.lang.NumberFormatException: For input string: "01")

  /* Either: represents a value of one of two possible types
   * - Left: represents failure (not restricted to the Throwable type)
   * - Right: represents success
   * Useful when you want to return a value or an error message
   */
  def eitherDivision(a: Float, b: Float): Either[String, Float] =
    if b == 0 then Left("Division by zero") else Right(a / b)

  val eitherSuccess = eitherDivision(42, 2) // Right(21)
  val eitherFailure = eitherDivision(42, 0) // Left(Division by zero)

  // Bridge Try and Either
  def safeParseIntEither(s: String): Either[String, Float] =
    Try(s.toFloat).toEither.left
      .map(failure => s"Failure $s: ${failure.getClass}")

  val failed = safeParseIntEither("01") // Left(Failure 01: class java.lang.NumberFormatException)

  // Composing computations
  def average(values: List[Float]): Float =
    safeDivide(values.sum, values.size).get

  // val failureAverage = average(List.empty[Float]) // java.util.NoSuchElementException: None.get

  // Option 1, provide a default value, a way of recovering from the error
  def averageOption1(values: List[Float]): Float =
    safeDivide(values.sum, values.size).getOrElse(0)

  val successAverage1 = averageOption1(List.empty[Float]) // 0

  // Option 2, pattern matching, describe what to do in case of success and failure
  def averageOption2(values: List[Float]): Float =
    safeDivide(values.sum, values.size.toFloat) match
      case Some(value) => value
      case None        => 0

  val successAverage2 = averageOption2(List.empty[Float]) // 0

  // Option 3, fold, describe what to do in case of success and failure
  trait Option2[A]:
    def fold[B](ifEmpty: => B, f: A => B): B

  trait Try2[A]:
    def fold[B](failure: Throwable => B, success: A => B): B

  trait Either2[E, A]:
    def fold[B](onLeft: E => B, onRight: A => B): B

  def averageFold(values: List[Float]): Float =
    safeDivide(values.sum, values.size).fold(0.0f)(x => x)

  val successFold = averageFold(List.empty[Float]) // 0.0

  // Escaping error context
  // We always "peel off" the Option, Try, or Either to get the value
  // What if we want to keep the error context?
  // Higher order functions to the rescue (lift functions into the error context)
  val ok1 = safeDivide(42, 2)            // Some(21)
  val ok2 = safeDivide(42, 2).map(_ * 2) // Some(42)
  val bad = safeDivide(42, 0).map(_ + 1) // None

  // We sequence operations, fail fast, fail on the first error
  class Request:
    val id: String = "123"

  class Account:
    val address: Address = Address("123 Main St")

  class Address(val street: String)

  class Error(message: String)

  private def parseId(request: Request): Either[Error, String] =
    Right(request.id)

  private def fetchAccount(id: String): Either[Error, Account] =
    Right(Account())

  def showAddress(request: Request): Either[Error, Address] =
    parseId(request).flatMap { id =>    // flatMap is the sequencing operation
      fetchAccount(id).map { account => // map is the lifting operation
        account.address
      }
    }

  trait Either3[E, A]:
    // the next computation may fail, so we can't lift it into the error context
    def flatMap[B](f: A => Either3[E, B]): Either3[E, B]
    // the next computation may not fail, so we can lift it into the error context
    def map[B](f: A => B): Either3[E, B]

  def showAddress2(request: Request): Either[Error, Address] =
    for // for comprehension is a syntactic sugar for flatMap and map
      id      <- parseId(request) // flatMap - Either[Error, String]
      account <- fetchAccount(id) // map - Either[Error, Account]
    yield account.address // Either[Error, Address]

  // for-comprehension restriction, every step executed within the same error context
  // What if we want to sequence operations that may fail in different ways?
  def showAddress3(request: Request): Either[Error, Address] =
    for
      id      <- parseId(request) // Try[String]
      account <- fetchAccount(id) // Option[Account]
    yield account.address
    // Error: type mismatch; found: Option[Account], required: Either[Error, Account]

  // unify to either, the more general error type
  //  def showAddress4(request: Request): Either[Error, Address] =
  //    for
  //      id <- parseId(request).toEither.left.map(_ => Error("Invalid id"))
  //      account <- fetchAccount(id).left.map(_ => Error("Account not found"))
  //    yield account.address

  @main
  def main(): Unit =
    println(safeDivide(42, 2))  // Some(21)
    println(safeDivide(42, 0))  // None
    println(safeParseInt("42")) // Success(42)
    println(safeParseInt("01")) // Failure(java.lang.NumberFormatException: For input string: "01")
    println(eitherDivision(42, 2))    // Right(21)
    println(eitherDivision(42, 0))    // Left(Division by zero)
    println(safeParseIntEither("01")) // Left(Failure 01: class java.lang.NumberFormatException)
    println(average(List(1, 2, 3)))   // 2.0
    println(averageOption1(List.empty[Float])) // 0
    println(averageOption2(List.empty[Float])) // 0
    println(averageFold(List.empty[Float]))    // 0.0
    println(ok1)                               // Some(21)
    println(ok2)                               // Some(42)
    println(bad)                               // None
    println(showAddress(Request()))            // Right(Address(123 Main St))
    println(showAddress2(Request()))           // Right(Address(123 Main St))
    println(
      showAddress3(Request())
    ) // Error: type mismatch; found: Option[Account], required: Either[Error, Account]
    // println(showAddress4(Request())) // Error: type mismatch; found: Option[Account], required: Either[Error, Account]
end OptionTryEither
