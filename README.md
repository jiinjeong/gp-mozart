# gp-mozart
* **Description**: Genetic programming system to generate Mozart-like melodies using PUSH and Klangmeister (Clojure). GP evolves programs over time to accomplish a desired task, in this case, producing better melodies. This program utilizes various note generation instructions, music stack manipulation instructions, repeat instructions, fitness functions (accuracy, accuracy-distance, Levenshtein), multi-objective optimization through tournament-based Pareto front, and developmental GP.
* **Date**: October - December 2020
* **Special Thanks**: Professor Helmuth (Hamilton, CS) who provided guidance.

## Usage

There are two ways to run the main PushGP function:

1. Load `core.clj` into the interpreter, and then run `(-main)`.
2. From the command line, run `lein run`.
