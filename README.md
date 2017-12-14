## Global Minimum

---

### Introduction

We implement a Sudoku solver using (Akka) actors.
None of the components in this implementation maintains
mutable state. A such we are able to implement a
solution that uses concurrency and that can exploit
compute resources (multi-cpu/core/thread) when available.

The core idea is that a sudoku is composed of rows,
columns and blocks that can be mapped independently
to individual actors. Hence the core solver consists
of a main actor and 27 "detail" actors.

After sending an initial update that contains the "known"
cells to the main actor, these changes are sent to the
appropriate (9) row detail actors. These apply an algorithm
to calculate the consequences of the received update. Next
the changes, if any, are sent back to the main actor who
then sends these changes to the column and block detail actors.

These actor repeat the process leading to more updates being
sent, etc...

Ultimatly, the system converges to a stable state which is the
sudoku solution, unless the original start state is underspecified.

In a second step, an actor (SudokuProgressTracker) is added to
detect the reaching of the stable state followed by the collection
of the result from the row detail actors (as this is the more
straightforward way to get it). After collecting this information,
the full result is sent back to the main actor who will forward
it the the original requestor.

Todo:
  - Add monitoring to analyse algorith behaviour
  - Integrate the sudoku solver actor into Akka Streams so
    that flow control (backpressure) is added
  
