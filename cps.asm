*=$0000
pixel=$0232
px=$0230
py=$0231
lda #$00
l1:
    sta py
    ldy #$ff
    l2:
        sta px
        ldx #$ff
        l3:
            inc pixel
            inc px
            dex
            bne l3
        inc py
        dey
        bne l2
    jmp l1