// SPDX-License-Identifier: Apache-2.0

package life

import chisel3._
import chiseltest._
import LifeCell.{Alive, Dead}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LifeCellTest extends AnyFreeSpec with ChiselScalatestTester with Matchers {
  "cell should work properly" in {
    test(new LifeCell) { c =>
      def setNeighbors(
        ntl: Int,
        ntc: Int,
        ntr: Int,
        nml: Int,
        nmc: Int,
        nmr: Int,
        nbl: Int,
        nbc: Int,
        nbr: Int
      ): Unit = {
        c.io.topLeft.poke(ntl.B)
        c.io.topCenter.poke(ntc.B)
        c.io.topRight.poke(ntr.B)
        c.io.midLeft.poke(nml.B)
        // center "neighbor" is the value of the cell itself
        // c.isAlive.poke(nmc.B)
        c.io.midRight.poke(nmr.B)
        c.io.botLeft.poke(nbl.B)
        c.io.botCenter.poke(nbc.B)
        c.io.botRight.poke(nbr.B)
      }

      c.io.running.poke(true.B)

      // dead cell with no neighbors stays dead
      setNeighbors(
        0, 0, 0, 0, 0, 0, 0, 0, 0
      )
      c.clock.step()
      c.io.isAlive.expect(Dead)

      // dead cell with > 3 neighbors stays dead
      setNeighbors(
        1, 1, 1, 1, 0, 1, 1, 1, 1
      )
      c.clock.step()
      c.io.isAlive.expect(Dead)

      // live cell with > 3 neighbors stays dead
      setNeighbors(
        1, 1, 1, 1, 1, 1, 1, 1, 1
      )
      c.clock.step()
      c.io.isAlive.expect(Dead)

      // dead cell with exactly three neighbors becomes alive
      setNeighbors(
        1, 0, 0, 1, 0, 0, 1, 0, 0
      )
      c.clock.step()
      c.io.isAlive.expect(Alive)
      setNeighbors(
        1, 0, 0, 0, 0, 1, 0, 1, 0
      )
      c.clock.step()
      c.io.isAlive.expect(Alive)

      // live cell with one neighbor dies
      setNeighbors(
        0, 0, 0, 0, 1, 1, 0, 0, 0
      )
      c.clock.step()
      c.io.isAlive.expect(Dead)

      // live cell with exactly three neighbors stays alive
      setNeighbors(
        1, 0, 0, 1, 1, 0, 1, 0, 0
      )
      c.clock.step()
      c.io.isAlive.expect(Alive)

      // live cell with exactly four neighbors dies
      setNeighbors(
        1, 0, 0, 1, 1, 1, 1, 0, 0
      )
      c.clock.step()
      c.io.isAlive.expect(Dead)

      // test setAlive
      setNeighbors(
        0, 0, 0, 0, 0, 0, 0, 0, 0
      )
      c.io.setAlive.poke(Alive)
      c.io.setDead.poke(Dead)
      c.io.running.poke(Alive)
      c.clock.step()
      c.io.isAlive.expect(Alive)

      c.io.setAlive.poke(Alive)
      c.io.setDead.poke(Dead)
      c.io.running.poke(Dead)
      c.clock.step()
      c.io.isAlive.expect(Alive)

      c.io.setDead.poke(Alive)
      c.io.setAlive.poke(Dead)
      c.io.running.poke(Alive)
      c.clock.step()
      c.io.isAlive.expect(Dead)

      c.io.setDead.poke(Alive)
      c.io.setAlive.poke(Dead)
      c.io.running.poke(Dead)
      c.clock.step()
      c.io.isAlive.expect(Dead)
    }
  }
}
