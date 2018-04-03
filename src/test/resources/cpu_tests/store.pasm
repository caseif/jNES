;;; STORE TESTS

;; ACCUMULATOR TESTS

; preparation
LDA #$01        ; a = 0x01
LDX #$02        ; x = 0x02
LDY #$04        ; y = 0x04

; test zero-page addressing
STA $10         ; write a=1 to $0010
STA $90         ; write a=1 to $0090
STA $FF         ; write a=1 to $00FF
NOP             ; perform assertions:
                ; $0010 = 0x01
                ; $0090 = 0x01
                ; $00FF = 0x01

; test zero-page (x-indexed) addressing
STA $10,X       ; write a=1 to 10+2=$0012
STA $90,X       ; write a=1 to 10+2=$0092
STA $FF,X       ; write a=1 to FF+2=$0001 (should wrap around)
LDX #$90        ; x = 0x90 (testing indices > 0x7F)
STA $11,X       ; write a=1 to 11+90=$00A1
STA $72,X       ; write a=1 to 72+90=$0002
STA $81,X       ; write a=1 to 81+90=$0011
NOP             ; perform assertions:
                ; $0012 = 0x01
                ; $0092 = 0x01
                ; $0001 = 0x01
                ; $00A1 = 0x01
                ; $0001 = 0x01
                ; $0011 = 0x01

; test absolute addressing
STA $0023       ; write a=1 to $0023
STA $0303       ; write a=1 to $0303
STA $0903       ; write a=1 to $0903 ($0103 with mirroring)
STA $0A03       ; write a=1 to $0A03 ($0203 with mirroring)
STA $0F03       ; write a=1 to $0F03 ($0303 with mirroring)
; TODO: test I/O registers
; TODO: test expansion ROM
; TODO: test SRAM
STA $8000       ; write to ROM (should be ignored silently)
STA $FFFF       ; write to ROM (high address)
NOP             ; perform assertions:
                ; $0023 = 0x01
                ; $0303 = 0x01
                ; $0103 = 0x01
                ; $0203 = 0x01
                ; $0303 = 0x01

; test absolute (x-indexed) addressing
LDX #$02        ; x = 0x02
STA $0023,X     ; write a=1 to 23+2=$0025
STA $0303,X     ; write a=1 to 303+2=$0305
STA $0903,X     ; write a=1 to 903+2=$0905 ($0105 with mirroring)
STA $0A03,X     ; write a=1 to A03+2=$0A03 ($0205 with mirroring)
STA $0F03,X     ; write a=1 to F03+2=$0F03 ($0305 with mirroring)
LDX #$06        ; x = 0x06
STA $FFFF,X     ; write a=1 to FFFF+6=$0005 (wraparound)
STA $7FFC,X     ; write to ROM (should be ignored silently)
STA $FFF0,X     ; write to ROM (high address)
NOP             ; perform assertions:
                ; $0025 = 0x01
                ; $0305 = 0x01
                ; $0105 = 0x01
                ; $0205 = 0x01
                ; $0305 = 0x01
                ; $0005 = 0x01

; test absolute (y-indexed addressing)


; test indexed indirect addressing


; test indirect indexed addressing


;;; X REGISTER TESTS

; preparation
LDX #$02        ; x = 0x02
LDY #$04        ; y = 0x04

; test zero-page addressing
STX $10         ; write x=2 to $0010
STX $90         ; write x=2 to $0090
STX $FF         ; write x=2 to $00FF
NOP             ; perform assertions:
                ; $0010 = 0x02
                ; $0090 = 0x02
                ; $00FF = 0x02

; test zero-page (y-indexed) addressing
STX $10,Y       ; write x=2 to 10+4=$0014
STX $90,Y       ; write x=2 to 10+4=$0094
STX $FF,Y       ; write x=2 to FF+4=$0003 (should wrap around)
LDY #$90        ; y = 0x90 (testing indices > 0x7F)
STX $10,Y       ; write x=2 to 10+90=$00A0
STX $70,Y       ; write x=2 to 70+90=$0000
STX $90,Y       ; write x=2 to 90+90=$0020
NOP             ; perform assertions:
                ; $0014 = 0x02
                ; $0094 = 0x02
                ; $0003 = 0x02
                ; $00A0 = 0x02
                ; $0000 = 0x02
                ; $0020 = 0x02

; remaining instruction+addressing combos are redundant due to shared implementations
