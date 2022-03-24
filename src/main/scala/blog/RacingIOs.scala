package blog

import cats.effect.{IO, IOApp}
import scala.concurrent.duration._

// IO = any computation that might have a side effect.

object RacingIOs extends IOApp.Simple {

  val meaningOfLife: IO[Int] = IO(42)

  extension [A] (io: IO[A])
    def debug: IO[A] = io.map { value =>
      println(s"[${Thread.currentThread().getName}] $value")
      value
    }

  val valuableIO: IO[Int] = IO("task: starting").debug *> IO.sleep(1.second) *> IO("task: completed") *> IO(42)
  val vIO: IO[Int] = valuableIO.onCancel(IO("task: cancelled").debug.void)
  val timeout: IO[Unit] = IO("timeout: starting").debug *> IO.sleep(500.millis) *> IO("timeout: ding ding").debug.void

  // racing

  def testRace(): IO[String] = {
    val firstIO: IO[Either[Int, Unit]] = IO.race(vIO, timeout) // IO.race => IO[Either[A, B]], where A = return type of first IO and B = return type of second IO

    firstIO.flatMap {
      case Left(value) => IO(s"task won: $value")
      case Right(value) => IO(s"timeout won")
    }
   }

  val testTimeout: IO[Int] = vIO.timeout(500.millis)

  def demoRacePair[A](iox: IO[A], ioy: IO[A]) = {
    val pair = IO.racePair(iox, ioy) //  IO[Either[(effect.OutcomeIO[A], effect.FiberIO[B]), (effect.FiberIO[A], effect.OutcomeIO[B])]] - If first IO wins the result will be the left side else the right side

    pair.flatMap {
      case Left((outcomeA, fiberB)) => fiberB.cancel *> IO("first task won").debug *> IO(outcomeA).debug
      case Right((fiberA, outcomeB)) => fiberA.cancel *> IO("second task won").debug *> IO(outcomeB).debug
    }
  }

  var iox = IO.sleep(1.second).as(1).onCancel(IO("first cancelled").debug.void)
  var ioy = IO.sleep(2.second).as(2).onCancel(IO("second cancelled").debug.void)

  override def run: IO[Unit] =
    //meaningOfLife.debug.void
    //testRace().debug.void
    //testTimeout.debug.void
    demoRacePair(iox, ioy).void
}
