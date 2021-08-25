// SPDX-License-Identifier: Apache-2.0

package life

import chisel3._
import chiseltest._

import scala.util.Random
import LifeCell.{Alive, Dead}


  class ExampleTests(c: AddressableLifeGrid) {
    val rnd = new Random()

    def clear(): Unit = {
      for {
        rowIndex <- c.grid.indices
        colIndex <- c.grid(0).indices
      } {
        c.grid(rowIndex)(colIndex).isAlive.poke(Dead)
      }
    }

    def pokeAlive(row: Int, col: Int, state: Bool): Unit = {
      c.grid(row)(col).isAlive.poke(state)
    }

    def expectAlive(row: Int, col: Int, state: Bool): Unit = {
      c.grid(row)(col).isAlive.expect(state)
    }

    def run(): Unit = {
      c.running.poke(Alive)
    }

    def pause(): Unit = {
      c.running(false.B)
    }

    def read(row: Int, col: Int): Unit = {
      c.io.rowAddress.poke(row.U)
      c.io.colAddress.poke(col.U)
    }

    def testGridReading(): Unit = {
      clear()
      pause()

      for {
        i <- 0 until c.rows
        j <- 0 until c.cols
      } {
        pokeAlive(i, j, if ((i + j) % 5 == 0) Dead else Alive)
        expectAlive(i, j,  if ((i + j) % 5 == 0) Dead else Alive)
      }

      val i = 0
      val j = 0
      expectAlive(i, j, if ((i + j) % 5 == 0) Dead else Alive)
      c.clock.step()
      expectAlive(i, j, if ((i + j) % 5 == 0) Dead else Alive)

      c.running.expect(false.B)
      for {
        i <- 0 until c.rows
        j <- 0 until c.cols
      } {
        expectAlive(i, j, if ((i + j) % 5 == 0) Dead else Alive)
        c.grid(i)(j).io.isAlive.expect( if ((i + j) % 5 == 0) Dead else Alive)
      }

      for {
        i <- 0 until c.rows
        j <- 0 until c.cols
      } {
        //TODO: fix c.io.aliveValue.expect(i, j, if ((i + j) % 5 == 0) Dead else Alive)
      }
    }

    def testGridWriting(): Unit = {
      clear()
      pause()

      def writeCell(row: Int, col: Int, setAlive: Boolean): Unit = {
        c.io.rowAddress.poke( row.U)
        c.io.colAddress.poke( col.U)
        c.io.setAlive.poke( if (setAlive) Alive else Dead)
        c.io.setDead.poke( if (!setAlive) Alive else Dead)
        c.io.writeEnable.poke( true.B)
        c.clock.step(1)
        c.io.writeEnable.poke( false.B)
      }

      def testReadWrite(): Unit = {
        for {
          i <- 0 until c.rows
          j <- 0 until c.cols
        } {
          writeCell(i, j, setAlive = true)
          c.grid(i)(j).io.isAlive.expect(Alive)
          writeCell(i, j, setAlive = false)
          c.grid(i)(j).io.isAlive.expect(Dead)
          //        show()
        }
      }

      testReadWrite()
    }

    def testBlinker() {
      clear()
      run()

      c.grid(2)(2).isAlive.poke(Alive)

      c.clock.step()

      c.grid(2)(2).isAlive.expect(Dead)

      c.grid(2)(1).isAlive.poke(Alive)
      c.grid(2)(2).isAlive.poke(Alive)
      c.grid(2)(3).isAlive.poke(Alive)

      show()

      c.grid(2)(2).isAlive.expect(Alive)
      c.clock.step()
      show()
      c.grid(2)(2).isAlive.expect(Alive)

      expectAlive(2, 1, Dead)
      expectAlive(2, 2, Alive)
      expectAlive(2, 3, Dead)

      // stop machine running, despite step, things should stay the same
      c.running.poke(false.B)
      c.clock.step()
      expectAlive(2, 1, Dead)
      expectAlive(2, 2, Alive)
      expectAlive(2, 3, Dead)
      show()

      // start machine back up
      c.running.poke(true.B)
      c.clock.step()
      expectAlive(2, 1, Alive)
      expectAlive(2, 2, Alive)
      expectAlive(2, 3, Alive)
      show()

      //    for (g <- 0 until 10) {
      //      step(1)
      //      show()
      //    }
    }

    def testLine() {
      clear()

      for (row <- c.grid.indices) {
        pokeAlive(row, 2, Alive)
      }

      for (_ <- 0 until 100) {
        c.clock.step()
        show()
      }
    }

    def testRandom() {
      clear()

      for {
        row <- c.grid.indices
        col <- c.grid(row).indices
      } {
        pokeAlive(row, col, (rnd.nextInt(2) == 1).B)
      }

      for (g <- 0 until 100) {
        c.clock.step()
        show()
      }
    }

    def show(): Unit = {
//      System.out.println("+" + ("-" * c.grid.head.length) + "+")
//      for {
//        row <- c.grid
//      } {
//        System.out.println(
//          "|" + row.map { cell => if (cell.isAlive) == Alive) "*" else " " }.mkString("") + "|"
//        )
//      }
//      System.out.println("+" + ("-" * c.grid.head.length) + "+")
    }

    testGridWriting()
    testGridReading()
    testBlinker()
    testLine()
    testRandom()

}
