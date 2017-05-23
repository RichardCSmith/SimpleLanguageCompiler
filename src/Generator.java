import java.util.ArrayList;
import java.util.Stack;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.LinkedList;

public class Generator {
	
	public Generator (ArrayList<Token> input, ArrayList<Symbol> symbolTable){
		this.input = input;
		this.symbolTable = symbolTable;
		this.inputIndex = 0;
		this.placeInMemory = 100;
		this.stackPointer = 100;
	}
	
	public void createPALIFile(){
		try {
			writer = new PrintWriter("output.yasl");
			generatePALI();
			writer.close();
		} catch (IOException e) {
			System.out.println("IOException in making file.");
			System.exit(0);
		}
	}
	
	public void generatePALI(){
		writer.println("$junk #1");
		writer.println("$main movw SP r0");
		genConstsVars();
		ArrayList<Token> completeProcs = genProc();
		genAfterBegin();
		writer.println("inb $junk :Pause");
		writer.println("end");
		if (!completeProcs.isEmpty()){
			printProcs(completeProcs);
		}
	}
	
	public static void generateProc(){
		genConstsVars();
		ArrayList<Token> completeProcs = genProc();
		genAfterBegin();
		if (!completeProcs.isEmpty()){
			printProcs(completeProcs);
		}
	}

	public static void genConstsVars(){
		while (input.get(inputIndex).type != TokenType.BEGIN && input.get(inputIndex).type != TokenType.PROC){
			if (input.get(inputIndex).type == TokenType.CONST){
				for (int i = 0; i < symbolTable.size(); i++) {
					if (symbolTable.get(i).getName().equalsIgnoreCase(input.get(inputIndex + 1).lexeme)){
						symbolTable.get(i).placeInMemory = placeInMemory;
						break;
					}
				}
				
				writer.println("movw #" + input.get(inputIndex+3).lexeme + " @SP");
				writer.println("addw #4 SP");

				placeInMemory += 4;
				stackPointer += 4;
				
			} else if (input.get(inputIndex).type == TokenType.VAR) {
				for (int i = 0; i < symbolTable.size(); i++) {
					if (symbolTable.get(i).getName().equalsIgnoreCase(input.get(inputIndex + 1).lexeme)){
						symbolTable.get(i).placeInMemory = placeInMemory;
						break;
					}
				}

				writer.println("addw #4 SP");
				
				placeInMemory += 4;
				stackPointer += 4;
			}
			inputIndex++;
		}
	}
	
	public static ArrayList<Token> genProc(){
		ArrayList<Token> procs = new ArrayList<Token>();
		if (input.get(inputIndex).type == TokenType.PROC){
			while (true) {
				procs.add(input.get(inputIndex));
				
				if (input.get(inputIndex).type == TokenType.BEGIN){
					beginEndDiff++;
				}
				
				if (input.get(inputIndex).type == TokenType.END){
					beginEndDiff--;
				}
				
				if (input.get(inputIndex).type == TokenType.END && beginEndDiff == 0){
					inputIndex+=2;
					break;
				}
				inputIndex++;
			}
		}
		return procs;
	}
	
	public static void printProcs(ArrayList<Token> procs){
		input = procs;
		inputIndex = 0;
		inputIndex++;
		writer.println("");
		writer.println("");
		writer.println("$" + input.get(inputIndex).lexeme);
		inputIndex+=2;
		generateProc();
	}
	
