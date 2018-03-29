; test accumulator
; test immediate loading
LDA_IMM $01     ; a = 1
LDX_IMM $02     ; x = 2
LDY_IMM $03     ; y = 3
NOP             ; perform assertions
; test zero-page addressing
STA_ZRP $10     ; write a=1 to $0010
STA_ZPX $10     ; write a=1 to 10+2=$0012
NOP             ; perform assertions
; test zero-page addressing with index > 127
LDX_IMM $90     ; x = 0x90 = 144
LDY_IMM $91     ; x = 0x91 = 145
STA_ZPX $10     ; write a=1 to 10+90=$00A0
