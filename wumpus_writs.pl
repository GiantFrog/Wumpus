scream:- false.

% true when one coordinate matches and the other is exactly +/-1 away.
% nextTo is asserted in Java during map generation for only the numbers we will use.
adjacent([X1, Y1], [X2, Y2]):- X1 = X2, nextTo(Y1, Y2).
adjacent([X1, Y1], [X2, Y2]):- Y1 = Y2, nextTo(X1, X2).

% both safe and adjacent, as you might expect
safeAndAdj([X1, Y1], [X2, Y2]):- safe(X2, Y2), adjacent([X1, Y1], [X2, Y2]).

% we know a tile is safe if there's certainly no pit/wumpus or if we've been there without dying
safe(X, Y):- explored(X, Y).
safe(X, Y):- noPit(X, Y), scream.
safe(X, Y):- noPit(X, Y), noWumpus(X, Y).

% there is never a wumpus in a space if an adjacent space has been visited and wasn't stinky!
noWumpus(X, Y):- adjacent([X,Y], [A,B]), explored(A, B), \+stinky(A, B).
noPit(X, Y):- adjacent([X,Y], [A,B]), explored(A, B), \+drafty(A, B).
