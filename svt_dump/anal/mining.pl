#!/usr/bin/perl -w
#
# Declaration of globals
use vars qw/$M_PI $PHI_SCALE $D0_SCALE $CUR_SCALE/;
use vars qw/$CHI_SCALE $HIT0_SCALE $HIT1_SCALE $HIT2_SCALE $HIT3_SCALE $HIT4_SCALE/;
use vars qw/$verbose $verbose_level $help $glob $crate $slot/;
use vars qw/$buffer $fdir $ext $outdir $hits_only $single/;
use vars qw/@hspy_list @rspy_list @pspy_list @tspy_list @mspy_list @slots/;

use strict;
use diagnostics;
use POSIX qw(:math_h);
use Data::Dumper;
use Getopt::Long;

# Forward Declaration of subroutines
sub main();
sub usage();
sub pt($); 
sub unpack_packet_parameters($$);
sub unpack_road_parameters($$);
sub unpack_hit_parameters($$);
sub unpack_track_parameters($$);
sub unpack_xft_parameters($$);

$M_PI = 3.141592654;

# Define constants  
$PHI_SCALE = 8192.;
$D0_SCALE  = 0.0010; 
$CUR_SCALE = 1.17e-5;  
$CHI_SCALE = 1.;

# To convert from strip/16 to cm: using SVXII layers 0,1,2,3,4 
$HIT0_SCALE = 10000.*16./60.;
$HIT1_SCALE = 10000.*16./62.;
$HIT2_SCALE = 10000.*16./60.;
$HIT3_SCALE = 10000.*16./60.;
$HIT4_SCALE = 10000.*16./65.;

# Command line options with Getopt::Long
$verbose = '';
$help    = '';
$glob    = '';
$crate   = 'b0svt03';
$slot    = 12;
$buffer  = 'TF_OSPY';
$fdir    = '/cdf/home/belforte/svt_data/svtspy';
$ext     = 'dat';
$outdir  = "./";
$single  = '';
$hits_only     = 0;
$verbose_level = 0;

@hspy_list = qw/HF_OUT_SPY AMS_HIT_SPY HB_HIT_SPY MRG_A_SPY MRG_B_SPY MRG_C_SPY MRG_OUT_SPY/; 
@rspy_list = qw/AMS_OUT_SPY HB_ROAD_SPY/;
@pspy_list = qw/HB_OUT_SPY TF_ISPY/;
@tspy_list = qw/TF_OSPY MRG_OUT_SPY/;

@slots     = 4..21;

# table of Pt at bin center from XFT linker specs 
# long traks 
my @PtAtBin_l = (1.52,  1.57,  1.63,  1.68,  1.75,
                 1.81,  1.88,  1.96,  2.04,  2.13,
                 2.23,  2.34,  2.46,  2.59,  2.74,
                 2.91,  3.05,  3.15,  3.25,  3.37,
                 3.49,  3.62,  3.76,  3.92,  4.09,
                 4.27,  4.47,  4.68,  4.92,  5.19,
                 5.49,  5.82,  6.19,  6.62,  7.11,
                 7.68,  8.35,  9.14, 10.11, 11.29,
                12.80, 14.77, 17.45, 21.33, 27.43,
                38.40, 64.00, 99.99);
# short traks */
my @PtAtBin_s = (1.57,  1.71,  1.89,  2.12,  2.40,
                 2.77,  3.27,  4.00,  4.80,  5.54,
                 6.55,  8.00, 10.29, 14.40, 24.00,
                99.99);

