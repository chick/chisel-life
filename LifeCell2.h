#ifndef __LifeCell2__
#define __LifeCell2__

#include "emulator.h"

class LifeCell2_t : public mod_t {
 private:
  val_t __rand_seed;
  void __srand(val_t seed) { __rand_seed = seed; }
  val_t __rand_val() { return ::__rand_val(&__rand_seed); }
 public:
  dat_t<1> reset;
  dat_t<1> T13;
  dat_t<1> LifeCell2__is_alive;
  dat_t<1> LifeCell2__io_is_alive;
  dat_t<4> LifeCell2__io_bot_right;
  dat_t<4> LifeCell2__io_bot_center;
  dat_t<4> LifeCell2__io_bot_left;
  dat_t<4> LifeCell2__io_mid_right;
  dat_t<4> LifeCell2__io_mid_left;
  dat_t<4> LifeCell2__io_top_right;
  dat_t<4> LifeCell2__io_top_center;
  dat_t<4> LifeCell2__io_top_left;
  dat_t<4> LifeCell2__io_mid_center;
  clk_t clk;

  void init ( val_t rand_init = 0 );
  void clock_lo ( dat_t<1> reset );
  void clock_hi ( dat_t<1> reset );
  int clock ( dat_t<1> reset );
  void print ( FILE* f );
  void print ( std::ostream& s );
  void dump ( FILE* f, int t );
  void dump_init ( FILE* f );

};

#include "emul_api.h"
class LifeCell2_api_t : public emul_api_t {
 public:
  LifeCell2_api_t(mod_t* m) : emul_api_t(m) { }
  void init_sim_data();
};

#endif
