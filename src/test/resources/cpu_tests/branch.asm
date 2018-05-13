; test branching
LDA #$00            ; reset registers
LDX #$00
JMP skip1           ; skip next instruction
LDX #$01            ; set X register (this shouldn't execute)
skip1:  LDA #$01    ; a = 1
NOP                 ; perform assertions:
                    ; a = 0x01
                    ; x = 0x00

LDX #$00            ; reset X register
SBC #$01            ; subtract 1 from a
BEQ skip2           ; skip next instruction if a=1
LDX #$01            ; set X register (this shouldn't execute)
skip2:  LDA #$01    ; a = 1
NOP                 ; perform assertions:
                    ; a = 0x01
                    ; x = 0x00

SBC #$02            ; subtract 2 from a
BNE skip3           ; skip next instruction if a != 0
LDX #$01            ; set X register (this shouldn't execute)
skip3:  BMI skip4   ; skip next instruction if a < 0
        LDX #$01    ; set X register (this shouldn't execute)
skip4:  LDA #$01    ; set A register
        BPL skip5   ; skip next instruction if a > 0
        LDX #$01    ; set X register (this shouldn't execute)
skip5:  LDA #$01    ; set A register
NOP                 ; perform assertions:
                    ; a = 0x01
                    ; x = 0x00