sub usage () {
  print <<HEAD;

  Extract physics level informations from different Spy buffers.
  HF_ISPY_N buffers are not being decoded yet. This program opens 
  input files specified in the command line one at a time and saves 
  the relevant physics parameters for the specified buffer in an 
  output file which is constructed from the input name, slot number
  and the buffer name. For example, for TF_OSPY in slot 12 from 
  b0svt03_20010406042909.dat the extracted parameters are saved in
  b0svt03_20010406042909_12_TF_OSPY.dat.
 
  The command line options are  
 
  --verbose  Default --noverbose
  --verbose_level Default = 0
  --help     show help on this tool and quit, default --nohelp
  --glob     wildcard match, shell glob occasionally fails, default is --noglob
  --crate    b0svtnn
  --slot     Slot number
  --buffer   svtvme name i.e MRG_A_SPY etc.
  --fdir     i.e /cdf/home/belforte/svt_data/svtspy 
  --ext      Input dump file name extension, default is 'dat'
  --outdir   Directory where the output files to be saved (default "./")
  --hits_only   From HB_OUT_SPY print only coordinate info, options
                0 - Silicon_hit/Cot_hit/Long_cluster/RoadId/Sector saved
                1 - 4 layer numbers and Hit coordinates are saved in a line
                2 - 4 full Hit words saved
  --single   if on prints the data word one word per line

  Example usage:
 
  if --glob is not specified do usual thing,,
  ./mining.pl --slot=12 --buffer=TF_OSPY /cdf/home/belforte/svt_data/svtspy/b0svt03_*.dat
 
  if --glob i.e shell glob fails, there is a fair chance that perl glob will succeed,
  ./mining.pl --glob --crate=b0svt03 --slot=12 --buffer=TF_OSPY 
                     --fdir=/cdf/home/belforte/svt_data/svtspy --outdir=./
  I have not fully tested the --glob option yet, though.
 
  The following parameters are saved in output files:
  - Hit word
       Hit Coordinate, Long Cluster, Barrel Id, Layer #
  - Road word
       Road Id, AMBoard+AMPlug+AMChip(0-511), Pattern, AMChip, AMPlug, AMBoard, Phi Sector
  - Road-Hit packet word
       # of Silicon Hit, # of Cot Hit, # of Long Clusters, Road Id, Phi Sector
  - Track word
       Phi, Z In, Z Out, Impact parameter(d0), Curvature, Transverse Momentum(pt), Road ID, 
       Phi Sector, Hit Coordinate in layer 0, Hit Coordinate in layer 1, Hit Coordinate in layer 2,
       Hit Coordinate in layer 3, Hit Coordinate in layer 4,
       Chi2 of fit, XTF track number, TF Status, Fit Quality, TF Error

  Note:
  Mergers are different in different crates and hence a problem. We must check the crate name
  when dealing with a merger board and act accordingly.

  S. Sarkar 04/28/2001
HEAD
exit 0;
}

# Compute track pt from curvature
sub pt ($) {
  my $curv = shift;
  my $pt;
  if (fabs $curv > 1e-10) {
    $pt = fabs(0.002112 / $curv);
  }
  else {
    $pt = 1e04;
  }
  return $pt;
}

# Extract hit parameters from data words and dump in a file specified
# as an input to the subroutine
sub unpack_hit_parameters ($$) {
  my ($events, $filename) = @_;
  my ($coord, $l_cluster, $barrel_id, $layer_no) = (0,0,0,0);
  my $format = "%6d%3d%3d%3d\n";
  my $result = "";
  my $hit_parm = {};

  my $nevt = @$events;
  printf "%s %d\n", "Number of events", $nevt if $verbose and $verbose_level > 1;

  open OUTPUT, ">$filename"  or die "Cannot create file $filename!";
  foreach my $event (@$events) {
    foreach my $word (@$event) {
      $coord     = $word & 0x3fff;
      $l_cluster = ($word >> 14) & 0x1;
      $barrel_id = ($word >> 15) & 0x7;
      $layer_no  = ($word >> 18) & 0x7;
 
      $hit_parm = {
         'Coordinate' => $coord,     'Long_Cluster'  => $l_cluster,
    	 'Barrel_Id'  => $barrel_id, 'Layer_Number'  => $layer_no
      };
      print Dumper $hit_parm if $verbose and $verbose_level > 3;
      $result = sprintf $format, $coord, $l_cluster, $barrel_id, $layer_no;
      print OUTPUT $result;
    }
  }
  close OUTPUT; 
}

