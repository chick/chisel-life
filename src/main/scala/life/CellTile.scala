// SPDX-License-Identifier: Apache-2.0

package life

import chisel3._
import chisel3.util.MuxCase

/** Implements a tile Life cell.
 * state of tile is a single UInt traversing the tile
 * left to right, top to bottom
 */
case class CellTile(rows: Int, cols: Int) extends Module {
  val borderSize = 2 * rows + 2 * cols + 4
  val neighborStates = IO(Input(UInt(borderSize.W)))

  val state = IO(Output(UInt((rows * cols).W)))

  val tile = Seq.fill(rows, cols) { new Cell }

  // build state output
  state := VecInit(tile.flatMap { row =>
    row.map { col =>
      col.isAlive
    }
  }).asUInt

  // wire the cells together

}

trait CellTileConnectHelpers {
  self: CellTile =>

  def cellNeighborStates(row: Int, col: Int): UInt = {
    if (row == 0) {
      if (col == 0) {
        // top left corner
        neighborStates(2, 0)
      } else if (col == cols - 1) {
        // to right corner
        neighborStates(2, 0)
      } else {
        neighborStates(row + 1, row -1)
      }
    } else {
      if (col == 0) {
        // top left corner
        neighborStates(2, 0)
      } else if (col == cols - 1) {
        // to right corner
        neighborStates(2, 0)
      } else {
        neighborStates(row + 1, row -1)
      }
    }
  }
}
