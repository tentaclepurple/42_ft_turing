

# My_UTM

### UTM input

"1.+= . ABC aA1.B> bB11B> bB+1B> bB=.C> #1+1= @"
run src/main/resources/MY_UTM_ADD.json "1.+= . ABC aA1B.> bB1B1> bB+B1> bB=C.> #1+1= @"
rules:

- "a, b, c..." : marks where a transition start and what family it belongs
- transition order:
    - 1.- Initial state
    - 2.- Read symbol
    - 3.- Write symbol
    - 4.- To state
    - 5.- Action Right or Left
- "#" : marks where the input start
- "@" : markes where the outpu start. The machine writes the output right after @

### UTM config

- "skip_intro",  # Skips unary simulator input until transition descriptions
- "f_start", # Finds from the back the new start "~"
- "f_input_front", # travels right until it finds the input marked with "#"
- "f_input_back", # travels left until it finds the input "#"
- "r_0_input", # read first position input. Travels right until the first element of the input. It will "save" the symbol readed in the next "state memory"
- "r_*_input", # read any other position in input. Travels from "#" through "_" finding "1", "+" or "=" and saves the symbol in state memory.
- "f_init-1", # finds first transition carrying the symbol "1" read in the last step.
- "f_scan_(1+), # find scanright transitions (b) from the start that match read symbols "1" and "+"
- "r_trans-1", # after finding first transition, reads the transition carryng 1 form input.
- "r_tr_init-1", # continues reading transition. Carries actual state "init" and first element of input "1"
- "r_tr_init_if1-1", # continues reading transition. Carries actual state "init", transition symbol read "1", input "1"
- "r_tr_init_if1_w.-1", # continues reading transition. Carries actual state "init", transition symbol read "1", transition symbol write ".", input "1"
- "f_output_1->.", # find output position "@" carrying if "1" write "."
- "woutput_1->.", # write in output. if "1" write "."
- "HALT"

    "r_tr_init_if1_-1": [ "HALT"],
    "initial": "skip_intro",
    "finals": ["HALT"],
    "transitions": {
		"skip_intro": [
			{ "read": "1", "to_state": "skip_intro", "write": "_", "action": "RIGHT" },
            { "read": "+", "to_state": "skip_intro", "write": "_", "action": "RIGHT" },
            { "read": "=", "to_state": "skip_intro", "write": "_", "action": "RIGHT" },
            { "read": " ", "to_state": "skip_intro", "write": "_", "action": "RIGHT" },
            { "read": ".", "to_state": "skip_intro", "write": "_", "action": "RIGHT" },
            { "read": "A", "to_state": "skip_intro", "write": "_", "action": "RIGHT" },
            { "read": "B", "to_state": "skip_intro", "write": "_", "action": "RIGHT" },
            { "read": "C", "to_state": "skip_intro", "write": "_", "action": "RIGHT" },
            { "read": "a", "to_state": "f_input", "write": "a", "action": "RIGHT" }
        ],
        "f_input": [
            { "read": "1", "to_state": "f_input", "write": "1", "action": "RIGHT" },
            { "read": "+", "to_state": "f_input", "write": "+", "action": "RIGHT" },
            { "read": "=", "to_state": "f_input", "write": "=", "action": "RIGHT" },
            { "read": " ", "to_state": "f_input", "write": " ", "action": "RIGHT" },
            { "read": ".", "to_state": "f_input", "write": ".", "action": "RIGHT" },
            { "read": "A", "to_state": "f_input", "write": "A", "action": "RIGHT" },
            { "read": "B", "to_state": "f_input", "write": "B", "action": "RIGHT" },
            { "read": "C", "to_state": "f_input", "write": "C", "action": "RIGHT" },
            { "read": "a", "to_state": "f_input", "write": "a", "action": "RIGHT" },
            { "read": "b", "to_state": "f_input", "write": "b", "action": "RIGHT" },
            { "read": ">", "to_state": "f_input", "write": ">", "action": "RIGHT" },
            { "read": "_", "to_state": "f_input", "write": "_", "action": "RIGHT" },
            { "read": "#", "to_state": "r_0_input", "write": "#", "action": "RIGHT" }
        ],
        "r_0_input": [
           { "read": "1", "to_state": "f_init-1", "write": "_", "action": "LEFT" }
        ],
        "f_init-1": [
            {"read": "#", "to_state": "f_init-1", "write": "#", "action": "LEFT"},
            {"read": "1", "to_state": "f_init-1", "write": "1", "action": "LEFT"},
            {"read": " ", "to_state": "f_init-1", "write": " ", "action": "LEFT"},
            {"read": ".", "to_state": "f_init-1", "write": ".", "action": "LEFT"},
            {"read": "A", "to_state": "f_init-1", "write": "A", "action": "LEFT"},
            {"read": "=", "to_state": "f_init-1", "write": "=", "action": "LEFT"},
            {"read": "+", "to_state": "f_init-1", "write": "+", "action": "LEFT"},
            {"read": "B", "to_state": "f_init-1", "write": "B", "action": "LEFT"},
            {"read": "C", "to_state": "f_init-1", "write": "C", "action": "LEFT"},
            {"read": "b", "to_state": "f_init-1", "write": "b", "action": "LEFT"},
            {"read": ">", "to_state": "f_init-1", "write": ">", "action": "LEFT"},
            {"read": "a", "to_state": "r_trans-1", "write": "a", "action": "RIGHT"}
        ],
        "r_trans-1": [
            {"read": "A", "to_state": "r_tr_init-1", "write": "A", "action": "RIGHT"}
        ],
        "r_tr_init-1": [
            {"read": "1", "to_state": "r_tr_init_if1-1", "write": "1", "action": "RIGHT"}
        ],
        "r_tr_init_if1-1": [
            {"read": "B", "to_state": "r_tr_init_if1-1", "write": "B", "action": "RIGHT"},
            {"read": ".", "to_state": "r_tr_init_if1_w.-1", "write": ".", "action": "RIGHT"}
        ], #Estoy en transicion init, he leido 1 en input, se que hay que cambiar 1 por . en el input
        "r_tr_init_if1_w.-1": [
            {"read": "A", "to_state": "HALT", "write": "A", "action": "RIGHT"}
        ]
        
    }
}



### unary_add

{
    "name": "unary_add_simple",
    "alphabet": ["1", "+", "=", "."],
    "blank": ".",
    "states": ["A", "B", "C"],
    "initial": "A",
    "finals": ["C"],
    "transitions": {
		"A": [ init
			{ "read": "1", "to_state": "B", "write": ".", "action": "RIGHT" }
		],
        "B": [ scanright
            { "read": "1", "to_state": "B scanright", "write": "1", "action": "RIGHT" },
            { "read": "+", "to_state": "B scanright", "write": "1", "action": "RIGHT" },
            { "read": "=", "to_state": "C HALT", "write": ".", "action": "RIGHT"}
        ]
    }
}

