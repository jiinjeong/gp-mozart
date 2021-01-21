;; Author : Jiin Jeong
;; Date   : November 18, 2020
;; Desc   : GP-Mozart

(ns push307.core
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:gen-class))

;;;;;;;;;
;; PART I. Music stack and representation

; An empty music note
(def empty-music-note
  {:pitch 0
   :duration 0})

; An example music note (goes in the melody buffer)
(def example-music-note
  {:pitch 72
   :duration 1/1})

; An empty music Push state
(def empty-music-push-state
  {:exec '()
   :integer '()
   :notes-alph '()
   :notes-pitch '()
   :notes-duration '()
   :octave '()
   :bool '()
   ; Developmental GP (This is our embryo)
   :melody-buffer '({})
   })

; An example music Push state
(def example-music-push-state
  {:exec '('integer_+)
   :integer '(4 2 1 2 4)
   :notes-alph '("c" "c" "g")
   :notes-pitch '({:pitch 72} {:pitch 72} {:pitch 79})
   :notes-duration '({:duration 1/1} {:duration 1/1} {:duration 1/1})
   :octave '(5.0 5.0 5.0)
   :bool '(true false true)
   :melody-buffer '({:pitch 72 :duration 1/1} {:pitch 78 :duration 2/3})})


;;;;;;;;;
;; PART II. Objectives (Mozart songs)

; C major
; https://www.youtube.com/watch?v=9bK9h12Qdvs&ab_channel=GabrieleTomasello
(def melody-twinkle
  '({:pitch 72, :duration 1}  ; 1
    {:pitch 72, :duration 1}
    {:pitch 79, :duration 1}
    {:pitch 79, :duration 1}
    {:pitch 81, :duration 1}  ; 2
    {:pitch 81, :duration 1}
    {:pitch 79, :duration 2}
    {:pitch 77, :duration 1}  ; 3
    {:pitch 77, :duration 1}
    {:pitch 76, :duration 1}
    {:pitch 76, :duration 1}
    {:pitch 74, :duration 1}  ; 4
    {:pitch 74, :duration 1}
    {:pitch 72, :duration 2}))

; G major
; https://www.youtube.com/watch?v=o1FSN8_pp_o&ab_channel=GabrieleTomaselloGabrieleTomasello
(def melody-eine
  '({:pitch 79, :duration 1}  ; 1
    {:pitch 0, :duration 1/2}
    {:pitch 74, :duration 1/2}
    {:pitch 79, :duration 1}
    {:pitch 0, :duration 1/2}
    {:pitch 74, :duration 1/2}  ; 2
    {:pitch 79, :duration 1/2}
    {:pitch 74, :duration 1/2}
    {:pitch 79, :duration 1/2}
    {:pitch 83, :duration 1/2}
    {:pitch 86, :duration 2}  ; 3, 4
    {:pitch 84, :duration 1}  ; 5
    {:pitch 0, :duration 1/2}
    {:pitch 81, :duration 1/2}
    {:pitch 84, :duration 1}
    {:pitch 0, :duration 1/2}
    {:pitch 81, :duration 1/2}
    {:pitch 84, :duration 1/2} ; 6
    {:pitch 81, :duration 1/2}
    {:pitch 78, :duration 1/2}
    {:pitch 81, :duration 1/2}
    {:pitch 74, :duration 2})) ; 7, 8

; A minor
; https://www.youtube.com/watch?v=s71I_EWJk7I&ab_channel=Steinway%26Sons
(def melody-fur-elise
  '({:pitch 76, :duration 1/4}  ; 1
    {:pitch 75, :duration 1/4}
    {:pitch 76, :duration 1/4}
    {:pitch 75, :duration 1/4}
    {:pitch 76, :duration 1/4}  ; 2
    {:pitch 71, :duration 1/4}
    {:pitch 74, :duration 1/4}
    {:pitch 72, :duration 1/4}
    {:pitch 69, :duration 1}    ; 3
    {:pitch 0, :duration 1/4}   ; 4
    {:pitch 60, :duration 1/4}
    {:pitch 64, :duration 1/4}
    {:pitch 69, :duration 1/4}
    {:pitch 71, :duration 1}    ; 5
    {:pitch 0, :duration 1/4}   ; 6
    {:pitch 64, :duration 1/4}
    {:pitch 68, :duration 1/4}
    {:pitch 71, :duration 1/4}
    {:pitch 72, :duration 2}))  ; 7, 8

; D major
; https://www.youtube.com/watch?v=8OZCyp-LcGw&ab_channel=wmd10
(def melody-figaro
  '({:pitch 74, :duration 1/4}  ; 1
    {:pitch 73, :duration 1/4}
    {:pitch 74, :duration 1/4}
    {:pitch 73, :duration 1/4}
    {:pitch 74, :duration 1}    ; 2
    {:pitch 74, :duration 1/4}  ; 3
    {:pitch 73, :duration 1/4}
    {:pitch 74, :duration 1/4}
    {:pitch 76, :duration 1/4}
    {:pitch 78, :duration 1/4}  ; 4
    {:pitch 76, :duration 1/4}
    {:pitch 78, :duration 1/4}
    {:pitch 79, :duration 1/4}
    {:pitch 81, :duration 1/4}  ; 5
    {:pitch 80, :duration 1/4}
    {:pitch 81, :duration 1/4}
    {:pitch 80, :duration 1/4}
    {:pitch 81, :duration 1}))  ; 6

; A major
; https://www.youtube.com/watch?v=HMjQygwPI1c&ab_channel=AlexTar
(def melody-turkish
  '({:pitch 71, :duration 1/8}   ; 1
    {:pitch 69, :duration 1/8}
    {:pitch 68, :duration 1/8}
    {:pitch 69, :duration 1/8}
    {:pitch 72, :duration 1/2}   ; 2
    {:pitch 74, :duration 1/8}
    {:pitch 72, :duration 1/8}
    {:pitch 71, :duration 1/8}
    {:pitch 72, :duration 1/8}
    {:pitch 76, :duration 1/2}   ; 3
    {:pitch 77, :duration 1/8}
    {:pitch 76, :duration 1/8}
    {:pitch 75, :duration 1/8}
    {:pitch 76, :duration 1/8}
    {:pitch 83, :duration 1/8}   ; 4
    {:pitch 81, :duration 1/8}
    {:pitch 80, :duration 1/8}
    {:pitch 81, :duration 1/8}
    {:pitch 83, :duration 1/8}
    {:pitch 81, :duration 1/8}
    {:pitch 80, :duration 1/8}
    {:pitch 81, :duration 1/8}
    {:pitch 84, :duration 1/2}))  ; 5

(def melody-piano-16
  '({:pitch 72, :duration 1}    ; 1
    {:pitch 76, :duration 1/2}
    {:pitch 79, :duration 1/2}
    {:pitch 71, :duration 3/4}  ; 2
    {:pitch 72, :duration 1/8}
    {:pitch 74, :duration 1/8}
    {:pitch 72, :duration 1}
    {:pitch 81, :duration 1}    ; 3
    {:pitch 79, :duration 1/2}
    {:pitch 84, :duration 1/2}
    {:pitch 79, :duration 1/2}  ; 4
    {:pitch 77, :duration 1/8}
    {:pitch 79, :duration 1/8}
    {:pitch 77, :duration 1/4}
    {:pitch 76, :duration 1}))

; G minor
; https://www.youtube.com/watch?v=rNeirjA65Dk&ab_channel=Am4d3usM0z4rt
(def melody-symphony-25
  '({:pitch 79, :duration 1/2}  ; 1
    {:pitch 79, :duration 1}
    {:pitch 79, :duration 1}
    {:pitch 79, :duration 1}
    {:pitch 79, :duration 1/2}
    {:pitch 74, :duration 1/2}  ; 2
    {:pitch 74, :duration 1}
    {:pitch 74, :duration 1}
    {:pitch 74, :duration 1}
    {:pitch 74, :duration 1/2}
    {:pitch 75, :duration 1/2}  ; 3
    {:pitch 75, :duration 1}
    {:pitch 75, :duration 1}
    {:pitch 75, :duration 1}
    {:pitch 75, :duration 1/2}
    {:pitch 66, :duration 1/2}  ; 4
    {:pitch 66, :duration 1}
    {:pitch 66, :duration 1}
    {:pitch 66, :duration 1}
    {:pitch 66, :duration 1/2}
    {:pitch 67, :duration 1}))  ; 5

