/**
 * Enumeration of the different kinds of tokens in the YASL subset.
 * 
 * @author bhoward
 * @author sthede
 * @author rsmith
 */

public enum TokenType 
{
	DEFAULT, // Default for symbols
	ID, // Identifier
	NUM, // numeric literal
	STRING, //String
	PROGRAM, // keyword: program
	CONST, // keyword: const
	BEGIN, // keyword: begin
	PRINT, // keyword: print
	END, // keyword: end
	DIV, // keyword: div
	MOD, // keyword: mod
	VAR, // keyword: var
	INT, // keyword int
	BOOL, // keyword: boolean
	PROC, // keyword: proc
	IF, // keyword: if
	THEN, // keyword: then
	ELSE, // keyword: else
	WHILE, // keyword: while
	DO, // keyword: do
	PROMPT, // keyword: prompt
	AND, // keyword: and
	OR, // keyword: or
	NOT, // keyword: not
	TRUE, // keyword: true
	FALSE, // keyword: false
	SEMI, // semicolon (;)
	PER, // period (.)
	COLON, // colon (:)
	LPAREN, // left parenthesis (
	RPAREN, // right parenthesis )
	COMMA, // comma (,)
	PLUS, // plus operator (+)
	MINUS, // minus operator (-)
	STAR, // times operator (*)
	ASSIGN, // equals operator (=)
	EQUAL, // comparator (==)
	NEQUAL, // not equal to (<>)
	LEQUAL, // less than or equal to (<=)
	GEQUAL, // greater than or equal to (>=)
	LESS, // less than (<)
	GREAT, // greater than (>)
	EOF, // end-of-file
	QQQ
}
