import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Main class for Project -- Scanner for a Subset of YASL (Fall 2015). Scans
 * tokens from standard input and prints the token stream to standard output.
 * 
 * @author bhoward
 * @author sthede
 * @author rsmith
 */

public class Project {
	public static void main(String[] args) throws IOException {
		java.util.Scanner sc = new java.util.Scanner(System.in);
		System.out.println("Input file name: ");
		String fn = sc.nextLine();
		String fileName = fn;
		sc.close();
		FileReader fileReader = new FileReader(fileName);

		Scanner scanner = new Scanner(new BufferedReader(fileReader));

		ArrayList<Token> input = new ArrayList<Token>();
		Token token;
		do {
			token = scanner.next();
			// System.out.println(token);
		} while (token.type != TokenType.EOF);

		// Copy the input from the scanner class to evaluate
		input = scanner.input;
		
		parse(input);
		System.out.println("Program parsed correctly.");
		
		//Generate PALI code
		Generator generator = new Generator(input, symbolTable);
		generator.createPALIFile();
		
		scanner.close();
	}

	public static void parse(ArrayList<Token> input) {
		
		//System.out.println("Called validBeginning()");
		// Tests to see if the input has a valid beginning
		// Will exit program if invalid
		validBeginning(input);
		// Keeps track of where the parser is in the input ArrayList
		// Starts at 3 because indices 0, 1, 2 are the beginning
		inputIndex = 3;

		//System.out.println("Called parseAfterBeginning()");
		// Continue with normal parsing
		inputIndex = parseAfterBeginning(input, inputIndex);
		//System.out.println("Called endsWithPeriod()");
		// Tests for end period
		inputIndex++;
		endsWithPeriod(input, inputIndex);
	}

	// This method parses the input and prints out
	// "Successfully parsed file" or "Error: (error type)"
	public static int parseAfterBeginning(ArrayList<Token> input, int inputIndex) {

		//System.out.println("Called validConsts()");
		// Tests to see if the constants of the program are valid
		inputIndex = validConsts(input, inputIndex);
		//System.out.println("Called validVars()");
		// Tests to see if the variables of the program are valid
		inputIndex = validVars(input, inputIndex);
		//System.out.println("Called validProc()");
		// Tests to see if the procedures of the program are valid
		symbolTable.add(0, new Symbol());
		symbolTable.get(0).setScope(true);
		inputIndex = validProc(input, inputIndex);
		
		int findScope = findSymbolTableScope();
		if (findScope != -1){
			for (int i = findScope; i >= 0; i--){
				symbolTable.remove(i);
			}
		}
		//System.out.println("Checked for BEGIN");
		// Tests to see if BEGIN is present before statements
		if (input.get(inputIndex).type != TokenType.BEGIN) {
			System.out.println("Error: Begin not present before statements.");
			System.exit(0);
		}
		
		inputIndex++;
		
		//System.out.println("Called validStatementsHelper()");
		// Tests to see if the statements of the program are valid
		inputIndex = validStatementsHelper(input, inputIndex);
		return inputIndex;
	}

	// This method tests to see if the program begins validly
	public static void validBeginning(ArrayList<Token> input) {
		// Determine that the input contains the valid beginning to a YASL
		// program
		// Valid beginning MUST be PROGRAM ID SEMI

		// Index in the input arrayList of our current position
		int inputIndex = 0;

		// First, ensure the tokens exist
		for (int i = inputIndex; i > 3; i++) {
			if (input.get(i) == null) {
				System.out.println("Error: Expected PROGRAM ID SEMI");
				System.exit(0);
			}
		}

		// Then test them
		if (input.get(inputIndex).type != TokenType.PROGRAM || input.get(inputIndex + 1).type != TokenType.ID
				|| input.get(inputIndex + 2).type != TokenType.SEMI) {

			System.out.println("Error: Expected PROGRAM ID SEMI");
			System.exit(0);
		}
	}

