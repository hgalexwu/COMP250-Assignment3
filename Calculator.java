package a3;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import java.util.*;


/* ACADEMIC INTEGRITY STATEMENT
 * 
 * By submitting this file, we state that all group members associated
 * with the assignment understand the meaning and consequences of cheating, 
 * plagiarism and other academic offenses under the Code of Student Conduct 
 * and Disciplinary Procedures (see www.mcgill.ca/students/srr for more information).
 * 
 * By submitting this assignment, we state that the members of the group
 * associated with this assignment claim exclusive credit as the authors of the
 * content of the file (except for the solution skeleton provided).
 * 
 * In particular, this means that no part of the solution originates from:
 * - anyone not in the assignment group
 * - Internet resources of any kind.
 * 
 * This assignment is subject to inspection by plagiarism detection software.
 * 
 * Evidence of plagiarism will be forwarded to the Faculty of Science's disciplinary
 * officer.
 */

/**
 * Main class for the calculator: creates the GUI for the calculator 
 * and responds to presses of the "Enter" key in the text field 
 * and clicking on the button. You do not need to understand or modify 
 * the GUI code to complete this assignment. See instructions below the line
 * BEGIN ASSIGNMENT CODE
 * 
 * @author Martin P. Robillard 26 February 2015
 *
 */
@SuppressWarnings("serial")
public class Calculator extends JFrame implements ActionListener
{
	private static final Color LIGHT_RED = new Color(214,163,182);

	private final JTextField aText = new JTextField(40);

