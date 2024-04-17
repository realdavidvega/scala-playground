package _6_functional_effects

import cats.*
import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.*

import scala.concurrent.Future

object TestingIO:
  // Testing IO programs is problematic
  // Operations may be side effectful, even destructive (remove a file, change DB)
  // Concurrency and parallelism are problematic

  // For the side effects, we can use Tagless Final
  // While for the concurrency and parallelism, we can use TestContext instead of ExecutionContext
  // It allows full control over the execution, and allows us to mimic time passing

  // Working on Cats Effect 2.2.x
  // import cats.effect.laws.TestContext

  // val testContext = TestContext()

  // val io = timer.sleep(10.seconds) *> IO(1 + 1)
  // val f = io.unsafeToFuture()
  // assert(f.value == None)

  // Not yet completed, because this does not simulate time passing:
  // testContext.tick()
  // assert(f.value == None)

  // testContext.tick(10.seconds)
  // assert(f.value == Some(Success(2)))
end TestingIO
