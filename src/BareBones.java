import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BareBones {
	private HashMap<String, Integer> variableList;
	private int lineNum;
	private String[] sourceCode;
	private Stack<Integer> lineStack;
	private Stack<String[]> conditionStack;
	private Stack<Boolean> ifStack;
		
	public BareBones() throws IOException {
		this.variableList = new HashMap<String, Integer>();
		this.lineStack = new Stack<Integer>();
		this.conditionStack = new Stack<String[]>();
		this.ifStack = new Stack<Boolean>();
		this.lineNum = 0;
	}
	
	public void initialize() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("triple.txt"));
		String line = br.readLine();
		StringBuilder builder = new StringBuilder();
		
		while (line != null) {
			builder.append(line);
			line = br.readLine();
		}
		this.sourceCode = builder.toString().split(";"); // Loads entire source code, allows random access to code
		
		br.close(); // todo: throw error if last character is not ;	
		
		while (this.lineNum < this.sourceCode.length) {
			interpret(this.sourceCode[this.lineNum]);
			this.lineNum++;
		}
		
		if (!(lineStack.isEmpty() && conditionStack.isEmpty() && ifStack.isEmpty())) {
			throwError("UNCLOSED STATEMENT. OUTPUT MAY BE WRONG"); // Ensures all while/if statements are properly closed
		}
	}
	
	public void throwError(String reason) { // Error handling different scenarios
		if (reason == "interpret") {
			System.out.println("ERROR INTERPRETING [" + this.sourceCode[this.lineNum].trim() + "] AT LINE " + ((int)this.lineNum + 1));
		} else if (reason == "negative") {
			System.out.println("ERROR: CANNOT ASSIGN NON NEGATIVE INTEGER TO VARIABLE");
		} else {
			System.out.println("ERROR: " + reason);
		}
		System.exit(1);
	}
	
	public void resetIfNull(String value) { // Assigns variable to zero if it was previously unassigned
		if (this.variableList.get(value) == null) {
			this.variableList.put(value, 0);
		}
	}
	
	public void jumpForwardUntil(String value) {
		try {
			String trimmedLine = this.sourceCode[this.lineNum].trim();
			while (!trimmedLine.equals(value)) {
				trimmedLine = this.sourceCode[this.lineNum].trim();
				this.lineNum++;
			}
			
			this.lineNum--;
		} catch (ArrayIndexOutOfBoundsException e) {
			throwError("UNCLOSED STATEMENT");
		}
	}
	
	private void interpret(String line) {
		line = line.trim();
		Pattern p = Pattern.compile("" 			  			  // -- LIST OF VALID COMMANDS --
				+ "(incr (.*))|" 				  			  // increment by one
				+ "(decr (.*))|" 				  			  // decrement by one
				+ "(clear (.*))|" 	  			  			  // set variable value to zero
				+ "(while (.*) not ([0-9]*) do)|" 			  // open while loop
				+ "(end while)|" 				  			  // close while loop
				+ "(#(.*))|" 					  			  // comments
				+ "(if (.*) is ([0-9]*) do)|" 	  			  // open if statement
				+ "(else do)|"					  			  // else statements
				+ "(end if)|"					  			  // close if statement
				+ "(calculate (.*) (\\+|\\-|\\*|/|%) (.*))|"  // perform operation on left operand
				+ "(copy (.*) to (.*))|"  		  			  // copy value of one variable to other
				+ "(print \"(.*)\")");						  // prints operand
		Matcher m = p.matcher(line);
		if (!m.matches()) {
			throwError("interpret"); // terminates program if syntax is wrong
		}
		
		String[] splitLine = line.split(" ");
		
		if (splitLine[0].equals("while")) {
			resetIfNull(splitLine[1]);
			
			if (splitLine[3].equals(this.variableList.get(splitLine[1]).toString())) { // Deals with conditions that are already satisfied
				jumpForwardUntil("end while");
				return;
			}
			
			String[] condition = {splitLine[1], splitLine[3]};
			this.conditionStack.push(condition);
			this.lineStack.push(this.lineNum);
			return;
		} else if (splitLine[0].equals("if")) {
			resetIfNull(splitLine[1]);
			if (splitLine[3].equals(this.variableList.get(splitLine[1]).toString()) ) {
				this.ifStack.push(true);
			} else {
				jumpForwardUntil("else do");
				this.ifStack.push(false);
			}
			return;
		} else if (line.equals("else do")) {
			if (this.ifStack.peek()) {
				jumpForwardUntil("end if");
			}
			this.ifStack.pop();
			return;
		} else if (line.equals("end if")) {
			this.ifStack.pop();
			return;
		} else if (splitLine[0].equals("clear")) {
			this.variableList.put(splitLine[1], 0);
		} else if ((line).equals("end while")) {
			try {
				if (this.variableList.get(this.conditionStack.peek()[0]).toString().equals(this.conditionStack.peek()[1])) {			
					conditionStack.pop();
					this.lineStack.pop();
					return; // ends the while loop if condition has been met
				}
			} catch (java.util.EmptyStackException e) { // throws error if end is called before while loop is initialised
				throwError("interpret");
			}
			this.lineNum = lineStack.peek();
			return;
		} else if (splitLine[0].equals("#")){
			return; // do nothing
		} else if (splitLine[0].equals("print")) {
			System.out.println(splitLine[1].replaceAll("\"", ""));
			return;
		} else if (splitLine[0].equals("calculate")) {
			int leftValue = 0;
			int rightValue = 0;
			try {
				leftValue = this.variableList.get(splitLine[1]);
				rightValue = this.variableList.get(splitLine[3]);
			} catch (NullPointerException e){
				throwError("UNINITIALISED VARIABLE");
			}
			if (splitLine[2].equals("+")) {
				this.variableList.put(splitLine[1], leftValue + rightValue);
			} else if (splitLine[2].equals("-")) {
				if (leftValue - rightValue < 0) {
					throwError("negative");
				} else {
					this.variableList.put(splitLine[1], leftValue - rightValue);
				}
			} else if (splitLine[2].equals("*")) {
				this.variableList.put(splitLine[1], leftValue * rightValue);
			} else if (splitLine[2].equals("/")) {
				if (rightValue != 0) {
					this.variableList.put(splitLine[1], leftValue / rightValue);
				} else {
					throwError("DIVISION BY ZERO");
				}
			} else if (splitLine[2].equals("%")) {
				this.variableList.put(splitLine[1], leftValue % rightValue);
			}
		} else if (splitLine[0].equals("incr") || splitLine[0].equals("decr")) {
			resetIfNull(splitLine[1]);
			
			if (splitLine[0].equals("incr")) {
				this.variableList.put(splitLine[1], this.variableList.get(splitLine[1]) + 1);
			} else if (splitLine[0].equals("decr")){
				if (this.variableList.get(splitLine[1]) >= 0) {
					this.variableList.put(splitLine[1], this.variableList.get(splitLine[1]) - 1);
				} else {
					throwError("negative");
				}
			}
		} else if (splitLine[0].equals("copy")) {
			resetIfNull(splitLine[3]);
			resetIfNull(splitLine[1]);
			this.variableList.put(splitLine[3], this.variableList.get(splitLine[1]));
		}
		
		System.out.println((Arrays.asList(this.variableList)).toString().replace("[{", "").replace("}]", ""));
	}
}