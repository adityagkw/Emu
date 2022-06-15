*=$0300
arr=*
*'A'
*'R'
*'R'
*'A'
*'Y'
*0
*=$0200
f:
lda #1
adc #2
ldx #$ff
loop:
inx
ldy arr,X
bne loop
jsr func
jmp f

func:
lda #$ff
nop
nop
nop
nop
nop
rts