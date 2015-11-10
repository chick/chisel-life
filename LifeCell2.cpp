#include "LifeCell2.h"

void LifeCell2_t::init ( val_t rand_init ) {
  this->__srand(rand_init);
  LifeCell2__is_alive.randomize(&__rand_seed);
  clk.len = 1;
  clk.cnt = clk.len;
  clk.values[0] = 0;
}


int LifeCell2_t::clock ( dat_t<1> reset ) {
  uint32_t min = ((uint32_t)1<<31)-1;
  if (clk.cnt < min) min = clk.cnt;
  clk.cnt-=min;
  if (clk.cnt == 0) clock_lo( reset );
  if (clk.cnt == 0) clock_hi( reset );
  if (clk.cnt == 0) clk.cnt = clk.len;
  return min;
}


void LifeCell2_t::print ( FILE* f ) {
}
void LifeCell2_t::print ( std::ostream& s ) {
}


void LifeCell2_t::dump_init ( FILE* f ) {
}


void LifeCell2_t::dump ( FILE* f, int t ) {
}




void LifeCell2_t::clock_lo ( dat_t<1> reset ) {
  val_t T0;
  { T0 = LifeCell2__io_top_left.values[0]+LifeCell2__io_top_center.values[0];}
  T0 = T0 & 0xfL;
  val_t T1;
  { T1 = T0+LifeCell2__io_top_right.values[0];}
  T1 = T1 & 0xfL;
  val_t T2;
  { T2 = T1+LifeCell2__io_mid_left.values[0];}
  T2 = T2 & 0xfL;
  val_t T3;
  { T3 = T2+LifeCell2__io_mid_right.values[0];}
  T3 = T3 & 0xfL;
  val_t T4;
  { T4 = T3+LifeCell2__io_bot_left.values[0];}
  T4 = T4 & 0xfL;
  val_t T5;
  { T5 = T4+LifeCell2__io_bot_center.values[0];}
  T5 = T5 & 0xfL;
  val_t LifeCell2__neighbor_sum;
  { LifeCell2__neighbor_sum = T5+LifeCell2__io_bot_right.values[0];}
  LifeCell2__neighbor_sum = LifeCell2__neighbor_sum & 0xfL;
  val_t T6;
  T6 = LifeCell2__neighbor_sum == 0x3L;
  val_t T7;
  T7 = LifeCell2__neighbor_sum == 0x2L;
  val_t T8;
  { T8 = T7 | T6;}
  val_t T9;
  { T9 = TERNARY_1(LifeCell2__is_alive.values[0], T8, LifeCell2__is_alive.values[0]);}
  val_t T10;
  T10 = LifeCell2__neighbor_sum == 0x3L;
  val_t T11;
  { T11 = LifeCell2__is_alive.values[0] ^ 0x1L;}
  val_t T12;
  { T12 = TERNARY_1(T11, T10, T9);}
  { T13.values[0] = TERNARY(reset.values[0], 0x0L, T12);}
  { LifeCell2__io_is_alive.values[0] = LifeCell2__is_alive.values[0];}
}


void LifeCell2_t::clock_hi ( dat_t<1> reset ) {
  dat_t<1> LifeCell2__is_alive__shadow = T13;
  LifeCell2__is_alive = T13;
}


void LifeCell2_api_t::init_sim_data (  ) {
  sim_data.inputs.clear();
  sim_data.outputs.clear();
  sim_data.signals.clear();
  LifeCell2_t* mod = dynamic_cast<LifeCell2_t*>(module);
  assert(mod);
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell2__io_top_left));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell2__io_top_center));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell2__io_top_right));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell2__io_mid_left));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell2__io_mid_center));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell2__io_mid_right));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell2__io_bot_left));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell2__io_bot_center));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell2__io_bot_right));
  sim_data.outputs.push_back(new dat_api<1>(&mod->LifeCell2__io_is_alive));
  sim_data.signals.push_back(new dat_api<1>(&mod->LifeCell2__is_alive));
  sim_data.signal_map["LifeCell2.is_alive"] = 0;
  sim_data.clk_map["clk"] = new clk_api(&mod->clk);
}
