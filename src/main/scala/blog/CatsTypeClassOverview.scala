package blog

object CatsTypeClassOverview {

  trait Semigroup[A] {
    def combine(x: A, y: A): A
  }

  trait Monoid[A] extends Semigroup[A] {
    def empty: A
  }

  /*
    Semigroup -> Monoid

    Semigroupal -> Apply -> FlatMap -->
                /       \              \
    Functor --->         Applicative -> Monad ----------> MonadError
                                     \                 /
                                      ApplicativeError
  */

  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }

  def do10xList(list: List[Int]): List[Int] = list.map(_ * 10)
  def do10xOption(option: Option[Int]): Option[Int] = option.map(_ * 10)

  def do10xGeneral[F[_]](container: F[Int])(using functor: Functor[F]): F[Int] =
    functor.map(container)(_ * 10)

  // raise a value of A to an F of A
  trait Applicative[F[_]] extends Apply[F] {
    def pure[A](a: A): F[A]

    def map[A, B](fa: F[A])(f: A => B): F[B] =
      ap(pure(f), fa)
  }

  trait FlatMap[F[_]] {
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  }

  trait Monad[F[_]] extends Applicative[F] with FlatMap[F]{
    override def map[A, B](fa: F[A])(f: A => B) =
      flatMap(fa)(a => pure(f(a)))
  }

  // cartesian product
  trait Semigroupal[F[_]] {
    def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
  }

  trait Apply[F[_]] extends Semigroupal[F] with Functor[F]{
    def ap[A, B](fab: F[A => B], fa: F[A]): F[B]

    def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] = {
      val myFunction: A => B => (A, B) = (a: A) => (b: B) => (a, b)
      val fab: F[B => (A, B)] = map(fa)(myFunction)
      ap(fab, fb)
    }

    def mapN[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] = {
      map(product(fa, fb)) {
        case (a, b) => f(a, b)
      }
    }
  }

  trait ApplicativeError[F[_], E] extends Applicative[F] {
    def raiseError[A](error: E): F[A]
  }

  trait MonadError[F[_], E] extends ApplicativeError[F, E] with Monad[F]
}
