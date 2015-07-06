#include <stdio.h>
#include <stdlib.h>
#ifdef VXWORKS
#include <sysLib.h>
#endif
#include "util.h"

#define MAXSTRING 100

static time_keeper tk;   /* Known only to this file */

void start_time(void) 
{
  tk.begin_clock = tk.save_clock = clock();
  tk.begin_time  = tk.save_time  = time(NULL);
}

double prn_time(void) 
{
  char s1[MAXSTRING], s2[MAXSTRING];
  int field_width, n1, n2;
  double clocks_per_second = (double) CLOCKS_PER_SEC,
         user_time, real_time;  

  user_time = (clock() - tk.save_clock) / clocks_per_second;
  real_time = difftime(time(NULL), tk.save_time);
  tk.save_clock = clock();
  tk.save_time  = time(NULL);

  /* print the values found, and do it neatly */
  n1 = sprintf(s1, "%.1f", user_time);
  n2 = sprintf(s2, "%.1f", real_time);

  field_width = (n1 < n2) ? n1 : n2;
  fprintf(stderr, "%s%*.1f%s\n%s%*.1f%s\n\n",
	  "User time: ", field_width, user_time, " Seconds", 
	  "Real time: ", field_width, real_time, " Seconds"
  );
  return user_time;
} 

/* 
 * Open a file gracefully, if file open fails, use stdout 
 */
FILE *gfopen(char *filename, char *mode)
{
  FILE *fp;
  if ((fp = fopen(filename, mode)) == NULL) {
    fprintf(stderr, "Cannot open %s - using stdout!\n", filename);
    fp = stderr;
  }
  return fp;
}
/*
 * Split date time information 
 */
int get_datime(int *u_year, int *u_month, int *u_day, 
               int *u_hour, int *u_min,   int *u_sec) 
{
  time_t now;
  struct tm *tp;

  /* Date and time */
  now = time(NULL);
#ifdef VXWORKS
  now -=5*3600; /* timezone? */ 
#endif
  tp       = localtime(&now);
  *u_year  = tp->tm_year+1900;
  *u_month = tp->tm_mon+1;
  *u_day   = tp->tm_mday; 
  *u_hour  = tp->tm_hour; 
  *u_min   = tp->tm_min; 
  *u_sec   = tp->tm_sec;

  return 0;
}