	// Tests the constants part of the program and returns the inputIndex
	public static int validConsts(ArrayList<Token> input, int inputIndex) {
		if (input.get(inputIndex).type == TokenType.CONST) {
			for (int i = inputIndex; i < input.size(); i += 5) {
				for (int j = i; j < i + 5; j++) {
					if (input.get(i) == null) {
						System.out.println("Error: Found null in CONST declarations");
						System.exit(0);
					}
				}
				if (input.get(i).type == TokenType.CONST) {
					if (input.get(i + 1).type == TokenType.ID && input.get(i + 2).type == TokenType.ASSIGN
							&& input.get(i + 3).type == TokenType.NUM && input.get(i + 4).type == TokenType.SEMI) {
						if (!isInSymbolTable(input.get(i + 1).lexeme)) {
							//constants.add(input.get(i + 1).lexeme);
							symbolTable.add(0, new Symbol());
							symbolTable.get(0).setName(input.get(i + 1).lexeme);
							symbolTable.get(0).setIntValue(Integer.parseInt(input.get(i + 3).lexeme));
							if (findSymbolTableScope() == -1){
								symbolTable.get(0).setGlobal(true);
							}
							//System.out.println("Added " + symbolTable.get(0).getName() + " to the symbol table");
						} else {
							System.out.println("Error: Cannot declare two constants with the same name");
							System.exit(0);
						}
						//constValues.add(input.get(i + 3).lexeme);

						inputIndex += 5;

					} else {
						System.out.println("Error: Expected CONST ID ASSIGN NUM SEMI in constant declarations");
						System.exit(0);
					}
				} else {
					break;
				}
			}
		}

		return inputIndex;
	}

	// Tests the variables part of the program and returns the inputIndex
	public static int validVars(ArrayList<Token> input, int inputIndex) {
		if (input.get(inputIndex).type == TokenType.VAR) {
			for (int i = inputIndex; i < input.size(); i += 5) {
				for (int j = i; j < i + 5; j++) {
					if (input.get(i) == null) {
						System.out.println("Error: Found null in VAR declarations");
						System.exit(0);
					}
				}

				boolean validTypeThree = false;
				if (input.get(i + 3).type == TokenType.INT || input.get(i + 3).type == TokenType.BOOL) {
					validTypeThree = true;
				}

				if (input.get(i).type == TokenType.VAR) {
					if (input.get(i + 1).type == TokenType.ID && input.get(i + 2).type == TokenType.COLON
							&& validTypeThree && input.get(i + 4).type == TokenType.SEMI) {
						if (!isInSymbolTable(input.get(i + 1).lexeme)) {
							//constants.add(input.get(i + 1).lexeme);
							symbolTable.add(0, new Symbol());
							symbolTable.get(0).setName(input.get(i + 1).lexeme);
							symbolTable.get(0).setType(input.get(i + 3).type);
							if (findSymbolTableScope() == -1){
								symbolTable.get(0).setGlobal(true);
							}
						} else {
							System.out.println("Error: Cannot declare two var with the same name");
							System.exit(0);
						}

						inputIndex += 5;

					} else {
						System.out.println("Error: Expected VAR ID COLON <Type> SEMI in constant declarations");
						System.exit(0);
					}
				} else {
					break;
				}
			}
		}

		return inputIndex;
	}

	// Tests to see if the procedures are valid and returns the inputIndex
	public static int validProc(ArrayList<Token> input, int inputIndex) {
		if (input.get(inputIndex).type == TokenType.PROC) {
			symbolTable.add(0, new Symbol());
			symbolTable.get(0).setName(input.get(inputIndex + 1).lexeme);
			symbolTable.get(0).isProc = true;
			inputIndex++;
			if (input.get(inputIndex).type == TokenType.ID) {
				inputIndex++;
				if (input.get(inputIndex).type == TokenType.LPAREN) {
					// Has parameters
					inputIndex++;
					while (input.get(inputIndex).type != TokenType.RPAREN) {
						if (input.get(inputIndex).type != TokenType.ID
								|| input.get(inputIndex + 1).type != TokenType.COLON) {
							System.out.println("Error: Invalid parameters");
							System.exit(0);
						} else if (input.get(inputIndex + 2).type != TokenType.INT
								&& input.get(inputIndex).type != TokenType.BOOL) {
							System.out.println("Error: Invalid parameters");
							System.exit(0);
						}
						//if (!constants.contains(input.get(inputIndex).lexeme)) {
						//	constants.add(input.get(inputIndex).lexeme);
						//} else {
						//	System.out.println("Error: Cannot declare two constants with the same name");
						//	System.exit(0);
						//}
						inputIndex += 3;
						if (input.get(inputIndex).type != TokenType.COMMA
								&& input.get(inputIndex).type != TokenType.RPAREN) {
							System.out.println("Error: Invalid parameters");
							System.exit(0);
						}
						if (input.get(inputIndex).type == TokenType.COMMA) {
							inputIndex++;
						}
					}
					if (input.get(inputIndex - 1).type == TokenType.COMMA) {
						System.out.println("Error: Invalid comma in parameters");
						System.exit(0);
					}
					inputIndex++;
				} else if (input.get(inputIndex).type != TokenType.SEMI) {
					System.out.println("Error: Expected semicolon or parameters after ID in PROC");
					System.exit(0);
				}
				// No parameters
				inputIndex++;
				inputIndex = parseAfterBeginning(input, inputIndex);
				inputIndex++;
				if (input.get(inputIndex).type == TokenType.SEMI) {
					inputIndex++;
					if (input.get(inputIndex).type == TokenType.PROC) {
						// Another procedure after this one
						inputIndex = validProc(input, inputIndex);
					}
				} else {
					System.out.println("Error: Expected SEMI after PROC");
					System.exit(0);
				}

			} else {
				System.out.println("Error: Expected ID after PROC");
				System.exit(0);
			}
		}
		return inputIndex;
	}

