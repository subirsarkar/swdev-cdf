#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <rtworks/ipc.h>
#include <unistd.h>
/*
 * A little utility which can send single commands to a destination.
 * Special care must be taken in order not to disturb unrelated systems
 * by sending messages to wrong subject area. Presently the destination string
 * must contain /spymon somewhere for simplicity. The default values for command
 * and destination are 'Resume' and '/spymon/command/...' respectively. It is the 
 * resposbility of the receiver of the messages to setup proper callbacks to handle
 * the messages.
 */
int main(int argc, char *argv[])
{
  T_IPC_MT mt;
  T_IPC_MSG msg;
  char commStr[20];
  char dest[255];
  char configFile[256];
  char *configDir = 0;

  strcpy(commStr, "Resume");
  strcpy(dest, "/spymon/command/...");
  if (argc < 2) {
    printf("No argument specified! using default:\n");
    printf("-> %s %s %s\n", argv[0], commStr, dest);
  }
  if (argc > 1) strcpy(commStr, argv[1]);
  if (argc > 2) strcpy(dest, argv[2]);

  if (!strstr(dest, "/spymon")) {
    printf("Can only send messaged to subject areas within /spymon\n");
    exit(1);
  }
  /* Set the name of the project */
  configDir = getenv("SMARTSOCKETS_CONFIG_DIR");
  sprintf(configFile, "%s/vxworks.cm", configDir);
  TutCommandParseFile(configFile);

  /* Connect to RTserver */
  if (!TipcSrvCreate(T_IPC_SRV_CONN_FULL)) {
    fprintf(stderr, "Could not connect to RTserver!\n");
    exit(T_EXIT_FAILURE);
  } 

  mt  = TipcMtLookupByNum(T_MT_CONTROL);
  msg = TipcMsgCreate(mt);
  T_ASSERT(msg != NULL);
  TipcMsgSetNumFields(msg, 0);
  TipcMsgAppendStr(msg, commStr);
  TipcMsgSetDest(msg, dest);

#if 0
  TipcMsgPrint(msg, TutOut);
#endif
  TipcSrvMsgSend(msg, TRUE);
  TipcSrvFlush();
  TipcMsgDestroy(msg);

  /* Shutdown connection once back from the loop */
  TipcSrvDestroy(T_IPC_SRV_CONN_NONE);

  return TRUE;
}
