# gp-mozart
* **Description**: Genetic programming system to generate Mozart-like melodies using PUSH and Klangmeister (Clojure). GP evolves programs over time to accomplish a desired task, in this case, producing better melodies. This program utilizes various note generation instructions, music stack manipulation instructions, repeat instructions, fitness functions (accuracy, accuracy-distance, Levenshtein), multi-objective optimization through tournament-based Pareto front, and developmental GP. (My first GP system found programs to solve a simple function.). To listen to the melodies generated, you can visit [Klangmeister](http://ctford.github.io/klangmeister/), a browser-based music live coding environment in Clojure. An example generated melody is `(->> (phrase [1/2 1/8 1/8 1 1/2 1/3 1/6 1/3 1/2 1 1/3 1 1 1 1/4 1/3 1/16 4][76 74 72 75 76 81 75 75 77 77 77 76 74 74 75 75 64 75]))` (50 generations, 500 population).

* **Date**: October - December 2020
* **Special Thanks**: Professor Helmuth (Hamilton, CS) who provided starter code for a basic GP system and guidance.

## Usage

There are two ways to run the main PushGP function:

1. Load `core.clj` into the interpreter, and then run `(-main)`.
2. From the command line, run `lein run`.
