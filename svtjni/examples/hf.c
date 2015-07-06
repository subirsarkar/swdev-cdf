#include <stdio.h>
#include <stdlib.h>
#include <svtvme_public.h>

int main() 
{
   svtvme_h hf;
   int error;
   uint4 state;

   hf = svtvme_openBoard("b0svt05", 5, HF);

#if 1
   printf("isFrozen(hf, HF_ISPY_0) = %d\n", svtvme_isFrozen(hf, HF_ISPY_0));
   printf("isFrozen(hf, HF_ISPY_1) = %d\n", svtvme_isFrozen(hf, HF_ISPY_1));
   printf("isFrozen(hf, HF_ISPY_2) = %d\n", svtvme_isFrozen(hf, HF_ISPY_2));
   printf("isFrozen(hf, HF_ISPY_3) = %d\n", svtvme_isFrozen(hf, HF_ISPY_3));
   printf("isFrozen(hf, HF_ISPY_4) = %d\n", svtvme_isFrozen(hf, HF_ISPY_4));
   printf("isFrozen(hf, HF_ISPY_5) = %d\n", svtvme_isFrozen(hf, HF_ISPY_5));
   printf("isFrozen(hf, HF_ISPY_6) = %d\n", svtvme_isFrozen(hf, HF_ISPY_6));
   printf("isFrozen(hf, HF_ISPY_7) = %d\n", svtvme_isFrozen(hf, HF_ISPY_7));
   printf("isFrozen(hf, HF_ISPY_8) = %d\n", svtvme_isFrozen(hf, HF_ISPY_8));
   printf("isFrozen(hf, HF_ISPY_9) = %d\n", svtvme_isFrozen(hf, HF_ISPY_9));

   printf("isWrapped(hf, HF_ISPY_0) = %d\n", svtvme_isWrapped(hf, HF_ISPY_0));
   printf("isWrapped(hf, HF_ISPY_1) = %d\n", svtvme_isWrapped(hf, HF_ISPY_1));
   printf("isWrapped(hf, HF_ISPY_2) = %d\n", svtvme_isWrapped(hf, HF_ISPY_2));
   printf("isWrapped(hf, HF_ISPY_3) = %d\n", svtvme_isWrapped(hf, HF_ISPY_3));
   printf("isWrapped(hf, HF_ISPY_4) = %d\n", svtvme_isWrapped(hf, HF_ISPY_4));
   printf("isWrapped(hf, HF_ISPY_5) = %d\n", svtvme_isWrapped(hf, HF_ISPY_5));
   printf("isWrapped(hf, HF_ISPY_6) = %d\n", svtvme_isWrapped(hf, HF_ISPY_6));
   printf("isWrapped(hf, HF_ISPY_7) = %d\n", svtvme_isWrapped(hf, HF_ISPY_7));
   printf("isWrapped(hf, HF_ISPY_8) = %d\n", svtvme_isWrapped(hf, HF_ISPY_8));
   printf("isWrapped(hf, HF_ISPY_9) = %d\n", svtvme_isWrapped(hf, HF_ISPY_9));

   error = svtvme_getState(hf, HF_ISPY_0_PTR, &state);
   printf("getState(hf, HF_ISPY_0_PTR,state) = %d\n", state);

   error = svtvme_getState(hf, HF_ISPY_1_PTR, &state);
   printf("getState(hf, HF_ISPY_1_PTR,state) = %d\n", state);
   
   error = svtvme_getState(hf, HF_ISPY_2_PTR, &state);
   printf("getState(hf, HF_ISPY_2_PTR,state) = %d\n", state);

   error = svtvme_getState(hf, HF_ISPY_3_PTR, &state);
   printf("getState(hf, HF_ISPY_3_PTR,state) = %d\n", state);

   error = svtvme_getState(hf, HF_ISPY_4_PTR, &state);
   printf("getState(hf, HF_ISPY_4_PTR,state) = %d\n", state);

   error = svtvme_getState(hf, HF_ISPY_5_PTR, &state);
   printf("getState(hf, HF_ISPY_5_PTR,state) = %d\n", state);

   error = svtvme_getState(hf, HF_ISPY_6_PTR, &state);
   printf("getState(hf, HF_ISPY_6_PTR,state) = %d\n", state);

   error = svtvme_getState(hf, HF_ISPY_7_PTR, &state);
   printf("getState(hf, HF_ISPY_7_PTR,state) = %d\n", state);

   error = svtvme_getState(hf, HF_ISPY_8_PTR, &state);
   printf("getState(hf, HF_ISPY_8_PTR,state) = %d\n", state);

   error = svtvme_getState(hf, HF_ISPY_9_PTR, &state);
   printf("getState(hf, HF_ISPY_9_PTR,state) = %d\n", state);

#endif
   svtvme_closeBoard(hf);
}