# Extract road parameters from data words and dump in a file specified
# as an input to the subroutine
sub unpack_road_parameters ($$) {
  my ($events, $filename) = @_;
  my($road_id, $am_comb, $wedge, $am_board, $am_plug, $am_chip, $pattern);
  my $format = "%8d%6d%6d%3d%3d%2d%3d\n";
  my $result = "";
  my $road_parm = {};

  my $nevt = @$events;
  printf "%s %d\n", "Number of events", $nevt if $verbose and $verbose_level > 1;

  open OUTPUT, ">$filename" or die "Cannot create file $filename!";
  foreach my $event (@$events) {
    foreach my $word (@$event) {
      $road_id  = $word & 0xffff;
      $am_comb  = ($word >> 7) & 0x1ff;
      $pattern  = $road_id & 0x7f;
      $am_chip  = ($road_id >> 7) & 0x7;
      $am_plug  = ($road_id >> 10) & 0xf;
      $am_board = ($road_id >> 14) & 0x3;
      $wedge    = ($word >> 17) & 0xf;
      $road_parm = {
         'Road_Id' => $road_id,  
         'AMB+Plug+Chip' => $am_comb,
         'Pattern' => $pattern,
         'Chip'    => $am_chip,  
    	 'Plug'    => $am_plug,    
         'AMB'     => $am_board,
         'Wedge'   => $wedge
      };
      print Dumper $road_parm if $verbose and $verbose_level > 3;
      $result = sprintf $format, 
         $road_id, $am_comb, $pattern, $am_chip, $am_plug, $am_board, $wedge;
      print OUTPUT $result;
    }
  }
  close OUTPUT; 
}

# Extract road-hit packet parameters from data words and dump in a file specified
# as an input to the subroutine
sub unpack_packet_parameters ($$) {
  my ($events, $filename) = @_;
  my ($road_id, $wedge);
  my ($phi, $curv, $iso, $eta, $superstrip);
  my ($coord, $l_cluster, $barrel_id, $layer_no);
  my $format = "";
  my $result = "";
  $phi  = 0;
  $curv = 0;
  my $packet_parm = {};

  my $nevt = @$events;
  printf "%s %d\n", "Number of events", $nevt if $verbose and $verbose_level > 1;

  open OUTPUT, ">$filename" or die "Cannot create file $filename!";
  foreach my $event (@$events) {
    foreach my $packet (@$event) {
      my $nword = @$packet;
      next if $nword < 7;
      my $road = pop @$packet;
      $road_id = $road & 0xffff;
      $wedge   = ($road >> 17) & 0xf;
        
      my $silicon_hit    = 0;
      my $cot_hit        = 0;
      my $long_cluster   = 0;
      my %hits_per_layer = ();
      my @hits           = ();
      my @layers         = ();
      my $line_index     = 0;

      foreach my $word (@$packet) {
        my $layer = ($word >> 18) & 0x7;
        if ($layer < 5) {
          $hits_per_layer{$layer}++;
          printf "%6.6x %d %d\n", $word, $layer, $hits_per_layer{$layer} 
             if $verbose and $verbose_level > 2;
          if ($hits_per_layer{$layer} == 1) {
            $silicon_hit++;
            if ($hits_only == 1) {
              push @hits, $word & 0x3ffff;
              push @layers, $layer; 
            }
            elsif ($hits_only) {
              push @hits, $word & 0x1fffff;
            }
          }
          $long_cluster++ if (($word >> 14) & 0x1);
        }
        else {
          $line_index++;
          if ($line_index == 2) {
            $phi  = $word & 0xfff;
            $curv = ($word >> 12) & 0x7f;
            $cot_hit++;
            $line_index = 0;
          }
          last if $cot_hit > 1;
        }
      }
      $packet_parm = {
         '# of Silicon hits'  => $silicon_hit,  
         '# of COT hits'      => $cot_hit,
         '# of Long Clusters' => $long_cluster,
         'Road ID'            => $road_id,  
    	 'Phi Sector'         => $wedge
      };
      print Dumper $packet_parm if $verbose and $verbose_level > 3;
      if ($silicon_hit >= 4) {
        $format = "%4d%4d%4d%8d%3d\n";
        $result = 
          sprintf $format, $silicon_hit, $cot_hit, $long_cluster, $road_id, $wedge;
        print OUTPUT $result if not $hits_only;
        for (my $i = 0; $i < 4; $i++) {
          $hits[$i] = 0xffffff if not($hits[$i]);
        }
        if ($hits_only == 1) {
          $format = "%d %d %d %d %6.6x %6.6x %6.6x %6.6x\n";
          $result = sprintf $format, $layers[0], $layers[1], $layers[2], $layers[3],
                                     $hits[0],   $hits[1],   $hits[2],   $hits[3];
        }
        elsif ($hits_only == 2) {
          $format = "%6.6x %6.6x %6.6x %6.6x\n";
          $result = sprintf $format, $hits[0], $hits[1], $hits[2], $hits[3];
        }
        elsif ($hits_only) {
          $format = "%3.3x %2.2x\n";
          $result = sprintf $format, $phi, $curv;
        }
        print OUTPUT $result;
      }
    }
  }
  close OUTPUT; 
}

