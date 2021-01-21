;; Author : Jiin Jeong
;; Date   : October 14, 2020
;; Desc   : Basic GP implementation

(ns push307.core
  (:gen-class))

;;;;;;;;;;
;; Examples

; An example Push state
(def example-push-state
  {:exec '(integer_+ integer_-)
   :integer '(1 2 3 4 5 6 7)
   :string '("abc" "def")
   :input {:in1 4 :in2 6}})

; An example Plushy genome
(def example-plushy-genome
  '(3 5 integer_* exec_dup "hello" 4 "world" integer_- close))

; An example Push program
; This is the program tha would result from the above Plushy genome
(def example-push-program
  '(3 5 integer_* exec_dup ("hello" 4 "world" integer_-)))

; An example individual in the population
; Made of a map containing, at mimimum, a program, the errors for
; the program, and a total error
(def example-individual
  {:genome '(3 5 integer_* exec_dup "hello" 4 "world" integer_- close)
   :errors [8 7 6 5 4 3 2 1 0 1]
   :total-error 37})


;;;;;;;;;;
;; Instructions

; Must be either (1) functions that take one Push state and return another or (2) constant literals.
; The exception is `close`, which is only used in translation from Plushy to Push
(def default-instructions
  (list
   'in1
   'integer_+
   'integer_-
   'integer_*
   'integer_%
   'exec_dup
   'close
   0
   1))

; Number of code blocks opened by instructions (default = 0)
(def opened-blocks
  {'exec_dup 1})


;;;;;;;;;
;; Utilities

;; An empty Push state (for the purpose of this problem, only :exec and :integer stacks).
(def empty-push-state
  {:exec '()
   :integer '()})

; #1.
(defn push-to-stack
  "Pushes item onto stack in state, returning the resulting state."
  [state stack item]
  (assoc state stack (conj (get state stack) item)))

; #2.
(defn pop-stack
  "Removes top item of stack, returning the resulting state."
  [state stack]
  (assoc state stack (rest (get state stack))))

; #3.
(defn peek-stack
  "Returns top item on a stack. If stack is empty, returns :no-stack-item"
  [state stack]
  (if (empty? (get state stack))
    :no-stack-item
    (first (get state stack))))

; #4.
(defn empty-stack?
  "Returns true if the stack is empty in state."
  [state stack]
  (empty? (get state stack)))

(defn get-args-from-stacks
  "Takes a state and a list of stacks to take args from. If there are enough args
  on each of the desired stacks, returns a map of the form {:state :args}, where
  :state is the new state with args popped, and :args is a list of args from
  the stacks. If there aren't enough args on the stacks, returns :not-enough-args."
  [state stacks]
  (loop [state state
         stacks (reverse stacks)
         args '()]
    (if (empty? stacks)
      {:state state :args args}
      (let [stack (first stacks)]
        (if (empty-stack? state stack)
          :not-enough-args
          (recur (pop-stack state stack)
                 (rest stacks)
                 (conj args (peek-stack state stack))))))))

(defn make-push-instruction
  "A utility function for making Push instructions. Takes a state, the function
  to apply to the args, the stacks to take the args from, and the stack to return
  the result to. Applies the function to the args (taken from the stacks) and pushes
  the return value onto return-stack in the resulting state."
  [state function arg-stacks return-stack]
  (let [args-pop-result (get-args-from-stacks state arg-stacks)]
    (if (= args-pop-result :not-enough-args)
      state
      (let [result (apply function (:args args-pop-result))
            new-state (:state args-pop-result)]
        (push-to-stack new-state return-stack result)))))

;;;;;;;;;
;; Instructions

; Helper for #5.
(defn exec-w-in1
  "Returns an :exec stack with :in1 pushed"
  [state]
  (conj (:exec state) (:in1 (:input state))))

; #5.
(defn in1
  "Pushes the input labeled :in1 on the inputs map onto the :exec stack."
  [state]
  (assoc state :exec (exec-w-in1 state)))  ; Updates :exec stack.

(defn integer_+
  "Adds the top two integers and leaves result on the integer stack.
  If integer stack has fewer than two elements, noops."
  [state]
  (make-push-instruction state +' [:integer :integer] :integer))

; #6. 
(defn integer_-
  "Subtracts the top two integers and leaves result on the integer stack."
  [state]
  (make-push-instruction state -' [:integer :integer] :integer))

; #7.
(defn integer_*
  "Multiplies the top two integers and leaves result on the integer stack."
  [state]
  (make-push-instruction state *' [:integer :integer] :integer))

; Helper for #8.
(defn safe-div
  "Safely integer divides a number by checking if denominator is 0."
  [numer denom]
  (if (zero? denom)
    numer
    (quot numer denom)))

; #8.
(defn integer_%
  "Acts like integer division most of the time, but if the denominator is 0,
   it returns the numerator, to avoid divide-by-zero errors."
  [state]
  ; CITE : Prof. Helmuth
  ; DESC : Suggested making a helper fn for safe-div
  (make-push-instruction state safe-div [:integer :integer] :integer))

(defn exec_dup
  "Duplicates the exec stack."
  [state]
  (if (empty-stack? state :exec)
    state
    (push-to-stack state :exec (first (:exec state)))))


;;;;;;;;;
;; Interpreter

; Helper for #9.
(defn unpack-code-block
  "Unpacks the code block by pushing each item in code block to :exec stack."
  [push-state elem]
  (assoc push-state :exec (concat elem (:exec push-state))))

; #9.
(defn interpret-one-step
  "Helper function for interpret-push-program.
  Takes a Push state and executes the next instruction on the exec stack,
  or if the next element is a literal, pushes it onto the correct stack.
  Returns the new Push state."
  [push-state]
  ; CITE : Prof. Helmuth
  ; DESC : Suggested popping instruction first in case the instruction touches :exec stack
  (let [elem (peek-stack push-state :exec)
        new-state (pop-stack push-state :exec)]
    (cond (integer? elem) (push-to-stack new-state :integer elem)  ;; Integer literal
          (string? elem) (push-to-stack new-state :string elem)    ;; String literal
          (list? elem) (unpack-code-block new-state elem)          ;; Code block
          :else ((eval elem) new-state))))                         ;; Instruction

; Helper for #10.
(defn push-program-to-exec
  "Pushes program to :exec stack."
  [program start-state]
  (assoc start-state :exec program))

; #10.
(defn interpret-push-program
  "Runs the given program starting with the stacks in start-state. Continues
  until the exec stack is empty. Returns the state of the stacks after the
  program finishes executing or when it reaches the max number of interpreter steps."
  [program start-state]
  (loop [state (push-program-to-exec program start-state)
         max-recursion 1000]  ;; Can be adjusted depending on computation speed.
    (cond
      ;; Terminates when :exec stack is empty or max # of interpreter steps is reached.
      (empty-stack? state :exec) state
      (zero? max-recursion) state  
      ;; Handles instruction/literal on :exec stack one by one.
      :else (recur (interpret-one-step state)
                   (dec max-recursion)))))

;;;;;;;;;
;; Translation from Plushy genomes to Push programs

(defn translate-plushy-to-push
  "Returns the Push program expressed by the given plushy representation."
  [plushy]
  (let [opener? #(and (vector? %) (= (first %) 'open))] ;; [open <n>] marks opened-blocks
    (loop [push () ;; iteratively build the Push program from the plushy
           plushy (mapcat #(if-let [n (get opened-blocks %)] [% ['open n]] [%]) plushy)]
      (if (empty? plushy)       ;; maybe we're done?
        (if (some opener? push) ;; done with plushy, but unclosed open
          (recur push '(close)) ;; recur with one more close
          push)                 ;; otherwise, really done, return push
        (let [i (first plushy)]
          (if (= i 'close)
            (if (some opener? push) ;; process a close when there's an open
              (recur (let [post-open (reverse (take-while (comp not opener?)
                                                          (reverse push)))
                           open-index (- (count push) (count post-open) 1)
                           num-open (second (nth push open-index))
                           pre-open (take open-index push)]
                       (if (= 1 num-open)
                         (concat pre-open [post-open])
                         (concat pre-open [post-open ['open (dec num-open)]])))
                     (rest plushy))
              (recur push (rest plushy))) ;; unmatched close, ignore
            (recur (concat push [i]) (rest plushy)))))))) ;; anything else


;;;;;;;;;
;; GP

; #11.
(defn make-random-plushy-genome
  "Creates and returns a new plushy genome. Takes a list of instructions and
  a maximum initial Plushy genome size."
  [instructions max-initial-plushy-size]
  (loop [n (rand-int max-initial-plushy-size)
         genome '()]
    (if (neg? n)
      genome
      (recur (dec n)
             (conj genome (rand-nth instructions))))))

; #12.
(defn tournament-selection
  "Selects an individual from the population using a tournament. Returned 
  individual will be a parent in the next generation. Uses a fixed tournament size of 3."
  [population]
  (let [tournament-pool (take 3 (repeatedly #(rand-nth population)))]
    (apply min-key :total-error tournament-pool)))

; #13.
(defn crossover
  "Crosses over Plushy genomes (note: not individuals) using uniform crossover.
  If length of two genomes differ, crossovers until the size of the smaller genome.
  Returns child Plushy genome"
  [prog-a prog-b]
  (let [length (min (count prog-a) (count prog-b))
        rand-elem (rand-nth (list (first prog-a) (first prog-b)))]
    (take length (lazy-seq (cons rand-elem (crossover (rest prog-a) (rest prog-b)))))))

; #14.
(defn uniform-addition
  "Randomly adds new instructions before every instruction (and at the end of
  the Plushy genomes) with some probability (0.05). Returns child Plushy genome"
  [instructions prog]
  (loop [prog prog
         new-prog '()]
    ; Handles element addition to the front of the list.
    (cond (empty? prog) (if (<= (rand) 0.05)
                          (conj (reverse new-prog) (rand-nth instructions))
                          (reverse new-prog))
          ; 0.05 chance of being added after each element.
          (<= (rand) 0.05) (recur (rest prog)
                                  (conj new-prog (first prog) (rand-nth instructions)))
          ; Program remains the same.
          :else (recur (rest prog) 
                       (conj new-prog (first prog))))))

; #15.
(defn uniform-deletion
  "Randomly deletes instructions from Plushy genomes at some rate (0.05).
   Returns child Plushy genome."
  [prog]
  (random-sample 0.95 prog))

;; Reflects probabilities (50% crossover, 25% addition, 25% deletion).
(def genetic-operators
  (list
   'crossover
   'crossover
   'uniform-addition
   'uniform-deletion))

;; Stores num of parents each operator requires.
(def parent-numbers
  {'crossover 2
   'uniform-addition 1
   'uniform-deletion 1})

; Helper for #17.
(defn perform-operation
  "Performs operation based on given operator and parents.
   Returns genome for our child individual."
  [instructions operator parents]
  (cond
    (= operator 'crossover) (crossover (:genome (first parents)) (:genome (last parents)))
    (= operator 'uniform-addition) (uniform-addition instructions (:genome (first parents)))
    (= operator 'uniform-deletion) (uniform-deletion (:genome (first parents)))))

; #16.
(defn select-and-vary
  "Selects parent(s) from population and varies them, returning
  a child individual (note: not program). Chooses which genetic operator
  to use probabilistically. Gives 50% chance to crossover,
  25% to uniform-addition, and 25% to uniform-deletion."
  [instructions population]
  (let [operator (rand-nth genetic-operators)
        num-parents (operator parent-numbers)
        parents (take num-parents (repeatedly #(tournament-selection population)))]
    (hash-map :genome (perform-operation instructions operator parents))))

; Helper for #17.
(defn find-best-individual
  "Finds best individual from a population."
  [population]
  (apply min-key :total-error population))

; #17.
(defn report
  "Reports information on the population each generation, including
  best program, best program size, best total error, and best errors."
  [population generation]
  (let [best-individual (find-best-individual population)
        best-genome (:genome best-individual)
        best-program (translate-plushy-to-push best-genome)
        best-program-size (count best-genome)
        best-total-error (:total-error best-individual)
        best-errors (:errors best-individual)]
    (println "-------------------------------------------------------")
    (println "               Report for Generation " generation "               ")
    (println "-------------------------------------------------------")
    (println "Best genome: " best-genome)
    (println "Best PUSH program: " best-program)
    (println "Best program size: " best-program-size)
    (println "Best total error: " best-total-error)
    (println "Best errors: " best-errors)))

; Helper for #18.
(defn genomes-to-individuals
  "Converts genomes to individuals (hash-map format)."
  [genomes]
  (map #(hash-map :genome %) genomes))

; Helper for #18.
(defn initialize-population
  "Initializes the population of a given size."
  [instructions population-size max-initial-plushy-size]
  (genomes-to-individuals
   (take population-size
         (repeatedly #(make-random-plushy-genome instructions max-initial-plushy-size)))))

; Helper for #18.
(defn generate-new-population
  "Generates a new population of a given size, selecting and varying the previous population."
  [instructions population-size population]
  (take population-size (repeatedly #(select-and-vary instructions population))))

; Helper for #18.
(defn run-error-function
  "Runs the error function on all indviduals in population and
   maps the list-error and errors to them."
  [population error-function]
  (map #(error-function %) population))

; Helper for #18.
(defn has-solution?
  "Checks if there is an individual with 0 error in the population."
  [population]
  (zero? (:total-error (find-best-individual population))))

; #18.
(defn push-gp
  "Main GP loop. Initializes the population, and then repeatedly
  generates and evaluates new populations. Stops if it finds an
  individual with 0 error (and should return :SUCCESS, or if it
  exceeds the maximum generations (and should return nil).
  Should print report each generation."
  [{:keys [population-size max-generations error-function instructions max-initial-plushy-size]
    :as argmap}]
  (loop
   [n 0
    population (initialize-population instructions population-size max-initial-plushy-size)]  ;; Initialize population
    (let [population-w-errors (run-error-function population error-function)]
      (report population-w-errors n)  ;; Prints report
      (cond
        (has-solution? population-w-errors) :SUCCESS  ;; Finds solution in population
        (= n max-generations) nil  ;; Exceeds max generation
        :else (recur (inc n)
                     (generate-new-population instructions population-size population-w-errors))))))  ;; Generates new population


;;;;;;;;;;
;; The error function
;; Problem: f(x) = x^3 + x + 3

; #19.
(defn target-function
  "Computes the target function: f(x) = x^3 + x + 3."
  [x]
  (+ (* x x x) x 3))

;; Example training cases.
(def example-tests
  '(-50 -30 -20 -12 -10 -5 0 1 2 3 4 5 8 10 20 26 29 32 100 111))

; Helper for #20.
(defn push-input-to-state
  "Pushes input to in1 in the input stack of the state."
  [state input]
  (let [input-stack (:input state)
        new-input-stack (assoc input-stack :in1 input)]
    (assoc state :input new-input-stack)))

; Helper for #20.
(defn return-individual-w-error
  "Returns individual with list-error and total-errors."
  [individual list-errors total-error]
  (assoc individual :errors list-errors
         :total-error total-error))

; Helper for #20.
(defn absolute-value
  "Absolute val function that accounts for big ints."
  [num]
  (if (neg? num)
    (-' num)
    num))

; Helper for #20.
(defn compute-error
  "Computes the error by comparing program output to correct output.
   If no number remains on the stack, error is penalized as 1 billion."
  [correct-output program-output]
  ; CITE : Prof. Helmuth
  ; DESC : Suggested making the penalty big so that the program is not
  ; evolutionally pressured to empty itself for a smaller penalty value.
  (if (= program-output :no-stack-item)
    1000000000
    (absolute-value (- program-output correct-output))))

; #20.
(defn regression-error-function
  "Takes an individual and evaluates it on some test cases.
  For each test case, runs program with the input set to :in1 in the :input map.
  The output is the integer on top of the integer stack in the Push state
  returned by the interpreter. Computes each error and returns individual with errors."
  [individual]
  (loop [tests example-tests
         list-errors []
         total-error 0]
    (if (empty? tests)
      (return-individual-w-error individual list-errors total-error)
      (let [correct-output (target-function (first tests))
            test-state (push-input-to-state empty-push-state (first tests))
            program-state (interpret-push-program (translate-plushy-to-push (:genome individual)) test-state)
            program-output (peek-stack program-state :integer)
            error (compute-error correct-output program-output)]
        (recur (rest tests)
               (conj list-errors error)
               (+' total-error error))))))


;;;;;;;;;;
;; The main function call
;; Can call this in a REPL, or from the command line with "lein run"

(defn -main
  "Runs push-gp, giving it a map of arguments."
  [& args]
  (binding [*ns* (the-ns 'push307.core)]
    ; The above line is necessary to allow `lein run` to work
    (push-gp {:instructions default-instructions
              :error-function regression-error-function
              :max-generations 500
              :population-size 200
              :max-initial-plushy-size 50})))


;;;;;;;;;;
;; Testing

(comment
  (push-gp {:instructions default-instructions
            :error-function regression-error-function
            :max-generations 500
            :population-size 200
            :max-initial-plushy-size 50}))
