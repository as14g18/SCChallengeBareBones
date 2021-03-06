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
		
	public BareBones() throws IOException {
		this.variableList = new HashMap<String, Integer>();
		this.lineStack = new Stack<Integer>();
		this.conditionStack = new Stack<String[]>();
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
		
		while (this.lineNum < this.sourceCode.length) {
			// System.out.println(this.sourceCode[this.lineNum]);
			interpret(this.sourceCode[this.lineNum]);
			this.lineNum++;
		}
		
		br.close();
	}
	
	public void throwError() {
		System.out.println("ERROR INTERPRETING [" + this.sourceCode[this.lineNum].trim() + "] AT LINE " + ((int)this.lineNum + 1));
		System.exit(1);;
	}
	
	private void interpret(String line) {
		Pattern p = Pattern.compile("(incr (.*))|(decr (.*))|(clear (.*))|end|(while (.*) not ([0-9]*) do)");
		Matcher m = p.matcher(line);
		if (!m.matches()) {
			throwError(); // terminates program if syntax is wrong
		}
		
		line = line.trim();
		String[] splitLine = line.split(" ");
		
		if (splitLine[0].equals("while")) {
			String[] condition = new String[2];
			condition[0] = splitLine[1];
			condition[1] = splitLine[3];
			
			if (this.variableList.get(condition[0]) == null) {
				this.variableList.put(condition[0], 0); // Assigns variable to zero if it was previously unassigned
			}
			
			if (condition[1].equals(this.variableList.get(condition[0]).toString())) { // Deals with conditions that are already satisfied
				String trimmedLine = this.sourceCode[this.lineNum].trim();
				while (!trimmedLine.equals("end")) {
					trimmedLine = this.sourceCode[this.lineNum].trim();
					this.lineNum++;
				}
				
				lineNum--;
				return;
			}
			
			conditionStack.push(condition);
			lineStack.push(this.lineNum);
		} else if (splitLine[0].equals("clear")) {
			this.variableList.put(splitLine[1], 0);
		} else if (splitLine[0].equals("end")) {
			try {
				if (this.variableList.get(this.conditionStack.peek()[0]).toString().equals(this.conditionStack.peek()[1])) {			
					conditionStack.pop();
					lineStack.pop();
					return; // ends the while loop if condition has been met
				}
			} catch (java.util.EmptyStackException e) { // throws error if end is called before while loop is initialised
				throwError();
			}
			
			this.lineNum = lineStack.peek();
		} else {
			String value;
			value = splitLine[1];
			
			if (this.variableList.get(value) == null) {
				this.variableList.put(value, 0); // Assigns variable to zero if it was previously unassigned
			}
			
			if (splitLine[0].equals("incr")) {
				this.variableList.put(value, this.variableList.get(value) + 1);
			} else if (splitLine[0].equals("decr")){
				if (this.variableList.get(value) >= 0) {
					this.variableList.put(value, this.variableList.get(value) - 1);
				} else {
					System.out.println("ERROR: CANNOT ASSIGN NON NEGATIVE INTEGER TO VARIABLE");
					System.exit(1);
				}
			}
		}
		
		System.out.println((Arrays.asList(this.variableList)).toString().replace("[{", "").replace("}]", ""));
	}
}
