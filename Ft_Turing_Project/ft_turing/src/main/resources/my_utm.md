

# My_UTM

### UTM input

run src/main/resources/MY_UTM_ADD.json "1.+= . ABC aA1B.> bB1B1> bB+B1> bB=C.> #1+1= @"

rules:

- "a, b, c..." : marks where a transition start and what family it belongs
- transition order:
    - 1.- Initial state
    - 2.- Read symbol
    - 3.- To state
    - 4.- Write symbol
    - 5.- Action Right or Left
- "#" : marks where the input start
- "@" : markes where the outpu start. The machine writes the output right after @

### UTM config

- "skip_intro",  # Skips unary add simulator input until transition descriptions

- "f_start", # Finds from the back the new start "~" but ignores init transition

- "f_input_front", # travels right until it finds the input marked with "#"

- "f_input_back", # travels left until it finds the input "#"

- "r_0_input", # read first position input. Travels right until the first element of the input. It will "save" the symbol readed in the next "state memory"

- "r_*_input", # read any other position in input. Travels from "#" through "_" finding "1", "+" or "=" and saves the symbol in state memory.

- "f_init-1", # finds first transition carrying the symbol "1" read in the last step.

- "f_scan-(1+), # find scanright transitions (b) from the start that match read symbols "1" and "+"

- "r_trans-(1+)", # after finding first transition, reads the transition carryng "1" or "+" form input.

- "r_tr_init-1", # continues reading transition. Carries actual state "init" and first element of input "1"

- "r_tr_scan-(1+) # once a "B" transition has been found (scanright transition for "1" and "+" only), reads the symbol.

- "r_tr_init_if1-1", # continues reading transition. Carries actual state "init", transition symbol read "1", input "1"

- "r_tr_scan_if(1+)-(1+)", # reading a "B" scanright transition, if finds a "1" (same for "+"), saves in state memory, next will read what symbol has to write in the output.

- "r_tr_init_if1_w.-1", # continues reading transition. Carries actual state "init", transition symbol read "1", transition symbol write ".", input "1"

- "r_tr_scan_if(1+)_w(1)-(1+)", # had read a scanright transition, read "1" or "+", we have to write "1" in the output (we carry a "1" or "+")

- "f_output->.", # find output position "@" carrying if "1" write "."

- "f_output_last", # finds "@" to write after the last "."

- "woutput->.", # write in output. if "1" write "."

- "HALT"

