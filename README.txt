README:

The ID space = 1000 (0 - 999)

1. javac *.java on any of the servers
2. run java BootStrap on glados.cs.rit.edu
3. run java Chord on any of the other running monitors eg. comet.cs.rit.edu
4. 'join' in order to join the Chord ID space
5. 'view' will display the node's ID, successor's name, predecessor's name, upper bound, lower bound and the files present
6. 'insert <file name>' will insert the file at its appropriate location 
This will also show the path traced. This will take the optimal path.
7. 'search <file name>' will search for the given file name
This will also show the path traced. This will take the optimal path.
8. 'leave' will make the node leave the ID space and will redistribute any remaining files.
9. All nodes can leave the ID Space and can join back via the BootStrap server running on glados.cs.rit.edu