	// Repeats calling statement evaluations until END is found
	public static int validStatementsHelper(ArrayList<Token> input, int inputIndex) {
		while (input.get(inputIndex).type != TokenType.END) {
			//System.out.println("Called validStatements()");
			inputIndex = validStatements(input, inputIndex);
		}

		return inputIndex;
	}

	// Determines which statement tester function to use
	public static int validStatements(ArrayList<Token> input, int inputIndex) {
		if (input.get(inputIndex).type == TokenType.ASSIGN && input.get(inputIndex-1).type == TokenType.ID){
			inputIndex--;
			System.out.println(input.get(inputIndex));
		}
		if (input.get(inputIndex).type == TokenType.IF) {
			inputIndex = statementIf(input, inputIndex);
			if (input.get(inputIndex+1).type == TokenType.END){
				if (input.get(inputIndex).type == TokenType.SEMI){
					System.out.println("Error: Incorrect statement format, SEMI found before END.");
					System.exit(0);
				}
			}
		} else if (input.get(inputIndex).type == TokenType.PROC){
			// Do nothing
		} else if (input.get(inputIndex).type == TokenType.WHILE) {
			inputIndex = statementWhile(input, inputIndex);
			if (input.get(inputIndex+1).type == TokenType.END){
				if (input.get(inputIndex).type == TokenType.SEMI){
					System.out.println("Error: Incorrect statement format, SEMI found before END.");
					System.exit(0);
				}
			}
		} else if (input.get(inputIndex).type == TokenType.BEGIN) {
			inputIndex = statementBegin(input, inputIndex);
			if (input.get(inputIndex+1).type == TokenType.END){
				if (input.get(inputIndex).type == TokenType.SEMI){
					System.out.println("Error: Incorrect statement format, SEMI found before END.");
					System.exit(0);
				}
			}
		} else if (input.get(inputIndex).type == TokenType.ID) {
			inputIndex = statementID(input, inputIndex);
			if (input.get(inputIndex+1).type == TokenType.END){
				if (input.get(inputIndex).type == TokenType.SEMI){
					System.out.println("Error: Incorrect statement format, SEMI found before END.");
					System.exit(0);
				}
			}
		} else if (input.get(inputIndex).type == TokenType.PROMPT) {
			inputIndex = statementPromptString(input, inputIndex);
			if (input.get(inputIndex+1).type == TokenType.END){
				if (input.get(inputIndex).type == TokenType.SEMI){
					System.out.println("Error: Incorrect statement format, SEMI found before END.");
					System.exit(0);
				}
			}
		} else if (input.get(inputIndex).type == TokenType.PRINT) {
			inputIndex = statementPrint(input, inputIndex);
			if (input.get(inputIndex+1).type == TokenType.END){
				if (input.get(inputIndex).type == TokenType.SEMI){
					System.out.println("Error: Incorrect statement format, SEMI found before END.");
					System.exit(0);
				}
			}
		} else {
			if (input.get(inputIndex).type != TokenType.SEMI){
				System.out.println(input.get(inputIndex-1));
				System.out.println(input.get(inputIndex));
				System.out.println("Error: Incorrect statement format, must begin with valid token.");
				System.exit(0);
			}
			inputIndex++;;
			validStatements(input, inputIndex);
		}
		return inputIndex;
	}

	// Tester function for if statements
	public static int statementIf(ArrayList<Token> input, int inputIndex) {
		inputIndex++;
		inputIndex = expression(input, inputIndex, false, true);
		if (input.get(inputIndex).type != TokenType.THEN) {
			System.out.println("Error: Expected THEN.");
			System.exit(0);
		} else {
			inputIndex = validStatements(input, inputIndex);
		}
		if (input.get(inputIndex).type == TokenType.ELSE) {
			inputIndex++;
			inputIndex = validStatements(input, inputIndex);
		}
		return inputIndex;
	}

