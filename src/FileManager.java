import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileManager {	
	public String[] readFile(String file) throws IOException  {
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder builder = new StringBuilder();
		String line = br.readLine();
		
		while (line != null) {
			builder.append(line);
			line = br.readLine();
		}
		
		String[] sourceCode = builder.toString().split(";"); // Loads entire source code, allows random access to code
		br.close(); // todo: throw error if last character is not ;
		
		return sourceCode;
	}
}
