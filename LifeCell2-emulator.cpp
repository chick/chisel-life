#include "LifeCell2.h"

int main (int argc, char* argv[]) {
  LifeCell2_t module;
  LifeCell2_api_t api(&module);
  module.init();
  api.init_sim_data();
  FILE *f = NULL;
  module.set_dumpfile(f);
  while(!api.exit()) api.tick();
  if (f) fclose(f);
}
