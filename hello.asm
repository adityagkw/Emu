*=$0300
str=*
*"Hello World!!!",10,"How are You?",0

text = $0235

*=$400

ldx #0
print_loop:
lda str, X
beq break
sta text
inx
jmp print_loop
break:
jmp break