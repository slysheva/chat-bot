import java.util.Scanner;

public class Program {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		ChatBot bot = new ChatBot(new GameFactory());

        //noinspection InfiniteLoopStatement
        while (true)
		{
			String request = sc.nextLine();
			System.out.println(bot.Answer(request));
		}
	}
}