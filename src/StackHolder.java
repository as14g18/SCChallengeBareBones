import java.util.Stack;

public class StackHolder {
	private Stack<Integer> lineStack;
	private Stack<String[]> conditionStack;
	public Stack<Boolean> ifStack;
	
	public StackHolder() {
		this.lineStack = new Stack<Integer>();
		this.conditionStack = new Stack<String[]>();
		this.ifStack = new Stack<Boolean>();
	}
	
	public Stack<Integer> getStackLine() {
		return lineStack;
	}
	
	public Stack<String[]> getStackCondition() {
		return conditionStack;
	}
	
	public Stack<Boolean> getStackIf() {
		return ifStack;
	}
	
	public Boolean allStacksAreEmpty() {
		return lineStack.isEmpty() && conditionStack.isEmpty() && ifStack.isEmpty();
	}
}