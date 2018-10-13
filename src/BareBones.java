import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class BareBones {
	private HashMap<String, Integer> variableList;
	
	public BareBones() throws IOException {
		this.variableList = new HashMap<String, Integer>();
		initialize();
	}
	
	public void initialize() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("file.txt"));
		String line = br.readLine();
		
		while (line != null) {
			interpret(line);
			line = br.readLine();
		}
	}
	
	public void interpret(String line) {
		String[] splitLine = line.split(" ");
		
		if (splitLine[0].equals("while")) {
			
		} else if (splitLine[0].equals("clear")) {
			this.variableList.put(splitLine[1], 0);
		} else {
			if (this.variableList.get(splitLine[1]) == null) {
				System.out.println("ERROR IN LINE");
			}
			String value = splitLine[1];
			if (splitLine[0] == "incr") {
				this.variableList.put(value, this.variableList.get(value) + 1);
			} else { // if "decr"
				this.variableList.put(value, this.variableList.get(value) - 1);
			}
		}
	}
}
