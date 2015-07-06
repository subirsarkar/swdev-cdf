#include <stdio.h>
#include "svtvme_public.h"

int main () { 
  int type, serial, status; 
  svtvme_h b;
   
  b = svtvme_openBoard("b0svt05", 10, SVTB); 
  assert(b);
  printf("Generic: slot/type/id/sn = %d/%d/%d/%d\n",  
           svtvme_boardSlot(b),  
           svtvme_boardType(b),  
           svtvme_boardId(b),  
           svtvme_boardSn(b)); 

  status = svtvme_getBoardIdentifier(b, &type, &serial); 
  printf("Specific: status/type/serial: %d/%d/%d\n", status, type, serial); 
 
  svtvme_closeBoard(b);  /* Close generic board and open the specific one */ 
  b = 0; 
 
  b = svtvme_openBoard("b0svt05", 10, type); 
  assert(b);
  printf("Specific: slot/type/id/sn = %d/%d/%d/%d\n",  
           svtvme_boardSlot(b),  
           svtvme_boardType(b),  
           svtvme_boardId(b),  
           svtvme_boardSn(b)); 

  svtvme_closeBoard(b);
  exit(0);
}
