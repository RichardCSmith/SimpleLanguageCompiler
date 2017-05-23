import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * A Lexical Analyzer for a subset of YASL. Uses a (Mealy) state machine to
 * extract the next available token from the input each time next() is called.
 * Input comes from a Reader, which will generally be a BufferedReader wrapped
 * around a FileReader or InputStreamReader (though for testing it may also be
 * simply a StringReader).
 * 
 * @author bhoward
 * @author sthede
 * @auther rsmith
 */

public class Scanner {
	/**
	 * Construct the Scanner ready to read tokens from the given Reader.
	 * 
	 * @param in
	 */
	public Scanner(Reader in) {
		source = new Source(in);
	}

	// Create an ArrayList of Tokens to hold all input, as long as it's valid
	// Once stored, the compiler will go back through it and store the constant
	// values,
	// check the grammar of the constant declarations, check the grammar of the
	// statements, and print the statements in postfix to standard output
	ArrayList<Token> input = new ArrayList<Token>();

	// Start in state 0
	int state = 0;

	// Holders for column and line the current thing being looked at started on
	int startL = 0;
	int startC = 0;

	// Holder for TokenType being constructed
	// 0 = ID
	// 1 = NUM
	// 2 = KEYWORD
	// 3 = SEMI (;)
	// 4 = PER (.)
	// 5 = PLUS (+)
	// 6 = MINUS (-)
	// 7 = STAR (*)
	// 8 = ASSIGN or EQUAL(=) or (==)
	// 9 = no type / default
	// 10 = COLON (:)
	// 11 = LPAREN (
	// 12 = RPAREN )
	// 13 = COMMA (,)
	// 14 = LEQUAL or LESS or NEQUAL (<=) or (<) or (<>)
	// 15 = GEQUAL or GREAT (>=) or (>)
	// 16 = STRING ("")
	// 66 = comment: //
	// 67 = comment: {}
	int tt = 9;

	// String to hold lexeme if it is ID or NUM
	String holder = "";

