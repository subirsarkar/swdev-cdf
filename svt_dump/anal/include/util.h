#ifndef __HEADER_UTIL__
#define __HEADER_UTIL__

#include <time.h>

typedef struct {
  clock_t begin_clock, save_clock;
  time_t  begin_time, save_time; 
} time_keeper;

void start_time(void);
double prn_time(void);
FILE *gfopen(char *filename, char *mode);
int get_datime(int *u_year, int *u_month, int *u_day, 
               int *u_hour, int *u_min,   int *u_sec);

#endif