; G minor
; https://www.youtube.com/watch?v=JTc1mDieQI8&ab_channel=Am4d3usM0z4rt
(def melody-symphony-40
  '({:pitch 75, :duration 1/4}  ; 1
    {:pitch 74, :duration 1/4}
    {:pitch 74, :duration 1/2}  ; 2
    {:pitch 75, :duration 1/4}
    {:pitch 74, :duration 1/4}
    {:pitch 74, :duration 1/2}
    {:pitch 75, :duration 1/4}
    {:pitch 74, :duration 1/4}
    {:pitch 74, :duration 1/2}  ; 3
    {:pitch 82, :duration 1/2}
    {:pitch 0, :duration 1/2}
    {:pitch 82, :duration 1/4}
    {:pitch 81, :duration 1/4}
    {:pitch 79, :duration 1/2}  ; 4
    {:pitch 79, :duration 1/4}
    {:pitch 77, :duration 1/4}
    {:pitch 75, :duration 1/2}
    {:pitch 75, :duration 1/4}
    {:pitch 74, :duration 1/4}
    {:pitch 72, :duration 1/2}  ; 5
    {:pitch 72, :duration 1/2}
    {:pitch 0, :duration 1/2}))

;;;;;;;;;
;; PART III. MIDI & Functions
;; I don't utilize these functions much in my program,
;; but in the long run, may be useful for representing
;; music in more meaningful way than just MIDI values.

