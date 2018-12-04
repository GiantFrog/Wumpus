scream:- false.
wall(0, 0).

% true when one coordinate matches and the other is exactly +/-1 away.
% nextTo is asserted in Java during map generation for only the numbers we will use.
adjacent([X1,Y1], [X2,Y2]):- X1 = X2, nextTo(Y1, Y2).
adjacent([X1,Y1], [X2,Y2]):- Y1 = Y2, nextTo(X1, X2).

% X2,Y2 is both safe and adjacent to X1, Y1
safeAndAdj([X1,Y1], [X2,Y2]):- safe(X2, Y2), adjacent([X1,Y1], [X2,Y2]).

% we know a tile is safe if there's certainly no pit/wumpus
safe(X, Y):- noPit(X, Y), scream.
safe(X, Y):- noPit(X, Y), noWumpus(X, Y).

% if we've explored a tile, we know there's certainly no pit and no wumpus
% there is never a wumpus in a space if an adjacent space has been visited and wasn't stinky!
noWumpus(X, Y):- explored(X, Y).
noWumpus(X, Y):- adjacent([X,Y], [A,B]), explored(A, B), \+stinky(A, B).
noPit(X, Y):- explored(X, Y).
noPit(X, Y):- adjacent([X,Y], [A,B]), explored(A, B), \+drafty(A, B).

% we also want to know if a room is certainly dangerous, so we don't guess it when out of safe rooms.
dangerous(X, Y):- wumpus(X, Y), \+scream.
dangerous(X, Y):- pit(X, Y).

% if it's drafty around three edges of a room, that room is a pit!
pit(X, Y):- adjacent([X,Y],[A,B]), adjacent([X,Y],[C,D]), adjacent([X,Y], [E,F]), drafty(A,B), drafty(C,D), drafty(E,F), [A,B] \== [C,D], [A,B] \== [E,F], [C,D] \== [E,F].

% if there are two stinky rooms next to a safe room, the other room they both border must be the wumpus!
wumpus(X, Y):- adjacent([X,Y],[A,B]), adjacent([X,Y],[C,D]), stinky(A,B), stinky(C,D), nextTo(A,C), nextTo(B,D), adjacent([A,B],[J,K]), adjacent([C,D],[J,K]), safe(J,K), [X,Y] \== [J,K].
% if two stinky rooms border the room in question and share exactly one coordinate, we found the wumpus!
wumpus(X, Y):- adjacent([X,Y],[A,B]), adjacent([X,Y],[C,D]), stinky(A,B), stinky(C,D), A == C, B \== D.
wumpus(X, Y):- adjacent([X,Y],[A,B]), adjacent([X,Y],[C,D]), stinky(A,B), stinky(C,D), A \== C, B == D.

:- style_check(-singleton).	% we suppress the warning because we need the singleton A and B variables to keep the results in bounds
% an unexplored space is one that has coordinates within our map size, is not explored, and is not a wall.
unexplored(X, Y):- nextTo(X, A), nextTo(Y, B), \+explored(X, Y), \+wall(X, Y).