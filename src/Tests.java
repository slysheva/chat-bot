import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Tests
{

	private int extractNumber(String s)
	{
		return Integer.parseInt(s.replaceAll("[^0-9]", ""));
	}
	
	@Test
	void testBinSearch() 
	{
		ChatBot chatBot = new ChatBot();
		int[] testCases = {100, 99, 1, 0};
		for (int x : testCases) 
		{
			String botAnswer = chatBot.Answer("старт");
			int guessNumber;
			do
			{
				if (botAnswer.charAt(0) != 'И' && botAnswer.charAt(0) != 'М')
					fail("Expected guessing number");
				guessNumber = extractNumber(botAnswer);
				if (guessNumber > x) 
					botAnswer = chatBot.Answer("<");
				else
					botAnswer = chatBot.Answer(">");
				
			} while(x != guessNumber);	
		}
	}	
	
	@Test
	void badNumber() 
	{
		ChatBot chatBot = new ChatBot();
		String botAnswer = chatBot.Answer("старт");
		do
		{
			if (botAnswer.charAt(0) != 'И' && botAnswer.charAt(0) != 'М')
				fail("Expected guessing number");
			botAnswer = chatBot.Answer("<");
		} while(!botAnswer.equals("Ты меня обманываешь"));
	}

	@Test
	void badCommand()
	{
		ChatBot chatBot = new ChatBot();
		String botAnswer = chatBot.Answer("ла-ла-ла");
		assertEquals("Команда не распознана. Попробуй ещё раз или воспользуйся помощью.", botAnswer);
	}
	
}