; Map of midi notes
(def midi-notes
  {:r 0  ;; Rest

   ;; Octave 0
   :c0 0 :c#0 1 :d0 2 :d#0 3 :e0 4 :f0 5 :f#0 6
   :g0 7 :g#0 8 :a0 9 :a#0 10 :b0 11

   ;; Octave 1 (here and below is unhearable)
   :c1 12 :c#1 13 :d1 14 :d#1 15 :e1 16 :f1 17 :f#1 18
   :g1 19 :g#1 20 :a1 21 :a#1 22 :b1 23

   ;; Octave 2
   :c2 24 :c#2 25 :d2 26 :d#2 27 :e2 28 :f2 29 :f#2 30
   :g2 31 :g#2 32 :a2 33 :a#2 34 :b2 35

   ;; Octave 3
   :c3 36 :c#3 37 :d3 38 :d#3 39 :e3 40 :f3 41 :f#3 42
   :g3 43 :g#3 44 :a3 45 :a#3 46 :b3 47

   ;; Octave 4
   :c4 48 :c#4 49 :d4 50 :d#4 51 :e4 52 :f4 53 :f#4 54
   :g4 55 :g#4 56 :a4 57 :a#4 58 :b4 59

   ;; Octave 5 (from here sounds okay in Klangmeister)
   :c5 60 :c#5 61 :d5 62 :d#5 63 :e5 64 :f5 65 :f#5 66
   :g5 67 :g#5 68 :a5 69 :a#5 70 :b5 71

   ;; Octave 6 (optimal sound for Klangmeister -- in my opinion)
   :c6 72 :c#6 73 :d6 74 :d#6 75 :e6 76 :f6 77 :f#6 78
   :g6 79 :g#6 80 :a6 81 :a#6 82 :b6 83

   ;; Octave 7
   :c7 84 :c#7 85 :d7 86 :d#7 87 :e7 88 :f7 89 :f#7 90
   :g7 91 :g#7 92 :a7 93 :a#7 94 :b7 95

   ;; Octave 8
   :c8 96 :c#8 97 :d8 98 :d#8 99 :e8 100 :f8 101 :f#8 102
   :g8 103 :g#8 104 :a8 105 :a#8 106 :b8 107

   ;; Octave 9 (here and above here hurts your ears...)
   :c9 108 :c#9 109 :d9 110 :d#9 111 :e9 112 :f9 113 :f#9 114
   :g9 115 :g#9 116 :a9 117 :a#9 118 :b9 119

   ;; Octave 10
   :c10 120 :c#10 121 :d10 122 :d#10 123 :e10 124 :f10 125 :f#10 126 :g10 127})

(def key-alphabet-notes
  '(:c :d :e :f :g :a :b))

(def all-alphabet-notes
  '(:c :c# :d :d# :e :f :f# :g# :a :a# :b))

(defn convert-midi-to-alphabet
  "Converts MIDI note to human-readable alphabet note."
  [midi-note]
  (let [order (mod midi-note 12)]
    (nth all-alphabet-notes order)))

(defn convert-alphabet-to-midi
  "Converts human-readable alphabet note to MIDI note."
  [alphabet-note]
  (get midi-notes alphabet-note))

(defn convert-midis-to-alphabets
  "Converts MIDI note to human-readable alphabet note."
  [midi-notes]
  (map #(convert-midi-to-alphabet %) midi-notes))

(defn convert-alphabets-to-midis
  "Converts MIDI note to human-readable alphabet note."
  [alphabet-notes]
  (map #(convert-alphabet-to-midi %) alphabet-notes))

(comment
  (convert-midi-to-alphabet 73)
  (convert-alphabet-to-midi :c4)
  (convert-midis-to-alphabets '(72 72 74))
  (convert-alphabets-to-midis '(:c4 :c4 :e4)))


;;;;;;;;;
;; PART IV. Klangmeister Tools
;; Helps use the browser music-coding env Klangmeister
;; http://ctford.github.io/klangmeister/

(defn string-to-num
  " Converts a simple string to a number."
  [orig-string]
  (cond (str/includes? orig-string "/")  ; Duration is expressed as ratio
        (let [split-string (str/split orig-string #"/")
              numer (Integer/parseInt (first split-string))
              denom (Integer/parseInt (second split-string))
              ratio (/ numer denom)]
          ratio)
        (str/includes? orig-string ".") (Float/parseFloat orig-string)  ; As float
        :else (Integer/parseInt orig-string)))  ; As int

(defn from-klangmeister-tune
  "Turns a Klangmeister tune into a map of notes.
   Makes it easier to convert Klangmeister tune to GP-readable notes."
  [klang-tune]
  (let [dur-start (inc (str/index-of klang-tune "["))  ; Starting/ending index for the duration, pitch vectors
        dur-end (str/index-of klang-tune "]")
        pitch-start (inc (str/last-index-of klang-tune "["))
        pitch-end (str/last-index-of klang-tune "]")
        dur (str/split (subs klang-tune dur-start dur-end) #" ")  ; Converts into vector format
        pitch (str/split (subs klang-tune pitch-start pitch-end) #" ")]
    (map #(hash-map :duration (string-to-num %1)
                    :pitch (Integer/parseInt %2)) dur pitch)))

(defn to-klangmeister-tune
  "Turns a list of notes into Klangmeister tune.
   You can now input this format into Klangmeister (without the quotation marks!)"
  [melody]
  (let [pitch-vec (vec (remove nil? (map #(:pitch %) melody)))
        duration-vec (vec (remove nil? (map #(:duration %) melody)))]
    (str "(->> (phrase " duration-vec pitch-vec "))")))

(def example-output
  (list {:pitch 74, :duration 1} {:pitch 74, :duration 1} {:pitch 74, :duration 1} {:pitch 74, :duration 1} {:pitch 74, :duration 1/16} {}))

(def klang-twinkle
  "(->> (phrase 
       [1/1 1/1 1/1 1/1 1/1 1/1 2/1 1/1 1/1 1/1 1/1 1/1 1/1 2/1]
       [72 72 79 79 81 81 79 77 77 76 76 74 74 72]))")

; Eine Kleine in Klang form
(def klang-eine
  "(->> (phrase 
       [1/1 1/2 1/2 1 1/2 1/2 1/2 1/2 1/2 1/2 2/1 1/1 1/2 1/2 1 1/2 1/2 1/2 1/2 1/2 1/2 2/1]
       [79 0 74 79 0 74 79 74 79 83 86 84 0 81 84 0 81 84 81 78 81 74]
       ))")

; Fur Elise in Klang form
(def klang-fur-elise
  "(->> (phrase 
       [1/4 1/4 1/4 1/4 1/4 1/4 1/4 1/4 1 1/4 1/4 1/4 1/4 1 1/4 1/4 1/4 1/4 2]
       [76 75 76 75 76 71 74 72 69 0 60 64 69 71 0 64 68 71 72]
       ))")

(def klang-figaro
  "(->> (phrase 
       [1/4 1/4 1/4 1/4 1 1/4 1/4 1/4 1/4 1/4 1/4 1/4 1/4 1/4 1/4 1/4 1/4 1 ]
       [74 73 74 73 74 74 73 74 76 78 76 78 79 81 80 81 80 81]
       ))")

(def klang-turkish
  "(->> (phrase 
       [1/8 1/8 1/8 1/8 1/2 1/8 1/8 1/8 1/8 1/2 1/8 1/8 1/8 1/8 1/8 1/8 1/8 1/8 1/8 1/8 1/8 1/8 1/2]
       [71 69 68 69 72 74 72 71 72 76 77 76 75 76 83 81 80 81 83 81 80 81 84]
       ))")

(def klang-piano-sonata-16
  "(->> (phrase 
       [1/1 1/2 1/2 3/4 1/8 1/8 1/1 1/1 1/2 1/2 1/2 1/8 1/8 1/4 1/1]
       [72 76 79 71 72 74 72 81 79 84 79 77 79 77 76]))")

(def klang-symphony-25
  "(->> (phrase 
       [1/2 1/1 1/1 1/1 1/2 1/2 1/1 1/1 1/1 1/2 1/2 1/1 1/1 1/1 1/2 1/2 1/1 1/1 1/1 1/2 1/1]
       [79 79 79 79 79 74 74 74 74 74 75 75 75 75 75 66 66 66 66 66 67]))")

(def klang-symphony-40
  "(->> (phrase 
       [1/4 1/4 1/2 1/4 1/4 1/2 1/4 1/4 1/2 1/2 1/2 1/4 1/4 1/2 1/4 1/4 1/2 1/4 1/4 1/2 1/2 1/2]
       [75 74 74 75 74 74 75 74 74 82 0 82 81 79 79 77 75 75 74 72 72 0]))")

(comment
  (to-klangmeister-tune melody-twinkle)
  (to-klangmeister-tune melody-eine)
  (from-klangmeister-tune klang-twinkle)
  (from-klangmeister-tune klang-eine)
  (from-klangmeister-tune klang-fur-elise)
  (from-klangmeister-tune klang-figaro)
  (from-klangmeister-tune klang-turkish)
  (from-klangmeister-tune klang-piano-sonata-16)
  (from-klangmeister-tune klang-symphony-25)
  (from-klangmeister-tune klang-symphony-40))


;;;;;;;;;
;; PART V. GP Stack Utilities

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

(defn peek-second-stack
  "Returns second item on a stack. If stack is <= 1, returns :no-stack-item"
  [state stack]
  (let [stack-size (count (get state stack))]
    (if (<= stack-size 1)
      :no-stack-item
      (second (get state stack)))))

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


;;;;;;;;;;
;; PART VI. General GP Instructions

; Number of code blocks opened by instructions (default = 0)
(def opened-blocks
  {'exec_dup 1})

; Basic arithmetic: (1) - (4)
(defn integer_+
  "Adds the top two integers and leaves result on the integer stack.
  If integer stack has fewer than two elements, noops."
  [state]
  (make-push-instruction state +' [:integer :integer] :integer))

(defn integer_-
  "Subtracts the top two integers and leaves result on the integer stack."
  [state]
  (make-push-instruction state -' [:integer :integer] :integer))

(defn integer_*
  "Multiplies the top two integers and leaves result on the integer stack."
  [state]
  (make-push-instruction state *' [:integer :integer] :integer))

(defn safe-div
  "Safely integer divides a number by checking if denominator is 0."
  [numer denom]
  (if (zero? denom)
    numer
    (quot numer denom)))

(defn integer_%
  "Acts like integer division most of the time, but if the denominator is 0,
   it returns the numerator, to avoid divide-by-zero errors."
  [state]
  ; CITE : Prof. Helmuth
  ; DESC : Suggested making a helper fn for safe-div
  (make-push-instruction state safe-div [:integer :integer] :integer))

; (5)
(defn exec_dup
  "Duplicates the exec stack."
  [state]
  (if (empty-stack? state :exec)
    state
    (push-to-stack state :exec (first (:exec state)))))

(defn pick-exec
  "Leaves first item to be executed if bool is true; otherwise, second item."
  [bool exec-item2 exec-item1]
  (if bool
    exec-item1
    exec-item2))

; (6)
(defn exec_if
  "Peeks at top item in boolean stack and removes first/second item on exec."
  [state]
  (make-push-instruction state pick-exec [:bool :exec :exec] :exec))

(defn compare-top-2
  "Compares the top two item on the stack."
  [item2 item1]
  (= item1 item2))

; (7)
(defn exec_=
  "Pushes true if top two items in exec are equal; false otherwise."
  [state]
  (make-push-instruction state compare-top-2 [:exec :exec] :bool))

; (8)
(defn exec_swap
  "Swaps the top two elements in exec stack."
  [state]
  (if (<= (count (:exec state)) 1)
    state
    (let [elem1 (first (:exec state))
          elem2 (second (:exec state))]
      (-> state
          (pop-stack :exec)
          (pop-stack :exec)
          (push-to-stack :exec elem1)
          (push-to-stack :exec elem2)))))

; (9)
(defn exec_do_count
  "Performs a loop the number of times indicated by the integer argument;
   Takes the body from the exec stack. A macro call to exec_do_range."
  [state]
  (let [n (peek-stack state :integer)]
    ; NOOP
    (if (or (empty-stack? state :integer)
            (empty-stack? state :exec)
            (<= n 0))
      state
      (-> state
          (pop-stack :integer)
          (push-to-stack :integer 0)
          (push-to-stack :integer (dec n))
          (push-to-stack :exec 'exec_do_range)))))

(defn inc-or-dec
  "Increases or decreases cur index depending on destination index."
  [dest cur]
  (if (> cur dest) 
    (dec cur)
    (inc cur)))

(defn compare-indices
  "Compares the destination and current index."
  [state dest cur body]
  (if (= dest cur)
    (-> state
        (push-to-stack :integer cur)
        (push-to-stack :exec body))
    (-> state
        (push-to-stack :integer cur)
        (push-to-stack :exec body)
        ; Recursive call to exec_do_range
        (push-to-stack :exec 'exec_do_range)
        (push-to-stack :exec dest)
        (push-to-stack :exec (inc-or-dec dest cur))
        (push-to-stack :exec body))))

; (10)
(defn exec_do_range
  "Executes the top item on the exec stack a number of times 
   that depends on the top two integers; Pushes the 
   loop counter onto the integer stack."
  [state]
  (let [dest-index (peek-stack state :integer)
        cur-index (peek-second-stack state :integer)
        body (peek-stack state :exec)]
    ; NOOP
    (if (or (<= (count (:integer state)) 1) (empty-stack? state :exec))
      state
      (-> state
          ; Code and the integer arguments are saved locally and popped
          (pop-stack :integer)
          (pop-stack :integer)
          (pop-stack :exec)
          ; Compare indices!
          (compare-indices dest-index cur-index body)))))


;;;;;;;;;;
;; PART VII. Music GP Instructions

; Default durations for each note
(def default-durations
  (list 1/16 1/8 1/6 1/4 1/3 1/2 1 2 3 4))

(defn generate-random-duration
  "Helper fn; Generates a random note duration."
  []
  (rand-nth default-durations))

(defn generate-random-pitch
  "Helper fn; Generates a random MIDI numeric pitch."
  []
  (rand-nth (range 24 108)))

(defn generate-random-music-item
  "Helper fn; Generates a random music item."
  [state pitch]
  (let [pitch pitch
        random-duration (generate-random-duration)]
    (push-to-stack state
                   :melody-buffer
                   {:pitch pitch :duration random-duration})))

; (1)
(defn generate-note
  "Generates a note by pushing a random pitch and duration onto output buffer."
  [state]
  (let [random-pitch (generate-random-pitch)]
    (generate-random-music-item state random-pitch)))

(defn get-note-from-octave
  "Helper fn; Gets a note from a certain octave."
  [octave]
  (let [start-note (int (* octave 12))
        end-note (int (+ start-note 12))]
    (rand-nth (range start-note end-note))))

(defn pop-n-push-to-melody
  "Helper fn; Pop an item from stack and push item to melody buffer."
  [state stack pitch duration]
  (if (empty-stack? state stack)  ; Does nothing if stack is empty
    state
    (-> state
        (pop-stack stack)  ; Pops from stack
        (push-to-stack :melody-buffer  ; Pushes new note to melody buffer
                       {:pitch pitch :duration duration}))))

(defn pop2-n-push-to-melody
  "Helper fn; Pop an item from 2 stacks and pushes item to melody buffer."
  [state stack1 stack2 pitch duration]
  (if (or (empty-stack? state stack1) (empty-stack? state stack2))  ; NOOP
    state
    (-> state
        (pop-stack stack1)  ; Pops from stack
        (pop-stack stack2)
        (push-to-stack :melody-buffer  ; Pushes new note to melody buffer
                       {:pitch pitch :duration duration}))))

; (2)
(defn generate-note-w-octave
  "Generates a note of a certain octave by taking top elem of :octave stack
   Pushes its random pitch and random duration onto the output buffer."
  [state]
  (if (empty-stack? state :octave) state  ; Need to check beforehand because if the stack is empty,
                                          ; get-note-from-octave will complain.
      (let [octave (peek-stack state :octave)
            pitch (get-note-from-octave octave)
            random-duration (generate-random-duration)]
        (pop-n-push-to-melody state :octave pitch random-duration))))

; (3)
(defn generate-note-w-exactpitch
  "Generates a note of an exact pitch;
   Pushes the pitch and random duration onto the output buffer."
  [state]
  (let [exact-pitch (:pitch (peek-stack state :notes-pitch))
        random-duration (generate-random-duration)]
    (pop-n-push-to-melody state :notes-pitch exact-pitch random-duration)))

(defn generate-exact-duration
  "Helper fn; Generates exact duration of a note."
  [state pitch]
  (let [pitch pitch
        exact-duration (:duration (peek-stack state :notes-duration))]
    (pop-n-push-to-melody state :notes-duration pitch exact-duration)))

; (4)
(defn generate-note-w-exactduration
  "Generates a note of an exact duration;
   Pushes a random pitch and its duration onto the output buffer."
  [state]
  (let [random-pitch (generate-random-pitch)]
    (generate-exact-duration state random-pitch)))

; (5)
(defn generate-note-w-alph-n-duration
  "Generates a note of a certain alphabet (from default octave);
   Pushes the pitch and duration onto the output buffer."
  [state]
  (let [alph (peek-stack state :notes-alph)
        pitch (cond (= alph "c") 72
                    (= alph "d") 74
                    (= alph "e") 76
                    (= alph "f") 77
                    (= alph "g") 79
                    (= alph "a") 81
                    (= alph "b") 83
                    :else 0)
        exact-duration (:duration (peek-stack state :notes-duration))]
    (pop2-n-push-to-melody state :notes-alph :notes-duration pitch exact-duration)))

; (6)
(defn generate-note-w-pitch-n-duration
  "Generates a note of a certain pitch and duration;
   Pushes its pitch and duration onto the output buffer."
  [state]
  (let [exact-pitch (:pitch (peek-stack state :notes-pitch))
        exact-duration (:duration (peek-stack state :notes-duration))]
    (pop2-n-push-to-melody state :notes-pitch :notes-duration exact-pitch exact-duration)))

; (7)
(defn generate-rest
  "Generates a rest; Pushes its pitch 0 and random duration onto the output buffer."
  [state]
  (generate-random-music-item state 0))

; (8)
(defn generate-rest-w-exact-duration
  "Generates a rest of a certain duration;
   Pushes its pitch 0 and duration onto the output buffer."
  [state]
  (generate-exact-duration state 0))

(defn increase-midi-pitch
  "Increases a MIDI pitch"
  [pitch-map]
  (let [orig-pitch (:pitch pitch-map)
        new-pitch (inc orig-pitch)
        new-pitch-map (hash-map :pitch new-pitch)]
    new-pitch-map))

(defn decrease-midi-pitch
  "Decreases a MIDI pitch"
  [pitch-map]
  (let [orig-pitch (:pitch pitch-map)
        new-pitch (dec orig-pitch)
        new-pitch-map (hash-map :pitch new-pitch)]
    new-pitch-map))

; (9)
(defn midi-pitch-up
  "Goes a MIDI pitch up."
  [state]
  (make-push-instruction state increase-midi-pitch [:notes-pitch] :notes-pitch))

; (10)
(defn midi-pitch-down
  "Goes a MIDI pitch down."
  [state]
  (make-push-instruction state decrease-midi-pitch [:notes-pitch] :notes-pitch))

(defn increase-midi-octave
  "Increasea a MIDI octave"
  [pitch-map]
  (let [orig-pitch (:pitch pitch-map)
        new-pitch (+ orig-pitch 12)
        new-pitch-map (hash-map :pitch new-pitch)]
    new-pitch-map))

(defn decrease-midi-octave
  "Decreases a MIDI octave"
  [pitch-map]
  (let [orig-pitch (:pitch pitch-map)
        new-pitch (- orig-pitch 12)
        new-pitch-map (hash-map :pitch new-pitch)]
    new-pitch-map))

; (11)
(defn midi-octave-up
  "Goes a MIDI pitch up."
  [state]
  (make-push-instruction state increase-midi-octave [:notes-pitch] :notes-pitch))

; (12)
(defn midi-octave-down
  "Goes a MIDI pitch down."
  [state]
  (make-push-instruction state decrease-midi-octave [:notes-pitch] :notes-pitch))

; (13)
(defn octave-up
  "Makes the octave of a note higher."
  [state]
  (make-push-instruction state inc [:octave] :octave))

; (14)
(defn octave-down
  "Makes the octave of a note lower."
  [state]
  (make-push-instruction state dec [:octave] :octave))

(defn half-length
  "Decreases the length of a note by half"
  [duration-map]
  (let [orig-duration (:duration duration-map)
        new-duration (* orig-duration 1/2)
        new-duration-map (hash-map :duration new-duration)]
    new-duration-map))

(defn double-length
  "Decreases a MIDI octave"
  [duration-map]
  (let [orig-duration (:duration-map duration-map)
        new-duration (* orig-duration 2)
        new-duration-map (hash-map :duration new-duration)]
    new-duration-map))

; (15)
(defn speed-up
  "Speeds up the note/rest (shorter duration)."
  [state]
  (make-push-instruction state half-length [:duration] :duration))

; (16)
(defn speed-down
  "Slows down the note/rest (longer duration)."
  [state]
  (make-push-instruction state double-length [:duration] :duration))

; (17)
(defn repeat-note
  "Repeats the first note on the melody buffer."
  [state]
  (let [note (peek-stack state :melody-buffer)]
    (if (empty? note)
      state
      (push-to-stack state :melody-buffer note))))

; (18)
(defn repeat-two-notes
  "Repeats the first two notes on the melody buffer. Two-note repetition is common in music."
  [state]
  (let [melody (state :melody-buffer)]
    (if (< (count melody) 2)
      state
      (let [note1 (peek-stack state :melody-buffer)
            note2 (peek-second-stack state :melody-buffer)]
        (if (or (empty? note1) (empty? note2))
          state
          (-> state
              (push-to-stack :melody-buffer note2)
              (push-to-stack :melody-buffer note1)))))))

; (19)
(defn repeat-note-n-times
  "Repeates the first note on the melody buffer n times.
   n is taken from the integer stack."
  [state]
  (let [note (peek-stack state :melody-buffer)]
    (if (or (empty? note) (empty-stack? state :integer))
      state
      (let [n (peek-stack state :integer)
            n-notes (take n (repeat note))
            cur-melody (:melody-buffer state)]
        (-> state
            (assoc :melody-buffer (concat n-notes cur-melody))
            (pop-stack :integer))))))

; (20) Interesting fn; the only one that changes the melody buffer.
; Gives us ideas for the future...should we change things on the melody buffer?
(defn swap-note
  "Swaps the first two notes on the melody buffer."
  [state]
  (let [melody (state :melody-buffer)]
    (if (<= (count melody) 1)
      state
      (let [note1 (peek-stack state :melody-buffer)
            note2 (peek-second-stack state :melody-buffer)]
        (if (or (empty? note1) (empty? note2))
          state
          (-> state
              (pop-stack :melody-buffer)
              (pop-stack :melody-buffer)
              (push-to-stack :melody-buffer note1)
              (push-to-stack :melody-buffer note2)))))))

(comment
  example-music-push-state
  (repeat-note empty-music-push-state)
  (repeat-note example-music-push-state)
  (repeat-note-n-times example-music-push-state)
  (repeat-two-notes example-music-push-state)
  (swap-note example-music-push-state)
  (generate-random-pitch)
  (generate-note example-music-push-state)
  (generate-note-w-exactpitch example-music-push-state)
  (generate-note-w-exactduration example-music-push-state)
  (generate-note-w-pitch-n-duration example-music-push-state)
  (generate-rest example-music-push-state)
  (generate-rest-w-exact-duration example-music-push-state)
  (take 20 (repeatedly #(get-note-from-octave 2.0)))
  (generate-note-w-octave example-music-push-state)
  (octave-up example-music-push-state)
  (octave-down example-music-push-state)
  (midi-pitch-up example-music-push-state)
  (midi-pitch-down example-music-push-state)
  (midi-octave-down example-music-push-state))


;;;;;;;;;
;; PART VIII. Translation from Plushy genomes to Push programs

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
;; PART IX. Interpreter

; Example music program
(def example-music-program
  '(1 6 generate-note generate-note-w-octave 2))

(defn unpack-code-block
  "Helper for interpret-one-step;
   Unpacks the code block by pushing each item in code block to :exec stack."
  [push-state elem]
  (assoc push-state :exec (concat elem (:exec push-state))))

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
          (string? elem) (push-to-stack new-state :notes-alph elem)
          (map? elem) (if (contains? elem :pitch)
                        (push-to-stack new-state :notes-pitch elem)
                        (push-to-stack new-state :notes-duration elem))
          (float? elem) (push-to-stack new-state :octave elem)
          (boolean? elem) (push-to-stack new-state :bool elem)
          (list? elem) (unpack-code-block new-state elem)          ;; Code block
          :else ((eval elem) new-state))))                         ;; Instruction

(defn push-program-to-exec
  "Pushes program to :exec stack."
  [program start-state]
  (assoc start-state :exec program))

(defn interpret-push-program
  "Runs the given program starting with the stacks in start-state. Continues
  until the exec stack is empty. Returns the state of the stacks after the
  program finishes executing or when it reaches the max number of interpreter steps."
  [program start-state]
  (loop [state (push-program-to-exec program start-state)
         max-recursion 1000]  ;; Can be adjusted depending on computation speed.
    ;; (println (str "State:   " state))
    (cond
      ;; Terminates when :exec stack is empty or max # of interpreter steps is reached.
      (empty-stack? state :exec) state
      (zero? max-recursion) state
      ;; Handles instruction/literal on :exec stack one by one.
      :else (recur (interpret-one-step state)
                   (dec max-recursion)))))


;;;;;;;;;
;; PART X. GP Operations

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

(defn crossover
  "Crosses over Plushy genomes (note: not individuals) using uniform crossover.
  If length of two genomes differ, crossovers until the size of the smaller genome.
  Returns child Plushy genome"
  [prog-a prog-b]
  (let [length (min (count prog-a) (count prog-b))
        rand-elem (rand-nth (list (first prog-a) (first prog-b)))]
    (take length (lazy-seq (cons rand-elem (crossover (rest prog-a) (rest prog-b)))))))

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


;;;;;;;;;
;; PART XI. Tournament Selection - a. Normal

(defn tournament-selection
  "Selects an individual from the population using a tournament. Returned 
  individual will be a parent in the next generation. Uses a fixed tournament size of 3."
  [population]
  (let [tournament-pool (take 3 (repeatedly #(rand-nth population)))]
    (apply min-key :total-error tournament-pool)))


;;;;;;;;;;
;; PART XI. Tournament Selection - b. Pareto front

; How it performs on each objective (Each obj = Mozart song)
(def pareto-test-individual
  {:genome '(3 5 integer_* exec_dup "c" 4 "d" integer_- close)
   :errors [1 4]
   :total-error 5})

(def pareto-test-individual-2
  {:genome '(2 4 "a" 1 "f" integer_- close)
   :errors [6 2]
   :total-error 8})

(def pareto-test-individual-3
  {:genome '(1 1 "b" integer_+ close)
   :errors [3 5]
   :total-error 8})

(def pareto-test-individual-4
  {:genome '(1 1 "e" integer_+ close)
   :errors [5 5]
   :total-error 10})

(def simple-pareto-tests
  (list pareto-test-individual
        pareto-test-individual-2
        pareto-test-individual-3))

(defn check-if-less
  " Checks if elem is less than other. "
  [a b]
  (<= a b))

(defn is-in?
  " Checks if element is in collection (i.e. list)"
  [coll elem]
  (if (some #(= elem %) coll) true
      false))

(defn is-dominated-by-ind?
  " Checks if target is dominated by compared individual. "
  [target compared]
  ; Checks if there is at least one feature that is not dominated;
  ; if so, it is not dominated.
  (not (is-in?
        ; Goes through each feature and checks if a value is lesser or equal.
        (map #(check-if-less %1 %2)
             (:errors target)
             (:errors compared))
        true)))

(defn is-not-dominated?
  " Checks if target is not dominated by any individual. "
  [target all-compared]
  (not (is-in? (map #(is-dominated-by-ind? target %)
                    all-compared)
               true)))

(defn pareto-front
  " Gets solutions on the pareto front (all the non-dominated individuals)"
  [population]
  ; For each solution, check if it is dominated by an individual.
  ; Returns random ind on Pareto front.
  (rand-nth (filter #(is-not-dominated? % population) population)))

(comment (is-dominated-by-ind? pareto-test-individual pareto-test-individual-2)
         (is-not-dominated? pareto-test-individual-4 simple-pareto-tests)
         simple-pareto-tests
         (tournament-selection simple-pareto-tests)
         (pareto-front simple-pareto-tests))

;;;;;;;;;;
;; PART XII. Error calculation - a. Distance error function

(def simple-obj
  '({:pitch 72 :duration 1/1}  ; Measure 1
    {:pitch 73 :duration 1/1}
    {:pitch 74 :duration 1/1}
    {:pitch 75 :duration 1/1}
    {:pitch 76 :duration 1/1}  ; Measure 2
    {:pitch 77 :duration 1/1}
    {:pitch 78 :duration 1/1}))

(def simple-obj-2
  '({:pitch 72 :duration 1/1}  ; Measure 1
    {:pitch 74 :duration 1/1}
    {:pitch 74 :duration 2/1}
    {:pitch 74 :duration 1/1}
    {:pitch 74 :duration 1/1}
    {:pitch 76 :duration 2/1}))

(def simple-obj-3
  '({:pitch 72 :duration 1/1}  ; Measure 1
    {:pitch 74 :duration 1/1}
    {:pitch 74 :duration 2/1}))

(def music-objs
  (list
   simple-obj
   simple-obj-2
   simple-obj-3))

(def ex-melody
  '({:pitch 72 :duration 1/1}  ; Measure 1
    {:pitch 74 :duration 1/1}
    {:pitch 74 :duration 2/1}
    {:pitch 74 :duration 1/1}
    {:pitch 74 :duration 1/1}
    {:pitch 76 :duration 2/1}))

; An example music individual in the population
(def example-music-individual
  {:genome '({:pitch 72} {:duration 1} generate-note-w-pitch-n-duration generate-rest {:duration 1/8})
   :errors [5 2 0 3 2]})  ; A calculated error for each objective

(defn absolute-value
  "Absolute val function that accounts for big ints."
  [num]
  (if (neg? num)
    (-' num)
    num))

(defn abs-error
  "Absolute value of an error."
  [num1 num2]
  (if (nil? num1) 0
      (absolute-value (- num1 num2))))

(defn calculate-pitch-accuracy
  "Simplistic fn for calculating pitch."
  [my-melody test]
  (apply + (map #(abs-error (:pitch %1) (:pitch %2)) test my-melody)))

(defn calculate-duration-accuracy
  "Simplistic fn for calculating accuracy."
  [my-melody test]
  (apply + (map #(abs-error (:duration %1) (:duration %2)) test my-melody)))

(defn add-empty-notes-at-end
  "Adds empty-notes at the end"
  [diff-size shorter-melody]
  (let [rests (take diff-size (repeat empty-music-note))]
    (concat shorter-melody rests)))

(defn adjust-music-length
  "Matches up the shorter melody for comparison."
  [diff-size shorter-melody]
  (add-empty-notes-at-end diff-size shorter-melody))

(defn adjust-obj
  "Adjusts shorter objective melody."
  [my-melody obj]
  (let [obj-size (count obj)
        my-melody-size (count my-melody)
        diff (- my-melody-size obj-size)]
    (if (neg? diff) obj  ; obj size is greater
        (adjust-music-length diff obj))))

(defn adjust-melody
  "Adjusts shorter generated melody."
  [my-melody obj]
  (let [obj-size (count obj)
        my-melody-size (count my-melody)
        diff (- obj-size my-melody-size)]
    (if (neg? diff) my-melody  ; Melody size is greater
        (adjust-music-length diff my-melody))))

; Fitness fn 1: Pitch and duration accuracy (Distance)
(defn calculate-for-accuracy-distance
  "Takes an objective and the evolved melody; Calculates the musical error."
  [my-melody obj]
  ; Pitch, Duration : Let's simplistically add it up
  (let [obj-adj (adjust-obj obj my-melody)
        my-melody-adj (adjust-melody obj my-melody)
        pitch-error (calculate-pitch-accuracy obj-adj my-melody-adj)
        duration-error (calculate-duration-accuracy obj-adj my-melody-adj)]
    (+ pitch-error duration-error)))


;;;;;;;;;;
;; PART XII. Error calculation - b. Diversity error function

(defn calculate-diversity-error
  "Penalizes less diverse melodies"
  [list]
  (let [unique (count (distinct list))]
    (cond (= unique 1) 100  ;; When there is only one note/duration throughout
          (= unique 2) 50
          (= unique 3) 25
          :else 0)))

(defn recalculate-diversity-error
  "If a melody performs well on one diversity, we accept."
  [diversity1 diversity2]
  (cond (= diversity1 0) 0
        (= diversity2 0) 0
        :else (+ diversity1 diversity2)))

; Fitness fn 2: Accuracy + diversity
(defn calculate-for-accuracy-n-diversity
  "Takes an objective and the evolved melody; Calculates the musical error."
  [my-melody obj]
  (let [basic-error (calculate-for-accuracy-distance my-melody obj)
        pitch-list (map #(:pitch %) my-melody)
        duration-list (map #(:duration %) my-melody)
        pitch-diversity (calculate-diversity-error pitch-list)
        duration-diversity (calculate-diversity-error duration-list)
        diversity-error (recalculate-diversity-error pitch-diversity duration-diversity)]
    (+ basic-error diversity-error)))

(defn eval-melody-on-all-obj
  "Takes a melody buffer and evaluates it on all given objectives
  For each objective, computes each error and returns list of errors."
  [my-melody objectives error-calculation]
  (if (empty? my-melody) 1000000  ; Penalizes if there is no melody at all
      (loop [objs objectives
             list-errors []]
        (if (empty? objs) list-errors
        ; goes through objs, calculate error for each
            (let [error (error-calculation (first objs) my-melody)]
              (recur (rest objs)
                     (conj list-errors error)))))))

(defn return-individual-w-melody-error
  "Returns individual with list-error."
  [individual melody list-errors total-error]
  (assoc individual
         :melody melody
         :errors list-errors
         :total-error total-error))

(defn music-error-fn
  "Runs the music error function."
  [individual error-calculation tests]
  (let [objectives tests
        push-program (translate-plushy-to-push (:genome individual))
        program-state (interpret-push-program push-program empty-music-push-state)
        melody (:melody-buffer program-state)
        evaluated-melody (eval-melody-on-all-obj melody objectives error-calculation)
        total-error (apply + evaluated-melody)]
    (return-individual-w-melody-error individual melody evaluated-melody total-error)))


;;;;;;;;;;
;; PART XII. Error calculation - c. Levenshtein error function

(defn compute-cost
  "Computes cost (1 if chars are diff; 0 otherwise)."
  [coll1 coll2 index1 index2]
  (let
   [elem1 (nth coll1 index1)
    elem2 (nth coll2 index2)]
    (if (= elem1 elem2)
      0
      1)))

(defn compute-levenshtein
  "Computes levenshtein recursively. This is SLOW"
  [coll1 coll2 index1 index2]
  (cond
    ; Base case
    (= 0 index1) index2
    (= 0 index2) index1

    ; Recursive case
    :else
    (let [prev1 (dec index1)
          prev2 (dec index2)]
      (min
     ; Deletion
       (inc (compute-levenshtein coll1 coll2 prev1 index2))
     ; Insertion
       (inc (compute-levenshtein coll1 coll2 index1 prev2))
     ; Replacement
       (+ (compute-levenshtein coll1 coll2 prev1 prev2)
          (compute-cost coll1 coll2 prev1 prev2))))))

(defn levenshtein
  "Runs the levenshtein on two collections; Inefficient. Recursive."
  [coll1 coll2]
  (compute-levenshtein coll1 coll2 (count coll1) (count coll2)))

; ========== BETTER? ==========
(defn create-init-rows
  "Creates the first Levenshtein matrix row."
  [row-size col-size]
  (let [zeros (take (dec col-size) (repeat 0))]
    (vec (take row-size
               (map #(cons % zeros) (range))))))

(defn create-init-lev-matrix
  "Creates the initial dynamic programming Levenshtein matrix."
  [coll1 coll2]
  (let [row-size (inc (count coll1))
        col-size (inc (count coll2))
        row-1 (vec (take col-size (range)))
        rest-rows (map vec (rest (create-init-rows row-size col-size)))
        init-matrix (vec (concat (list row-1) rest-rows))]
    init-matrix))

; Was having a difficult time populating the matrix after this.

; ========== BETTER ==========
; CITE : https://github.com/lspector/Clojush/blob/master/src/clojush/util.clj#L338-L391
; DESC : Functioning DP method of calculating Lev distance
(defn compute-next-row
  "computes the next row using the prev-row current-element and the other seq"
  [prev-row current-element other-seq pred]
  (reduce
   (fn [row [diagonal above other-element]]
     (let [update-val (if (pred other-element current-element)
                        diagonal
                        (inc (min diagonal above (peek row))))]
       (conj row update-val)))
   [(inc (first prev-row))]
   (map vector prev-row (next prev-row) other-seq)))

(defn better-levenshtein
  "Levenshtein Distance - http://en.wikipedia.org/wiki/Levenshtein_distance
  Amount of difference between two sequences."
  [a b & {p :predicate  :or {p =}}]
  (cond
    (empty? a) (count b)
    (empty? b) (count a)
    :else (peek
           (reduce
            (fn [prev-row current-element]
              (compute-next-row prev-row current-element b p))
            (range (inc (count b)))
            a))))

(defn calculate-for-levenshtein
  "Takes an objective and the evolved melody; Calculates the Levenshtein error."
  [my-melody obj]
  (let [pitch-list (map #(:pitch %) my-melody)
        duration-list (map #(:duration %) my-melody)
        obj-pitch-list (map #(:pitch %) obj)
        obj-duration-list (map #(:duration %) obj)
        pitch-lev (better-levenshtein pitch-list obj-pitch-list)
        duration-lev (better-levenshtein duration-list obj-duration-list)]
    (+ pitch-lev duration-lev)))

(defn mixed-music-error-fn
  "Runs a mixed music error function;
  50% possibility of distance error and the other 50% of Levenshtein."
  [individual error-calculation1 error-calculation2 tests]
  (let [objectives tests
        push-program (translate-plushy-to-push (:genome individual))
        program-state (interpret-push-program push-program empty-music-push-state)
        melody (:melody-buffer program-state)

        ; Separates into two objectives
        half (quot (count objectives) 2)
        first-half (take half objectives)
        latter-half (drop half objectives)

        first-eval (eval-melody-on-all-obj melody first-half error-calculation1)
        latter-eval (eval-melody-on-all-obj melody latter-half error-calculation2)

        evaluated-melody (vec (concat first-eval latter-eval))
        total-error (+ (apply + evaluated-melody))]
    (return-individual-w-melody-error individual melody evaluated-melody total-error)))

(comment
  (levenshtein '(1 2 3) '(1 2 4))
  (create-init-lev-matrix '(1 2) '(3 4))
  (calculate-for-levenshtein ex-melody simple-obj)
  (calculate-for-levenshtein ex-melody simple-obj-2)
  (calculate-pitch-accuracy ex-melody simple-obj)
  (calculate-pitch-accuracy ex-melody simple-obj-2)
  (calculate-duration-accuracy ex-melody simple-obj)
  (calculate-duration-accuracy ex-melody simple-obj-2)
  (calculate-for-accuracy-distance ex-melody simple-obj)
  (calculate-for-accuracy-distance ex-melody (first music-objs))
  (calculate-for-accuracy-n-diversity ex-melody simple-obj)
  (adjust-melody ex-melody simple-obj)
  (adjust-obj ex-melody simple-obj-3)
  (eval-melody-on-all-obj ex-melody music-objs calculate-for-accuracy-distance)
  (interpret-push-program (:genome example-music-individual) example-music-push-state))

;;;;;;;;;
;; PART XIII. Report & GP Run

(defn perform-operation
  "Performs operation based on given operator and parents.
   Returns genome for our child individual."
  [instructions operator parents]
  (cond
    (= operator 'crossover) (crossover (:genome (first parents)) (:genome (last parents)))
    (= operator 'uniform-addition) (uniform-addition instructions (:genome (first parents)))
    (= operator 'uniform-deletion) (uniform-deletion (:genome (first parents)))))

(defn select-and-vary
  "Selects parent(s) from population and varies them, returning
  a child individual (note: not program). Chooses which genetic operator
  to use probabilistically. Gives 50% chance to crossover,
  25% to uniform-addition, and 25% to uniform-deletion."
  [instructions population parent-selection]
  (let [operator (rand-nth genetic-operators)
        num-parents (operator parent-numbers)
        parents (take num-parents (repeatedly #(parent-selection population)))]
    (hash-map :genome (perform-operation instructions operator parents))))

(defn find-best-individual
  "Finds best individual from a population."
  [population]
  (apply min-key :total-error population))

(defn get-melody
  "Gets melody from individual without the empty notes {} appended at the end."
  [individual]
  (filter seq (:melody individual)))

(defn report
  "Reports information on the population each generation, including
  best program, best program size, best total error, and best errors."
  [population generation]
  (let [best-individual (find-best-individual population)
        best-genome (:genome best-individual)
        best-program (translate-plushy-to-push best-genome)
        best-program-size (count best-genome)
        best-total-error (:total-error best-individual)
        best-errors (:errors best-individual)
        best-melody (get-melody best-individual)]
    (println "-------------------------------------------------------")
    (println "               Report for Generation " generation "               ")
    (println "-------------------------------------------------------")
    (println "Best genome: " best-genome)
    (println "Best PUSH program: " best-program)
    (println "Best program size: " best-program-size)
    (println "Best total error: " best-total-error)
    (println "Best errors: " best-errors)
    (println "Best melody: " best-melody)))

(defn genomes-to-individuals
  "Converts genomes to individuals (hash-map format)."
  [genomes]
  (map #(hash-map :genome %) genomes))

(defn initialize-population
  "Initializes the population of a given size."
  [instructions population-size max-initial-plushy-size]
  (genomes-to-individuals
   (take population-size
         (repeatedly #(make-random-plushy-genome instructions max-initial-plushy-size)))))

(defn generate-new-population
  "Generates a new population of a given size, selecting and varying the previous population."
  [instructions population-size population parent-selection]
  (take population-size (repeatedly #(select-and-vary instructions population parent-selection))))

(defn get-another-error-fn
  "Gets another error function that is not being already used."
  [error-function]
  (if (or (= error-function calculate-for-accuracy-distance)
    (= error-function calculate-for-accuracy-n-diversity))
    calculate-for-levenshtein
    calculate-for-accuracy-distance
    ))

(defn run-error-function
  "Runs the error function on all indviduals in population and
   maps the list-error and errors to them."
  [population error-function error-calculation tests]
  ; Music-error-fn is a single error function (can just run).
  (if (= error-function music-error-fn)
    (map #(error-function % error-calculation tests) population)
    ; Else, it's a mixed error function. Need to find alternate error fn to
    ; run for the latter half of the tests.
    (let
     [error-calculation2 (get-another-error-fn error-calculation)]
      (map #(error-function % error-calculation error-calculation2 tests) population))))

(defn print-best-melody
  "Prints the best melody in Klang"
  [best-melody-klang]
  (println "")
  (println "Thank you for using GP-Mozart!")
  (println "Best individual melody is the following: ")
  (println best-melody-klang)
  (println "You can hear the melody in http://ctford.github.io/klangmeister/."))

(defn write-report
  "Reports information on the population each generation, including
  best program, best program size, best total error, and best errors."
  [filename population generation]
  (let [best-individual (find-best-individual population)
        best-genome (:genome best-individual)
        best-program (translate-plushy-to-push best-genome)
        best-program-size (count best-genome)
        best-total-error (:total-error best-individual)
        best-errors (:errors best-individual)
        best-melody (get-melody best-individual)]
    (with-open [w (io/writer filename :append true)]
      (.write w "-------------------------------------------------------")
      (.newLine w)
      (.write w (str "               Report for Generation " generation "            "))
      (.newLine w)
      (.write w "-------------------------------------------------------")
      (.newLine w)
      (.write w "Best genome: ")
      (.write w "(")
      (.write w (apply str best-genome))
      (.write w ")")
      (.newLine w)
      (.newLine w)
      (.write w "Best PUSH program: ")
      (.write w "(")
      (.write w (apply str best-program))
      (.write w ")")
      (.newLine w)
      (.newLine w)
      (.write w (str "Best program size: " best-program-size))
      (.newLine w)
      (.newLine w)
      (.write w (str "Best total error: " best-total-error))
      (.newLine w)
      (.newLine w)
      (.write w (str "Best errors: " best-errors))
      (.newLine w)
      (.newLine w)
      (.write w "Best melody: ")
      (.write w "(")
      (.write w (apply str best-melody))
      (.write w ")")
      (.newLine w)
      (.newLine w)
      )))

(defn count-fielddiversity
  "Counts diversity of a field."
  [melody field]
  (let [field-list (remove nil? (map #(field %) melody))
        unique (count (distinct field-list))]
    unique))

(defn write-best-melody
  "Prints the best melody in Klang"
  [filename best-individual]
  (let [best-melody (get-melody best-individual)
        best-melody-klang (to-klangmeister-tune best-melody)
        best-error (:total-error best-individual)
        best-size (count (:genome best-individual))

        num-notes (count best-melody)
        pitch-unique (count-fielddiversity best-melody :pitch)
        duration-unique (count-fielddiversity best-melody :duration)]
    (with-open [w (io/writer filename :append true)]
      (.newLine w)
      (.newLine w)
      (.newLine w)

      (.write w "-------------------------------------------------------")
      (.newLine w)
      (.write w (str "               Summary " "            "))
      (.newLine w)
      (.write w "-------------------------------------------------------")
      (.newLine w)
      
      (.write w "-Best individual melody: ")
      (.write w best-melody-klang)
      (.newLine w)

      (.write w (str "-Best error: " best-error))
      (.newLine w)
      (.write w (str "-Best program size: " best-size))
      (.newLine w)

      (.write w (str "-Num of notes: " num-notes))
      (.newLine w)
      (.write w (str "-Unique pitch: " pitch-unique))
      (.newLine w)
      (.write w (str "-Unique duration: " duration-unique))
      (.newLine w))))

(defn get-fn-name
  [function]
  (let [start (str/index-of "#" function)
        end (str/index-of "@" function)]
    (subs function start (inc end))))

(defn write-config
  [filename population-size max-generations error-function error-calculation
   parent-selection instructions max-initial-plushy-size tests]
  (with-open [w (io/writer filename :append true)]
    (.newLine w)
    (.newLine w)
    (.write w "-------------------------------------------------------")
    (.newLine w)
    (.write w (str "               Config " "            "))
    (.newLine w)
    (.write w "-------------------------------------------------------")
    (.newLine w)
    (.write w (str "- Filename : " filename))
    (.newLine w)
    (.newLine w)
    (.write w (str "- Max Generations : " max-generations))
    (.newLine w)
    (.write w (str "- Population size : " population-size))
    (.newLine w)
    (.write w (str "- Max initial plushy size : " max-initial-plushy-size))
    (.newLine w)
    (.newLine w)
    (.write w (str "- Error function : "  error-function))
    (.newLine w)
    (.write w (str "- Error calculation : " error-calculation))
    (.newLine w)
    (.write w (str "- Parent selection : " parent-selection))
    (.newLine w)
    (.newLine w)))

(defn push-gp
  "Main GP loop. Initializes the population, and then repeatedly
  generates and evaluates new populations. Stops if it exceeds the
  maximum generations and prints melody of the best individual
  in the last generation. Prints report each generation."
  [{:keys [population-size max-generations error-function error-calculation
           parent-selection instructions max-initial-plushy-size tests
           filename]
    :as argmap}]
  (loop
   [n 0
    population (initialize-population instructions population-size max-initial-plushy-size)]  ;; Initialize population
    (let [population-w-errors (run-error-function population error-function error-calculation tests)]
      (write-report filename population-w-errors n)  ;; Prints report
      (cond
        (= n max-generations)
        (let [best-individual (find-best-individual population-w-errors)]
          (write-best-melody filename best-individual)) ;; Klang melody of the best individual in last generation
        :else (recur (inc n)
                     (generate-new-population instructions population-size population-w-errors parent-selection)))))
    (write-config filename population-size max-generations error-function error-calculation
                  parent-selection instructions max-initial-plushy-size tests)
  )  ;; Generates new population


;;;;;;;;;;
;; PART XIV. Testing Parameters

(def exec-instruction
  (list
   'exec_if
   'exec_=
   'exec_swap
   ))

(def int-instruction
  (list
   'integer_+
   'integer_-
   'integer_*
   'integer_%
   'close))

(def random-generation
  (list
   'generate-note
   'generate-note-w-octave
   'generate-note-w-exactpitch
   'generate-note-w-exactduration
   'generate-rest))

(def deterministic-generation
  (list
   'generate-note-w-alph-n-duration
   'generate-note-w-pitch-n-duration
   'generate-rest-w-exact-duration))

(def music-stack-manipulation
  (list
   'midi-pitch-up
   'midi-pitch-down
   'midi-octave-up
   'midi-octave-down
   'speed-up
   'speed-down
   'octave-up
   'octave-down))

(def repeat-instruction
  (list
   'repeat-note
   'repeat-two-notes
   'repeat-note-n-times
   'exec_dup
   'exec_do_range
   'exec_do_count))

(def music-buffer-instruction
  (list
   'swap-note))

(def default-integers
  (list 0 1 2 3 4 5))

(def default-bools
  (list true false))

(def default-note-alphs
  (list "c" "d" "e" "f" "g" "a" "b"))

(def default-select-durations
  (list {:duration 1/8} {:duration 1/6} {:duration 1/4} {:duration 1/3}
        {:duration 1/2} {:duration 1} {:duration 2}))

(def default-octaves
  (list 5.0 6.0 7.0 8.0))

(def default-pitches
  (list {:pitch 72} {:pitch 73} {:pitch 74} {:pitch 75}
        {:pitch 76} {:pitch 77} {:pitch 78} {:pitch 79}
        {:pitch 80} {:pitch 81} {:pitch 82} {:pitch 83}
        {:pitch 84}))

; Default instructions set
(def default-instructions
  (concat
   ;; Instructions
   exec-instruction
   int-instruction
   random-generation
   random-generation
   deterministic-generation
   deterministic-generation  ; Suggest including two of; otherwise, dup can take over very quickly
   music-stack-manipulation
   music-stack-manipulation
   repeat-instruction  ; Suggest commenting out; very powerful
   music-buffer-instruction

   ;; Constant literals
   default-integers
   default-bools
   default-note-alphs
   default-select-durations
   default-octaves
   default-pitches
   ))

; For Pareto, probably should choose max 5 objectives.
(def mozart-objs
  (list
   melody-twinkle
   melody-eine
   melody-fur-elise
   melody-figaro
  ;;  melody-turkish
  ;;  melody-piano-16
  ;;  melody-symphony-25
  ;;  melody-symphony-40
   ))

;;;;;;;;;;
;; PART XV. Actual Testing

; Example output:
; Tournament
; "(->> (phrase [1/2 1/2 1/2 1/2 1/2 1/2 1/2 1/2 1/8][76 75 76 75 76 75 76 75 72]))"
; "(->> (phrase [1/16 1 1 1/2 1/16 1 1/8 1/8][75 75 77 75 75 74 76 74]))"

; Pareto
; (->> (phrase [1/6 1/3 1/6 1 1/8 1/6 1/8 1/3 1/6 1/6] [75 75 75 77 75 75 76 75 75 0]))
; (->> (phrase [1 1/3 1/2 1/6 1/6 1/8 1/8 1 1/4 1/6] [78 73 75 78 76 72 73 72 77 0]))
; (->> (phrase [1/8 1/8 1/8 1/4 1/8 1/8 1/4 1/4][77 74 77 74 77 74 77 74]))

(comment
  (push-gp {; 1. Instructions easily commented in/out above.
            :instructions default-instructions
            ; 2. TWO parent selection: 
            ;   a. Tournament (tournament-selection)
            ;   b. Pareto tournament (pareto-front)
            :parent-selection pareto-front
            ; 3. THREE error calc: 
            ;   a. Accuracy distance (calculate-for-accuracy-distance)
            ;   b. Accuracy & diversity (calculate-for-accuracy-n-diversity)
            ;   c. Levenshtein (calculate-for-levenshtein)
            :error-calculation calculate-for-levenshtein
            ; Objectives easily commented in/out above.
            :tests mozart-objs
            ; 4. TWO error fns:
            ;   a. Single error calc method (music-error-fn)
            ;   b. Mixed error calc method (mixed-music-error-fn)   
            :error-function mixed-music-error-fn
            ; 5. Max generations. To be honest, it sometimes becomes LESS interesting
            ; with too much generations (i.e. 500 generations).
            :max-generations 10
            ; 6. Population size
            :population-size 100
            ; 7. Initial plushy size. I made it slightly larger, hoping this would
            ; lead to more flexibility for programs to get creative and musical.
            :max-initial-plushy-size 100}))

(comment
  ; We can time the runtime!
  (time (music-error-fn example-music-individual calculate-for-accuracy-n-diversity mozart-objs))
  (time (music-error-fn example-music-individual calculate-for-levenshtein mozart-objs)))

(defn write-additional
  [filename]
  (with-open [w (io/writer filename :append true)]
    (.write w "-Instructions: Full w/ 2 random, 2 deterministic, 2 music stack manipulation")
    (.newLine w)
    (.write w "-Objectives: First 4 Mozart")
    (.newLine w)))

;;;;;;;;;;
;; The main function call
;; Can call this in a REPL, or from the command line with "lein run"

(defn -main
  "Runs push-gp, giving it a map of arguments."
  [& args]
  (binding [*ns* (the-ns 'push307.core)]
    ; The above line is necessary to allow `lein run` to work
    (dotimes [run 20]
      (let [filename (str "result" run ".txt")]
        (push-gp {:parent-selection tournament-selection
                  
                  :error-calculation calculate-for-accuracy-distance
                  :error-function music-error-fn

                  :max-generations 200
                  :population-size 200
                  :max-initial-plushy-size 100

                  ;; Detailed in write-additional
                  :instructions default-instructions
                  :tests mozart-objs
                  
                  :filename filename})
        (write-additional filename)))))
