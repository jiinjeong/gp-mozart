########################################################
# FILE   : midi-calc.py
# AUTHOR : Jiin Jeong
# DATE   : Oct 23, 2020
# DESC   : Helps get the map of MIDI notes and vals;
#          Kept my hand from retyping 127 MIDI values.
########################################################

current_octave = input("Enter current octave: ")  # i.e. 5
notes_string = input("Enter current map of notes: ")  # i.e. :c5 60 :c#5 61 :d5 62 :d#5 63 :e5 64 :f5 65 :f#5 66 :g5 67 :g#5 68 :a5 69 :a#5 70 :b5 71
old_notes = notes_string.split(" ")
new_notes = []
for note in old_notes:
    if note[-1] == str(current_octave):
        new = note.replace(current_octave, str(int(current_octave) + 1))
        new_notes.append(new)
    else:
        new_notes.append(str(int(note) + 12))
seperator = " "
new_string = seperator.join(new_notes)

print(new_string)