# Extract track parameters from data words and dump in a file specified
# as an input to the subroutine
sub unpack_track_parameters ($$) {
  my ($events, $filename) = @_;
  my ($phi, $zin, $zout, $curv, $pt, $d0, $wedge); 
  my ($road, $chi2, $xtf, $TFstatus, $fitQuality);
  my (@hit);
  my ($TFerror, $hit_ofl, $layer_ofl, $comb_ofl, $invalid_data, $fit_ofl, $FIFO_ofl);
  my ($err_OR);
  my $format = "%7.3f%2d%2d%9.5f%9.5f%10.3f%6d%3d%9.5f%9.5f".
               "%9.5f%9.5f%9.5f%7.1f%5d%6d%6d%6d\n";
  my $result = "";
  my $track_parm = {};

  my $nevt = @$events;
  printf "%s %d\n", "Number of events", $nevt if $verbose and $verbose_level > 1;

  open OUTPUT, ">$filename" or die "Cannot create file $filename!";
  foreach my $event (@$events) {
    foreach my $track (@$event) {
      next if @$track != 7;
      $phi  = ($track->[0] & 0x1fff)*(($M_PI*2)/$PHI_SCALE); 
      $zin  = $track->[0] >> 13 & 0x7; 
      $zout = $track->[0] >> 16 & 0x7; 
    	
      $curv = (($track->[1] >> 18 & 0x1) ? -1.0 : 1.0)
              *($track->[1] >> 10 & 0xff)*$CUR_SCALE;
      $pt   = pt($curv);
      $d0   = (($track->[1] >> 9 & 0x1) ? -1.0 : 1.0)
              *($track->[1] & 0x1ff)*$D0_SCALE;
    	
      # road-ID and phi sector: is a copy of the 15 LS bits of the ROAD 
      # word received from AMS
      $wedge = $track->[2] >> 17 & 0xf;
      $road  = $track->[2] & 0x7fff;
    	
      # hit positions :
      $hit[0] = ($track->[3] & 0xff)      /$HIT0_SCALE;
      $hit[1] = ($track->[3] >> 10 & 0xff)/$HIT1_SCALE;
      $hit[2] = ($track->[4] & 0xff)      /$HIT2_SCALE; 
      $hit[3] = ($track->[4] >> 10 & 0xff)/$HIT3_SCALE;
      $hit[4] = ($track->[5] & 0xff)      /$HIT4_SCALE; 
    	
      $chi2 = ($track->[5] >> 10 & 0x7ff)*$CHI_SCALE; 
      $xtf  = $track->[6] & 0x1ff;
    	
      # Unpack of Track Fitter Status Word:
      $TFstatus   = $track->[6] >> 9 & 0xfff;
      $fitQuality = $TFstatus & 0xf;     # bits 0-3 = fit quality code

      # bit 4 unused
      $TFerror = $TFstatus >> 5 & 0x7f;  # bits 5-11 = TF error word  

      # Unpack of TF Error Word:
      # bit 1: hit overflow, set if more than 7 hits in one SS
      $hit_ofl = $TFerror & 0x1; 

      # bit 2: layer overflow, set if too many layers with multiple hits
      $layer_ofl = $TFerror >> 1 & 0x1;

      # bit 3: combinations overflow, set if too many combinations for fit
      $comb_ofl = $TFerror >> 2 & 0x1;

      # bit 4: invalid data, set if hit out of order, not enough SVT hits
      $invalid_data = $TFerror >> 3 & 0x1;

      # bit 5: overflow in the fit, set if fit result in XFT part is overfl
      $fit_ofl = $TFerror >> 4 & 0x1;

      # bit 6: FIFO overflow 
      $FIFO_ofl = $TFerror >> 5 & 0x1;

      # bit 7: error summary, OR of above 6 bits after a mask 
      $err_OR = $TFerror >> 6 & 0x1;

      $track_parm = {
         'phi'     => $phi,      'zin'       => $zin,        'zout'     => $zout,
    	 'd0'      => $d0,       'curv'      => $curv,       'pt'       => $pt,
    	 'road'    => $road,     'wedge'     => $wedge,      'hit0'     => $hit[0],
    	 'hit1'    => $hit[1],   'hit2'      => $hit[2],     'hit3'     => $hit[3],
    	 'hit4'    => $hit[4],   'chi2'      => $chi2,       'xtf'      => $xtf,
    	 'status'  => $TFstatus, 'quality'   => $fitQuality, 'TFerror'  => $TFerror,
         'hit_ofl' => $hit_ofl,  'layer_ofl' => $layer_ofl,  'comb_ofl' => $comb_ofl,
         'invalid' => $invalid_data, 'fit_ofl' => $hit_ofl, 'FIFO_ofl' => $FIFO_ofl,
         'error_OR' => $err_OR
      };
      print Dumper $track_parm if $verbose and $verbose_level > 3;
      $result = sprintf $format, 
        $phi, $zin, $zout, $d0, $curv, $pt, $road, $wedge, $hit[0],
        $hit[1], $hit[2], $hit[3], $hit[4], $chi2, $xtf, $TFstatus, 
        $fitQuality, $TFerror;
      print OUTPUT $result;
    }
  }
  close OUTPUT; 
}
# Extract track parameters from data words and dump in a file specified
# as an input to the subroutine
sub unpack_xft_parameters ($$) {
  my ($events, $filename) = @_;
  my $format = "%06x %4d %6.3f %2d %3d %2d %3d %s %5.2f %2d %2d\n";
  my $result = "";
  my $xft_parm = {};
 
  my $nevt = @$events;
  printf "%s %d\n", "Number of events", $nevt if $verbose and $verbose_level > 1;

  open OUTPUT, ">>$filename" or die "Cannot append to file $filename!";
  foreach my $event (@$events) {
    foreach my $track (@$event) {
      my ($phi, $iso, $shrt, $wedge, $miniwedge, $minphi, $ptbin, $sgn);
      my ($pt, $phirad);
      my ($illegal, $junk);
      $junk    = 0;
      $illegal = 0;

      $phi       = $track & 0xfff;
      $phirad    = $phi/2304.*2*$M_PI;
      $miniwedge = $phi >> 3;
      $minphi    = $phi & 0x7;
      $wedge     = $miniwedge/24;
      $ptbin     = ($track >> 12) & 0x7f;
      $iso       = ($track >> 19) & 0x1;
      $shrt      = ($track >> 20) & 0x1;

      if (!$shrt) {
        if ($ptbin < 48) { 
          $sgn = '-'; 
          $pt  = $PtAtBin_l[$ptbin]; 
        }
        if ($ptbin > 47) { 
          $sgn = '+'; 
          $pt  = $PtAtBin_l[95 - $ptbin]; 
        }
        if ($ptbin > 95) { 
          $sgn  = '?'; 
          $pt = 0.0; 
          $illegal = 1;
        }
      }
      else {
        if ($ptbin < 48) { 
          $sgn = '-'; 
          $pt  = $PtAtBin_s[$ptbin - 32]; 
        }
        if ($ptbin > 47) { 
          $sgn = '+'; 
          $pt  = $PtAtBin_s[63 - $ptbin]; 
        }
        if ($ptbin < 32) { 
          $sgn = '?'; 
          $pt  = 0.0; 
          $illegal = 1;
        }
        if ($ptbin > 95) { 
          $sgn = '?'; 
          $pt  = 0.0; 
          $illegal = 1;
        }
      }
      $junk = 1 if (($wedge == 1) and ($ptbin == 0));
      $xft_parm = {
         'Word'      => $track,
         'Phi'       => $phi,  
         'Phirad'    => $phirad,
         'Wedge'     => $wedge,
         'miniwedge' => $miniwedge,
         'minphi'    => $minphi,
         'Pt Bin'    => $ptbin,
         'sign'      => $sgn,
         'Pt'        => $pt,
         'Isolation' => $iso,
         'Short'     => $shrt
      };

      print Dumper $xft_parm if $verbose and $verbose_level > 3;
      if (!$junk and !$illegal) {
        $result = sprintf $format, $track, $phi, $phirad, $wedge, $miniwedge, 
                          $minphi, $ptbin, $sgn, $pt, $iso, $shrt;
        print OUTPUT $result;
      }
    }
  }
  close OUTPUT; 
}
# Main starts here
sub _main () {
  # Extract command line options
  GetOptions 'verbose!' => \$verbose,
             'verbose_level=i' => \$verbose_level,
  	     'help!'    => \&usage,
  	     'glob!'    => \$glob,
  	     'crate=s'  => \$crate, 
  	     'slot=i'   => \$slot, 
  	     'buffer=s' => \$buffer,
  	     'fdir=s'   => \$fdir, 
  	     'ext=s'    => \$ext,
             'outdir=s' => \$outdir,
             'hits_only=i'   => \$hits_only,
             'single!'   => \$single;
    
  print join " ", "Arglist ->", $verbose, $help, $glob, $crate, $slot, $buffer, $fdir, "\n"
     if not $single;
    
  # Check if the Spy buffer name is valid
  if ((!grep /$buffer/, @hspy_list) and
      (!grep /$buffer/, @rspy_list) and
      (!grep /$buffer/, @pspy_list) and
      (!grep /$buffer/, @tspy_list)) {
     die "Sorry, spy buffer for $buffer does not exist";
  }
  
  # Also check if the slot number is valid
  my $valid_slot = 0; 
  foreach (@slots) {
    $valid_slot = $_ if ($_ == $slot);
  }
  die "Sorry, slot number $slot given at command line is not valid!"  if !$valid_slot;

  # Create File name list depending on option (glob or not)
  my @filelist;
    
  my $pretxt = $fdir."/".$crate;
  my $postxt = "\.".$ext;
  print join " ", $pretxt, $postxt, "\n" if $verbose and not $single;
    
  if ($glob) {
    print "Using file glob" if $verbose and not $single;
    @filelist = <$pretxt*$postxt>;
  } 
  else {
    @filelist = @ARGV;
  } 
  print join "\n", @filelist, "\n" if not $single;
    
  # Now open each input file in turn and do analysis 
  foreach my $filename (@filelist) {
    open INPUT, $filename 
      or warn "sorry, Could not open file $filename, continuing with next";
    
    # Construct output file name
    my @fields  =  split '\.', $filename;
    my $outfile = (split '/', $fields[0])[-1];  
    
    # locals
    my $EE_bit  = 0;
    my $ntracks = 0;
    my $true_event = 0;
    
    my $events_rec   = [];
    my $xft_rec = [];
    my $found_buffer = 0;
    my $go_on = 0;
    my $nword = 0;
    my $nevt  = 0;
    my $nhit  = 0;
    my $nroad = 0;
    my $npkt  = 0;
    my $ntrk  = 0;
    
    print "\n" if $verbose and $verbose_level and not $single;
    while (<INPUT>) {
      chomp;
      if ($_ =~ m/$buffer/) {
  	my (@fields) = split;
  	$go_on = 1 if $fields[2] == $slot;
  	$found_buffer = 1;
  	next;                      # Continue with next line
      }
      if ($go_on and $_ =~ m/SB/) {
  	$go_on = 0;
        last;                      # No need to read more from this file
      }
      if ($go_on) {
  	my @words = split;
  	shift @words;              # Remove DA from the array
  	@words = map hex, @words;  # Convert from string to hexadecimal
    
  	foreach my $word (@words) {
  	  if (! (($word >> 22) & 0x1) ) {           # not End Event word
            if ($verbose and $verbose_level) {
    	      printf "%6.6x ", $word;
              print "\n" if $single;
            }
  	    if (grep /$buffer/, @tspy_list) {
              if (!(($word >> 21) & 0x1)) {         # End of Packet
    	        printf "%s %d/%d/%d\n", $buffer, $nevt, $ntrk, $nword 
                  if $verbose and $verbose_level > 2;
  	        $events_rec->[$nevt]->[$ntrk]->[$nword] = $word;
  	        $nword++;
              }
  	      elsif ($nword == 6 or (($word >> 21) & 0x1)) {
  	        $events_rec->[$nevt]->[$ntrk]->[$nword] = $word;
  		print "\n" if $verbose and $verbose_level and not $single;
  		$ntrk++;
  		$nword = 0;
  	      }
              if ($buffer eq 'MRG_OUT_SPY') {
                if ($nword == 0 or (($word >> 21) & 0x1)) {
     	          printf "%s %6.6x %d %d\n", $buffer, $word, $nevt, $npkt 
                      if $verbose and $verbose_level > 2;
  	          $xft_rec->[$nevt]->[$npkt] = $word;
                  $npkt++;
                }
              }
  	    }
  	    elsif (grep /$buffer/, @hspy_list) {
  	      $events_rec->[$nevt]->[$nhit] = $word;
  	      $nhit++;
  	    }
  	    elsif (grep /$buffer/, @rspy_list) {
  	      $events_rec->[$nevt]->[$nroad] = $word;
  	      $nroad++;
  	    }
  	    elsif (grep /$buffer/, @pspy_list) {
  	      printf "%s %d/%d/%d\n", $buffer, $nevt, $npkt, $nword 
                 if $verbose and $verbose_level > 2;
  	      $events_rec->[$nevt]->[$npkt]->[$nword] = $word;
  	      $nword++;
  	      if (($word >> 21) & 0x1) {         # End of Packet
  		print "\n" if $verbose and $verbose_level and not $single;
  		$npkt++;
  		$nword = 0;
  	      }
  	    }
  	    $EE_bit = 0;
  	    $true_event = 1;
  	  }
  	  else {
  	    $EE_bit++;
  	    $nevt++ if $EE_bit == 1 and $true_event;
  	    $true_event = 0;
  	    if (grep /$buffer/, @tspy_list) {
  	      $nword = 0;
  	      $ntrk = 0;
              if ($buffer eq 'MRG_OUT_SPY') {
                $npkt = 0;
	      }
  	    }
  	    elsif (grep /$buffer/, @hspy_list) {
  	      $nhit = 0;
  	    }
  	    elsif (grep /$buffer/, @rspy_list) {
  	      $nroad = 0;
  	    }
  	    elsif (grep /$buffer/, @pspy_list) {
  	      $npkt = 0;
  	      $nword = 0;
  	    }
  	  }
  	}
      }
    }
    close INPUT;
    
    # Now extract relevant physics parameters
    my $nevent = @$events_rec;
    if ($found_buffer and $nevent) {
      $outfile = $outdir.$outfile."_".$slot."_".$buffer."\.data";
      print "Output file: ", $outfile, "\n" if not $single;
      print Dumper $events_rec if $verbose and $verbose_level > 3;
      if (grep /$buffer/, @tspy_list) {
  	unpack_track_parameters ($events_rec, $outfile);
        if ($buffer eq 'MRG_OUT_SPY') {
    	  unpack_xft_parameters ($xft_rec, $outfile);
        }
      }
      elsif (grep /$buffer/, @hspy_list) {
  	unpack_hit_parameters ($events_rec, $outfile);
      }
      elsif (grep /$buffer/, @rspy_list) {
  	unpack_road_parameters ($events_rec, $outfile);
      }
      elsif (grep /$buffer/, @pspy_list) {
  	unpack_packet_parameters ($events_rec, $outfile);
      }
    }
  }
}
# Call main explicitly
_main;
__END__
