package life

import Chisel._

/**
 * Created by chick on 11/9/15.
 * Implements a single cell of a conway life space
 */
class LifeCell2 extends Module {
  val io = new Bundle {
    val top_left    = UInt(INPUT, width=4)
    val top_center  = UInt(INPUT, width=4)
    val top_right   = UInt(INPUT, width=4)
    val mid_left    = UInt(INPUT, width=4)
    val mid_center  = UInt(INPUT, width=4)
    val mid_right   = UInt(INPUT, width=4)
    val bot_left    = UInt(INPUT, width=4)
    val bot_center  = UInt(INPUT, width=4)
    val bot_right   = UInt(INPUT, width=4)

    val is_alive    = Bool(OUTPUT)
  }
  val is_alive = Reg(init=Bool(false))
  //  val will_be_alive = Reg(init=Bool(false))

//  val neighbor_sum = Reg(init=UInt(0, 4))

  // this causes infinite loop
  val neighbor_sum =
    io.top_left + io.top_center + io.top_right +
      io.mid_left + io.mid_right +
      io.bot_left + io.bot_center + io.bot_right

  when(is_alive) {
    is_alive := neighbor_sum === UInt(2) || neighbor_sum === UInt(3)
  } otherwise {
    is_alive := neighbor_sum === UInt(3)
  }

  //  is_alive := is_alive
  io.is_alive := is_alive

  //  printf(s"Life Cell! top_left ${io.top_left} neighbor_sum $neighbor_sum alive? $will_be_alive\n")
}

class LifeCell2Tests(c: LifeCell2) extends Tester(c) { self =>
  poke(c.io.top_left, 0)
  poke(c.io.top_center, 0)
  poke(c.io.top_right, 0)
  poke(c.io.mid_left, 0)
  poke(c.io.mid_center, 0)
  poke(c.io.mid_right, 0)
  poke(c.io.bot_left, 0)
  poke(c.io.bot_center, 0)
  poke(c.io.bot_right, 0)
  peek(c.neighbor_sum)

  step(1)

  peek(c.io.is_alive)
  peek(c.io.top_left)
  expect(c.io.is_alive, BigInt(0))

  step(1)

  peek(c.io.is_alive)
  peek(c.io.top_left)
  expect(c.io.is_alive, BigInt(0))

  poke(c.io.top_left, 1)
  poke(c.io.top_center, 1)
  poke(c.io.top_right, 1)
  poke(c.io.mid_left, 0)
  poke(c.io.mid_center, 0)
  poke(c.io.mid_right, 0)
  poke(c.io.bot_left, 0)
  poke(c.io.bot_center, 0)
  poke(c.io.bot_right, 0)

  step(1)

  peek(c.io.is_alive)
  //  peek(c.will_be_alive)
  peek(c.io.top_left)
  peek(c.neighbor_sum)

  step(1)

  peek(c.io.is_alive)
  //  peek(c.will_be_alive)
  peek(c.io.top_left)
  peek(c.neighbor_sum)

  expect(c.io.is_alive, BigInt(1))
}

object LifeCell2 {
  def main(args: Array[String]): Unit = {
    chiselMainTest(
            Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
//      Array[String]("--backend", "dot"),
      () => Module(new LifeCell2())
    ) {
      c => new LifeCell2Tests(c)
    }
  }
}
