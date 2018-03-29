;;; test accumulator
; test immediate loading
LDA_IMM $01     ; a = 0x01
LDX_IMM $02     ; x = 0x02
LDY_IMM $04     ; y = 0x04
NOP             ; perform assertions:
                ; a = 0x01
                ; x = 0x02
                ; y = 0x04
; test zero-page addressing
STA_ZRP $10     ; write a=1 to $0010
STA_ZRP $90     ; write a=1 to $0090
STA_ZRP $FF     ; write a=1 to $00FF
NOP             ; perform assertions:
                ; $0010 = 0x01
                ; $0090 = 0x01
                ; $00FF = 0x01
; test zero-page addressing (x-indexed)
STA_ZPX $10     ; write a=1 to 10+2=$0012
STA_ZPX $90     ; write a=1 to 10+2=$0092
STA_ZPX $FF     ; write a=1 to FF+2=$0002 (should wrap around)
LDX_IMM $90     ; x = 0x90 (testing indices > 0x7F)
STA_ZPX $10     ; write a=1 to 10+90=$00A0
STA_ZPX $70     ; write a=1 to 70+90=$0000
STA_ZPX $80     ; write a=1 to 80+90=$0010
NOP             ; perform assertions:
                ; $0012 = 0x01
                ; $0092 = 0x01
                ; $0002 = 0x01
                ; $00A0 = 0x01
                ; $0000 = 0x01
                ; $0010 = 0x01

;;; test x register
LDX_IMM $02    ; x = 0x02
; y-register is already set (y=0x04)
; test zero-page addressing
STX_ZRP $10     ; write x=2 to $0010
STX_ZRP $90     ; write x=2 to $0090
STX_ZRP $FF     ; write x=2 to $00FF
NOP             ; perform assertions:
                ; $0010 = 0x02
                ; $0090 = 0x02
                ; $00FF = 0x02
; test zero-page addressing (y-indexed)
STX_ZPY $10     ; write x=2 to 10+4=$0014
STX_ZPY $90     ; write x=2 to 10+4=$0094
STX_ZPY $FF     ; write x=2 to FF+4=$0004 (should wrap around)
LDX_IMM $90     ; y = 0x90 (testing indices > 0x7F)
STA_ZPX $10     ; write x=2 to 10+90=$00A0
STA_ZPX $70     ; write x=2 to 70+90=$0000
STA_ZPX $80     ; write x=2 to 80+90=$0010
NOP             ; perform assertions:
                ; $0014 = 0x02
                ; $0094 = 0x02
                ; $0004 = 0x02
                ; $00A0 = 0x02
                ; $0000 = 0x02
                ; $0010 = 0x02