	public Calculator()
	{
		setTitle("COMP 250 Calculator");
		setLayout(new GridLayout(2, 1));
		setResizable(false);
		add(aText);
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				aText.setText("");		
				aText.requestFocusInWindow();
			}
		});
		add(clear);

		aText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		aText.addActionListener(this);

		aText.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				aText.getHighlighter().removeAllHighlights();	
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				aText.getHighlighter().removeAllHighlights();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				aText.getHighlighter().removeAllHighlights();
			}
		});

		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Run this main method to start the calculator
	 * @param args Not used.
	 */
	public static void main(String[] args)
	{
		new Calculator();
	}

	/* 
	 * Responds to events by the user. Do not modify this method.
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( aText.getText().contains("="))
		{
			aText.setText("");		
		}
		else
		{
			Queue<Token> expression = processExpression(aText.getText());
			if( expression != null )
			{
				String result = evaluateExpression(expression);
				if( result != null )
				{
					aText.setText(aText.getText() + " = " + result);
				}
			}
		}
	}

	/**
	 * Highlights a section of the text field with a color indicating an error.
	 * Any change to the text field erase the highlights.
	 * Call this method in your own code to highlight part of the equation to 
	 * indicate an error.
	 * @param pBegin The index of the first character to highlight.
	 * @param pEnd The index of the first character not to highlight.
	 */
	public void flagError( int pBegin, int pEnd )
	{
		assert pEnd > pBegin;
		try
		{
			aText.getHighlighter().addHighlight(pBegin, pEnd, new DefaultHighlighter.DefaultHighlightPainter(LIGHT_RED));
		}
		catch(BadLocationException e)
		{

		}
	}

	/******************** BEGIN ASSIGNMENT CODE ********************/

	/**
	 * Tokenizes pExpression (see Tokenizer, below), and 
	 * returns a Queue of Tokens that describe the original 
	 * mathematical expression in reverse Polish notation (RPN).
	 * Flags errors and returns null if the expression
	 * a) contains any symbol except spaces, digits, round parentheses, or operators (+,-,*,/)
	 * b) contains unbalanced parentheses
	 * c) contains invalid combinations of parentheses and numbers, e.g., 2(3)(4)
	 * 
	 * @param pExpression A string.
	 * @return The tokenized expression transformed in RPN
	 */
	private Queue<Token> processExpression(String pExpression)
	{
		Queue<Token> outputQueue = new LinkedList<Token>();
		Stack<Token> operatorStack = new Stack<>();
		Tokenizer tokenized = new Tokenizer();
		try{//catches exceptions and highlights them
			tokenized.tokenize(pExpression);//tokenizes pExpression
			LinkedList<Token> tokenList = (LinkedList<Token>) tokenized.getTokenSequence();//cast the queue into a linkedlist
			for (Token token: tokenList)//enhanced for loop iterating through every tokens
			{
				if (token instanceof IntToken)//following the shunting yard algorithm
					outputQueue.add(token);
				else if (token instanceof OperatorToken)
				{
					if (!operatorStack.isEmpty())
					{
						//employs the compareTo method from the Operator Token class. returns negative nb if smaller, 0 if equal and positive nb if bigger.
						while(!operatorStack.isEmpty() && operatorStack.peek() instanceof OperatorToken && ((OperatorToken)token).compareTo((OperatorToken)operatorStack.peek()) <= 0)
							outputQueue.add(operatorStack.pop());
					}
					operatorStack.push(token);
				}
				else if (token instanceof ParenthesisToken)
				{
					if (((ParenthesisToken)token).getParenthesis())
						operatorStack.push(token);
					else
					{
						Token temp = null;
						boolean isLeftParenthesis = false;
						while (!isLeftParenthesis && !operatorStack.isEmpty())//searches the leftParenthesis
						{
							temp = operatorStack.pop();
							if (temp instanceof ParenthesisToken)
							{
								if (((ParenthesisToken)temp).getParenthesis())
									isLeftParenthesis = true;//termination condition of the while loop
								else
									outputQueue.add(temp);
							}
							else
								outputQueue.add(temp);
						}
						if (!isLeftParenthesis)//throws exception bcz no matching opening parenthesis
							throw new InvalidExpressionException(token.getEnd());
					}
				}
			}
			while (!operatorStack.isEmpty())
			{
				Token temp = operatorStack.pop();
				if (temp instanceof ParenthesisToken)
					throw new InvalidExpressionException(temp.getEnd());
				else//adds remaining operators to the outputQueue
					outputQueue.add(temp);
			}
		}
		catch (InvalidExpressionException e)
		{
			flagError(e.getPosition(), e.getPosition() +1);
			return null;
		}
		return outputQueue;
	}

	/**
	 * Assumes pExpression is a Queue of tokens produced as the output of processExpression.
	 * Evaluates the answer to the expression. The division operator performs a floating-point 
	 * division. 
	 * Flags errors and returns null if the expression is an invalid RPN expression e.g., 1+-
	 * @param pExpression The expression in RPN
	 * @return A string representation of the answer)
	 */
	private String evaluateExpression(Queue<Token> pExpression)
	{
		try{
			LinkedList<Token> expression = (LinkedList<Token>)pExpression;
			Stack<Double> integerStack = new Stack<>();
			if (expression.isEmpty())
				return null;
			else
			{
				for (Token token : expression)
				{
					if (token instanceof OperatorToken)
					{
						double sumRight = 0;//in double to support floating point division
						double sumLeft = 0;
						if (integerStack.isEmpty())//would mean that there are too many operators: ex, ++ and would throw exception
							throw new InvalidExpressionException(token.getStart());
						else
							sumRight = integerStack.pop();
						if (integerStack.isEmpty())
							throw new InvalidExpressionException(token.getStart());
						else
							sumLeft = integerStack.pop();
						double sum = 0;
						if (((OperatorToken)token).getValue() == '+')//does operations depending on operator
							sum = sumLeft+sumRight;
						else if (((OperatorToken)token).getValue() == '-')
							sum = sumLeft - sumRight;
						else if (((OperatorToken)token).getValue() == '*')
							sum = sumLeft*sumRight;
						else if (((OperatorToken)token).getValue() == '/')
						{
							if (sumRight == 0)//forbids division by zero
								throw new InvalidExpressionException(token.getStart()+1);
							sum = sumLeft/sumRight;
						}
						integerStack.push(sum);
					}
					else if (token instanceof IntToken)//adds to a stack
						integerStack.push(Double.parseDouble(((IntToken)token).getValue()));
				}
				double answer = integerStack.pop();
				if (integerStack.isEmpty())
					return String.format("%s", answer);
				else
					return null;
			}
		}
		catch (InvalidExpressionException e)
		{
			flagError(e.getPosition(), e.getPosition()+1);
			return null;
		}
	}

}


