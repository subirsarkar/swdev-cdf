#!/usr/bin/perl -w
#
# Declaration of globals
use vars qw/$CHI_SCALE $REF_TRACK_WORDS/;
use vars qw/$verbose $help $half $chi2_max/;

use strict;
use diagnostics;
use POSIX qw(:math_h);
use Data::Dumper;
use Getopt::Long;

# Forward Declaration of subroutines
sub main();
sub usage();
sub dump_hit_words($$$$$);

# Define constants  
$CHI_SCALE = 1.;
$REF_TRACK_WORDS = 7;

# Command line options with Getopt::Long
$verbose = 0;
$help = '';
$half = 0;
$chi2_max = 100.;

sub usage () {
  print <<HEAD;

  Extract hit+track input words from HB_HIT_SPY and TF_OSPY words for events 
  with good tracks. This list can eventually be used to run SVT with fake tracks. 
  The input can also be used in SVT simulation.

  The command line options are  
 
  --verbose  (default 0)
  --help     show help on this tool and quit (default --nohelp)
  --half     first(0) or second(1) half of crate (default 0)
  --chi2     Max chi2 value allowed for a track (default 100.)

  Example usage:
 
  ./create_data.pl --help
  ./create_data.pl --half=0 data_dir/b0svt03_*.dat
  ./create_data.pl --half=0 --verbose=1 data_dir/b0svt03_*.dat
  ./create_data.pl --half=0 --verbose=3 data_dir/b0svt03_*.dat
 
  S. Sarkar,  Last updated: 18/02/2002
HEAD
exit 0;
}

# Main starts here
sub _main () {
  # Extract command line options
  GetOptions 'verbose=i' => \$verbose,
             'help!'     => \&usage,
             'half=i'    => \$half,
             'chi2=f'    => \$chi2_max;
    
  print join " ", "Arglist -> \n\$verbose = ", $verbose, 
                  "\n\$help = ", ($help) ? $help : 0, 
                  "\n\$half = ", $half, 
                  "\n\$chi2_max = ", $chi2_max, "\n";
    
  exit(1) if not scalar @ARGV;

  my $tf_slot = ($half) ? 21 : 12;
  my $hb_slot = ($half) ? 20 : 11;

  printf "HB Slot: %d, TF Slot: %d\n", $hb_slot, $tf_slot;

  my $ntot_matched = 0;

  # Create File name list
  my @filelist = @ARGV;
  my $nfiles = @filelist;
  printf "Number of files = %d\n", $nfiles;

  print join "\n", @filelist, "\n" if ($verbose & 0x1);
    
  my $count = 0;

  my $hbfile  = "hit_word_$count.dat.test";
  open HBOUT, ">$hbfile" or die "Cannot create file $hbfile!"; 

  my $tffile  = "tf_word_$count.dat.test";
  open TFOUT, ">$tffile" or die "Cannot create file $tffile!"; 

  # Now open each input file in turn and do analysis 
  foreach my $filename (@filelist) {
    open INPUT, $filename 
      or warn "Could not open file $filename, continuing with the next";
    
    # locals
    my ($EE_bit, $true_event) = (0, 0);
    
    my $track_rec = [];
    my $hit_rec   = [];

    my $hb_events_rec = [];
    my $tf_events_rec = [];
    my ($nevt, $nevt_tf, $nevt_hb) = (0,0,0);
    my ($found_buffer, $more_data, $nword, $nhit, $ntrk) = (0, 0, 0, 0, 0);
    
    my ($hb_count, $tf_count) = (0, 0);
    while (<INPUT>) {
      chomp;
      if ($_ =~ m/HB_HIT_SPY/o or $_ =~ m/TF_OSPY/o) {
        my (@fields) = split;
        next if ($fields[2] != $hb_slot and $fields[2] != $tf_slot);
        print $_, "\n" if $verbose;
        $found_buffer = ($fields[2] == $hb_slot) ? 1 : 2;
        $more_data = 1;
        next;                      # Continue with next line
      }
      if ($more_data and $_ =~ m/SB/o) {
        $more_data = 0;
        last if not $more_data and $found_buffer == 2;
      }
      if ($more_data) {
        my @words = split;
        shift @words;              # Remove DA from the array
        @words = map hex, @words;  # Convert from string to hexadecimal
    
        foreach my $word (@words) {
          printf "%6.6x\n", $word if ($verbose >> 1 & 0x1);
          if (($word & 0x600000) != 0x600000) {           # NOT End Event word

            if ($found_buffer == 2) {
              $track_rec->[$ntrk]->[$nword] = $word; 
              $nword++;
              if ($nword == $REF_TRACK_WORDS) {
                $nword = 0;
                $ntrk++;
              }
            }
            else {
              $hit_rec->[$nhit] = $word; 
              $nhit++;
            }
            $EE_bit = 0;
            $true_event = 1;
          }
          else {
            if ($found_buffer == 2) {
              $tf_count++;
              my $ngtrk = 0;
              foreach my $track (@$track_rec) {
                next if @$track != $REF_TRACK_WORDS;
                my $chi2 = ($track->[5] >> 10 & 0x7ff)*$CHI_SCALE; 
                printf "chi2 = %5.1f, chi2_max = %7.1f\n", 
                        $chi2, $chi2_max if ($verbose >> 3 & 0x1);
                $ngtrk++ if $chi2 < $chi2_max;
              }
              printf "\$ngtrk = %d\n", $ngtrk if ($verbose >> 3 & 0x1);
              if ($ngtrk) {
                # A single TF Event
                my $tf_event = {
                                  'TrackList' => $track_rec, 
                                  'EE'        => $word, 
                                  'Index'     => $tf_count
                               };
                # Dump the event
                print Dumper $tf_event if ($verbose >> 4 & 0x1);

                # Add to the event list
                $tf_events_rec->[$nevt_tf++] = $tf_event;
              }
              # Reset
              $nword     = 0;
              $ntrk      = 0;
              $track_rec = [];   # is it correct?
            }
            else {
              $hb_count++;
              my $len = @$hit_rec;
              if ($len) {
                # A single HB Event
                my $hb_event = {
                                 'HitList' => $hit_rec, 
                                 'EE'      => $word, 
                                 'Index'   => $hb_count
                               };
                # Dump the event
                print Dumper $hb_event if ($verbose >> 4 & 0x1);

                # Add to the event list
                $hb_events_rec->[$nevt_hb++] = $hb_event;
              }
              # Reset
              $nhit    = 0;
              $hit_rec = [];  # is it correct?
            }
            $EE_bit++;
            $nevt++ if $EE_bit == 1 and $true_event;
            $true_event = 0;
          }
        }
      }
    }
    close INPUT;

    # Dump both the event lists
    if ($verbose >> 5 & 0x1) {
      print Dumper @$hb_events_rec;
      print Dumper @$tf_events_rec;
    }
  
    # Now extract relevant words from HB_HIT_SPY
    # First find events with tracks with chi2 < chi2_max
    # Then find the same event (matching Bunch Counter) in the HB_HIT_SPY words
    # and dump all the hit words in a file, one word per line.
    # As and when the file overflows 60000 words open a new file.
    my $ntot = -1; 
    my $nevent = @$tf_events_rec;
    $ntot = dump_hit_words($hb_events_rec, $tf_events_rec, $hb_count, $tf_count, $filename) if ($nevent);
    $ntot_matched += $ntot if ($ntot > 0);
  }
  printf "Total events = %d\n", $ntot_matched;

  close HBOUT;
  close TFOUT;
}

