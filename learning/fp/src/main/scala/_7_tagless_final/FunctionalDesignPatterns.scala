package _7_tagless_final

import cats.*
import cats.data.{Validated, ValidatedNec}
import cats.effect.*
import cats.syntax.all.*

import java.nio.file.{Files as JFiles, *}
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

object FunctionalDesignPatterns:
  @main
  def main2(): Unit =
    // Motivation: a design pattern is a general reusable solution to a common problem within a given context
    // In general, we would like to have patterns that are reusable, context-agnostic and generic
    // And then, we can have context-specific implementations

    // Traditionally, in OOP we have less reusability: singleton, factory, bridge... repeated over and over
    // Singleton
    object Singleton:
      private var instance: Option[Singleton] = None

      // Method to get the singleton instance
      def getInstance: Singleton =
        if instance == null then instance = new Singleton
        instance

    // In functional programming, we have more reusability: monads, applicatives, interpreters, ...
    // Also we have general Algebras, and then specific implementations in form of interpreters
    // Functor, Monad, Sync, Applicative, etc.
    trait MyMonad[F[_]]:
      def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

    object MyNiceMonad extends MyMonad[IO]:
      def flatMap[A, B](fa: IO[A])(f: A => IO[B]): IO[B] = fa.flatMap(f)

    // FP-oriented architecture
    // Leverage on hexagonal architecture and Domain Driven Design (DDD)

    // Hexagonal Architecture: leverage on inversion of control
    // Core: contains business logic
    // Ports: well-defined abstractions that the core uses to interact with the outside world
    // E.g. Retrieve orders
    // Adapters: implement actual communication
    // E.g. A way of retrieving orders from a database. Also test implementation with in-memory database

    // Hexagonal Architecture in FP
    // Core: pure functions
    // Ports: algebras / type classes
    // Adapters: interpreters / instances

    // Domain Driven Design (DDD): breaking large domains into smaller ones
    // Techniques to handle the core
    // A domain is a sphere of knowledge defined by a domain expert
    // Trying to model a large domain can be very difficult
    // DDD helps to break down a large domain into smaller ones

    // DDD subdomain: groups related ideas,actions and rules
    // Subdomains must be clearly bounded: terms specific to the subdomain
    // E.g. orders, products, users, etc.

    // DDD bounded context: space where a term has always the same meaning
    // Communicating between bounded contexts: implies a translation layer, and one must not leak the other

    // Domain Driven Design In FP
    // Use algebraic approach: model terms/languages with ADTs, behaviors with pure functions
    // Group those into modules or domain services: each module exposes an algebra
    // Separate the core from implementation details: provided by interpreters

    // Example login service
    // Domain Language: logins, sessions, credentials with ADTs
    type Auth        = String
    type Credentials = (String, String)

    case class Session(v: String)

    sealed trait LoginResult
    object LoginResult:
      case class Success(s: Session)   extends LoginResult
      case class Failure(t: Throwable) extends RuntimeException(t.getMessage) with LoginResult

    // Ports (things we require from outside)
    // Session and Auth services
    // Modelled as type classes which are required
    trait SessionCache[F[_]]:
      // finds some credentials
      def find(credentials: Credentials): F[Option[Session]]

    trait AuthService[F[_]]:
      // auths with some credentials
      def auth(credentials: Credentials): F[Auth]

    // Pure implementation
    // We bring the services in as parameters
    // We also need MonadCancel for sequencing and errors
    class LoginService[F[_]](cache: SessionCache[F], auth: AuthService[F])(using
        F: MonadCancel[F, Throwable]
    ):
      def login(credentials: Credentials): F[LoginResult] =
        cache
          .find(credentials)
          .flatMap {
            case Some(session) => session.pure[F]
            case None          => auth.auth(credentials).map(t => Session(t))
          }
          .map[LoginResult](LoginResult.Success)
          .handleError(LoginResult.Failure)

    // Injection of dependencies at the edges
    val cache: SessionCache[IO] = ???
    val auth: AuthService[IO]   = ???

    // service
    val loginService: LoginService[IO] = new LoginService[IO](cache, auth)

    // run
    val loginResult: LoginResult = loginService.login(("user", "pass"))

  end main2
end FunctionalDesignPatterns
