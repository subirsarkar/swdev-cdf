#ifndef __SendCommand_H
#define __SendCommand_H

#include <memory>
#include <string>
#include "rtworks/cxxipc.hxx"

using namespace std;

class SendCommand {

public:
  SendCommand();
  virtual ~SendCommand() {}

  // Public Interface
  // Handle connection to the RT Server
  void initSrv(TipcSrv& srv);
  void closeSrv(TipcSrv& srv);
  bool validate(const string& dest, const string& command, const string& file="");
  bool send();

  static void showValidCommands();
  static void showValidDestinations();

protected:
  void set(const string& dest, const string& command, const string& file);
  bool validDestination() const;
  bool validCommand() const;
  
public:
  static const string commandList[];
  static const string crateList[];

private:
  string _dest;
  string _command;
  string _file;
};

#endif
