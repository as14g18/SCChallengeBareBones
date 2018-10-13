import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

public class BareBones {
	private HashMap<String, Integer> variableList;
	private int lineNum;
	private String[] sourceCode;
	private Stack<Integer> lineStack;
	
	public BareBones() throws IOException {
		this.variableList = new HashMap<String, Integer>();
		this.lineStack = new Stack<Integer>();
		this.lineNum = 0;
	}
	
	public void initialize() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("file.txt"));
		String line = br.readLine();
		StringBuilder builder = new StringBuilder();
		
		while (line != null) {
			builder.append(line);
			line = br.readLine();
		}
		this.sourceCode = builder.toString().split(";");
		
		while (this.lineNum < this.sourceCode.length) {
			System.out.println(this.sourceCode[this.lineNum]); //////////
			interpret(this.sourceCode[this.lineNum]);
			this.lineNum++;
		}
		
		br.close();
	}
	
	private void interpret(String line) {
		line = line.trim();
		String[] splitLine = line.split(" ");
		
		if (splitLine[0].equals("while")) {
			this.lineStack.push(this.lineNum + 1);
			this.lineNum++;
			while (true) {
				if ((this.sourceCode[this.lineNum].equals("end"))) {
					if (this.variableList.get(splitLine[1]).toString().equals(splitLine[3])) {
						this.lineStack.pop();
						return;
					}
					this.lineNum = this.lineStack.peek();
				} else {
					interpret(this.sourceCode[this.lineNum]);
					this.lineNum++;
				}
			}
		} else if (splitLine[0].equals("clear")) {
			this.variableList.put(splitLine[1], 0);
		} else {
			if (this.variableList.get(splitLine[1]) == null) {
				System.out.println("ERROR: ATTEMPTING TO MODIFY UNASSIGNED VARIABLE");
				return;
			}
			String value = splitLine[1];
			if (splitLine[0].equals("incr")) {
				this.variableList.put(value, this.variableList.get(value) + 1);
			} else if (splitLine[0].equals("decr")){
				this.variableList.put(value, this.variableList.get(value) - 1);
			}
		}
		
		System.out.println(Arrays.asList(this.variableList));
	}
}
