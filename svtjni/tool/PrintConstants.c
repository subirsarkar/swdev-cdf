#include <stdio.h>
#include <svtvme_public.h>
int main() {
  printf("package jsvtvme; \n\n\n");
  printf("public interface SvtvmeConstants {\n");
  printf("  public static final int 
     VERBOSE = %d, ERROR_REPORT = %d;\n", VERBOSE, ERROR_REPORT);
  printf("  public static final int 
     LOCK = %d, PROTECT = %d, DEBUG = %d;\n", LOCK, PROTECT, DEBUG);
  printf("  public static final int 
     SLOWER = %d, FASTER = %d;\n", SLOWER, FASTER);
  printf("  public static final int 
     IGNORE_ERRORS = %d, STOP_AT_FIRST_ERROR = %d, STOP_AT_ITERATION_END = %d;\n", 
     IGNORE_ERRORS, STOP_AT_FIRST_ERROR, STOP_AT_ITERATION_END);
  printf("  public static final int 
     SVTB = %d, AMB = %d, AMS  = %d, HB = %d, MRG  = %d,
     HF   = %d, TF  = %d, XTFA = %d, SC = %d, XTFC = %d;\n",
     SVTB, AMB, AMS, HB,  MRG, HF, TF, XTFA, SC, XTFC); 
  printf("  public static final int 
     DUMMY_OBJECT = %d;\n",DUMMY_OBJECT);

  printf("     // GENERIC BOARD \n");
  printf("  public static final int 
     SVTB_IDPROM = %d, SVTB_64K_IDPROM = %d;\n", SVTB_IDPROM, SVTB_64K_IDPROM);

  printf("     // AMB REGISTERs\n");
  printf("  public static final int 
     AMB_REG0 = %d, AMB_BOARD_ID = %d, AMB_TMODE = %d, AMB_MODE = %d,
     AMB_P3_TO_VME = %d, AMB_VME_TO_P3 = %d, AMB_HALT_TO_PIPE = %d,
     AMB_NOP0 = %d, AMB_OUTPUT = %d, AMB_INPUT = %d, AMB_CLRHIT = %d,
     AMB_NOP6 = %d, AMB_SHIFT = %d, AMB_COUNT = %d, AMB_CLRCNT = %d,
     AMB_INIT = %d, AMB_DISABLE_L5 = %d, AMB_RESET_HANDSHAKE = %d,
     AMB_ENABLE_L5 = %d, AMB_NOP14 = %d, AMB_NOP15 = %d;\n", 
     AMB_REG0, AMB_BOARD_ID, AMB_TMODE, AMB_MODE,
     AMB_P3_TO_VME, AMB_VME_TO_P3, AMB_HALT_TO_PIPE,
     AMB_NOP0, AMB_OUTPUT, AMB_INPUT, AMB_CLRHIT,
     AMB_NOP6, AMB_SHIFT, AMB_COUNT, AMB_CLRCNT,
     AMB_INIT, AMB_DISABLE_L5, AMB_RESET_HANDSHAKE,
     AMB_ENABLE_L5, AMB_NOP14, AMB_NOP15);

  printf("     // AMB MEMORYs\n");
  printf("  public static final int 
     AMB_IDPROM = %d, AMB_RD_PATTERNS = %d, AMB_WR_PATTERNS = %d,
     AMB_RD_PATT_LAY0 = %d, AMB_RD_PATT_LAY1 = %d, AMB_RD_PATT_LAY2 = %d,
     AMB_RD_PATT_LAY3 = %d, AMB_RD_PATT_LAY4 = %d, AMB_RD_PATT_LAY5 = %d,
     AMB_WR_PATT_LAY0 = %d, AMB_WR_PATT_LAY1 = %d, AMB_WR_PATT_LAY2 = %d,
     AMB_WR_PATT_LAY3 = %d, AMB_WR_PATT_LAY4 = %d, AMB_WR_PATT_LAY5 = %d;\n",
     AMB_IDPROM, AMB_RD_PATTERNS, AMB_WR_PATTERNS,
     AMB_RD_PATT_LAY0, AMB_RD_PATT_LAY1, AMB_RD_PATT_LAY2,
     AMB_RD_PATT_LAY3, AMB_RD_PATT_LAY4, AMB_RD_PATT_LAY5,
     AMB_WR_PATT_LAY0, AMB_WR_PATT_LAY1, AMB_WR_PATT_LAY2,
     AMB_WR_PATT_LAY3, AMB_WR_PATT_LAY4, AMB_WR_PATT_LAY5);

  printf("     // AMS REGISTERs\n");
  printf("  public static final int 
     AMS_INIT = %d, AMS_TMODE = %d,  AMS_ERROR = %d, AMS_HIT_FIFO_REG = %d,
     AMS_HIT_FIFO_EMPTY = %d, AMS_OUT_HOLD = %d,  AMS_USEQ_STATE = %d, AMS_HF_FSM = %d, 
     AMS_OUT_REG = %d,  AMS_HSPY_PTR = %d, AMS_OSPY_PTR = %d,
     AMS_HSPY_FRZ = %d, AMS_OSPY_FRZ = %d,  AMS_HSPY_WRP = %d, AMS_OSPY_WRP = %d,
     AMS_ERROR_EN = %d, AMS_CDFERR_EN = %d, AMS_CDFERR = %d,
     AMS_ROAD_LIMIT = %d, AMS_WILD_LAYERS = %d, AMS_PHI_SECTOR = %d;\n",
     AMS_INIT, AMS_TMODE,  
     AMS_ERROR, AMS_HIT_FIFO_REG,  
     AMS_HIT_FIFO_EMPTY, AMS_OUT_HOLD, 
     AMS_USEQ_STATE, AMS_HF_FSM, AMS_OUT_REG, 
     AMS_HSPY_PTR, AMS_OSPY_PTR, 
     AMS_HSPY_FRZ, AMS_OSPY_FRZ, 
     AMS_HSPY_WRP, AMS_OSPY_WRP,  
     AMS_ERROR_EN, AMS_CDFERR_EN, AMS_CDFERR, 
     AMS_ROAD_LIMIT, AMS_WILD_LAYERS, AMS_PHI_SECTOR);

  printf("     // AMS MEMORYs\n");
  printf("  public static final int 
     AMS_IDPROM = %d, AMS_SS_MAP = %d, AMS_USEQ = %d;\n",
     AMS_IDPROM, AMS_SS_MAP, AMS_USEQ);

  printf("     // AMS SPYs\n");
  printf("  public static final int 
     AMS_HIT_SPY = %d, AMS_OUT_SPY = %d;\n", 
     AMS_HIT_SPY, AMS_OUT_SPY);

  printf("     // AMS FIFOs\n");
  printf("  public static final int 
     AMS_HIT_FIFO = %d;\n", AMS_HIT_FIFO);

  printf("     // HB REGISTERs\n");
  printf("  public static final int 
     HB_INIT = %d, HB_TMODE = %d,
     HB_ERROR = %d, HB_HIT_FIFO_REG = %d, HB_ROAD_FIFO_REG = %d,
     HB_HIT_FIFO_EMPTY = %d, HB_ROAD_FIFO_EMPTY = %d, HB_OUT_HOLD = %d,
     HB_FSM = %d, HB_HF_FSM = %d, HB_RF_FSM = %d, HB_OUT_REG = %d, HB_VME_FREEZE = %d,
     HB_HSPY_PTR = %d, HB_RSPY_PTR = %d, HB_OSPY_PTR = %d,
     HB_HSPY_FRZ = %d, HB_RSPY_FRZ = %d, HB_OSPY_FRZ = %d,
     HB_HSPY_WRP = %d, HB_RSPY_WRP = %d, HB_OSPY_WRP = %d,
     HB_ERROR_EN = %d, HB_CDFERR_EN = %d, HB_CDFERR = %d, HB_SWITCHES = %d;\n",
     HB_INIT, HB_TMODE,  
     HB_ERROR, HB_HIT_FIFO_REG, HB_ROAD_FIFO_REG,
     HB_HIT_FIFO_EMPTY, HB_ROAD_FIFO_EMPTY, HB_OUT_HOLD,
     HB_FSM, HB_HF_FSM, HB_RF_FSM, HB_OUT_REG, HB_VME_FREEZE,
     HB_HSPY_PTR, HB_RSPY_PTR, HB_OSPY_PTR,
     HB_HSPY_FRZ, HB_RSPY_FRZ, HB_OSPY_FRZ,
     HB_HSPY_WRP, HB_RSPY_WRP, HB_OSPY_WRP,
     HB_ERROR_EN, HB_CDFERR_EN, HB_CDFERR, HB_SWITCHES);

  printf("    // HB MEMORYs \n");
  printf("  public static final int 
     HB_IDPROM = %d, HB_SS_MAP = %d, HB_AM_MAP = %d;\n",
     HB_IDPROM, HB_SS_MAP, HB_AM_MAP);

  printf("    // HB SPYs \n");
  printf("  public static final int 
     HB_HIT_SPY = %d, HB_ROAD_SPY = %d, HB_OUT_SPY = %d;\n",
     HB_HIT_SPY, HB_ROAD_SPY, HB_OUT_SPY);
  printf("    // HB FIFOs \n");
  printf("  public static final int 
     HB_HIT_FIFO = %d, HB_ROAD_FIFO = %d;\n", 
     HB_HIT_FIFO, HB_ROAD_FIFO);

  printf("    // MERGER REGISTERs\n");
  printf("  public static final int 
     MRG_INIT = %d,MRG_TMODE = %d, MRG_ERROR = %d, MRG_SHADOW_ERROR = %d,
     MRG_TMODOUTD_REG = %d, MRG_TMODINPR_REG = %d, MRG_TMODCMP_REG = %d,
     MRG_OUT1_HOLD = %d, MRG_OUT2_HOLD = %d, MRG_OUT_HOLD = %d,
     MRG_ERR_EN = %d, MRG_CDFERR_EN = %d, MRG_CDFERR = %d, MRG_GCDFERR_EN = %d,
     MRG_SWITCHES = %d, MRG_FSM = %d, MRG_DETERMINISTIC = %d, MRG_ENDEVENT = %d,
     MRG_A_FIFO_REG = %d, MRG_B_FIFO_REG = %d, MRG_C_FIFO_REG = %d, MRG_D_FIFO_REG = %d,
     MRG_A_FIFO_EMPTY = %d, MRG_B_FIFO_EMPTY = %d, MRG_C_FIFO_EMPTY = %d, MRG_D_FIFO_EMPTY = %d, 
     MRG_OSPY_PTR = %d, MRG_OSPY_FRZ = %d, MRG_OSPY_WRP = %d, MRG_OSPY_TMPTR = %d,
     MRG_ASPY_PTR = %d, MRG_ASPY_FRZ = %d, MRG_ASPY_WRP = %d,
     MRG_BSPY_PTR = %d, MRG_BSPY_FRZ = %d, MRG_BSPY_WRP = %d,
     MRG_CSPY_PTR = %d, MRG_CSPY_FRZ = %d, MRG_CSPY_WRP = %d,
     MRG_DSPY_PTR = %d, MRG_DSPY_FRZ = %d, MRG_DSPY_WRP = %d;\n",
     MRG_INIT, MRG_TMODE, MRG_ERROR, MRG_SHADOW_ERROR,
     MRG_TMODOUTD_REG, MRG_TMODINPR_REG, MRG_TMODCMP_REG,
     MRG_OUT1_HOLD, MRG_OUT2_HOLD, MRG_OUT_HOLD,
     MRG_ERR_EN, MRG_CDFERR_EN, MRG_CDFERR, MRG_GCDFERR_EN,
     MRG_SWITCHES,
     MRG_FSM, MRG_DETERMINISTIC, MRG_ENDEVENT,
     MRG_A_FIFO_REG, MRG_B_FIFO_REG, MRG_C_FIFO_REG, MRG_D_FIFO_REG,
     MRG_A_FIFO_EMPTY, MRG_B_FIFO_EMPTY, MRG_C_FIFO_EMPTY, MRG_D_FIFO_EMPTY, 
     MRG_OSPY_PTR, MRG_OSPY_FRZ, MRG_OSPY_WRP, MRG_OSPY_TMPTR,
     MRG_ASPY_PTR, MRG_ASPY_FRZ, MRG_ASPY_WRP,
     MRG_BSPY_PTR, MRG_BSPY_FRZ, MRG_BSPY_WRP,
     MRG_CSPY_PTR, MRG_CSPY_FRZ, MRG_CSPY_WRP,
     MRG_DSPY_PTR, MRG_DSPY_FRZ, MRG_DSPY_WRP);

  printf("     // MERGER MEMORYs \n");
  printf("  public static final int 
     MRG_IDPROM = %d;\n",MRG_IDPROM);

  printf("     // MERGER SPYs \n");
  printf("  public static final int 
     MRG_A_SPY = %d, MRG_B_SPY = %d, MRG_C_SPY = %d, 
     MRG_D_SPY = %d, MRG_OUT_SPY = %d;\n",
     MRG_A_SPY, MRG_B_SPY, MRG_C_SPY, MRG_D_SPY, MRG_OUT_SPY);
  printf("     // MERGER FIFOs \n");
  printf("  public static final int 
     MRG_A_FIFO = %d, MRG_B_FIFO = %d, MRG_C_FIFO = %d, MRG_D_FIFO = %d;\n",
     MRG_A_FIFO , MRG_B_FIFO, MRG_C_FIFO, MRG_D_FIFO);

  printf("     // SPY CONTROL REGISTERs \n");
  printf("  public static final int 
     SC_JUMPER_MASTER = %d, SC_JUMPER_LAST = %d, 
     SC_GINIT_IN = %d, SC_GFREEZE_IN = %d,  
     SC_GERROR_OUT = %d, SC_GLLOCK_OUT = %d, SC_GBUS = %d,
     SC_INIT_FORCE = %d, SC_INIT_ON_GINIT = %d, SC_INIT_PULSE = %d, 
     SC_BACKPLANE_INIT = %d, SC_BACKPLANE_FREEZE = %d, 
     SC_BACKPLANE_ERROR = %d, SC_BACKPLANE_LLOCK = %d,  
     SC_BACKPLANE = %d, SC_GERROR_FORCE = %d, SC_GERROR_ON_ERROR = %d, SC_GERROR_DRIVEN = %d,  
     SC_GLLOCK_FORCE = %d, SC_GLLOCK_ON_LLOCK = %d, SC_GLLOCK_DRIVEN = %d,
     SC_FREEZE_FORCE = %d, SC_FREEZE_ON_ERROR = %d, 
     SC_FREEZE_ON_LLOCK = %d, SC_FREEZE_ON_GFREEZE = %d, 
     SC_FREEZE_DELAY = %d, SC_LEVEL1COUNTER = %d, 
     SC_CDFERR_FORCE = %d, SC_CDFERR_ON_ERROR = %d, SC_CDFERR_ON_LLOCK = %d,  
     SC_CDFERR_ON_GERROR = %d, SC_CDFERR_ON_GLLOCK = %d, 
     SC_CDFRECOV = %d, SC_CDFRUN = %d, 
     SC_GINIT_FORCE = %d, SC_GINIT_ON_CDFSIGS = %d, SC_GINIT_DRIVEN = %d,  
     SC_GINIT_PULSE = %d, 
     SC_GFREEZE_FORCE = %d, SC_GFREEZE_ON_GERROR = %d, SC_GFREEZE_ON_GLLOCK = %d,  
     SC_GFREEZE_DELAY = %d;\n",
     SC_JUMPER_MASTER, SC_JUMPER_LAST, 
     SC_GINIT_IN, SC_GFREEZE_IN,  
     SC_GERROR_OUT, SC_GLLOCK_OUT, SC_GBUS,
     SC_INIT_FORCE, SC_INIT_ON_GINIT, SC_INIT_PULSE, 
     SC_BACKPLANE_INIT, SC_BACKPLANE_FREEZE, 
     SC_BACKPLANE_ERROR, SC_BACKPLANE_LLOCK,  
     SC_BACKPLANE, 
     SC_GERROR_FORCE, SC_GERROR_ON_ERROR, SC_GERROR_DRIVEN,  
     SC_GLLOCK_FORCE, SC_GLLOCK_ON_LLOCK, SC_GLLOCK_DRIVEN,
     SC_FREEZE_FORCE, SC_FREEZE_ON_ERROR, 
     SC_FREEZE_ON_LLOCK, SC_FREEZE_ON_GFREEZE, 
     SC_FREEZE_DELAY,
     SC_LEVEL1COUNTER, 
     SC_CDFERR_FORCE, SC_CDFERR_ON_ERROR, SC_CDFERR_ON_LLOCK,  
     SC_CDFERR_ON_GERROR, SC_CDFERR_ON_GLLOCK, 
     SC_CDFRECOV, SC_CDFRUN, 
     SC_GINIT_FORCE, SC_GINIT_ON_CDFSIGS, SC_GINIT_DRIVEN,  
     SC_GINIT_PULSE, 
     SC_GFREEZE_FORCE, SC_GFREEZE_ON_GERROR, SC_GFREEZE_ON_GLLOCK,  
     SC_GFREEZE_DELAY);
  
  printf("     // Alias Thomas's names, for convenience \n");
  printf("  public static final int 
     SC_JUMPER_REG = %d, SC_G_BUS_INP_REG = %d, SC_SVT_INIT_GEN_REG = %d,
     SC_SVT_INIT_PULSE_REG = %d, SC_BACKPLANE_REG = %d, SC_G_ERROR_GEN_REG = %d,
     SC_G_LLOCK_GEN_REG = %d, SC_SVT_FREEZE_GEN_REG = %d, SC_SVT_FREEZE_DELAY_REG = %d,
     SC_LVL1_COUNT_REG = %d, SC_CDF_ERROR_GEN_REG = %d, SC_CDF_RECOVER_REG = %d,
     SC_G_INIT_GEN_REG = %d, SC_G_INIT_PULSE_REG = %d, SC_G_FREEZE_GEN_REG = %d,
     SC_G_FREEZE_DELAY_REG = %d;\n",   
     SC_JUMPER_REG, SC_G_BUS_INP_REG, SC_SVT_INIT_GEN_REG,
     SC_SVT_INIT_PULSE_REG, SC_BACKPLANE_REG, SC_G_ERROR_GEN_REG,
     SC_G_LLOCK_GEN_REG, SC_SVT_FREEZE_GEN_REG, SC_SVT_FREEZE_DELAY_REG,
     SC_LVL1_COUNT_REG, SC_CDF_ERROR_GEN_REG, SC_CDF_RECOVER_REG,
     SC_G_INIT_GEN_REG, SC_G_INIT_PULSE_REG, SC_G_FREEZE_GEN_REG,
     SC_G_FREEZE_DELAY_REG);

  printf("     // SPY CONTROL MEMORYs\n");
  printf("  public static final int 
     SC_IDPROM = %d;\n", SC_IDPROM);

  printf("     // HF REGISTERs \n");
  printf("  public static final int 
     HF_INIT = %d, 
     HF_BRAIN = %d,  HF_MODE = %d,
     HF_TMODE_MOP = %d, HF_TMODE_MERGER = %d, HF_TMODE_HITMAN = %d,
     HF_TEST_CLOCK = %d, HF_VME_FREEZE = %d, HF_ENABLE_BKUP_CONFIG = %d,
     HF_STROBE_VME_CLOCK = %d,
     HF_MERGER_STREAM_DISABLE = %d, 
     HF_MOP_STREAM_DISABLE = %d,
     HF_HOLD_DISABLE = %d, HF_DS_DISABLE = %d,
     HF_OUT_HOLD = %d, HF_OUT1_HOLD = %d, HF_OUT2_HOLD = %d,
     HF_OSPY_PTR = %d, HF_OSPY_FRZ = %d, HF_OSPY_WRP = %d,
     HF_FIFO_0_EMPTY = %d, HF_FIFO_1_EMPTY = %d, HF_FIFO_2_EMPTY = %d, HF_FIFO_3_EMPTY = %d, 
     HF_FIFO_4_EMPTY = %d, HF_FIFO_5_EMPTY = %d, HF_FIFO_6_EMPTY = %d, HF_FIFO_7_EMPTY = %d, 
     HF_FIFO_8_EMPTY = %d, HF_FIFO_9_EMPTY = %d, HF_FIFO_EMPTY_ALL = %d,
     HF_ISPY_0_PTR = %d, HF_ISPY_0_WRP = %d, HF_ISPY_1_PTR = %d, HF_ISPY_1_WRP = %d,
     HF_ISPY_2_PTR = %d, HF_ISPY_2_WRP = %d, HF_ISPY_3_PTR = %d, HF_ISPY_3_WRP = %d,
     HF_ISPY_4_PTR = %d, HF_ISPY_4_WRP = %d, HF_ISPY_5_PTR = %d, HF_ISPY_5_WRP = %d,
     HF_ISPY_6_PTR = %d, HF_ISPY_6_WRP = %d, HF_ISPY_7_PTR = %d, HF_ISPY_7_WRP = %d,
     HF_ISPY_8_PTR = %d, HF_ISPY_8_WRP = %d, HF_ISPY_9_PTR = %d, HF_ISPY_9_WRP = %d,
     HF_ISPY_ALL_PTR = %d, HF_ISPY_ALL_WRP = %d,                               
     HF_ISPY_FRZ = %d,
     HF_ENDEVENTRESET = %d, HF_ERRORREGRESET = %d,
     HF_AENDEVENTMASK = %d, HF_BENDEVENTMASK = %d, HF_CENDEVENTMASK = %d, 
     HF_AENDEVENTCLR  = %d, HF_BENDEVENTCLR = %d, HF_CENDEVENTCLR = %d, 
     HF_ACDFERRORMASK = %d, HF_BCDFERRORMASK = %d, HF_CCDFERRORMASK = %d, 
     HF_ASVTERRORMASK = %d, HF_BSVTERRORMASK = %d, HF_CSVTERRORMASK = %d,
     HF_AERRORREG = %d, HF_BERRORREG = %d, HF_CERRORREG = %d,
     HF_ALOCALREG = %d, HF_BLOCALREG = %d, HF_CLOCALREG = %d,
     HF_CTHRESH_ALL = %d, HF_CBIG_ALL = %d, HF_CHIPIDRESET_ALL = %d, HF_MAXNUMCLUST_ALL = %d,
     HF_LASTHFADDR = %d, HF_LOSTLOCK = %d, HF_EVENTCOUNTER = %d;\n",
     HF_INIT, 
     HF_BRAIN,  HF_MODE,
     HF_TMODE_MOP, HF_TMODE_MERGER, HF_TMODE_HITMAN,
     HF_TEST_CLOCK, HF_VME_FREEZE, HF_ENABLE_BKUP_CONFIG,
     HF_STROBE_VME_CLOCK,
     HF_MERGER_STREAM_DISABLE, 
     HF_MOP_STREAM_DISABLE,
     HF_HOLD_DISABLE, HF_DS_DISABLE,
     HF_OUT_HOLD, HF_OUT1_HOLD, HF_OUT2_HOLD,
     HF_OSPY_PTR, HF_OSPY_FRZ, HF_OSPY_WRP,
     HF_FIFO_0_EMPTY, HF_FIFO_1_EMPTY, HF_FIFO_2_EMPTY, HF_FIFO_3_EMPTY, 
     HF_FIFO_4_EMPTY, HF_FIFO_5_EMPTY, HF_FIFO_6_EMPTY, HF_FIFO_7_EMPTY, 
     HF_FIFO_8_EMPTY, HF_FIFO_9_EMPTY, HF_FIFO_EMPTY_ALL,
     HF_ISPY_0_PTR, HF_ISPY_0_WRP, HF_ISPY_1_PTR, HF_ISPY_1_WRP,
     HF_ISPY_2_PTR, HF_ISPY_2_WRP, HF_ISPY_3_PTR, HF_ISPY_3_WRP,
     HF_ISPY_4_PTR, HF_ISPY_4_WRP, HF_ISPY_5_PTR, HF_ISPY_5_WRP,
     HF_ISPY_6_PTR, HF_ISPY_6_WRP, HF_ISPY_7_PTR, HF_ISPY_7_WRP,
     HF_ISPY_8_PTR, HF_ISPY_8_WRP, HF_ISPY_9_PTR, HF_ISPY_9_WRP,
     HF_ISPY_ALL_PTR, HF_ISPY_ALL_WRP,                               
     HF_ISPY_FRZ, 
     HF_ENDEVENTRESET, HF_ERRORREGRESET,
     HF_AENDEVENTMASK, HF_BENDEVENTMASK, HF_CENDEVENTMASK, 
     HF_AENDEVENTCLR, HF_BENDEVENTCLR, HF_CENDEVENTCLR, 
     HF_ACDFERRORMASK, HF_BCDFERRORMASK, HF_CCDFERRORMASK, 
     HF_ASVTERRORMASK, HF_BSVTERRORMASK, HF_CSVTERRORMASK,
     HF_AERRORREG, HF_BERRORREG, HF_CERRORREG,
     HF_ALOCALREG, HF_BLOCALREG, HF_CLOCALREG,
     HF_CTHRESH_ALL, HF_CBIG_ALL, HF_CHIPIDRESET_ALL, HF_MAXNUMCLUST_ALL,
     HF_LASTHFADDR, HF_LOSTLOCK, HF_EVENTCOUNTER);

  printf("     // HF MEMORYs \n");
  printf("  public static final int 
     HF_IDPROM = %d, HF_CRAMLO_ALL = %d, HF_STHRESH_ALL = %d, HF_CHIPID_ALL = %d,
     HF_PED_ALL = %d, HF_BLMAP = %d;\n",
     HF_IDPROM, HF_CRAMLO_ALL, HF_STHRESH_ALL, HF_CHIPID_ALL,
     HF_PED_ALL, HF_BLMAP);

  printf("     // HF SPYs \n");
  printf("  public static final int 
     HF_ISPY_0 = %d, HF_ISPY_1 = %d, HF_ISPY_2 = %d, HF_ISPY_3 = %d, HF_ISPY_4 = %d,
     HF_ISPY_5 = %d, HF_ISPY_6 = %d, HF_ISPY_7 = %d, HF_ISPY_8 = %d, HF_ISPY_9 = %d,
     HF_ISPY_ALL = %d, HF_OUT_SPY = %d;\n",
     HF_ISPY_0, HF_ISPY_1, HF_ISPY_2, HF_ISPY_3, HF_ISPY_4,
     HF_ISPY_5, HF_ISPY_6, HF_ISPY_7, HF_ISPY_8, HF_ISPY_9,
     HF_ISPY_ALL, HF_OUT_SPY);

  printf("     // HF FIFOs \n");
  printf("  public static final int 
     HF_FIFO_0 = %d, HF_FIFO_1 = %d, HF_FIFO_2 = %d, HF_FIFO_3 = %d, HF_FIFO_4 = %d,
     HF_FIFO_5 = %d, HF_FIFO_6 = %d, HF_FIFO_7 = %d, HF_FIFO_8 = %d, HF_FIFO_9 = %d,
     HF_FIFO_ALL = %d;\n",
     HF_FIFO_0, HF_FIFO_1, HF_FIFO_2, HF_FIFO_3, HF_FIFO_4,
     HF_FIFO_5, HF_FIFO_6, HF_FIFO_7, HF_FIFO_8, HF_FIFO_9,
     HF_FIFO_ALL);

  printf("  public static final int 
     LAST_PREDEFINED_OBJECT = %d;\n",LAST_PREDEFINED_OBJECT);

  printf("     // AMB constant values for register data \n");
  printf("  public static final int 
     AMB_PIPE = %d , AMB_READ = %d, AMB_WRITE = %d, AMB_HALT = %d;\n",
     AMB_PIPE, AMB_READ, AMB_WRITE, AMB_HALT);

  printf("     // HF constant values for register data \n");
  printf("  public static final int 
     HF_LOAD = %d, HF_RUN = %d, HF_TEST = %d, HF_BOOT = %d;\n",
     HF_LOAD, HF_RUN, HF_TEST, HF_BOOT);
  printf("  public static final int 
     HF_CLOCK_VME = %d, HF_CLOCK_7_5MHZ = %d, 
     HF_CLOCK_15MHZ = %d, HF_CLOCK_30MHZ = %d;\n",
     HF_CLOCK_VME, HF_CLOCK_7_5MHZ, HF_CLOCK_15MHZ, HF_CLOCK_30MHZ);
  printf("}\n");
  return 0;
}
