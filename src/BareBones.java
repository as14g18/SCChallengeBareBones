import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BareBones {
	private HashMap<String, Integer> variableList;
	private StackHolder stackHolder;
	private String[] sourceCode;
	private int lineNum;
	
	private FileManager fileManager;
		
	public BareBones() {
		this.variableList = new HashMap<String, Integer>();
		this.lineNum = 0;
		this.fileManager = new FileManager();
		this.stackHolder = new StackHolder();
	}
	
	public void initialize() {
		try {
			sourceCode = fileManager.readFile("triple.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while (lineNum < sourceCode.length) {
			interpret(sourceCode[lineNum]);
			lineNum++;
		}
		
		if (!stackHolder.allStacksAreEmpty()) {
			throwError("UNCLOSED STATEMENT. OUTPUT MAY BE WRONG"); // Ensures all while/if statements are properly closed
		}
	}
	
	private void throwError(String reason) { // Error handling different scenarios
		if (reason == "interpret") {
			System.out.println("ERROR INTERPRETING [" + sourceCode[lineNum].trim() + "] AT LINE " + (lineNum + 1));
		} else if (reason == "negative") {
			System.out.println("ERROR: CANNOT ASSIGN NON NEGATIVE INTEGER TO VARIABLE");
		} else {
			System.out.println("ERROR: " + reason);
		}
		System.exit(1);
	}
	
	private void resetIfNull(String value) { // Assigns variable to zero if it was previously unassigned
		if (variableList.get(value) == null) {
			variableList.put(value, 0);
		}
	}
	
	private void jumpForwardUntil(String value) {
		int skipNested = -1;
		String branchType = "";
		
		if (value.split(" ")[1].equals("if") || value.split(" ")[0].equals("else")) {
			branchType = "if";
		} else {
			branchType = "while";
		}
		
		try {
			String trimmedLine = sourceCode[lineNum].trim();
			while (true) {
				if (trimmedLine.split(" ")[0].equals(branchType)) {
					skipNested++;
				}
				if (trimmedLine.equals(value)) {
					if (skipNested == 0) {
						break;
					} else {
						skipNested--;
					}
				}
				lineNum++;
				trimmedLine = sourceCode[lineNum].trim();
			}
			
			lineNum--;
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
				+ "(print \"(.*)\")|"						  // prints operand
				+ "(set (.*) to [0-9]*)");					  // sets value of variable to number
		Matcher m = p.matcher(line);
		if (!m.matches()) {
			throwError("interpret"); // terminates program if syntax is wrong
		}
		
		String[] splitLine = line.split(" ");
		
		if (splitLine[0].equals("while")) {
			resetIfNull(splitLine[1]);
			
			if (splitLine[3].equals(variableList.get(splitLine[1]).toString())) { // Deals with conditions that are already satisfied
				jumpForwardUntil("end while");
				return;
			}
			
			String[] condition = {splitLine[1], splitLine[3]};
			stackHolder.getStackCondition().push(condition);
			stackHolder.getStackLine().push(lineNum);
			return;
			
		} else if (splitLine[0].equals("if")) {
			resetIfNull(splitLine[1]);
			if (splitLine[3].equals(variableList.get(splitLine[1]).toString())) {
				stackHolder.getStackIf().push(true);
			} else {
				jumpForwardUntil("else do");
				stackHolder.getStackIf().push(false);
			}
			return;
			
		} else if (line.equals("else do")) {
			if (stackHolder.getStackIf().peek()) {
				jumpForwardUntil("end if");
				stackHolder.getStackIf().pop();
			}
			return;
			
		} else if (line.equals("end if")) {
			stackHolder.getStackIf().pop();
			return;
			
		} else if (splitLine[0].equals("clear")) {
			variableList.put(splitLine[1], 0);
			
		} else if ((line).equals("end while")) {
			try {
				if (variableList.get(stackHolder.getStackCondition().peek()[0]).toString().equals(stackHolder.getStackCondition().peek()[1])) {			
					stackHolder.getStackCondition().pop();
					stackHolder.getStackLine().pop();
					return; // ends the while loop if condition has been met
				}
			} catch (java.util.EmptyStackException e) { // throws error if end is called before while loop is initialised
				throwError("interpret");
			}
			lineNum = stackHolder.getStackLine().peek();
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
				leftValue = variableList.get(splitLine[1]);
				rightValue = variableList.get(splitLine[3]);
			} catch (NullPointerException e){
				throwError("UNINITIALISED VARIABLE");
			}
			if (splitLine[2].equals("+")) {
				variableList.put(splitLine[1], leftValue + rightValue);
			} else if (splitLine[2].equals("-")) {
				if (leftValue - rightValue < 0) {
					throwError("negative");
				} else {
					variableList.put(splitLine[1], leftValue - rightValue);
				}
			} else if (splitLine[2].equals("*")) {
				variableList.put(splitLine[1], leftValue * rightValue);
			} else if (splitLine[2].equals("/")) {
				if (rightValue != 0) {
					variableList.put(splitLine[1], leftValue / rightValue);
				} else {
					throwError("DIVISION BY ZERO");
				}
			} else if (splitLine[2].equals("%")) {
				variableList.put(splitLine[1], leftValue % rightValue);
			}
			
		} else if (splitLine[0].equals("incr") || splitLine[0].equals("decr")) {
			resetIfNull(splitLine[1]);
			
			if (splitLine[0].equals("incr")) {
				variableList.put(splitLine[1], variableList.get(splitLine[1]) + 1);
			} else if (splitLine[0].equals("decr")){
				if (variableList.get(splitLine[1]) >= 0) {
					variableList.put(splitLine[1], variableList.get(splitLine[1]) - 1);
				} else {
					throwError("negative");
				}
			}
			
		} else if (splitLine[0].equals("copy")) {
			resetIfNull(splitLine[3]);
			resetIfNull(splitLine[1]);
			variableList.put(splitLine[3], variableList.get(splitLine[1]));
			
		} else if (splitLine[0].equals("set")) {
			resetIfNull(splitLine[1]);
			variableList.put(splitLine[1], Integer.parseInt(splitLine[3]));
		}
		
		System.out.println((Arrays.asList(variableList)).toString().replace("[{", "").replace("}]", ""));
	}
}