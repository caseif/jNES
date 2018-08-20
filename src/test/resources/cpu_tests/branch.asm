;;;;;;;;;;;;;;;;
; test branching
;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;
; test JMP (indirect)
;;;;;;;;;;;;;;;;

LDX #$00            ; reset registers
; note that the address needs to be offset by 0x8000 bytes due to the NES's memory model
LDA #$11            ; low byte of target address
STA $0050
LDA #$80            ; high byte of target address
STA $0051

JMP ($0050)         ; jump to address specified at $0050 ($8012)

LDX #$01            ; set X=1 (this shouldn't execute)

;;; this is offset $0011 in PRG ;;;
NOP                 ; perform assertions:
                    ; x = 0x0

;;;;;;;;;;;;;;;;
; test JMP
;;;;;;;;;;;;;;;;

LDA #$00            ; reset registers
LDX #$00

JMP skip1           ; skip next instruction
LDX #$01            ; set X register (this shouldn't execute)
skip1:  LDA #$01    ; a = 1

NOP                 ; perform assertions:
                    ; a = 0x01
                    ; x = 0x00

;;;;;;;;;;;;;;;;
; test BEQ
;;;;;;;;;;;;;;;;

LDA #$00            ; reset registers
LDX #$00

BEQ skip2           ; skip next instruction if a=0
LDX #$01            ; set X register (this shouldn't execute)
skip2:  LDA #$01    ; a = 1
NOP                 ; perform assertions:
                    ; a = 0x01
                    ; x = 0x00
;;;;;;;;;;;;;;;;
; test BNE, BMI, BPL
;;;;;;;;;;;;;;;;

LDA #$00            ; reset accumulator

SBC #$02            ; subtract 2 from a

BNE skip3           ; skip next instruction if a != 0
LDX #$01            ; set X register (this shouldn't execute)

skip3:  BMI skip4   ; skip next instruction if a < 0
        LDX #$02    ; set X register (this shouldn't execute)

skip4:  LDA #$01    ; set A register
        BPL skip5   ; skip next instruction if a > 0
        LDX #$03    ; set X register (this shouldn't execute)

skip5:  LDA #$01    ; set A register

NOP                 ; perform assertions:
                    ; a = 0x01
                    ; x = 0x00
