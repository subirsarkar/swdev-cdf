from getmod import * 
import sys 
 
def main(): 
  print sys.argv[1]
  d = DictImpl.svtsim_dict_new(-1) 
  assert(0 <= DictImpl.svtsim_dict_addFile(d, sys.argv[1])) 
  crc = DictImpl.svtsim_dict_crc(d, "hwsetCrc") 
  DictImpl.svtsim_dict_free(d) 
  print "%s: hwsetCrc %u"%(sys.argv[1], crc) 
 
if __name__=="__main__": 
  main() 

