# Android-Threads-Gopher-Catcher
game of gopher hunting as an Android app. Imagine you are in a field occupied by a gopher. The gopher could be hiding in one of any number of holes in the field’s ground. The goal of the game is to find the hole that contains the gopher. (The game does not say what to do with the gopher, once it is found.) The game is played by two worker threads contained in your app, with the threads playing against each other. There are exactly 100 holes in the field; the holes are arranged as a 10×10 matrix and equally spaced with respect to each other. The threads take turns at guessing the hole containing the gopher; the first thread to find gopher wins the game.