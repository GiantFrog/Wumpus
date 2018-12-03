scream:- false.
wall(0, 0).

% true when one coordinate matches and the other is exactly +/-1 away.
% nextTo is asserted in Java during map generation for only the numbers we will use.
adjacent([X1,Y1], [X2,Y2]):- X1 = X2, nextTo(Y1, Y2).
adjacent([X1,Y1], [X2,Y2]):- Y1 = Y2, nextTo(X1, X2).

% X2,Y2 is both safe and adjacent to X1, Y1
safeAndAdj([X1,Y1], [X2,Y2]):- safe(X2, Y2), adjacent([X1,Y1], [X2,Y2]).

% we know a tile is safe if there's certainly no pit/wumpus or if we've been there without dying
safe(X, Y):- explored(X, Y).
safe(X, Y):- noPit(X, Y), scream.
safe(X, Y):- noPit(X, Y), noWumpus(X, Y).

% there is never a wumpus in a space if an adjacent space has been visited and wasn't stinky!
noWumpus(X, Y):- adjacent([X,Y], [A,B]), explored(A, B), \+stinky(A, B).
noPit(X, Y):- adjacent([X,Y], [A,B]), explored(A, B), \+drafty(A, B).

:- style_check(-singleton).	% we suppress the warning because we need the singleton A and B variables to keep the results in bounds
% an unexplored space is one that has coordinates within our map size, is not explored, and is not a wall.
unexplored(X, Y):- nextTo(X, A), nextTo(Y, B), \+explored(X, Y), \+wall(X, Y).