sub dump_hit_words($$$$$) {
  my($hb_events, $tf_events, $ntot_hb, $ntot_tf, $filename) = @_;

  # Reverse the array so that most recent events are found first
  @$hb_events = reverse @$hb_events;
  @$tf_events = reverse @$tf_events;

  # Number of valid events
  my $n_hb_evt = @$hb_events;
  my $n_tf_evt = @$tf_events;

  if ($verbose) {
    # Print the number of events
    printf "Total no of  HB events = %d, TF events = %d\n", $ntot_hb, $ntot_tf;
    printf "No of valid  HB events = %d, TF events = %d\n", $n_hb_evt, $n_tf_evt;
  

    # Print the EE words and the index of the corresponding events in the event pool
    print "TF EE Words\n";
    map {printf "%x %d\n", $_->{EE}, $ntot_tf - $_->{Index}} @$tf_events;

    print "HB EE Words\n";
    map {printf "%x %d\n", $_->{EE}, $ntot_hb - $_->{Index}} @$hb_events;
  }

  # Do not proceed any further if the first valid TF event is found 
  # deeper than the total number of HB events
  my $index = (@$tf_events)[0]->{Index};
  printf "Index = %d\n", $index if $verbose;
  return -1 if ($ntot_tf - $index > $n_hb_evt);

  # Here we are. Find uniquely matched TF and HB events 
  # based on bunch counters and if the match occurs 
  # dump the HB/TF words alongwith the EE word
  my $result;
  my $unique_match = (); # Reference to a hash
  my $n_matched = 0;

  for my $tf_event (@$tf_events) {
    for my $hb_event (@$hb_events) {
      if (($tf_event->{EE} & 0xff) == ($hb_event->{EE} & 0xff)) {
        last if ( abs( ($ntot_hb - $hb_event->{Index}) 
                     - ($ntot_tf - $tf_event->{Index}) ) > 1);
        printf "%d\n", $hb_event->{EE} & 0xff if ($verbose >> 3 & 0x1);
        last if ++$unique_match->{$hb_event->{EE} & 0xff} > 1;
        $n_matched++;

        # Dump matched HB Input words
        my $hit_rec = $hb_event->{HitList};
        for my $hit (@$hit_rec) {
          $result = sprintf "%6.6x\n", $hit;  
          print HBOUT $result; 
        }
        $result = sprintf "%6.6x\n", $hb_event->{EE};
        print HBOUT $result; 

        # Do the same for TF Output
        my $track_rec = $tf_event->{TrackList};
        for my $track (@$track_rec) {
          next if @$track != $REF_TRACK_WORDS;
          my $chi2 = ($track->[5] >> 10 & 0x7ff)*$CHI_SCALE;  
          next if $chi2 > $chi2_max; 
          for my $word (@$track) {
            $result = sprintf "%6.6x\n", $word;  
            print TFOUT $result; 
          }
        }
        $result = sprintf "%6.6x\n", $tf_event->{EE};
        print TFOUT $result; 
      }
    }
  }
  # Debug
  if ($verbose) {
    for my $key (sort keys %$unique_match) {
      printf "%2.2x = %d\n", $key, $unique_match->{$key};
    }
  }
  printf "file: %s, No of matched events = %d\n", $filename, $n_matched;

  return $n_matched;
}

# Call main explicitly
_main;
__END__
