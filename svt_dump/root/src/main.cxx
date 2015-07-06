#include <iostream.h>
#include <getopt.h>

#include <TROOT.h> 
#include <TApplication.h>
#include <TSystem.h>
#include <TEnv.h>
#include "CreateRootFile.h"

static char dir_name[120];
static int readargs(int argc, char **argv);

TROOT root("hello","Hello World");

/*
 * Parse command-line arguments
 */
static int
readargs(int argc, char **argv)
{
  int c;
  if (argc == 1) {
    printf("Usage:\n  %s <hist_file(s)> --dir=<dir> \n", argv[0]);
    printf("Where:\n <data_file(s)> may contain wild cards\n");
    printf("Example: %s ./b0svt02*.hist --dir=./ \n", argv[0]);
  }
  while (1) {
    const char *optnam = 0;
    int option_index = 0;
    static struct option long_options[] = {
      { "dir", required_argument, 0, 0 },
      { 0, 0, 0, 0 }
    };
    c = getopt_long(argc, argv, "", long_options, &option_index);
    if (c == -1) break;
    switch (c) {
    case 0:
      optnam = long_options[option_index].name;
      if (0) {
      } else if (!strcmp(optnam, "dir")) {
	assert(strlen(optarg)<sizeof(dir_name));
	strcpy(dir_name, optarg);
      } else {
	fprintf(stderr, "option %s not implemented\n", optnam);
	assert(0);
      }
      break;
    case '?':
      assert(0);
      break;
    default:
      fprintf(stderr, "?? getopt returned character code 0%o ??\n", c);
    }
  }
  return optind;
}
int main(int argc, char **argv)
{
  char infile[120], outfile[120], tmpname[120];
  int dirlen;
  char format[20], ext[3];
  int i = 0;

  CreateRootFile *rootFile = new CreateRootFile();

  for (i = readargs(argc, argv); i < argc; i++) {
    printf("infile = %s\n", argv[i]);
    dirlen = strlen(dir_name);
    sprintf(format, "%s%d%s", "%", dirlen,"s%s");
    printf("format = \"%s\"\n", format);
    
    sscanf(argv[i], format, dir_name, tmpname);

    sprintf(format, "%s%d%s", "%", strlen(tmpname)-5,"s%5s");
    printf("format = \"%s\"\n", format);

    sscanf(tmpname, format, infile, ext);

    sprintf(outfile, "%s%s%s", "/data1/sarkar/root_files/", infile, ".root");
    printf("outfile = %s\n", outfile);

    rootFile->createHistograms(argv[i]);
    rootFile->saveHistograms(outfile);
  }  
  return 0;
}
