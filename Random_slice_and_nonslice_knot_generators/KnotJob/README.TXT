To run KnotJob with the GUI from the command line, go to the KnotJob
folder and type the following:

java -jar KnotJob.jar

or

java -Xmx16g -jar KnotJob.jar

to use more memory.

To run KnotJob without the GUI, you need to add arguments which are
either files containing link diagrams, or commands that start with -

Allowed files end in .kjb (for KnotJob files), .kts (for SKnotJob 
files), .adt (for alphabetical DT-code files), *.dtc (for knotscape 
files), or *.txt (for planar diagram files).

You can add several such files, and if no file is selected you can
enter a planar diagram into the command line.

Allowed commands are

-s0  to calculate the s-invariant over the field of characteristic
     0. You can replace 0 by any prime number p less than 212.
-sqe to calculate the even Sq^1 refinement of s mod 2.
-sqo to calculate the odd Sq^1 refinement of s mod 2.
-kb0 to calculate both reduced and unreduced even Khovanov 
     cohomology. Here 0 means integral coefficients. You can also
     calculate rational coefficients with 1, or modular 
     coefficients over Z/qZ, where q is a prime power such that
     the corresponding prime is less than 212, and q^2 is less
     than 2^31.
-kr0 as -kb0, but only the reduced Khovanov cohomology will be 
     calculated. 
-ku0 as -kb0, but only the unreduced Khovanov cohomology will be
     calculated.
-ko0 as -kr0, but the reduced odd Khovanov homology will be
     calculated.
-ns  to prevent printout in the terminal.
-nf  to prevent writing into a file.
-h   to print out this file in the terminal.

Without the -nf command information on the calculations will be 
written into a file with the same name as the file of link 
diagrams, but where the extension contains information on the
various calculations that have been done.

For example,

java -jar KnotJob.jar Rolfsen.kjb -s0 -s2 -sqo -ku1

will calculate the s-invariants with rational and Z/2Z
coefficients, along with the odd Sq^1 refinement and rational
unreduced even Khovanov cohomology, for every knot in the file
Rolfsen.kjb.