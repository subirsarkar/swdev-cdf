#include <iostream>
#include <iomanip>
#include <fstream>
#include <sstream>
#include <vector>
#include <string>

// C++ style inclusion of Standard C library  
#include <cstdlib>
#include <ctime>
#include <cstring>
#include <getopt.h>

#include "rtworks/cxxipc.hxx"

#define NEL(x) (sizeof((x))/sizeof((x)[0]))

using std::string;
using std::cout;
using std::cerr;
using std::endl;

static void showUsage(const string& app);

static const string SendCommand::commandList[] = {
    "configure",
    "sendStatus",
    "sendHist",
    "resetError",
    "resetHist",
    "ReadDefFile",
    "ReadFile"
};
static const string SendCommand::crateList[] = {
  "b0svt00",  
  "b0svt01",  
  "b0svt02",  
  "b0svt03",
  "b0svt04",  
  "b0svt05",  
  "b0svt06",  
  "b0svt07"
};

SendCommand::SendCommand() {
  set("/spymon/command"+crateList[0], commandList[6]);  // discuss
}
bool SendCommand::validate(const string& dest, const string& command, const string& file) {
  set(dest, command, file); 
  return (validDestination() && validCommand());
}
bool SendCommand::validDestination() const {
  for (unsigned int i = 0; i < NEL(crateList); i++) {
    string dest = "/spymon/command/"+crateList[i];
    if (_dest == dest) return true;
  }  
  return false;
}
bool SenCommand::validCommand() const {
  // Handle file also in future 
  for (unsigned int i = 0; i < NEL(commandList); i++) {
    if (_command == commandList[i]) return true;
  }  
  return false;
}
bool SendCommand::send() {
  TipcMt mt(STRING_TYPE);
  TipcMsg msg(mt);

  if (!msg) {
    TutOut("Could not create message.\n");
    return T_EXIT_FAILURE;
  }

  // THIS IS STILL NOT implemented
  msg.NumFields(0);
  msg.Append(command);
  msg.Append(file);
  if (DEBUG) msg.Print(TutOut);

  // -------------------
  // Publish the message
  // ------------------- 
  msg.Dest(dest);

  srv.Send(msg);
  srv.Flush();

  return T_EXIT_SUCCESS;
}
// -----------------------------------------------------
// Initialise and establish connection to the RTServer 
// Try to see if the default init method sometime
// -----------------------------------------------------
void SendCommand::InitSrv(TipcSrv& srv) {
  ostringstream configFileName;
  configFileName << getenv("SMARTSOCKETS_CONFIG_DIR") << "/unix.cm";
  cout << "Config File: " << configFileName.str() << endl; 

  // Read SmartSockets configuration from a standard file, Does not work !!!!
  TutCommandParseFile(configFileName.str());

  // Connect to RTserver 
  if (!srv.Create(T_IPC_SRV_CONN_FULL)) {
    TutOut("ERROR. InitSrv: Could not connect to RTserver!\n");
    exit(T_EXIT_FAILURE);
  }
  else {
    TutOut("INFO. Connected to RT Server...\n");
  }
}
void SendCommand::closeSrv(TipcSrv& srv) {
  if (srv) srv.Destroy(T_IPC_SRV_CONN_NONE); // disconnect from RTserver
}
static void SendCommand::showValidCommands() {
  for (unsigned int i = 0; i < NEL(commandList); i++) {
    cout << commandList[i] << endl;
  }  
}
static void SendCommand::showValidDestinations() {
  for (unsigned int i = 0; i < NEL(crateList); i++) {
    cout << "/spymon/command/"+crateList[i] << endl;
  }  
}
void showUsage(const string& app) {
  cout << "Usage:  " << app << " [--help --verbose --dest destination -command command --file file]"
       << endl;
  cout << "--verbose    Show some detail as you go along\n"
       << "--help       Show usage and exit\n"
       << "--dest       SS message destination path [/spymon/command/[destination]]\n" 
       << "--command    Single word command to be sent\n"
       << "--file       Application only for ReadConfig"
       << endl;
  cout << "valid Commands are:\n"
       << SendCommand::showValidCommands()
       << endl;
  cout << "Valid destinations are:\n"  
       << SendCommand::showValidDestinations()
       << endl;
}
// ----------------
// Main entry point 
// ----------------

int main(int argc, char *argv[]) {
  const char *app = argv[0];
  if (argc < 2) {
    showUsage(app);
    exit(1);
  }

  int c,
    help_i    = 0,
    verbose_i = 0;
  string dest_n, command_n, file_n;
  while (1) {
    const char *optnam = 0;
    int option_index = 0;
    static struct option long_options [] =
    {
      { "help",    no_argument,           &help_i,    1},
      { "verbose", no_argument,           &verbose_i, 1},
      { "dest",    required_argument,     0,          0},
      { "command", required_argument,     0,          0},
      { "file",    required_argument,     0,          0},
      { 0,         0,                     0,          0}
    };
    c = getopt_long(argc, argv, "", long_options, &option_index);
    if (c == -1) break;
    switch (c) {
    case 0:
      string optnam(long_options[option_index].name);
      if (optnam == "help" || optnam == "verbose") {
	// already parsed
        if (help_i) {
          showUsage(app); 
          exit(2);
        }
      }
      else if (optnam == "dest")
        dest_n = optarg;
      else if (optnam == "command")
        command_n = optarg;
      else if (optnam == "file")
        file_n = optarg;
      else
        cerr << "option " << optnam << " not implemented!" << endl;
      break;
    case '?':
      assert(0);
      break;
    default:
      cerr << "?? getopt returned character code 0" << c << "??" << endl;
    }
  }

  // Create the sender class and validate destination, command and file content (if applicable)
  SendCommand app;
  TipcSrv& srv = TipcSrv::Instance();  // Obtain singleton handle to SS Server
  app.initSrv(srv);

  bool valid = app.validate();
  if (valid) app.send();
  app.closeSrv(srv);            // This is a one-shot app 

  return T_EXIT_SUCCESS;
}