	/**
	 * Extract the next available token. When the input is exhausted, it will
	 * return an EOF token on all future calls.
	 * 
	 * @return the next Token object
	 */
	public Token next() {

		// Alphabet array to compare to characters in input
		char[] alphabet = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
				'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
				'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
				'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

		// Character array of digits (non-zero) to compare to input
		char[] digitsNZ = new char[] { '1', '2', '3', '4', '5', '6', '7', '8', '9' };

		// Loop until token is returned or the EOF is reached
		while (!source.atEOF) {

			// Update line and column holders if state is 0 (matched something
			// and started over)
			if (state == 0) {
				startL = source.line;
				startC = source.column;
			}

			// Read character and update state to match corresponding state on
			// DFA
			// Determine if it could be an ID or a KEYWORD
			boolean isLetter = false;
			for (int i = 0; i < alphabet.length; i++) {
				if (alphabet[i] == source.current) {
					isLetter = true;
				}
			}

			// Determine if it is a non-zero digit
			boolean isNZDigit = false;
			for (int i = 0; i < digitsNZ.length; i++) {
				if (digitsNZ[i] == source.current) {
					isNZDigit = true;
				}
			}

			// Determine if it is a zero
			boolean isZero = false;
			if ('0' == source.current) {
				isZero = true;
			}

			// Take steps based on source.current and state
			if (state == 0) {
				if (isLetter) {
					tt = 0;
					state = 1;
					holder = holder + source.current;
				} else if (isNZDigit) {
					tt = 1;
					state = 2;
					holder = holder + source.current;
				} else if (isZero) {
					tt = 1;
					state = 3;
					holder = holder + source.current;
				} else if (source.current == '.') {
					tt = 4;
					state = 104;
				} else if (source.current == ';') {
					tt = 3;
					state = 103;
				} else if (source.current == '+') {
					tt = 5;
					state = 105;
				} else if (source.current == '-') {
					tt = 6;
					state = 106;
				} else if (source.current == '*') {
					tt = 7;
					state = 107;
				} else if (source.current == '=') {
					tt = 8;
					state = 108;
				} else if (source.current == ':'){
					tt = 10;
					state = 109;
				} else if (source.current == '('){
					tt = 11;
					state = 110;
				} else if (source.current == ')'){
					tt = 12;
					state = 111;
				} else if (source.current == ','){
					tt = 13;
					state = 112;
				} else if (source.current == '<'){
					tt = 14;
					state = 113;
				} else if (source.current == '>'){
					tt = 15;
					state = 114;
				} else if (source.current == '"'){
					tt = 16;
					state = 115;
				} else if (source.current == '{') {
					tt = 67;
					state = 6;
				} else if (source.current == '/') {
					tt = 66;
					state = 7;
				} else if (source.current == ' ' || source.current == '\n' || source.current == '\t'
						|| source.current == '\r') {
					// Do nothing
				} else {
					if (!source.atEOF) {
						System.out.println("ERROR: INVALID TOKEN OR REACHED END OF KEYBOARD INPUT");
						System.out.println(source.current);
						System.exit(0);
					}
				}
			} else if (state == 1) {
				Token myToken;
				if (isLetter || isNZDigit || isZero) {
					holder = holder + source.current;
				} else {
					state = 100; // Note for me to locate action on DFA
					if (holder.equals("program")) {
						myToken = new Token(startL, startC, TokenType.PROGRAM, null);
					} else if (holder.equals("const")) {
						myToken = new Token(startL, startC, TokenType.CONST, null);
					} else if (holder.equals("begin")) {
						myToken = new Token(startL, startC, TokenType.BEGIN, null);
					} else if (holder.equals("print")) {
						myToken = new Token(startL, startC, TokenType.PRINT, null);
					} else if (holder.equals("end")) {
						myToken = new Token(startL, startC, TokenType.END, null);
					} else if (holder.equals("div")) {
						myToken = new Token(startL, startC, TokenType.DIV, null);
					} else if (holder.equals("mod")) {
						myToken = new Token(startL, startC, TokenType.MOD, null);
					} else if (holder.equals("var")) {
						myToken = new Token(startL, startC, TokenType.VAR, null);
					} else if (holder.equals("int")) {
						myToken = new Token(startL, startC, TokenType.INT, null);
					} else if (holder.equals("boolean")) {
						myToken = new Token(startL, startC, TokenType.BOOL, null);
					} else if (holder.equals("proc")) {
						myToken = new Token(startL, startC, TokenType.PROC, null);
					} else if (holder.equals("if")) {
						myToken = new Token(startL, startC, TokenType.IF, null);
					} else if (holder.equals("then")) {
						myToken = new Token(startL, startC, TokenType.THEN, null);
					} else if (holder.equals("else")) {
						myToken = new Token(startL, startC, TokenType.ELSE, null);
					} else if (holder.equals("while")) {
						myToken = new Token(startL, startC, TokenType.WHILE, null);
					} else if (holder.equals("do")) {
						myToken = new Token(startL, startC, TokenType.DO, null);
					} else if (holder.equals("prompt")) {
						myToken = new Token(startL, startC, TokenType.PROMPT, null);
					} else if (holder.equals("and")) {
						myToken = new Token(startL, startC, TokenType.AND, null);
					} else if (holder.equals("or")) {
						myToken = new Token(startL, startC, TokenType.OR, null);
					} else if (holder.equals("not")) {
						myToken = new Token(startL, startC, TokenType.NOT, null);
					} else if (holder.equals("true")) {
						myToken = new Token(startL, startC, TokenType.TRUE, null);
					} else if (holder.equals("false")) {
						myToken = new Token(startL, startC, TokenType.FALSE, null);
					} else {
						myToken = new Token(startL, startC, TokenType.ID, holder);
					}
					holder = "";
					state = 0;
					input.add(myToken);
					continue;
				}
			} else if (state == 2) {
				if (isNZDigit || isZero) {
					holder = holder + source.current;
				} else {
					state = 101; // Note for me to locate action on DFA
					Token myToken = new Token(startL, startC, TokenType.NUM, holder);
					holder = "";
					state = 0;
					input.add(myToken);
					continue;
				}
			} else if (state == 3) {
				Token myToken = new Token(startL, startC, TokenType.NUM, holder);
				holder = "";
				state = 0;
				input.add(myToken);
				continue;
			} else if (state == 6) {
				if (source.current == '}') {
					state = 0; // Comment is finished, resume looking at input
				} else if (source.atEOF) {
					System.out.println("ERROR: EOF IN COMMENT");
					System.exit(0);
				}
			} else if (state == 7) {
				if (source.current != '/') {
					System.out.println("ERROR: INVALID TOKEN");
					System.exit(0);
				} else {
					state = 8; // New state to mark comment start
				}
			} else if (state == 8) {
				if (source.current == '\n') {
					state = 0;
				} else if (source.atEOF) {
					System.out.println("ERROR: EOF IN COMMENT");
					System.exit(0);
				}
			} else if (state == 103) {
				Token myToken = new Token(startL, startC, TokenType.SEMI, null);
				holder = "";
				state = 0;
				input.add(myToken);
				continue;
			} else if (state == 104) {
				Token myToken = new Token(startL, startC, TokenType.PER, null);
				holder = "";
				state = 0;
				input.add(myToken);
				break;
			} else if (state == 105) {
				Token myToken = new Token(startL, startC, TokenType.PLUS, null);
				holder = "";
				state = 0;
				input.add(myToken);
				continue;
			} else if (state == 106) {
				Token myToken = new Token(startL, startC, TokenType.MINUS, null);
				holder = "";
				state = 0;
				input.add(myToken);
				continue;
			} else if (state == 107) {
				Token myToken = new Token(startL, startC, TokenType.STAR, null);
				holder = "";
				state = 0;
				input.add(myToken);
				continue;
			} else if (state == 108) {
				Token myToken;
				if (source.current != '=') {
					myToken = new Token(startL, startC, TokenType.ASSIGN, null);
					holder = "";
					state = 0;
					input.add(myToken);
					continue;
				} else {
					myToken = new Token(startL, startC, TokenType.EQUAL, null);
					holder = "";
					state = 0;
					input.add(myToken);
				}
				
			} else if (state == 109) {
				Token myToken = new Token(startL, startC, TokenType.COLON, null);
				holder = "";
				state = 0;
				input.add(myToken);
				continue;
			} else if (state == 110){
				Token myToken = new Token(startL, startC, TokenType.LPAREN, null);
				holder = "";
				state = 0;
				input.add(myToken);
				continue;
			} else if (state == 111){
				Token myToken = new Token(startL, startC, TokenType.RPAREN, null);
				holder = "";
				state = 0;
				input.add(myToken);
				continue;
			} else if (state == 112){
				Token myToken = new Token(startL, startC, TokenType.COMMA, null);
				holder = "";
				state = 0;
				input.add(myToken);
				continue;
			} else if (state == 113){
				if (source.current == '='){
					Token myToken = new Token(startL, startC, TokenType.LEQUAL, null);
					holder = "";
					state = 0;
					input.add(myToken);
				} else if (source.current == '>'){
					Token myToken = new Token(startL, startC, TokenType.NEQUAL, null);
					holder = "";
					state = 0;
					input.add(myToken);
				} else {
					Token myToken = new Token(startL, startC, TokenType.LESS, null);
					holder = "";
					state = 0;
					input.add(myToken);
					continue;
				}
			} else if (state == 114){
				if (source.current == '='){
					Token myToken = new Token(startL, startC, TokenType.GEQUAL, null);
					holder = "";
					state = 0;
					input.add(myToken);
				} else {
					Token myToken = new Token(startL, startC, TokenType.GREAT, null);
					holder = "";
					state = 0;
					input.add(myToken);
					continue;
				}
			} else if (state == 115){
				if (source.current == '"'){
					state = 116;
				}
				holder = holder + source.current;
			} else if (state == 116){
				if (source.current == '"'){
					holder = holder + '"';
					state = 115;
				} else {
					holder = holder.substring(0, holder.length()-1);
					Token myToken = new Token(startL, startC, TokenType.STRING, holder);
					input.add(myToken);
					holder = "";
					state = 0;
					continue;
				}
			} else {
				System.out.println("ERROR: NO VALID STATE MATCH FOR STATE: " + state);
				System.exit(0);
			}
			source.advance();
		}

		// Quit if at EOF
		Token myToken = new Token(startL, startC, TokenType.EOF, null);
		input.add(myToken);
		return myToken;
	}

	/**
	 * Close the underlying Reader.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		source.close();
	}

	private Source source;
}
