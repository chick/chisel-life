// See README.md for license details.

package life

import chisel3._
import chisel3.iotesters.PeekPokeTester
import chisel3.util.MuxCase

/**
  * Created by chick on 11/9/15.
  * Implements a single cell of a conway life space
  */
class LifeCell extends Module {
  val io = IO(new Bundle {
    val running     = Input(Bool())
    val top_left    = Input(UInt(4.W))
    val top_center  = Input(UInt(4.W))
    val top_right   = Input(UInt(4.W))
    val mid_left    = Input(UInt(4.W))
    val mid_right   = Input(UInt(4.W))
    val bot_left    = Input(UInt(4.W))
    val bot_center  = Input(UInt(4.W))
    val bot_right   = Input(UInt(4.W))

    val set_alive   = Input(Bool())
    val set_dead    = Input(Bool())

    val is_alive    = Output(Bool())
  })

  def set_neighbor(neighbor: LifeCell, delta_row:Int, delta_col: Int) {
    if(delta_row < 0 && delta_col < 0)   io.bot_left   := neighbor.io.is_alive
    if(delta_row < 0 && delta_col == 0)  io.bot_center := neighbor.io.is_alive
    if(delta_row < 0 && delta_col > 0 )  io.bot_right  := neighbor.io.is_alive
    if(delta_row == 0 && delta_col < 0)  io.mid_left   := neighbor.io.is_alive
    if(delta_row == 0 && delta_col == 0) throw new Exception("bad connection")
    if(delta_row == 0 && delta_col > 0)  io.mid_right  := neighbor.io.is_alive
    if(delta_row > 0 && delta_col < 0)   io.top_left   := neighbor.io.is_alive
    if(delta_row > 0 && delta_col == 0)  io.top_center := neighbor.io.is_alive
    if(delta_row > 0 && delta_col > 0)   io.top_right  := neighbor.io.is_alive
  }

  val is_alive = RegInit(false.B)

  val sum0 = io.top_left + io.top_center
  val sum1 = io.top_right + io.mid_left
  val sum2 = io.mid_right + io.bot_left
  val sum3 = io.bot_center + io.bot_right

  val sum4 = sum0 + sum1
  val sum5 = sum2 + sum3

  val neighbor_sum = sum4 + sum5

  is_alive := MuxCase(is_alive,
    Seq(
      io.set_alive              -> true.B,
      io.set_dead               -> false.B,
      (io.running && is_alive)  -> (neighbor_sum === 2.U || neighbor_sum === 3.U),
      (io.running && !is_alive) -> (neighbor_sum === 3.U)
    )
  )
  //  when(io.set_alive) {
  //    is_alive := Bool(true)
  //  }.elsewhen(io.set_dead) {
  //    is_alive := Bool(false)
  //  }
  //
  //  when(io.running) {
  //    when(is_alive) {
  //      is_alive := neighbor_sum === UInt(2) || neighbor_sum === UInt(3)
  //    } otherwise {
  //      is_alive := neighbor_sum === UInt(3)
  //    }
  //  }

  io.is_alive := is_alive
}

class LifeCellTests(c: LifeCell) extends PeekPokeTester(c) { self =>
  def set_neighbors(
    ntl: Int, ntc: Int, ntr: Int,
    nml: Int, nmc: Int, nmr: Int,
    nbl: Int, nbc: Int, nbr: Int): Unit = {
    poke(c.io.top_left, ntl)
    poke(c.io.top_center, ntc)
    poke(c.io.top_right, ntr)
    poke(c.io.mid_left, nml)
    // center "neighbor" is the value of the cell itself
    // poke(c.is_alive, nmc)
    poke(c.io.mid_right, nmr)
    poke(c.io.bot_left, nbl)
    poke(c.io.bot_center, nbc)
    poke(c.io.bot_right, nbr)
  }

  poke(c.io.running, 1)

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

  // test set_alive
  set_neighbors(
    0,0,0,
    0,0,0,
    0,0,0
  )
  poke(c.io.set_alive, 1)
  poke(c.io.set_dead, 0)
  poke(c.io.running, 1)
  step(1)
  expect(c.io.is_alive, 1)

  poke(c.io.set_alive, 1)
  poke(c.io.set_dead, 0)
  poke(c.io.running, 0)
  step(1)
  expect(c.io.is_alive, 1)

  poke(c.io.set_dead, 1)
  poke(c.io.set_alive, 0)
  poke(c.io.running, 1)
  step(1)
  expect(c.io.is_alive, 0)

  poke(c.io.set_dead, 1)
  poke(c.io.set_alive, 0)
  poke(c.io.running, 0)
  step(1)
  expect(c.io.is_alive, 0)

}

object LifeCell {
  def main(args: Array[String]): Unit = {
    iotesters.Driver.execute(
      Array[String]("--backend-name", "firrtl"),
      //      Array[String]("--backend", "dot"),
      () => new LifeCell()
    ) {
      c => new LifeCellTests(c)
    }
  }
}