	// Tester function for while statements
	public static int statementWhile(ArrayList<Token> input, int inputIndex) {
		inputIndex++;
		inputIndex = expression(input, inputIndex, false, true);
		if (input.get(inputIndex).type != TokenType.DO) {
			System.out.println("Error: Expected DO.");
			System.exit(0);
		}
		inputIndex = validStatements(input, inputIndex);
		return inputIndex;
	}

	// Tester function for begin statements
	public static int statementBegin(ArrayList<Token> input, int inputIndex) {
		inputIndex = validStatementsHelper(input, inputIndex);
		return inputIndex;
	}

	// Tester function for ID statements
	public static int statementID(ArrayList<Token> input, int inputIndex) {
		inputIndex++;
		if (input.get(inputIndex).type == TokenType.ASSIGN) {
			inputIndex++;
			if (input.get(inputIndex - 2).type == TokenType.BOOL){
				inputIndex = expression(input, inputIndex, false, true);
			} else {
				inputIndex = expression(input, inputIndex, false, false);
			}
		} else if (input.get(inputIndex).type == TokenType.LPAREN) {
			inputIndex++;
			while (input.get(inputIndex).type != TokenType.RPAREN) {
				inputIndex = expression(input, inputIndex, false, false);
				if (input.get(inputIndex).type == TokenType.COMMA) {
					inputIndex++;
					inputIndex = expression(input, inputIndex, false, false);
				} else {
					System.out.println("Error: Invalid arguments passed.");
					System.exit(0);
				}
			}
		} else {
			System.out.println("Found statement not followed by assign or parentheses, this is probably an error.");
		}
		return inputIndex;
	}

	// Tester function for prompt string statements
	public static int statementPromptString(ArrayList<Token> input, int inputIndex) {
		inputIndex++;
		if (input.get(inputIndex).type == TokenType.STRING){
			inputIndex++;
			if (input.get(inputIndex).type == TokenType.COMMA){
				inputIndex++;
				if (input.get(inputIndex).type != TokenType.ID){
					System.out.println("Error: Invalid PROMPT STRING statement.");
					System.exit(0);
				} else {
					inputIndex++;
				}
			}
		} else {
			System.out.println("Error: No String after prompt");
		}
		return inputIndex;
	}

	// Tester function for print statements
	public static int statementPrint(ArrayList<Token> input, int inputIndex) {
		//System.out.println("Called statementPrint()");
		inputIndex++;
		if (input.get(inputIndex).type == TokenType.STRING){
			//System.out.print(input.get(inputIndex).lexeme);
			inputIndex++;
		} else {
			inputIndex = expression(input, inputIndex, true, false);
		}
		return inputIndex;
	}

