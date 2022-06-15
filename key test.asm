*=$0300
key_w=*
*0
key_a=*
*0
key_s=*
*0
key_d=*
*0
vb=*
*$7f
hb=*
*$7f

pixel=$0232
px=$0230
py=$0231

*=$0400
lda #$7f
sta px
sta py
loop:
lda #$00
sta pixel
jsr storeKey
jsr move
jmp loop

storeKey:
lda $020A
rol A
rol A
and #$01
sta key_w
lda $0208
ror A
and #$01
sta key_a
lda $020A
ror A
ror A
ror A
and #$01
sta key_s
lda $0208
ror A
ror A
ror A
ror A
and #$01
sta key_d
rts

move:
lda key_w
beq wnp
dec vb
bne wnp
dec py
wnp:
lda key_a
beq anp
dec hb
bne anp
dec px
anp:
lda key_s
beq snp
inc vb
bne snp
inc py
snp:
lda key_d
beq dnp
inc hb
bne dnp
inc px
dnp:
lda #$01
sta pixel
rts
