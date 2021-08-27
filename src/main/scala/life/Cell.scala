// SPDX-License-Identifier: Apache-2.0

package life

import chisel3._
import chisel3.util.MuxCase

/** Implements a single Life cell.
 *  want to turn this off? turn off the clock input
 *
 */
class Cell extends Module {
  val neighborStates = IO(Input(UInt(8.W)))
  val setAlive = IO(Input(Bool()))
  val setDead = IO(Input(Bool()))
  val isAlive = IO(Output(Bool()))

  val state = RegInit(false.B)

  val neighborSum = util.PopCount(neighborStates.asBools)

  state := MuxCase(
    state,
    Seq(
      setAlive -> true.B,
      setDead -> false.B,
      state -> (neighborSum === 2.U || neighborSum === 3.U),
      !state -> (neighborSum === 3.U)
    )
  )

  isAlive := state
}

object Cell {
  val Alive = true.B
  val Dead = false.B
}
