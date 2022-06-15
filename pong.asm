*=$400
key_w=*
*0
key_s=*
*0
key_i=*
*0
key_k=*
*0
b1=*
*$7f
b2=*
*$7f
p1=*
*$7
p2=*
*$7
pp1=*
*0
pp2=*
*0
bx=*
*$7f
by=*
*$7f
vx=*
*$01
vy=*
*$00
i=*
*0
j=*
*0
k=*
*0
x=*
*0
y=*
*0
z=*
*0
p=*
*0
mode=*
*1

pixel=$0232
px=$0230
py=$0231
random=$0224

*=$500
loop:
jsr clear
jsr storeKey
jsr move
jsr draw
jsr wait
jmp loop

storeKey:
lda $020A
rol A
rol A
and #$01
sta key_w
lda $020A
ror A
ror A
ror A
and #$01
sta key_s
lda $0209
ror A
and #$01
sta key_i
lda $0209
ror A
ror A
ror A
and #$01
sta key_k
rts

move:
lda key_w
beq key_w_np
dec p1
bne key_w_np
inc p1
key_w_np:
lda key_s
beq key_s_np
inc p1
lda p1
cmp #$0f
bne key_s_np
dec p1
key_s_np:
lda key_i
beq key_i_np
dec p2
bne key_i_np
inc p2
key_i_np:
lda key_k
beq key_k_np
inc p2
lda p2
cmp #$0f
bne key_k_np
dec p2
key_k_np:
jsr fix_pad
jsr move_ball
rts

fix_pad:
lda p1
and #$0f
sta p1
lda p2
and #$0f
sta p2
rts

move_ball:
lda mode
cmp #01
bne move_ball_nr
lda random
clc
and #2
clc
sbc #00
clc
adc vx
sta vx
lda random
clc
and #2
clc
sbc #00
clc
adc vy
sta vy
move_ball_nr:
lda bx
clc
adc vx
sta bx
lda by
clc
adc vy
sta by
rts

wait:
ldy #$ff
wait_y:
ldx #$0f
wait_x:
nop
dex
bne wait_x
dey
bne wait_y
rts

clear:
lda #$0f
sta p
jsr draw_ball
jsr draw_pad1
jsr draw_pad2
rts

draw:
lda #$11
sta p
jsr draw_ball
lda #$16
sta p
jsr draw_pad1
jsr draw_pad2
ldx bx
ldy by
lda #$05
stx px
sty py
sta pixel
lda p1
ldy #$10
draw_p1_mul:
adc p1
dey
bne draw_p1_mul
sta py
ldx #7
stx px
lda #$05
sta pixel
rts

draw_ball:
lda by
clc
sbc #2
clc
tay
lda #5
sta i
draw_ball_i:
lda bx
clc
sbc #2
clc
tax
lda #5
sta j
lda p
sty py
draw_ball_j:
stx px
sta pixel
inx
dec j
bne draw_ball_j
iny
dec i
bne draw_ball_i
rts

draw_pad1:
lda p1
ldy #$10
draw_pad1_mul:
adc p1
dey
bne draw_pad1_mul
sbc #10
tay
lda #21
sta i
draw_pad1_i:
lda #5
sbc #2
tax
lda #5
sta j
lda p
sty py
draw_pad1_j:
stx px
sta pixel
inx
dec j
bne draw_pad1_j
iny
dec i
bne draw_pad1_i
rts

draw_pad2:
lda p2
ldy #$10
draw_pad2_mul:
adc p2
dey
bne draw_pad2_mul
sbc #10
tay
lda #21
sta i
draw_pad2_i:
lda #$f5
sbc #2
tax
lda #5
sta j
lda p
sty py
draw_pad2_j:
stx px
sta pixel
inx
dec j
bne draw_pad2_j
iny
dec i
bne draw_pad2_i
rts
