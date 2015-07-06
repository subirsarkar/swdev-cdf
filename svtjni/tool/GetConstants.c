#include <stdio.h>
#include "svtvme_public.h"
#include "svtvme_private.h"

int get_object_names();

int main() 
{
  svtvme_initialise();
  svtvme_initialise_tclk();
  svtvme_initialise_gb();
  get_object_names();
  return 0;
}
int get_object_names()
{
  int i;
  char name[200];
  FILE *fp;
  if (!(fp = fopen("SvtvmeConstants.java", "w"))) {
    printf("File SvtvmeConstants.java cannot be opened for writing\n");
    return -1;
  } 
  fprintf(fp, "package jsvtvme;\n\n");
  fprintf(fp, "public class SvtvmeConstants {\n");

  fprintf(fp, "  public static final int VERBOSE = %d;\n", VERBOSE);
  fprintf(fp, "  public static final int ERROR_REPORT = %d;\n", ERROR_REPORT);
  fprintf(fp, "  public static final int MAX_WORDS_IN_BLOCK = %d;\n", MAX_WORDS_IN_BLOCK);

  fprintf(fp, "  public static final int SAFE = %d;\n", SAFE);
  fprintf(fp, "  public static final int OPEN = %d;\n", OPEN);
  fprintf(fp, "  public static final int LOCK = %d;\n", LOCK);
  fprintf(fp, "  public static final int RUNNING = %d;\n", RUNNING);
  fprintf(fp, "  public static final int DEBUG = %d;\n", DEBUG);

  fprintf(fp, "  public static final int SLOWER = %d;\n", SLOWER);
  fprintf(fp, "  public static final int FASTER = %d;\n", FASTER);

  fprintf(fp, "  public static final int IGNORE_ERRORS = %d;\n", IGNORE_ERRORS);
  fprintf(fp, "  public static final int STOP_AT_FIRST_ERROR = %d;\n", STOP_AT_FIRST_ERROR);
  fprintf(fp, "  public static final int STOP_AT_ITERATION_END = %d;\n", STOP_AT_ITERATION_END);

  fprintf(fp, "  public static final int EMPTY_SLOT = %d;\n", EMPTY_SLOT); 
  fprintf(fp, "  public static final int TRC = %d;\n", TRC); 
  fprintf(fp, "  public static final int TCLK = %d;\n", TCLK); 
  fprintf(fp, "  public static final int AMB = %d;\n", AMB); 
  fprintf(fp, "  public static final int AMS = %d;\n", AMS); 
  fprintf(fp, "  public static final int HB = %d;\n", HB); 
  fprintf(fp, "  public static final int MRG = %d;\n", MRG); 
  fprintf(fp, "  public static final int HF = %d;\n", HF); 
  fprintf(fp, "  public static final int TF = %d;\n", TF); 
  fprintf(fp, "  public static final int XTFA = %d;\n", XTFA); 
  fprintf(fp, "  public static final int SC = %d;\n", SC); 
  fprintf(fp, "  public static final int XTFC = %d;\n", XTFC); 
  fprintf(fp, "  public static final int GB = %d;\n", GB); 
  fprintf(fp, "  public static final int SVTB = %d;\n", SVTB); 
  fprintf(fp, "  public static final int MAX_BOARD_ID = %d;\n", MAX_BOARD_ID); 

  fprintf(fp, "  public static final int DUMMY_OBJECT = %d;\n",DUMMY_OBJECT);
  for (i = 0; i < MaxObjects; i++)  {
    if (SVTVME_OBJS[i] != NULL) {
      svtvme_objectName(i, name);
      fprintf(fp,"  public static final int %s", name);
      fprintf(fp," =  %d; \n", svtvme_stringToObject(name));
    }
  }
  fprintf(fp, "  public static final int LAST_PREDEFINED_OBJECT = %d;\n", 
      LAST_PREDEFINED_OBJECT);
  fprintf(fp, "  public static final int AMB_PIPE = %d;\n", AMB_PIPE); 
  fprintf(fp, "  public static final int AMB_READ = %d;\n", AMB_READ); 
  fprintf(fp, "  public static final int AMB_WRITE = %d;\n", AMB_WRITE); 
  fprintf(fp, "  public static final int AMB_HALT = %d;\n", AMB_HALT); 

  fprintf(fp, "  public static final int HF_LOAD = %d;\n", HF_LOAD); 
  fprintf(fp, "  public static final int HF_RUN = %d;\n", HF_RUN); 
  fprintf(fp, "  public static final int HF_TEST = %d;\n", HF_TEST); 
  fprintf(fp, "  public static final int HF_BOOT = %d;\n", HF_BOOT); 

  fprintf(fp, "  public static final int HF_CLOCK_VME = %d;\n", HF_CLOCK_VME); 
  fprintf(fp, "  public static final int HF_CLOCK_7_5MHZ = %d;\n", HF_CLOCK_7_5MHZ); 
  fprintf(fp, "  public static final int HF_CLOCK_15MHZ = %d;\n", HF_CLOCK_15MHZ); 
  fprintf(fp, "  public static final int HF_CLOCK_30MHZ = %d;\n", HF_CLOCK_30MHZ); 

  fprintf(fp,"}\n");
  fclose(fp);
  return (0);
}