/**
 * Use this class as the root class of a hierarchy of token classes
 * that can represent the following types of tokens:
 * a) Integers (e.g., "123" "4", or "345") Negative numbers are not allowed as inputs
 * b) Parentheses '(' or ')'
 * c) Operators '+', '-', '/', '*' Hint: consider using the Comparable interface to support
 * comparing operators for precedence
 */
class Token
{
	private int aStart;
	private int aEnd;

	/**
	 * @param pStart The index of the first character of this token
	 * in the original expression.
	 * @param pEnd The index of the last character of this token in
	 * the original expression
	 */
	public Token( int pStart, int pEnd )
	{
		aStart = pStart;
		aEnd = pEnd;
	}

	public int getStart()
	{
		return aStart;
	}

	public int getEnd()
	{
		return aEnd;
	}

	public String toString()
	{
		return "{" + aStart + "," + aEnd + "}";
	}
}

class IntToken extends Token//subclass of Token that inherits all of its methods and fields
{
	private String value;
	//explicit constructor that adds the parameter values of the constructor to the superclass's fields
	public IntToken(int start, int end, String value)
	{
		super(start,end);//calls the superclass's constructor
		this.value = value;
	}

	public String getValue()//getter method that returns the value of the integer token
	{
		return this.value;
	}

}
class ParenthesisToken extends Token//subclass of token too
{
	private boolean isOpen;//if true, it is an opening parenthesis
	public ParenthesisToken(int start, int end, boolean isOpen)
	{
		super(start,end);
		this.isOpen = isOpen;
	}
	public boolean getParenthesis()//getter method that returns parenthesis
	{
		return this.isOpen;
	}
}
class OperatorToken extends Token implements Comparable<OperatorToken>//implements the interface comparable
{
	private char value;
	private int priority;
	public OperatorToken(int start, int end, char character)
	{
		super(start,end);
		this.value = character;
		if (character == '+' || character == '-')//+ and - characters have values of 1
			this.priority = 1;
		else//* and / characters have values of 4
			this.priority = 4;
	}
	@Override
	public int compareTo(OperatorToken operator)//overrides the comparble's compareTo method
	{
		return this.priority - operator.priority;
	}
	public char getValue(){
		return this.value;
	}
}


/**
 * Partial implementation of a tokenizer that can convert any valid string
 * into a stream of tokens, or detect invalid strings. Do not change the signature
 * of the public methods, but you can add private helper methods. The signature of the
 * private methods is there to help you out with basic ideas for a design (it is strongly 
 * recommended to use them). Hint: consider making your Tokenizer an Iterable<Token>
 */
class Tokenizer implements Iterable<Token>
{
	private Queue<Token> tokenSequence;
	private int index;
	private String integerString = "";
	private int startIndex;
	private int endIndex;
	private int length;
	/**
	 * Converts pExpression into a sequence of Tokens that are retained in
	 * a data structure in this class that can be made available to other objects.
	 * Every call to tokenize should clear the structure and start fresh.
	 * White spaces are tolerated but should be ignored (not converted into tokens).
	 * The presence of any illegal character should raise an exception.
	 * 
	 * @param pExpression An expression to tokenize. Can contain invalid characters.
	 * @throws InvalidExpressionException If any invalid character is detected or if parentheses
	 * are misused as operators.
	 */
	public void tokenize(String pExpression) throws InvalidExpressionException
	{
		this.tokenSequence = new LinkedList<>();//initialize the queue as a linkedlist
		length = pExpression.length();
		for (index=0; index<length; index++)//consume every characters 
			consume(pExpression.charAt(index));
		validate();//validate method
	}

	@Override
	public Iterator<Token> iterator(){
		return tokenSequence.iterator();
	}


