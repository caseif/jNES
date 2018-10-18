;;;;;;;;;;;;;;;;
; test bitwise logic
;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;
; test AND
;;;;;;;;;;;;;;;;

LDA #%00011100      ; set accumulator
AND #%00101110      ; logical and, should produce 0b00001100
NOP                 ; perform assertions:
                    ; a = 12
                    ; n = 0
                    ; z = 0

LDA #%11111111      ; set accumulator
AND #%10000000      ; logical and, should produce 0b10000000
NOP                 ; perform assertions:
                    ; a = 0x80
                    ; n = 1
                    ; z = 0

LDA #%11110000      ; set accumulator
AND #%00001111      ; logical and, should produce 0
NOP                 ; perform assertions:
                    ; a = 0
                    ; n = 0
                    ; z = 1
