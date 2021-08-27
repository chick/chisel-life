// SPDX-License-Identifier: Apache-2.0

package life

import chisel3._
import chiseltest._
import chiseltest.ChiselScalatestTester
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import Cell.{Dead, Alive}

class CellTests extends AnyFreeSpec with ChiselScalatestTester with Matchers {
  def neighbors(neighborString: String): UInt = {
    BigInt(neighborString, 2).U
  }

  "cell should follow correct transition rules" in {
    test(new Cell) { c =>
      c.isAlive.expect(Dead)

      c.setAlive.poke(true.B)
      c.clock.step()
      c.setAlive.poke(false.B)
      c.isAlive.expect(Alive)

      c.clock.step()
      c.isAlive.expect(Dead)

      c.neighborStates.poke(neighbors("00000111"))
      c.clock.step()
      c.isAlive.expect(Alive)

      c.neighborStates.poke(neighbors("00000111"))
      c.clock.step()
      c.isAlive.expect(Alive)

      c.neighborStates.poke(neighbors("00001111"))
      c.clock.step()
      c.isAlive.expect(Dead)
    }
  }
}
