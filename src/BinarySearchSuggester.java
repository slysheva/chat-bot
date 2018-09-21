
class BinarySearchSuggester {
	private int leftBorder;
	private int rightBorder;
	int middle;
	
	BinarySearchSuggester()
	{
		leftBorder = -1;
		rightBorder =  101;
		middle = (leftBorder + rightBorder) / 2;
	}
	
	int generateNewRequest(NumberIs answer) throws BorderException
	{
		if (answer == NumberIs.SMALLER)
			rightBorder = middle;
		else
			leftBorder = middle;
		if (rightBorder - leftBorder <= 1)
			throw new BorderException("Wrong border values");	
		middle = (leftBorder + rightBorder) / 2;
		return middle;
	}
}

class BorderException extends Exception{

	BorderException(String message){
        super(message);
    }
}
