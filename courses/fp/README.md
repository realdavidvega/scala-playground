# Functional Programming Fundamentals with Scala and Cats

Course: https://functional.xebia.academy/courses/take/functional-programming-fundamentals-with-scala-and-cats

## Summary

### Algebraic Data Types (ADTs)

Product and sums
- Put together and give choice
- Sealed hierarchies of case classes

Represent your domain
- Core of your architecture

### Effects

Side effects should be contained

- IO separates description from execution
- Cats Effect provides a rich hierarchy of effects

Failure and validation as effects

### Type classes

FP-way to abstract over behavior
- Eq, Semigroup

Good match with type constructors
- Functor, Applicative, Monad, Traverse

### Type classes / Algebras

Final Tagless

- Separation of concerns
- Better integration and testability