	public static void genAfterBegin() {
		inputIndex++;
		while (input.get(inputIndex).type != TokenType.END){
			// Print works
			if (input.get(inputIndex).type == TokenType.PRINT){
				inputIndex++;
				if (input.get(inputIndex).type == TokenType.STRING){
					for (int i = 0; i < input.get(inputIndex).lexeme.length(); i++){
						if (input.get(inputIndex).lexeme.charAt(i) != ' '){
							writer.println("outw ^" + input.get(inputIndex).lexeme.charAt(i));
						} else {
							writer.println("outw #32");
						}
					}
					writer.println("outw #10");
					if (input.get(inputIndex+1).type == TokenType.SEMI){
						inputIndex++;
					}
				} else {
					if (input.get(inputIndex).type == TokenType.ID){
						for (int i = 0; i < symbolTable.size(); i++){
							if (symbolTable.get(i).getName().equalsIgnoreCase(input.get(inputIndex).lexeme)){
								int location = symbolTable.get(i).placeInMemory;
								location -= 100;
								if (location > 0){
									writer.println("outw +" + location + "@r0");
									if (input.get(inputIndex+1).type == TokenType.SEMI){
										inputIndex++;
									}
								} else {
									writer.println("outw @r0");
									if (input.get(inputIndex+1).type == TokenType.SEMI){
										inputIndex++;
									}
								}
							}
						}
					} else if (input.get(inputIndex).type == TokenType.NUM){
						for (int i = 0; i < input.get(inputIndex).lexeme.length(); i++){
							writer.println("outw ^" + input.get(inputIndex).lexeme.charAt(i));
						}
						writer.println("outw #10");
						if (input.get(inputIndex+1).type == TokenType.SEMI){
							inputIndex++;
						}
					}
				}
				inputIndex++;
			// Prompt works
			} else if (input.get(inputIndex).type == TokenType.PROMPT){
				if (input.get(inputIndex + 2).type == TokenType.COMMA){
					// Prompt with variable, looking for input
					inputIndex++;
					for (int i = 0; i < input.get(inputIndex).lexeme.length(); i++){
						if (input.get(inputIndex).lexeme.charAt(i) != ' '){
							writer.println("outw ^" + input.get(inputIndex).lexeme.charAt(i));
						} else {
							writer.println("outw #32");
						}
					}
					writer.println("outw #10");
					inputIndex+=2;
					for (int i = 0; i < symbolTable.size(); i++) {
						if (symbolTable.get(i).getName().equalsIgnoreCase(input.get(inputIndex).lexeme)){
							int location = symbolTable.get(i).placeInMemory;
							location -= 100;
							if (location > 0){
								writer.println("inw +" + location + "@r0");
							} else {
								writer.println("inw @r0");
							}
							break;
						}
					}
					if (input.get(inputIndex+1).type == TokenType.SEMI){
						inputIndex++;
					}
				} else {
					// Prompt without variable, not looking for input
					inputIndex++;
					for (int i = 0; i < input.get(inputIndex).lexeme.length(); i++){
						writer.println("outw ^" + input.get(inputIndex).lexeme.charAt(i));
					}
					writer.println("outw #10");
					writer.println("inb $junk");
					if (input.get(inputIndex+1).type == TokenType.SEMI){
						inputIndex++;
					}
				}
				inputIndex++;
			// Basic expressions work
			} else if (input.get(inputIndex).type == TokenType.ID){
				Symbol thisOne = new Symbol();
				thisOne = null;
				for (int i = 0; i < symbolTable.size(); i++){
					if (symbolTable.get(i).getName().equalsIgnoreCase(input.get(inputIndex).lexeme)){
						thisOne = symbolTable.get(i);
						break;
					}
				}
				inputIndex+=2;
				boolean firstOp = true;
				Token holder1 = null;
				int savedPriority = 0;
				int currPriority = 0;
				LinkedList<Token> postFix = new LinkedList<Token>();
				while (input.get(inputIndex).type != TokenType.SEMI && input.get(inputIndex).type != TokenType.END){
					if (input.get(inputIndex).type == TokenType.NUM
							|| input.get(inputIndex).type == TokenType.CONST
							|| input.get(inputIndex).type == TokenType.VAR){
						postFix.add(input.get(inputIndex));
						inputIndex++;
					} else {
						if (firstOp){
							holder1 = input.get(inputIndex);
							firstOp = false;
							inputIndex++;
						} else {
							if (holder1.type == TokenType.PLUS || holder1.type == TokenType.MINUS){
								savedPriority = 1;
							} else {
								savedPriority = 2;
							}
							if (input.get(inputIndex).type == TokenType.PLUS || input.get(inputIndex).type == TokenType.MINUS){
								currPriority = 1;
							} else {
								currPriority = 2;
							}
							if (savedPriority >= currPriority){
								postFix.add(holder1);
								holder1 = input.get(inputIndex);
								inputIndex++;
							} else {
								postFix.add(input.get(inputIndex+1));
								postFix.add(input.get(inputIndex));
								inputIndex+=2;
							}
						}
					}
				}
				if (holder1 != null){
					postFix.add(holder1);
				}
				if (input.get(inputIndex).type == TokenType.SEMI){
					inputIndex++;
				}
				
				// PostFix complete, begin code generation
				int i = 0;
				Stack<Token> myStack = new Stack<Token>();
				while (!postFix.isEmpty()){ // TODO Add something to deal with a stack size of 1
					if (postFix.get(0).type == TokenType.NUM
							|| postFix.get(0).type == TokenType.ID){
						myStack.push(postFix.remove());
					} else {
						Token temp1 = myStack.pop();
						Token temp0 = myStack.pop();
						String string1 = "";
						String string0 = "";
						if (temp1.type == TokenType.QQQ){
							string1 = temp1.lexeme;
						} else if (temp1.type == TokenType.NUM){
							string1 = "#" + temp1.lexeme;
						} else {
							for (int k = 0; k < symbolTable.size(); k++){
								if (symbolTable.get(k).getName().equalsIgnoreCase(temp1.lexeme)){
									string1 = "+" + (symbolTable.get(k).placeInMemory - 100) + "@r0";
									break;
								}
							}
						}
						if (temp0.type == TokenType.QQQ){
							string0 = temp0.lexeme;
						} else if (temp0.type == TokenType.NUM){
							string0 = "#" + temp0.lexeme;
						} else {
							for (int k = 0; k < symbolTable.size(); k++){
								if (symbolTable.get(k).getName().equalsIgnoreCase(temp0.lexeme)){
									string0 = "+" + (symbolTable.get(k).placeInMemory - 100) + "@r0";
									break;
								}
							}
						}
						if (postFix.get(0).type == TokenType.PLUS){
							writer.println("movw " + string0 + " $" + i);
							writer.println("addw " + string1 + " $" + i);
						} else if (postFix.get(0).type == TokenType.MINUS){
							writer.println("movw " + string0 + " $" + i);
							writer.println("subw " + string1 + " $" + i);
						} else if (postFix.get(0).type == TokenType.STAR){
							writer.println("movw " + string0 + " $" + i);
							writer.println("mulw " + string1 + " $" + i);
						} else if (postFix.get(0).type == TokenType.DIV){
							writer.println("movw " + string0 + " $" + i);
							writer.println("divw " + string1 + " $" + i);
						} else if (postFix.get(0).type == TokenType.MOD){
							writer.println("movw " + string0 + " $" + i);
							writer.println("divw " + string1 + " $" + i);
							writer.println("mulw " + string1 + " $" + i);
							writer.println("movw " + "$" + i + " $" + (i+1));
							writer.println("movw " + string0 + " $" + i);
							writer.println("subw " + "$" + (i+1) + " $" + i);
						} else {
							System.out.println("I have no idea how this happened.");
							System.exit(0);
						}
						postFix.remove();
						if (!myStack.isEmpty() || !postFix.isEmpty()){
							Token newToken = new Token(0, 0, TokenType.QQQ, "");
							newToken.lexeme = "$" + i;
							myStack.push(newToken);
						} else {
							writer.println("movw " + "$" + i + " +" + (thisOne.placeInMemory - 100) + "@r0");
						}
						i++;
					}
				}
				
				
			} else if (input.get(inputIndex).type == TokenType.PROC){
				writer.println("call #0 $" + input.get(inputIndex).lexeme);
			} else {
				System.out.println("Error, improper stuff between begin and end");
				System.out.println(input.get(inputIndex-1).type);
				System.out.println(input.get(inputIndex).type);
				System.out.println(input.get(inputIndex+1).type);
				System.exit(0);
			}
			//inputIndex++;
		}
		
	}
	
	private static int beginEndDiff = 0;
	private static PrintWriter writer;
	private static int placeInMemory;
	private static int stackPointer;
	private static int inputIndex;
	private static ArrayList<Token> input;
	private static ArrayList<Symbol> symbolTable;
}