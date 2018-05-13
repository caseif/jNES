; test branching
LDA #$00            ; reset registers
LDX #$00
LDY #$00
JMP skip1           ; skip the next instruction
LDX #$01            ; set the X register (this shouldn't execute)
skip1:  LDA #$01    ; a = 1
NOP                 ; perform assertions:
                    ; a = 0x01
                    ; x = 0x00
SBC #$01            ; subtract 1 from a
BEQ skip2           ; skip the next instruction if a=1
LDY #$01            ; set the Y register (this shouldn't execute)
skip2:  LDA #$01    ; a = 1
NOP                 ; perform assertions:
                    ; a = 0x01
                    ; y = 0x00