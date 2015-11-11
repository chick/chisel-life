package life

import Chisel._

/**
 * Created by chick on 11/9/15.
 * Implements a single cell of a conway life space
 */
class LifeCell extends Module {
  val io = new Bundle {
    val top_left    = UInt(INPUT, width=4)
    val top_center  = UInt(INPUT, width=4)
    val top_right   = UInt(INPUT, width=4)
    val mid_left    = UInt(INPUT, width=4)
    val mid_right   = UInt(INPUT, width=4)
    val bot_left    = UInt(INPUT, width=4)
    val bot_center  = UInt(INPUT, width=4)
    val bot_right   = UInt(INPUT, width=4)

    val is_alive    = Bool(OUTPUT)
  }

  def set_neighbor(neighbor: LifeCell, delta_row:Int, delta_col: Int) {
    if(delta_row < 0 && delta_col < 0)   io.bot_left   := neighbor.io.is_alive
    if(delta_row < 0 && delta_col == 0)  io.bot_center := neighbor.io.is_alive
    if(delta_row < 0 && delta_col > 0 )  io.bot_right  := neighbor.io.is_alive
    if(delta_row == 0 && delta_col < 0)  io.mid_left   := neighbor.io.is_alive
    if(delta_row == 0 && delta_col == 0) throwException("bad connection")
    if(delta_row == 0 && delta_col > 0)  io.mid_right  := neighbor.io.is_alive
    if(delta_row > 0 && delta_col < 0)   io.top_left   := neighbor.io.is_alive
    if(delta_row > 0 && delta_col == 0)  io.top_center := neighbor.io.is_alive
    if(delta_row > 0 && delta_col > 0)   io.top_right  := neighbor.io.is_alive
  }

  val is_alive = Reg(init=Bool(false))
  val neighbor_sum = Reg(init=UInt(0, 4))

//  neighbor_sum :=
//    io.top_left + io.top_center + io.top_right +
//      io.mid_left + io.mid_right +
//      io.bot_left + io.bot_center + io.bot_right
//
//
  val sum0 = io.top_left + io.top_center
  val sum1 = io.top_right + io.mid_left
  val sum2 = io.mid_right + io.bot_left
  val sum3 = io.bot_center + io.bot_right

  val sum4 = sum0 + sum1
  val sum5 = sum2 + sum3

  neighbor_sum := sum4 + sum5

  when(is_alive) {
    is_alive := neighbor_sum === UInt(2) || neighbor_sum === UInt(3)
  } otherwise {
    is_alive := neighbor_sum === UInt(3)
  }

  io.is_alive := is_alive
}

class LifeCellTests(c: LifeCell) extends Tester(c) { self =>
  poke(c.io.top_left, 0)
  poke(c.io.top_center, 0)
  poke(c.io.top_right, 0)
  poke(c.io.mid_left, 0)
  poke(c.io.mid_right, 0)
  poke(c.io.bot_left, 0)
  poke(c.io.bot_center, 0)
  poke(c.io.bot_right, 0)
  peek(c.neighbor_sum)

  step(2)

  peek(c.io.is_alive)
  peek(c.io.top_left)
  expect(c.io.is_alive, BigInt(0))

  poke(c.io.top_left, 1)
  poke(c.io.top_center, 1)
  poke(c.io.top_right, 1)
  poke(c.io.mid_left, 0)
  poke(c.io.mid_right, 0)
  poke(c.io.bot_left, 0)
  poke(c.io.bot_center, 0)
  poke(c.io.bot_right, 0)

  step(2)

  peek(c.io.is_alive)
  peek(c.io.top_left)
  peek(c.neighbor_sum)

  expect(c.io.is_alive, BigInt(1))
}

object LifeCell {
  def main(args: Array[String]): Unit = {
    chiselMainTest(
//      Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
      Array[String]("--backend", "dot"),
      () => Module(new LifeCell())
    ) {
      c => new LifeCellTests(c)
    }
  }
}