	private void consume(char pChar) throws InvalidExpressionException
	{
		// Consume a single character in the input expression and deals with
		// it appropriately.
		if (pChar == ' ' && index!= length-1){//does nothing with empty spaces, also permits spaces trailing last token
		}
		else if (integerString != "")//checks if previous character was also an integer, if it is, do not create a new token yet
		{
			if (pChar >= 48 && pChar <= 57)//continues to add digits to the integer
			{
				integerString += pChar;
				endIndex = index;
				if (index == length-1)//check if its the last char, if it is, make it a token
					tokenSequence.add(new IntToken(startIndex,endIndex,integerString));
			}
			else//create integer token because the integer token is done
			{
				tokenSequence.add(new IntToken(startIndex,endIndex,integerString));//adds to linkedList
				integerString = "";//resets for next integer token
			}	
		}
		else if (pChar >= 48 && pChar <= 57)//captures the start index of the integer token
		{
			startIndex = index;
			endIndex = index;
			integerString += pChar;//adds it the integer token's string representation			
			if (index == length-1)
				tokenSequence.add(new IntToken(startIndex,index-1,integerString));
		}
		if (pChar == ' '){
		}
		else if (pChar == '(' || pChar == ')')
		{
			boolean isOpening = false;
			if (pChar =='(')
				isOpening = true;
			tokenSequence.add(new ParenthesisToken (index, index, isOpening));
		}
		else if (pChar == '*' || pChar == '/' || pChar == '+' || pChar == '-')
			tokenSequence.add(new OperatorToken(index,index,pChar));
		else if (!(pChar >= 48 && pChar <= 57))//if its an invalid character, will throw exception
			throw new InvalidExpressionException(index);
	}

	/**
	 * Detects if parentheses are misused
	 * @throws InvalidExpressionException
	 */
	private void validate() throws InvalidExpressionException
	{
		// An easy way to detect if parentheses are misused is 
		// to look for any opening parenthesis preceded by a token that
		// is neither an operator nor an opening parenthesis, and for any
		// closing parenthesis that is followed by a token that is
		// neither an operator nor a closing parenthesis. Don't check for
		// unbalanced parentheses here, you can do it in processExpression
		// directly as part of the Shunting Yard algorithm.
		// Call this method as the last statement in tokenize.
		if (tokenSequence.isEmpty()){//does nothing if tokenSequence is empty
		}
		else{
			int index = 0;
			Token previousToken = null;
			boolean checkClosing = false;
			for (Token tokenElement: tokenSequence)//enhanced for loop that iterates through every token in tokensequence
			{
				if (checkClosing)//checks the token after the closing Parenthesis
				{
					if (tokenElement instanceof IntToken)//ex: )1  <- misused
						throw new InvalidExpressionException(tokenElement.getStart());
					else if (tokenElement instanceof ParenthesisToken)
					{
						if (((ParenthesisToken)tokenElement).getParenthesis())//ex: )(
							throw new InvalidExpressionException(previousToken.getEnd());
					}
					checkClosing = false;//already checked closing
				}
				if (tokenElement instanceof ParenthesisToken)
				{
					//returns true if its '('
					boolean isOpening = ((ParenthesisToken)tokenElement).getParenthesis();
					if (isOpening && index > 0)//if it is the first token, can't have previous token
					{
						if (previousToken instanceof ParenthesisToken)
						{
							if (!((ParenthesisToken)previousToken).getParenthesis())//ex: )(
								throw new InvalidExpressionException(previousToken.getEnd());
						}
						else if (previousToken instanceof IntToken)//ex: 3(
							throw new InvalidExpressionException(previousToken.getEnd());
					}
					else if (!isOpening)//if last token, it can't be misused
					{
						if (previousToken != null && previousToken instanceof ParenthesisToken && ((ParenthesisToken)previousToken).getParenthesis())
							throw new InvalidExpressionException(previousToken.getStart());//checks if there is nothing between parenthesis
						else if (index<tokenSequence.size()-1)
							checkClosing = true;//will verify if next token is not misused
					}
				}
				previousToken = tokenElement;
				index++;
			}
		}
	}
	public Queue<Token> getTokenSequence(){//getter method
		return this.tokenSequence;
	}
}

/**
 * Thrown by the Tokenizer if an expression is detected to be invalid.
 * You don't need to modify this class.
 */
@SuppressWarnings("serial")
class InvalidExpressionException extends Exception
{
	private int aPosition;

	public InvalidExpressionException( int pPosition )
	{
		aPosition = pPosition;
	}

	public int getPosition()
	{
		return aPosition;
	}
}