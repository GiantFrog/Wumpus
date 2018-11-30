nextTo(0, 1).
nextTo(1, 2).
nextTo(2, 3).
nextTo(3, 4).
nextTo(4, 5).
nextTo(5, 4).
nextTo(4, 3).
nextTo(3, 2).
nextTo(2, 1).
nextTo(1, 0).
% true when one coordinate matches and the other is exactly one away.
adjacent([X1, Y1], [X2, Y2]):- X1 = X2, nextTo(Y1, Y2).
adjacent([X1, Y1], [X2, Y2]):- Y1 = Y2, nextTo(X1, X2).
% example fully explored test map
wall(0,1).
wall(0,2).
wall(0,3).
wall(0,4).
wall(1,0).
wall(1,5).
wall(2,0).
wall(2,5).
wall(3,0).
wall(3,5).
wall(4,0).
wall(4,5).
wall(5,1).
wall(5,2).
wall(5,3).
wall(5,4).
stinky(3,3).
stinky(3,4).
stinky(3,2).
stinky(2,3).
stinky(4,3).
drafty(1,1).
drafty(3,1).
drafty(2,2).

explored(1, 1).	% we always start at (1, 1).
% we know a tile is safe if there's certainly no pit/wumpus or if we've been there without dying
safe(X, Y):- explored(X, Y).
safe(X, Y):- noPit(X, Y), noWumpus(X, Y).
% there is never a wumpus in a space if an adjacent space has been visited and wasn't stinky!
noWumpus(X, Y):- adjacent([X,Y], [A,B]), explored(A, B), \+stinky(A, B).
noPit(X, Y):- adjacent([X,Y], [A,B]), explored(A, B), \+drafty(A, B).

% wumpus(X, Y):- stinky(Xm, Y), stinky(Xp, Y), Xm is X-1, Xp is X+1.
% wumpus(X, Y):- stinky(X, Ym), stinky(X, Yp), Ym is Y-1, Yp is Y+1.
% wumpus(X, Y):- \+stinky(Xm, Ym), stinky(Xm, Y), stinky(X, Ym), Xm is X-1, Ym is Y-1.
