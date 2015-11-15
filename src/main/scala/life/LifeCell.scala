package life

import Chisel._

/**
 * Created by chick on 11/9/15.
 * Implements a single cell of a conway life space
 */
class LifeCell extends Module {
  val io = new Bundle {
    val running     = Bool(INPUT)
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

  val sum0 = io.top_left + io.top_center
  val sum1 = io.top_right + io.mid_left
  val sum2 = io.mid_right + io.bot_left
  val sum3 = io.bot_center + io.bot_right

  val sum4 = sum0 + sum1
  val sum5 = sum2 + sum3

  val neighbor_sum = sum4 + sum5

  when(is_alive) {
    is_alive := neighbor_sum === UInt(2) || neighbor_sum === UInt(3)
  } otherwise {
    is_alive := neighbor_sum === UInt(3)
  }

  io.is_alive := is_alive
}

class LifeCellTests(c: LifeCell) extends Tester(c) { self =>
  def set_neighbors(
                     ntl: Int, ntc: Int, ntr: Int,
                     nml: Int, nmc: Int, nmr: Int,
                     nbl: Int, nbc: Int, nbr: Int): Unit = {
    poke(c.io.top_left, ntl)
    poke(c.io.top_center, ntc)
    poke(c.io.top_right, ntr)
    poke(c.io.mid_left, nml)
    // center "neighbor" is the value of the cell itself
    poke(c.is_alive, nmc)
    poke(c.io.mid_right, nmr)
    poke(c.io.bot_left, nbl)
    poke(c.io.bot_center, nbc)
    poke(c.io.bot_right, nbr)
  }

  // dead cell with no neighbors stays dead
  set_neighbors(
    0,0,0,
    0,0,0,
    0,0,0
  )
  step(1)
  expect(c.io.is_alive, 0)

  // dead cell with > 3 neighbors stays dead
  set_neighbors(
    1,1,1,
    1,0,1,
    1,1,1
  )
  step(1)
  expect(c.io.is_alive, 0)

  // live cell with > 3 neighbors stays dead
  set_neighbors(
    1,1,1,
    1,1,1,
    1,1,1
  )
  step(1)
  expect(c.io.is_alive, 0)

  // dead cell with exactly three neighbors becomes alive
  set_neighbors(
    1,0,0,
    1,0,0,
    1,0,0
  )
  step(1)
  expect(c.io.is_alive, 1)
  set_neighbors(
    1,0,0,
    0,0,1,
    0,1,0
  )
  step(1)
  expect(c.io.is_alive, 1)

  // live cell with one neighbor dies
  set_neighbors(
    0,0,0,
    0,1,1,
    0,0,0
  )
  step(1)
  expect(c.io.is_alive, 0)

  // live cell with exactly three neighbors stays alive
  set_neighbors(
    1,0,0,
    1,1,0,
    1,0,0
  )
  step(1)
  expect(c.io.is_alive, 1)

  // live cell with exactly four neighbors dies
  set_neighbors(
    1,0,0,
    1,1,1,
    1,0,0
  )
  step(1)
  expect(c.io.is_alive, 0)
}

object LifeCell {
  def main(args: Array[String]): Unit = {
    chiselMainTest(
      Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
//      Array[String]("--backend", "dot"),
      () => Module(new LifeCell())
    ) {
      c => new LifeCellTests(c)
    }
  }
}