	// Tester for not-comparative expressions
	public static int expression(ArrayList<Token> input, int inputIndex, boolean isPrint, boolean isBool) {
		// Array of the mathematical TokenTypes to make it easier to compare the
		// inputs to them
		TokenType[] functionArr = new TokenType[11];
		functionArr[0] = TokenType.PLUS;
		functionArr[1] = TokenType.MINUS;
		functionArr[2] = TokenType.DIV;
		functionArr[3] = TokenType.MOD;
		functionArr[4] = TokenType.STAR;
		functionArr[5] = TokenType.GREAT;
		functionArr[6] = TokenType.LESS;
		functionArr[7] = TokenType.GEQUAL;
		functionArr[8] = TokenType.LEQUAL;
		functionArr[9] = TokenType.EQUAL;
		functionArr[10] = TokenType.NEQUAL;
		
		
		// Boolean for indicating whether or not it is a boolean expression to compare to the varialble
		boolean isBoolExp = false;
		
		// If statement for simple one word true/false
		/**if (input.get(inputIndex).type == TokenType.TRUE || input.get(inputIndex).type == TokenType.FALSE) {
			if (input.get(inputIndex + 1).type == TokenType.END || input.get(inputIndex + 1).type == TokenType.SEMI){
				for (int i = 0; i < symbolTable.size(); i++) {
					if (input.get(inputIndex - 2).lexeme.equalsIgnoreCase(symbolTable.get(i).getName())){
						if (symbolTable.get(i).getType() == TokenType.BOOL) {
							inputIndex++;
							return inputIndex;
						} else {
							System.out.println("Error: Attempting to define a boolean variable as a non-boolean");
							System.exit(0);
						}
						break;
					}
				}
			} else {
				System.out.println("Error: Found something other than SEMI or END after TRUE or FALSE");
				System.exit(0);
			}
		}**/

		// Boolean to indicate if the current lexeme is an operator
		boolean isOp = false;

		// Int to indicate what level of priority the current lexeme is
		int lexPri = 0;

		// Int to indicate what level of priority the current stack top has
		int stackPri = 0;

		// Stack to hold operators
		Stack<TokenType> ops = new Stack<TokenType>();

		while (input.get(inputIndex).type == TokenType.ID || input.get(inputIndex).type == TokenType.NUM || isOp) {
			
			isOp = false;
			for (int i = 0; i < functionArr.length; i++) {
				if (functionArr[i] == input.get(inputIndex).type) {
					isOp = true;
					if (i < 2) {
						lexPri = 3;
					} else if (i < 5) {
						lexPri = 4;
					} else if (i < 9){
						isBoolExp = true;
						lexPri = 2;
					} else if (i < 11){
						isBoolExp = true;
						lexPri = 1;
					}
				}
			}
			if (input.get(inputIndex).type == TokenType.ID) {
				if (input.get(inputIndex).type == TokenType.BOOL || input.get(inputIndex).type == TokenType.TRUE || input.get(inputIndex).type == TokenType.FALSE){
					isBoolExp = true;
				}
				if (input.get(inputIndex - 1).type == TokenType.ID || input.get(inputIndex - 1).type == TokenType.NUM) {
					System.out.println("Error: Expression has ID next to an ID or NUM.");
					System.exit(0);
				}
				if (!isInSymbolTable(input.get(inputIndex).lexeme) && !isGlobalVar(input.get(inputIndex).lexeme)) {
					System.out.println("Error: Undeclared constant: " + input.get(inputIndex).lexeme);
					System.exit(0);
				} else {
					inputIndex++;
				}
			} else if (input.get(inputIndex).type == TokenType.NUM) {
				if (input.get(inputIndex - 1).type == TokenType.ID || input.get(inputIndex - 1).type == TokenType.NUM) {
					System.out.println("Error: Expression has NUM next to an ID or NUM.");
					System.exit(0);
				}
				inputIndex++;
			} else if (ops.isEmpty() && isOp) {
				ops.push(input.get(inputIndex).type);
				inputIndex++;
			} else if (isOp) {
				while (stackPri >= lexPri) {
					ops.pop();
					for (int i = 0; i < functionArr.length; i++) {
						if (functionArr[i] == ops.peek()) {
							if (i < 2) {
								stackPri = 1;
							} else if (i < 5) {
								stackPri = 2;
							}
						}
					}

				}
				ops.push(input.get(inputIndex).type);
				inputIndex++;
				isOp = false;
				stackPri = lexPri;
			} else {
				System.out.println("Error: Invalid token in expression.");
				System.exit(0);
			}
			inputIndex++;
		}
		if (isBoolExp && !isBool){
			System.out.println("Error: Assigning boolean value to non-boolean variable.");
			System.exit(0);
		} else if (!isBoolExp && isBool){
			System.out.println("Error: Assigning non-boolean value to boolean variable.");
			System.exit(0);
		}
		if (isOp) {
			System.out.println("Error: Cannot end expression with operator.");
			System.exit(0);
		}
		
		inputIndex--;
		return inputIndex;
	}

	// Tests to see if program ends with a period
	public static void endsWithPeriod(ArrayList<Token> input, int inputIndex) {
		if (input.get(inputIndex).type != TokenType.PER) {
			System.out.println("Error: Expected . after END");
			System.exit(0);
		}
	}

	
	public static boolean isInSymbolTable(String name){
		for (int i = 0; i < symbolTable.size(); i++){
			if (symbolTable.get(i).getScope()){
				return false;
			}
			if (symbolTable.get(i).getName().equalsIgnoreCase(name)){
				return true;
			}
		}
		return false;
	}
	
	public static int findSymbolTableScope(){
		for (int i = 0; i < symbolTable.size(); i++){
			if (symbolTable.get(i).getScope()){
				return i;
			}
		}
		
		return -1;
	}
	
	public static boolean isGlobalVar(String name){
		for (int i = 0; i < symbolTable.size(); i++){
			if (symbolTable.get(i).getName().equalsIgnoreCase(name)){
				return symbolTable.get(i).getGlobal();
			}
		}
		return false;
	}
	
	// Some ArrayLists we'll use when checking constants and vars
	//private static ArrayList<String> constants = new ArrayList<String>();
	//private static ArrayList<String> constValues = new ArrayList<String>();
	//private static ArrayList<String> variables = new ArrayList<String>();
	//private static ArrayList<String> varValues = new ArrayList<String>();
	
	// This is the symbol table for storing variable names. The values are stored in a separate arrayList
	private static ArrayList<Symbol> symbolTable = new ArrayList<Symbol>();
	private static int inputIndex;

}