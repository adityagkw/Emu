*=$1300
str1 = *
*"Enter something:",10,0
str2 = *
*"You Entered:",10,0
input_buffer = *

input = $0240
flags = $0242

output = $0235

*=$0400
lda #%10000000
sta flags

ldx #0
str1_loop_start:
lda str1,X
beq str1_loop_end
sta output
inx
jmp str1_loop_start
str1_loop_end:

input_wait_loop_start:
lda flags
and #%00000001
beq input_wait_loop_start
input_wait_loop_end:

ldx #0
str2_loop_start:
lda str2,X
beq str2_loop_end
sta output
inx
jmp str2_loop_start
str2_loop_end:

ldx #0
input_loop_start:
lda input
sta input_buffer,X
cmp #0
beq input_loop_end
inx
jmp input_loop_start
input_loop_end:

ldx #0
input_show_loop_start:
lda input_buffer,X
beq input_show_loop_end
sta output
inx
jmp input_show_loop_start
input_show_loop_end: